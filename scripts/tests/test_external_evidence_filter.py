"""Regresiones: solo referencias externas documentadas en el catálogo Android."""
from __future__ import annotations

import json
import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets"
REGISTRY = ROOT / "catalog/review/verification_registry.json"
SLUGS = ("upper", "lower", "core", "cardio", "mobility")


def load(path: pathlib.Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


class ExternalEvidenceFilterTest(unittest.TestCase):
    def test_only_externally_documented_names_are_packaged(self):
        accepted = load(REGISTRY)["accepted"]
        independently_documented = {
            (r["category"], r["name"].casefold()): r
            for r in accepted if r.get("externalEvidence")
        }
        actual = set()
        used_refs = set()
        for slug in SLUGS:
            research = load(ASSETS / f"research_{slug}.json")
            family_groups = load(ASSETS / f"family_groups_{slug}.json")
            by_source = {}
            for nominal in research["families"]:
                key = (research["category"], nominal["name"].casefold())
                self.assertIn(key, independently_documented)
                actual.add(key)
                expected = independently_documented[key]
                self.assertEqual(
                    {r["url"] for r in nominal["externalReferences"]},
                    {r["url"] for r in expected["externalEvidence"]})
                for record in nominal["records"]:
                    ref = record["source"] + ":" + str(record["sourceId"])
                    self.assertEqual(record["name"], nominal["name"])
                    self.assertFalse(record["autoPlanningEligible"])
                    self.assertFalse(record["difficultyVerified"])
                    self.assertIn(ref, expected["sourceReferences"])
                    self.assertNotIn(ref, used_refs)
                    used_refs.add(ref)
                    by_source[ref] = nominal["name"]

            covered = set()
            for group in family_groups["groups"]:
                self.assertFalse(group["autoPlanningEligible"])
                self.assertFalse(group["semanticMergeVerified"])
                self.assertGreater(len(group["externalReferences"]), 0)
                self.assertEqual(
                    group["distinctVariantNames"], len(set(group["relatedNames"])))
                for level, rows in group["variantsByLevel"].items():
                    for record in rows:
                        ref = record["source"] + ":" + str(record["sourceId"])
                        self.assertEqual(record["name"], by_source[ref])
                        self.assertEqual(record["suggestedDifficulty"], level)
                        self.assertFalse(record["autoPlanningEligible"])
                        covered.add(ref)
            self.assertEqual(covered, set(by_source))

        self.assertEqual(actual, set(independently_documented))
        self.assertEqual(len(actual), 3)
        self.assertEqual(len(used_refs), 6)

    def test_manifest_and_approval_gate_stay_closed(self):
        manifest = load(ASSETS / "ai_manifest.json")
        approved = load(ASSETS / "catalog_approved.json")
        self.assertEqual(manifest["nominalNamesByCategory"], 3)
        self.assertEqual(manifest["documentedRecords"], 6)
        self.assertEqual(manifest["proposedGroups"], 3)
        self.assertEqual(manifest["quarantinedNominalNames"], 1820)
        self.assertEqual(manifest["biomechanicallyApprovedGroups"], 0)
        self.assertEqual(approved["canonicalExercises"], [])


if __name__ == "__main__":
    unittest.main()
