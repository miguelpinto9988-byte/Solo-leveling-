#!/usr/bin/env python3
"""Garantiza que la APK solo exponga entradas documentadas y depuradas."""
from __future__ import annotations
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
REGISTER = ROOT / "catalog/review/verification_registry.json"
CATEGORIES = {
    "upper": "Tren superior",
    "lower": "Tren inferior",
    "core": "Zona central",
    "cardio": "Cardio",
    "mobility": "Movilidad"
}
LEVELS = {"inicio", "intermedio", "avanzado", "sin_determinar"}


def audit() -> int:
    ledger = json.loads(REGISTER.read_text(encoding="utf-8"))
    selected = {
        (entry["category"], entry["name"].casefold()): entry
        for entry in ledger["accepted"] if entry.get("externalEvidence")
    }
    assert selected, "No se ha documentado ninguna referencia técnica externa"
    assert all(
        all(ref.get("publisher") == "ACE" and ref.get("level") == entry["level"]
            for ref in entry["externalEvidence"])
        for entry in selected.values()
    ), "Referencia técnica externa no conciliada"
    actual_refs: set[str] = set()
    actual_names: set[tuple[str, str]] = set()

    for slug, category in CATEGORIES.items():
        document = json.loads(
            (ASSETS / f"research_{slug}.json").read_text(encoding="utf-8")
        )
        assert document["schemaVersion"] == 1
        assert document["kind"] == "research_only"
        assert document["category"] == category
        assert document["verifiedFamilies"] == 0
        assert document["originalResearchCount"] == ledger["originalRecordCount"]
        for family in document["families"]:
            key = (family["category"], family["name"].casefold())
            assert key in selected, f"Entrada sin corroboración: {key}"
            assert key not in actual_names, f"Nombre duplicado: {key}"
            actual_names.add(key)
            expected = selected[key]
            assert family["reviewStatus"] == "research_only"
            assert family["documentationStatus"] == "source_corrob_catalog_only"
            assert family["relatedFamilyHint"] == expected["familyKey"]
            assert {ref["url"] for ref in family.get("externalReferences", [])} == {ref["url"] for ref in expected["externalEvidence"]}, "Referencia técnica ACE ausente"
            refs = set()
            for record in family["records"]:
                ref = record["source"] + ":" + str(record["sourceId"])
                assert ref not in actual_refs, f"Fuente repetida: {ref}"
                actual_refs.add(ref)
                refs.add(ref)
                assert record["name"] == family["name"], "El nombre de la variante no coincide con el movimiento"
                assert record["autoPlanningEligible"] is False
                assert record["difficultyVerified"] is False
                assert record["suggestedDifficulty"] == expected["level"]
                assert record["suggestedDifficulty"] in LEVELS
                assert record["evidenceStatus"] == (
                    "source_concordance_not_professional_approval"
                )
            assert refs == set(expected["sourceReferences"]), (
                f"Fuentes incompletas para {key}"
            )
        assert document["sourceRecords"] == sum(
            len(f["records"]) for f in document["families"]
        )
        print(
            f"{category}: {len(document['families'])} denominaciones, "
            f"{document['sourceRecords']} referencias verificadas en el ledger"
        )

    assert len(actual_names) == len(selected)
    assert len(actual_refs) == sum(len(v["sourceReferences"]) for v in selected.values())
    assert not actual_names.intersection({
        (r["category"], r["name"].casefold()) for r in ledger["rejected"]
    })
    print("Ninguna entrada sin referencia ACE o descartada está empaquetada ni habilitada para IA.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (KeyError, AssertionError, ValueError, OSError) as error:
        print(f"CATÁLOGO DE LA APK NO VÁLIDO: {error}", file=sys.stderr)
        sys.exit(1)
