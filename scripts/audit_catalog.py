#!/usr/bin/env python3
"""Control del catálogo de investigación.

Los nombres diferentes NO prueban movimientos biomecánicamente diferentes.
Nunca habilitar automáticamente fichas de staging para Shadow Core AI.
"""
from __future__ import annotations

import collections
import json
import pathlib
import re
import sys
import unicodedata

ROOT = pathlib.Path(__file__).resolve().parents[1]
FILES = (
    ROOT / "catalog/staging/free_exercise_db_research.json",
    ROOT / "catalog/staging/glowupp_research.json",
    ROOT / "catalog/staging/rthepen_research_a.json",
    ROOT / "catalog/staging/rthepen_research_b.json",
    ROOT / "catalog/staging/buzops9_research.json",
)
CATEGORIES = ("Tren superior", "Tren inferior", "Zona central", "Cardio", "Movilidad")
TARGET = 400


def normalized_name(name: str) -> str:
    name = unicodedata.normalize("NFKD", name.casefold())
    name = "".join(c for c in name if not unicodedata.combining(c))
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9]+", " ", name)).strip()


def audit() -> int:
    rows = []
    seen_source_ids: set[tuple[str, str]] = set()
    for filename in FILES:
        data = json.loads(filename.read_text(encoding="utf-8"))
        src = data["source"]
        assert src["uri"].startswith("https://github.com/"), "Procedencia sin verificar"
        for rec in data["records"]:
            source_id = str(rec["sourceId"])
            key = (src["name"], source_id)
            assert source_id and key not in seen_source_ids, (
                "ID de fuente duplicado: " + str(key)
            )
            seen_source_ids.add(key)
            assert rec["category"] in CATEGORIES, "Categoría inválida"
            assert rec["sourceName"].strip(), "Registro sin nombre"
            assert rec["status"] == "research_only_pending_identity_review"
            assert rec["autoPlanningEligible"] is False, "No se permiten fichas sin revisar"
            rows.append(rec)
        print(f"Origen {src['name']}: {len(data['records'])} registros sin validar")

    raw_counts = collections.Counter(r["category"] for r in rows)
    unique_by_category = {
        cat: {
            normalized_name(r["sourceName"])
            for r in rows
            if r["category"] == cat
        }
        for cat in CATEGORIES
    }
    global_names = {normalized_name(r["sourceName"]) for r in rows}
    distinct_category_pairs = sum(len(s) for s in unique_by_category.values())
    assert all(name for name in global_names), "Nombre vacío tras normalizar"
    print("\nINVESTIGACIÓN (NO CERTIFICACIÓN DE EJERCICIOS)")
    print(f"Registros brutos: {len(rows)}")
    print(f"Nombres distintos globales: {len(global_names)}")
    print(f"Pares categoría/nombre distintos: {distinct_category_pairs}")
    print(f"Colisiones exactas por categoría: {len(rows) - distinct_category_pairs}")
    print("Categoría             Brutos  Nombres  Meta  Déficit mínimo nominal")
    deficit = 0
    for cat in CATEGORIES:
        unique = len(unique_by_category[cat])
        shortage = max(0, TARGET - unique)
        deficit += shortage
        print(f"{cat:<20} {raw_counts[cat]:>6} {unique:>8} {TARGET:>5} {shortage:>23}")
    print(f"Déficit mínimo nominal de cuotas: {deficit}")
    print("Ejercicios principales biomecánicamente verificados: NO CONTABILIZADOS")
    print("Todos los datos de staging mantienen autoPlanningEligible=false")
    return 0


if __name__ == "__main__":
    sys.exit(audit())
