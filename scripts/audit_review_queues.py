#!/usr/bin/env python3
"""Confirma que las colas de revisión corresponden al staging, sin aprobar fichas."""
from __future__ import annotations
import collections
import json
import pathlib
import sys

from validate_catalog_reviews import ROOT, stage_index, ensure, CatalogReviewError

REVIEW = ROOT / "catalog/review"

def normalized_name(name: str) -> str:
    from audit_catalog import normalized_name as normalize
    return normalize(name)

def audit() -> int:
    index = stage_index()
    desired_conflicts = collections.defaultdict(set)
    desired_priority = collections.defaultdict(list)
    for (source, source_id), record in index.items():
        name = normalized_name(record["sourceName"])
        desired_conflicts[name].add(record["category"])
        if record["category"] in ("Cardio", "Movilidad"):
            desired_priority[(record["category"], name)].append((source, source_id))

    conflicts = json.loads((REVIEW / "category_conflicts.json").read_text(encoding="utf-8"))
    actual_conflicts = {r["normalizedName"]: set(r["categories"]) for r in conflicts["conflicts"]}
    expected_conflicts = {name: cats for name, cats in desired_conflicts.items()
                          if len(cats) > 1}
    ensure(actual_conflicts == expected_conflicts,
           "Cola de conflictos de categoría desactualizada")

    for item in conflicts["conflicts"]:
        ensure(item["reviewStatus"] == "category_conflict_pending_human_review",
               "Conflicto marcado como aprobado")
        ensure(item["automaticPlanningEligible"] is False,
               "Conflicto habilitado automáticamente")
        refs = {(x["source"], str(x["sourceId"])) for x in item["sources"]}
        ensure(all(ref in index for ref in refs), "Conflicto con fuente inexistente")
        ensure(all(normalized_name(index[ref]["sourceName"]) == item["normalizedName"]
                   for ref in refs), "Conflicto con nombre no coincidente")

    priority = json.loads((REVIEW / "cardio_mobility_priority.json").read_text(encoding="utf-8"))
    actual_priority = {
        (item["category"], item["normalizedName"]): {
            (ref["source"], str(ref["sourceId"])) for ref in item["sources"]
        }
        for item in priority["records"]
    }
    desired_priority = {key: set(refs) for key, refs in desired_priority.items()}
    ensure(actual_priority == desired_priority, "Cola de cardio/movilidad desactualizada")
    ensure(all(item["reviewStatus"] == "priority_review_pending" and
               item["autoPlanningEligible"] is False for item in priority["records"]),
           "Cola de prioridad incorrectamente autorizada")
    suggestions = json.loads((REVIEW / "variant_family_suggestions.json").read_text(encoding="utf-8"))
    ensure(all(group["reviewStatus"] == "suggestion_only_not_approved"
               for group in suggestions["groups"]),
           "Grupos de variantes marcados como aprobados sin revisión")
    print(f"Colas revisadas: {len(actual_conflicts)} conflictos entre categorías, "
          f"{len(actual_priority)} nombres de cardio/movilidad, "
          f"{len(suggestions['groups'])} familias léxicas sugeridas")
    return 0

if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (CatalogReviewError, KeyError, ValueError) as exc:
        print(f"ERROR DE AUDITORÍA: {exc}", file=sys.stderr)
        sys.exit(1)
