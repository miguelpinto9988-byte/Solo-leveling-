#!/usr/bin/env python3
"""Valida agrupaciones propuestas y cubetas de dificultad sin aprobarlas.

Comprueba que las fuentes se conservan sin pérdida, que los grupos se
corresponden con las pistas nominales y que jamás pasan al planificador.
"""
from __future__ import annotations

import collections
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
SLUGS = ("upper", "lower", "core", "cardio", "mobility")
LEVELS = ("inicio", "intermedio", "avanzado", "sin_determinar")


def audit() -> int:
    group_total = 0
    record_total = 0
    nominal_total = 0
    all_source_keys: set[tuple[str, str]] = set()

    for slug in SLUGS:
        source = json.loads(
            (ASSETS / f"research_{slug}.json").read_text(encoding="utf-8")
        )
        grouped = json.loads(
            (ASSETS / f"family_groups_{slug}.json").read_text(encoding="utf-8")
        )
        assert grouped["schemaVersion"] == 2
        assert grouped["kind"] == "research_family_proposals"
        assert grouped["verifiedFamilies"] == 0
        assert grouped["category"] == source["category"]

        expected = collections.Counter()
        source_names: dict[str, set[str]] = collections.defaultdict(set)
        for nominal in source["families"]:
            key = (
                "suggested:" + nominal["relatedFamilyHint"]
                if nominal.get("relatedFamilyHint")
                else "name:" + nominal["key"].split("|", 1)[1]
            )
            source_names[key].add(nominal["name"])
            for r in nominal["records"]:
                expected[(r["source"], str(r["sourceId"]), r["suggestedDifficulty"], key)] += 1

        actual = collections.Counter()
        seen_keys = set()
        for group in grouped["groups"]:
            key = group["id"]
            assert key not in seen_keys, f"Grupo duplicado: {key}"
            seen_keys.add(key)
            assert group["category"] == grouped["category"]
            assert group["reviewStatus"] == "research_only_unverified"
            assert group["semanticMergeVerified"] is False
            assert group["autoPlanningEligible"] is False
            assert set(group["relatedNames"]) == source_names[key]
            records = 0
            buckets = group["variantsByLevel"]
            assert set(buckets) == set(LEVELS)
            for level in LEVELS:
                for r in buckets[level]:
                    assert r["autoPlanningEligible"] is False
                    assert r["difficultyVerified"] is False
                    source_key = (r["source"], str(r["sourceId"]))
                    assert source_key not in all_source_keys, (
                        "Se volvió a contar un registro de origen: " + str(source_key)
                    )
                    all_source_keys.add(source_key)
                    actual[(r["source"], str(r["sourceId"]), level, key)] += 1
                    records += 1
            assert records == group["totalRecords"], (
                "Conteo incorrecto en " + group["id"]
            )
        assert actual == expected, f"Se perdieron o alteraron variantes en {slug}"
        assert grouped["nominalFamilies"] == len(source["families"])
        assert grouped["proposedGroups"] == len(grouped["groups"])
        assert grouped["sourceRecords"] == sum(expected.values())

        group_total += len(grouped["groups"])
        record_total += grouped["sourceRecords"]
        nominal_total += grouped["nominalFamilies"]
        print(
            f"{grouped['category']}: {len(grouped['groups'])} grupos propuestos, "
            f"{grouped['nominalFamilies']} nombres y "
            f"{grouped['sourceRecords']} registros"
        )

    assert group_total == 1089, group_total
    assert nominal_total == 1823, nominal_total
    assert record_total == 2186, record_total
    print("Integridad comprobada: 2186 registros, 1823 nombres, "
          "1089 agrupaciones no verificadas, 0 ejercicios nuevos aprobados")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(audit())
    except (AssertionError, KeyError, ValueError) as error:
        print(f"AUDITORÍA DE FAMILIAS BLOQUEADA: {error}", file=sys.stderr)
        sys.exit(1)
