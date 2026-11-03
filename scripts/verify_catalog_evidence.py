#!/usr/bin/env python3
"""Verifica la selección documental contra todas las fuentes originales.

Coincidencia entre repositorios NO equivale a aprobación de un entrenador.
Solo se permite su consulta, nunca su selección automática.
"""
from __future__ import annotations

import collections
import json
import pathlib
import sys
import unicodedata
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
STAGING = ROOT / "catalog/staging"
REGISTRY = ROOT / "catalog/review/verification_registry.json"
ASSET = ROOT / "app/src/main/assets/catalog_approved.json"
LEVEL_MAP = {
    "beginner": "inicio", "easy": "inicio", "basic": "inicio",
    "initial": "inicio", "novice": "inicio", "inicio": "inicio",
    "intermediate": "intermedio", "moderate": "intermedio",
    "intermedio": "intermedio",
    "advanced": "avanzado", "expert": "avanzado", "avanzado": "avanzado"
}


def norm(value: str) -> str:
    text = unicodedata.normalize("NFKD", value.casefold())
    text = "".join(c for c in text if not unicodedata.combining(c))
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9]+", " ", text)).strip()


def check() -> int:
    record_index = {}
    nominal_index = collections.defaultdict(list)
    categories = collections.defaultdict(set)
    for filename in sorted(STAGING.glob("*.json")):
        payload = json.loads(filename.read_text(encoding="utf-8"))
        source = payload["source"]["name"]
        for row in payload["records"]:
            key = f"{source}:{row['sourceId']}"
            assert key not in record_index, f"ID original duplicado: {key}"
            record_index[key] = row
            name = norm(row["sourceName"])
            nominal_index[(row["category"], name)].append(key)
            categories[name].add(row["category"])

    registry = json.loads(REGISTRY.read_text(encoding="utf-8"))
    assert registry["schemaVersion"] == 2
    assert registry["policy"] == "strict_source_concordance_not_professional_training_approval"
    assert len(record_index) == registry["originalRecordCount"] == 2186
    assert len(nominal_index) == registry["originalNominalNames"] == 1823
    assert (registry["retainedNominalNames"], registry["removedNominalNames"]) == (198, 1625)

    accepted_keys = set()
    used_source_ids = set()
    for entry in registry["accepted"]:
        nominal = (entry["category"], norm(entry["name"]))
        assert nominal in nominal_index, "Nombre no presente en las fuentes"
        assert nominal not in accepted_keys, "Variante aceptada duplicada"
        accepted_keys.add(nominal)
        assert len(categories[nominal[1]]) == 1, (
            f"Conflicto de categoría no resuelto: {nominal}"
        )
        refs = entry["sourceReferences"]
        assert set(refs) == set(nominal_index[nominal]), (
            f"Faltan referencias originales para {nominal}"
        )
        src_names = {r.split(":", 1)[0] for r in refs}
        professional = entry.get("externalEvidence", [])
        assert len(src_names) >= 2 or professional, "Sin corroboración documental"
        original_levels = set()
        for ref in refs:
            assert ref not in used_source_ids, "Referencia reutilizada en dos ejercicios"
            used_source_ids.add(ref)
            record = record_index[ref]
            raw_level = str(
                record.get("level", record.get("difficulty",
                    record.get("reportedDifficulty", "")))
            ).lower().strip()
            level = LEVEL_MAP.get(raw_level, "sin_determinar")
            if level != "sin_determinar":
                original_levels.add(level)
        assert entry["level"] in ("inicio", "intermedio"), "Nivel no permitido"
        if not professional:
            assert original_levels == {entry["level"]}
            assert all(
                LEVEL_MAP.get(str(
                    record_index[ref].get("level", record_index[ref].get(
                        "difficulty", record_index[ref].get(
                            "reportedDifficulty", "")))
                ).lower().strip(), "sin_determinar") == entry["level"]
                for ref in refs
            ), "Nivel de origen contradictorio o indeterminado"
        else:
            assert all(
                x["url"].startswith(
                    "https://www.acefitness.org/resources/everyone/exercise-library/"
                ) and x["level"] == entry["level"]
                for x in professional
            ), "Referencia profesional no documentada"
            assert original_levels <= {entry["level"]}, "Contradicción con ACE"

    rejected_keys = {(x["category"], norm(x["name"])) for x in registry["rejected"]}
    assert accepted_keys.isdisjoint(rejected_keys), "Una variante retenida y excluida"
    assert accepted_keys | rejected_keys == set(nominal_index), (
        "Faltan decisiones sobre entradas originales"
    )
    assert len(accepted_keys) == 198
    assert len(used_source_ids) == registry["retainedSourceEvidence"] == 402
    assert registry["retainedNominalNames"] + registry["removedNominalNames"] == 1823

    approved = json.loads(ASSET.read_text(encoding="utf-8"))
    assert approved["canonicalExercises"] == [], (
        "No se permite promocionar variantes por automatización"
    )
    print("Evidencia corroborada: 198 denominaciones, 402 registros originales.")
    print("Descartados de la APK: 1625 nombres sin corroboración suficiente, "
          "con discrepancias o patrones ambiguos.")
    print("Aprobaciones técnicas para la IA: 0")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(check())
    except (AssertionError, KeyError, ValueError, OSError) as error:
        print(f"VALIDACIÓN DE EVIDENCIA BLOQUEADA: {error}", file=sys.stderr)
        sys.exit(1)
