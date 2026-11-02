#!/usr/bin/env python3
"""Comprueba que el explorador offline muestra fuentes, niveles sin aprobar e
identidad de cada registro original, sin permitir su uso en el planificador.
"""
from __future__ import annotations

import collections
import json
import pathlib
import re
import sys
import unicodedata

ROOT = pathlib.Path(__file__).resolve().parents[1]
STAGING = ROOT / "catalog/staging"
ASSETS = ROOT / "app/src/main/assets"
CATEGORIES = {
    "upper": "Tren superior",
    "lower": "Tren inferior",
    "core": "Zona central",
    "cardio": "Cardio",
    "mobility": "Movilidad"
}
LEVELS = {"inicio", "intermedio", "avanzado", "sin_determinar"}
SOURCE_LEVELS = {
    "beginner": "inicio", "basic": "inicio", "easy": "inicio",
    "initial": "inicio", "inicio": "inicio", "novice": "inicio",
    "intermediate": "intermedio", "moderate": "intermedio",
    "intermedio": "intermedio",
    "advanced": "avanzado", "expert": "avanzado", "avanzado": "avanzado"
}

def normalize(text: str) -> str:
    text = unicodedata.normalize("NFD", text.casefold())
    text = "".join(ch for ch in text if not unicodedata.combining(ch))
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9]+", " ", text)).strip()

def stage_records() -> dict[tuple[str, str], dict]:
    index = {}
    for path in STAGING.glob("*.json"):
        payload = json.loads(path.read_text(encoding="utf-8"))
        source = payload["source"]["name"]
        for record in payload["records"]:
            key = (source, str(record["sourceId"]))
            assert key not in index, f"Duplicated source ID {key}"
            index[key] = record
    return index

def audit() -> int:
    staged = stage_records()
    source_seen = set()
    category_totals = {}
    family_totals = {}
    difficulty_totals = collections.Counter()

    for slug, category in CATEGORIES.items():
        file = ASSETS / f"research_{slug}.json"
        payload = json.loads(file.read_text(encoding="utf-8"))
        assert payload["schemaVersion"] == 1
        assert payload["kind"] == "research_only"
        assert payload["category"] == category
        assert payload["verifiedFamilies"] == 0
        families = payload["families"]
        names = set()
        records_in_group = 0
        for family in families:
            assert family["category"] == category
            assert family["reviewStatus"] == "research_only"
            norm = normalize(family["name"])
            assert norm and norm not in names, f"Duplicate nominal family: {norm}"
            names.add(norm)
            assert family["records"], "Empty nominal research family"
            for entry in family["records"]:
                assert entry["autoPlanningEligible"] is False
                assert entry["difficultyVerified"] is False
                assert entry["suggestedDifficulty"] in LEVELS
                raw_level = entry["sourceDifficulty"].lower().strip()
                assert entry["suggestedDifficulty"] == SOURCE_LEVELS.get(
                    raw_level, "sin_determinar"
                ), f"Unsupported difficulty inference for {entry['name']}"
                key = (entry["source"], str(entry["sourceId"]))
                assert key in staged, f"Unknown source reference: {key}"
                assert key not in source_seen, f"Duplicated research asset reference: {key}"
                source_seen.add(key)
                source = staged[key]
                assert source["category"] == category
                assert normalize(source["sourceName"]) == norm
                difficulty_totals[entry["suggestedDifficulty"]] += 1
                records_in_group += 1
        category_totals[category] = records_in_group
        family_totals[category] = len(families)
        print(f"{category}: {len(families)} grupos nominales, "
              f"{records_in_group} registros y niveles declarados sin aprobar")

    assert len(source_seen) == len(staged), "Missing records from bundled offline catalog"
    assert sum(category_totals.values()) == 2186, "Unexpected total source entries"
    assert sum(family_totals.values()) == 1823, "Unexpected nominal family count"
    print(f"Validación OK: {len(source_seen)} registros, "
          f"{sum(family_totals.values())} nombres por categoría")
    print("Distribución de dificultades declaradas:", dict(difficulty_totals))
    print("Ningún registro documental habilitado para IA.")
    return 0

if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (AssertionError, ValueError, KeyError) as error:
        print(f"RESEARCH ASSET INVALID: {error}", file=sys.stderr)
        sys.exit(1)
