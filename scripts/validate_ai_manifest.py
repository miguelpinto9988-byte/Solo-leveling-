#!/usr/bin/env python3
"""Validación de recursos offline de Shadow Core AI (90 MB decimales).

No considera tamaño de APK ni RAM: esa verificación necesita Gradle y
dispositivos reales. Comprueba manifiesto, archivos, integridad y presupuesto.
"""
from __future__ import annotations

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
SLUGS = ("upper", "lower", "core", "cardio", "mobility")
LIMIT_BYTES = 90_000_000


def audit() -> int:
    manifest = json.loads((ASSETS / "ai_manifest.json").read_text(encoding="utf-8"))
    assert manifest["schemaVersion"] == 1
    assert manifest["engine"] == "Shadow Core AI"
    assert manifest["networkRequired"] is False
    assert manifest["storageBudgetBytes"] == LIMIT_BYTES
    assert manifest["modelWeightsIncluded"] is False
    assert manifest["includesCompiledDex"] is False

    expected_files = {
        *(f"research_{slug}.json" for slug in SLUGS),
        *(f"family_groups_{slug}.json" for slug in SLUGS),
        "catalog_approved.json",
        "catalog_source_licenses.txt",
        "ai_manifest.json",
    }
    actual_files = manifest["assetList"]
    assert len(actual_files) == len(set(actual_files)), "Archivos repetidos"
    assert set(actual_files) == expected_files, "Manifiesto de datos incompleto"
    assert all(pathlib.Path(p).name == p for p in actual_files)

    by_slug = {
        slug: (
            json.loads((ASSETS / f"research_{slug}.json").read_text(encoding="utf-8")),
            json.loads((ASSETS / f"family_groups_{slug}.json").read_text(encoding="utf-8"))
        )
        for slug in SLUGS
    }
    source_records = sum(groups["sourceRecords"] for _, groups in by_slug.values())
    nominal_names = sum(groups["nominalFamilies"] for _, groups in by_slug.values())
    proposed_groups = sum(groups["proposedGroups"] for _, groups in by_slug.values())
    approved = json.loads((ASSETS / "catalog_approved.json").read_text(encoding="utf-8"))
    assert source_records == manifest["documentedRecords"] == 2186
    assert nominal_names == manifest["nominalNamesByCategory"] == 1823
    assert proposed_groups == manifest["proposedGroups"] == 1089
    assert len(approved["canonicalExercises"]) == (
        manifest["biomechanicallyApprovedGroups"] == 0
    )
    assert manifest["sourceDatasets"] == 4
    assert len(manifest["categoryBreakdown"]) == 5

    count_by_cat = {
        groups["category"]: (groups["sourceRecords"], groups["nominalFamilies"],
                             groups["proposedGroups"])
        for _, groups in by_slug.values()
    }
    for record in manifest["categoryBreakdown"]:
        key = record["name"]
        assert key in count_by_cat, f"Categoría sin índice: {key}"
        actual = count_by_cat[key]
        assert actual == (
            record["records"], record["nominal"], record["proposed"]
        ), f"Conteo desactualizado: {key}"

    total_bytes = sum((ASSETS / name).stat().st_size for name in actual_files)
    # Incluye todo assets, aun archivos no listados en el manifiesto.
    all_asset_bytes = sum(
        file.stat().st_size for file in ASSETS.rglob("*") if file.is_file()
    )
    assert total_bytes <= LIMIT_BYTES and all_asset_bytes <= LIMIT_BYTES
    print(
        f"Shadow Core AI: {len(actual_files)} assets indexados, "
        f"{total_bytes:,} bytes de índices; "
        f"{all_asset_bytes:,} bytes incluyendo recursos adicionales."
    )
    print(
        f"Datos: {source_records} entradas, {nominal_names} nombres, "
        f"{proposed_groups} agrupaciones propuestas sin aprobar."
    )
    print("Presupuesto 90 MB conforme sobre archivos fuente. RAM y APK: por medir.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (AssertionError, KeyError, ValueError, OSError) as error:
        print(f"MANIFIESTO BLOQUEADO: {error}", file=sys.stderr)
        sys.exit(1)
