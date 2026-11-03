#!/usr/bin/env python3
"""Valida manifiesto offline depurado y techo de 90 MB decimales."""
from __future__ import annotations
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
LEDGER = ROOT / "catalog/review/verification_registry.json"
SLUGS = ("upper", "lower", "core", "cardio", "mobility")
LIMIT = 90_000_000


def audit() -> int:
    doc = json.loads((ASSETS / "ai_manifest.json").read_text(encoding="utf-8"))
    review = json.loads(LEDGER.read_text(encoding="utf-8"))
    assert doc["schemaVersion"] == 1
    assert doc["engine"] == "Shadow Core AI"
    assert doc["networkRequired"] is False
    assert doc["storageBudgetBytes"] == LIMIT
    assert doc["modelWeightsIncluded"] is True
    from fetch_offline_model import RELATIVE_PATH, EXPECTED_SHA256, verify
    assert doc["modelAssetPath"] == RELATIVE_PATH
    assert doc["modelSha256"] == EXPECTED_SHA256
    assert doc["sourceEvidenceStatus"] == "independent_ace_public_reference_not_professional_approval"
    assert doc["includesCompiledDex"] is False

    allowed = {
        *(f"research_{slug}.json" for slug in SLUGS),
        *(f"family_groups_{slug}.json" for slug in SLUGS),
        "catalog_approved.json",
        "catalog_source_licenses.txt",
        "ai_manifest.json"
    }
    listed = doc["assetList"]
    assert len(listed) == len(set(listed)) == len(allowed)
    assert set(listed) == allowed
    assert all(pathlib.Path(name).name == name for name in listed)

    source_rows, nominal, groups = 0, 0, 0
    by_category = {}
    for slug in SLUGS:
        candidates = json.loads(
            (ASSETS / f"research_{slug}.json").read_text(encoding="utf-8")
        )
        families = json.loads(
            (ASSETS / f"family_groups_{slug}.json").read_text(encoding="utf-8")
        )
        assert families["verifiedFamilies"] == 0
        source_rows += families["sourceRecords"]
        nominal += families["nominalFamilies"]
        groups += families["proposedGroups"]
        by_category[families["category"]] = (
            families["sourceRecords"], families["nominalFamilies"],
            families["proposedGroups"]
        )
        assert candidates["sourceRecords"] == families["sourceRecords"]

    externally_documented = [e for e in review["accepted"] if e.get("externalEvidence")]
    assert doc["documentedRecords"] == source_rows == sum(
        len(e["sourceReferences"]) for e in externally_documented)
    assert doc["nominalNamesByCategory"] == nominal == len(externally_documented)
    assert doc["proposedGroups"] == groups
    assert doc["originalResearchRecords"] == review["originalRecordCount"] == 2186
    assert doc["quarantinedNominalNames"] == review["originalNominalNames"] - nominal
    assert len(doc["categoryBreakdown"]) == 5
    for cat in doc["categoryBreakdown"]:
        assert by_category[cat["name"]] == (
            cat["records"], cat["nominal"], cat["proposed"]
        )

    approved = json.loads(
        (ASSETS / "catalog_approved.json").read_text(encoding="utf-8")
    )
    assert len(approved["canonicalExercises"]) == doc["biomechanicallyApprovedGroups"] == 0

    measured = sum((ASSETS / file).stat().st_size for file in listed)
    full_assets = sum(file.stat().st_size for file in ASSETS.rglob("*") if file.is_file())
    model_file = ASSETS / doc["modelAssetPath"]
    if model_file.exists():
        measured += verify(model_file)
        print("Modelo GGUF verificado y presente para empaquetar.")
    else:
        print("Aviso: modelo sin descargar todavía; CI debe descargarlo antes de compilar.")
    assert full_assets <= LIMIT and measured <= LIMIT
    print(f"Depuración conforme: {source_rows} referencias documentales, "
          f"{nominal} variantes nominales, {groups} familias provisionales.")
    print(f"{doc['originalResearchRecords']} entradas originales conservadas "
          "solo en staging y registro de auditoría.")
    print(f"Archivos de IA: {measured:,} bytes; assets totales: {full_assets:,} bytes.")
    print("Los 90 MB son almacenamiento máximo de recursos, no RAM ni APK total.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (AssertionError, KeyError, ValueError, OSError) as error:
        print(f"MANIFIESTO INVÁLIDO: {error}", file=sys.stderr)
        sys.exit(1)
