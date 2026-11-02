"""Pruebas de puerta de promoción del catálogo, sin aprobar ejercicios reales."""
import datetime as dt
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1]))
from validate_catalog_reviews import (  # noqa: E402
    CatalogReviewError,
    stage_index,
    validated_export,
)


class ApprovedCatalogTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.index = stage_index()
        cls.example_ref, cls.example_record = next(iter(cls.index.items()))

    def valid_family(self):
        return {
            "id": "fixture-family-one",
            "localId": 10001,
            "name": "FICHA FICTICIA, SOLO PRUEBA",
            "category": self.example_record["category"],
            "subcategory": "Movilidad",
            "patternKey": "fixture-movement",
            "equipment": "none",
            "level": "inicio",
            "reviewStatus": "approved",
            "planningEligibility": "catalog_only",
            "reviewedBy": "Tester (solo pruebas)",
            "reviewerRole": "qualified_exercise_reviewer",
            "reviewDate": dt.date.today().isoformat(),
            "identityRationale": "Justificación ficticia suficientemente larga solo para validar el esquema.",
            "riskScreeningNotes": "Observación ficticia de restricciones, no válida para producción.",
            "restrictionTags": [],
            "sources": [{
                "source": self.example_ref[0],
                "sourceId": self.example_ref[1]
            }],
            "variants": []
        }

    def test_empty_approved_manifest_is_valid(self):
        self.assertEqual(
            validated_export({"schemaVersion": 1, "canonicalExercises": []}, self.index),
            {"schemaVersion": 1, "canonicalExercises": []}
        )

    def test_signed_source_may_pass_schema(self):
        result = validated_export(
            {"schemaVersion": 1, "canonicalExercises": [self.valid_family()]},
            self.index
        )
        self.assertEqual(len(result["canonicalExercises"]), 1)

    def test_automatic_approval_rejected(self):
        family = self.valid_family()
        family["reviewedBy"] = "ChatGPT"
        with self.assertRaises(CatalogReviewError):
            validated_export({"schemaVersion": 1, "canonicalExercises": [family]}, self.index)

    def test_duplicate_patterns_rejected(self):
        family = self.valid_family()
        duplicate = self.valid_family()
        duplicate["id"] = "fixture-family-two"
        duplicate["localId"] = 10002
        with self.assertRaises(CatalogReviewError):
            validated_export(
                {"schemaVersion": 1, "canonicalExercises": [family, duplicate]}, self.index
            )

    def test_source_must_exist_in_staging(self):
        family = self.valid_family()
        family["sources"] = [{"source": "unknown", "sourceId": "irrelevant"}]
        with self.assertRaises(CatalogReviewError):
            validated_export({"schemaVersion": 1, "canonicalExercises": [family]}, self.index)

    def test_staging_cannot_be_auto_promoted(self):
        family = self.valid_family()
        family["reviewStatus"] = "research_only_pending_identity_review"
        with self.assertRaises(CatalogReviewError):
            validated_export({"schemaVersion": 1, "canonicalExercises": [family]}, self.index)

    def test_uncleared_restrictions_block_planning(self):
        family = self.valid_family()
        family["restrictionTags"] = ["pending-compatibility"]
        family["planningEligibility"] = "approved"
        with self.assertRaises(CatalogReviewError):
            validated_export({"schemaVersion": 1, "canonicalExercises": [family]}, self.index)

    def test_one_family_can_have_variants_without_extra_parents(self):
        family = self.valid_family()
        other_key = next(k for k, r in self.index.items()
                         if k != self.example_ref and
                         r["category"] == family["category"])
        family["variants"] = [{
            "id": "variant-test",
            "name": "Variante ficticia",
            "equipment": "none",
            "sources": [{"source": other_key[0], "sourceId": other_key[1]}]
        }]
        result = validated_export(
            {"schemaVersion": 1, "canonicalExercises": [family]},
            self.index
        )
        self.assertEqual(len(result["canonicalExercises"]), 1)
        self.assertEqual(len(result["canonicalExercises"][0]["variants"]), 1)


if __name__ == "__main__":
    unittest.main()
