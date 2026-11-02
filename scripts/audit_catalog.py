#!/usr/bin/env python3
"""Auditoría de fuentes del catálogo de Gym Breaker (sin convertir variantes en ejercicios).

No autoriza ningún registro para planificación, y distingue entradas, nombres
normalizados y familias biomecánicas certificadas (aún inexistentes).
"""
from __future__ import annotations

import collections
import json
import pathlib
import sys
import unicodedata
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
FILES = (
    ROOT / "catalog/staging/free_exercise_db_research.json",
    ROOT / "catalog/staging/glowupp_research.json",
)
CATEGORIES = ("Tren superior", "Tren inferior", "Zona central", "Cardio", "Movilidad")
TARGET = 400

def normalized_name(s: str) -> str:
    n = unicodedata.normalize("NFKD", s.casefold())
    n = "".join(ch for ch in n if not unicodedata.combining(ch))
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9]+", " ", n)).strip()

def audit() -> int:
    all_records = []
    for file in FILES:
        data = json.loads(file.read_text(encoding="utf-8"))
        records = data["records"]
        source_ids = [str(row["sourceId"]) for row in records]
        assert len(source_ids) == len(set(source_ids)), (
            f"IDs repetidos dentro de {file.name}"
        )
        assert data.get("source", {}).get("uri"), "Fuente sin procedencia"
        for row in records:
            assert row["category"] in CATEGORIES, "Categoría principal inválida"
            assert row["sourceName"].strip() and str(row["sourceId"]).strip()
            assert row["status"] == "research_only_pending_identity_review"
            assert row["autoPlanningEligible"] is False, (
                "No se permiten registros sin revisión en la IA"
            )
        print(f"Fuente: {data['source']['name']} · {len(records)} candidatos")
        all_records.extend(records)

    count = collections.Counter(x["category"] for x in all_records)
    unique = {}
    for x in all_records:
        unique.setdefault(normalized_name(x["sourceName"]), x)
    repeated_names = len(all_records) - len(unique)

    print("\nREGISTROS DE INVESTIGACIÓN — NO SON EJERCICIOS ÚNICOS VALIDADOS")
    print(f"Total bruto: {len(all_records)}")
    print(f"Nombres normalizados distintos: {len(unique)}")
    print(f"Colisiones exactas normalizadas: {repeated_names}")
    for category in CATEGORIES:
        n = count[category]
        deficit = max(0, TARGET - n)
        print(f"{category:<15} {n:>4} entradas · meta {TARGET} · "
              f"faltan al menos {deficit:>4} antes de agrupar variantes")

    # Las familias requieren revisión técnica humana; por tanto, la cifra
    # de 2000 ejercicios biomecánicamente diferentes NO se ha validado.
    print("\nFamilias de ejercicios confirmadas por revisión: 0")
    print("Ningún registro de investigación debe importarse automáticamente a Room.")
    return 0

if __name__ == "__main__":
    sys.exit(audit())
