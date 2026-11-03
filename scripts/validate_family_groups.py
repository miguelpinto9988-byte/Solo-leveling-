#!/usr/bin/env python3
"""Audita familias propuestas depuradas y variantes por niveles sin contar fuentes doble."""
from __future__ import annotations
import collections
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
REGISTER = ROOT / "catalog/review/verification_registry.json"
SLUGS = ("upper", "lower", "core", "cardio", "mobility")
LEVELS = ("inicio", "intermedio", "avanzado", "sin_determinar")


def audit() -> int:
    register = json.loads(REGISTER.read_text(encoding="utf-8"))
    all_refs = set()
    group_count = 0
    name_count = 0

    for slug in SLUGS:
        source = json.loads(
            (ASSETS / f"research_{slug}.json").read_text(encoding="utf-8")
        )
        data = json.loads(
            (ASSETS / f"family_groups_{slug}.json").read_text(encoding="utf-8")
        )
        assert data["schemaVersion"] == 2
        assert data["kind"] == "research_family_proposals"
        assert data["category"] == source["category"]
        assert data["verifiedFamilies"] == 0

        expected = collections.Counter()
        related = collections.defaultdict(set)
        for nominal in source["families"]:
            key = "documented:" + nominal["relatedFamilyHint"]
            related[key].add(nominal["name"])
            for record in nominal["records"]:
                expected[(
                    key, record["source"], str(record["sourceId"]),
                    record["suggestedDifficulty"]
                )] += 1

        actual = collections.Counter()
        seen_groups = set()
        for group in data["groups"]:
            key = group["id"]
            assert key not in seen_groups, f"Grupo duplicado: {key}"
            seen_groups.add(key)
            assert group["category"] == data["category"]
            assert group["reviewStatus"] == "research_only_unverified"
            assert group["documentationStatus"] == "source_corrob_catalog_only"
            assert group["semanticMergeVerified"] is False
            assert group["autoPlanningEligible"] is False
            assert set(group["relatedNames"]) == related[key]
            assert set(group["variantsByLevel"]) == set(LEVELS)
            refs = 0
            for level in LEVELS:
                for entry in group["variantsByLevel"][level]:
                    ref = (entry["source"], str(entry["sourceId"]))
                    assert ref not in all_refs, f"Referencia duplicada: {ref}"
                    all_refs.add(ref)
                    assert entry["autoPlanningEligible"] is False
                    assert entry["difficultyVerified"] is False
                    assert entry["evidenceStatus"] == (
                        "source_concordance_not_professional_approval"
                    )
                    assert entry["suggestedDifficulty"] == level
                    actual[(key, ref[0], ref[1], level)] += 1
                    refs += 1
            assert refs == group["totalRecords"]
            assert len(group["relatedNames"]) == group["distinctVariantNames"]

        assert expected == actual, f"Variantes perdidas o adicionales: {slug}"
        assert data["nominalFamilies"] == len(source["families"])
        assert data["proposedGroups"] == len(data["groups"])
        assert data["sourceRecords"] == source["sourceRecords"]
        group_count += len(data["groups"])
        name_count += len(source["families"])
        print(f"{data['category']}: {len(data['groups'])} familias propuestas")

    assert group_count == 119, group_count
    assert name_count == register["retainedNominalNames"] == 198, name_count
    assert len(all_refs) == register["retainedSourceEvidence"] == 402
    print("Auditoría conforme: 198 variantes nominales, 119 familias propuestas, "
          "402 referencias de origen, ninguna aprobada automáticamente")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (KeyError, AssertionError, ValueError, OSError) as error:
        print(f"AUDITORÍA DE FAMILIAS BLOQUEADA: {error}", file=sys.stderr)
        sys.exit(1)
