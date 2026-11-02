#!/usr/bin/env python3
"""Auditoría reproducible del catálogo de investigación de Gym Breaker.

No transforma variantes en ejercicios principales sin revisión humana.
No activa automáticamente movimientos para el planificador.
"""
from __future__ import annotations
import collections
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / "catalog/staging/free_exercise_db_research.json"
CATEGORIES = ("Tren superior", "Tren inferior", "Zona central", "Cardio", "Movilidad")
TARGET = 400

def audit() -> int:
    payload = json.loads(SOURCE.read_text(encoding="utf-8"))
    records = payload["records"]
    ids = [x["sourceId"] for x in records]
    assert len(ids) == len(set(ids)), "Hay identificadores duplicados en una misma fuente"
    counts = collections.Counter()
    hints = collections.defaultdict(list)
    for entry in records:
        assert entry["category"] in CATEGORIES, f"Categoría inválida: {entry['category']}"
        assert entry["sourceName"].strip() and entry["sourceId"].strip()
        assert not entry["autoPlanningEligible"], "No se permiten ejercicios sin revisión"
        assert entry["status"] == "research_only_pending_identity_review"
        counts[entry["category"]] += 1
        if entry.get("variationFamilyHint"):
            hints[(entry["category"], entry["variationFamilyHint"])].append(entry["sourceId"])

    print("CATÁLOGO EN INVESTIGACIÓN — NO SON EJERCICIOS PRINCIPALES VALIDADOS")
    print(f"Origen: {payload['source']['name']} · Total de candidatos: {len(records)}")
    for category in CATEGORIES:
        n = counts[category]
        print(f"  {category:<15} {n:>4} registros · meta {TARGET} · "
              f"diferencia previa a deduplicación {max(0, TARGET-n):>4}")
    print(f"Grupos de posibles variantes: {sum(len(v)>1 for v in hints.values())}")
    print(f"Registros presentes en esos grupos: {sum(len(v) for v in hints.values() if len(v)>1)}")
    print("No se autoriza importar ni sumar estos registros al contador de ejercicios únicos.")
    return 0

if __name__ == "__main__":
    sys.exit(audit())
