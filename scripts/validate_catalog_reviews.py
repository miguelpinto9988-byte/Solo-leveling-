#!/usr/bin/env python3
"""Puerta de validación entre investigación y catálogo distribuible de Android.

NO certifica la calidad biomecánica: exige constancia explícita de una revisión
humana y fuentes verificables antes de permitir su exportación. El motor de IA
no recibe nunca archivos de investigación (staging).
"""
from __future__ import annotations

import argparse
import datetime as dt
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
STAGING = ROOT / "catalog/staging"
REVIEW_FILE = ROOT / "catalog/review/approved_exercises.json"
APPROVED_ASSET = ROOT / "app/src/main/assets/catalog_approved.json"
CATEGORIES = ("Tren superior", "Tren inferior", "Zona central", "Cardio", "Movilidad")
EQUIPMENT = ("none", "dumbbell", "gym")
LEVELS = ("inicio", "intermedio", "avanzado")


class CatalogReviewError(ValueError):
    pass


def ensure(ok: bool, message: str) -> None:
    if not ok:
        raise CatalogReviewError(message)


def stage_index() -> dict[tuple[str, str], dict]:
    lookup: dict[tuple[str, str], dict] = {}
    for filename in sorted(STAGING.glob("*.json")):
        data = json.loads(filename.read_text(encoding="utf-8"))
        source = data["source"]["name"]
        for row in data["records"]:
            key = (source, str(row["sourceId"]))
            ensure(key not in lookup, f"Fuente duplicada: {key}")
            ensure(row["autoPlanningEligible"] is False, f"Staging activado: {key}")
            ensure(row["status"] == "research_only_pending_identity_review",
                   f"Staging no revisado incorrecto: {key}")
            lookup[key] = row
    return lookup


def review_ref(ref: dict, index: dict, family: dict, used_refs: set) -> tuple[str, str]:
    ensure(isinstance(ref, dict), "Referencia no es un objeto")
    key = (ref.get("source"), str(ref.get("sourceId", "")))
    ensure(key in index, f"Referencia inexistente: {key}")
    ensure(key not in used_refs, f"Referencia reutilizada: {key}")
    # Una discordancia de categoría exige justificación humana documentada.
    if index[key]["category"] != family["category"]:
        ensure(len(family.get("categoryRationale", "").strip()) >= 30,
               f"Conflicto de categoría sin justificación: {key}")
    used_refs.add(key)
    return key


def validated_export(payload: dict, index: dict) -> dict:
    ensure(payload.get("schemaVersion") == 1, "Versión de esquema incorrecta")
    families = payload.get("canonicalExercises")
    ensure(isinstance(families, list), "Falta lista canonicalExercises")
    used_ids: set[str] = set()
    used_local_ids: set[int] = set()
    used_pattern_keys: set[tuple[str, str]] = set()
    used_refs: set[tuple[str, str]] = set()
    output = []

    for family in families:
        ensure(isinstance(family, dict), "Familia mal formada")
        required = [
            "id", "localId", "name", "category", "subcategory",
            "patternKey", "equipment", "level", "reviewStatus",
            "planningEligibility", "reviewedBy", "reviewerRole",
            "reviewDate", "identityRationale", "riskScreeningNotes",
            "restrictionTags", "sources", "variants"
        ]
        ensure(all(k in family for k in required),
               "Faltan campos obligatorios: " + str([k for k in required if k not in family]))
        ensure(isinstance(family["id"], str) and
               re.fullmatch(r"[a-z0-9][a-z0-9_-]{2,80}", family["id"]) is not None,
               "ID de familia no válido")
        ensure(family["id"] not in used_ids, "Familia duplicada")
        used_ids.add(family["id"])

        local_id = family["localId"]
        ensure(type(local_id) is int and local_id >= 10000, "ID local fuera del rango reservado")
        ensure(local_id not in used_local_ids, "ID local duplicado")
        used_local_ids.add(local_id)

        ensure(family["category"] in CATEGORIES, "Categoría no pertenece a las cinco")
        ensure(family["equipment"] in EQUIPMENT, "Equipamiento no normalizado")
        ensure(family["level"] in LEVELS, "Nivel no normalizado")
        ensure(all(isinstance(family[k], str) and family[k].strip()
                   for k in ("name", "subcategory", "patternKey")), "Texto obligatorio vacío")

        pattern = (family["category"], family["patternKey"].strip().lower())
        ensure(pattern not in used_pattern_keys, "Patrón repetido: posible familia duplicada")
        used_pattern_keys.add(pattern)

        ensure(family["reviewStatus"] == "approved", "Familia sin aprobación humana")
        ensure(family["planningEligibility"] in ("approved", "catalog_only"),
               "Estado de planificación inválido")
        reviewer = str(family["reviewedBy"]).strip()
        ensure(len(reviewer) >= 3 and reviewer.lower() not in
               ("ai", "chatgpt", "automated", "automatico", "sistema"),
               "No se admite una aprobación automática")
        ensure(family["reviewerRole"] == "qualified_exercise_reviewer",
               "Falta perfil de revisión técnica")
        try:
            review_date = dt.date.fromisoformat(family["reviewDate"])
        except (ValueError, TypeError):
            raise CatalogReviewError("Fecha de revisión inválida") from None
        ensure(review_date <= dt.date.today(), "Revisión en fecha futura")
        ensure(len(family["identityRationale"].strip()) >= 40,
               "Falta justificación de independencia biomecánica")
        ensure(len(family["riskScreeningNotes"].strip()) >= 30,
               "Falta valoración de riesgos y restricciones")
        ensure(isinstance(family["restrictionTags"], list) and
               all(isinstance(tag, str) and tag.strip() for tag in family["restrictionTags"]),
               "Etiquetas de restricciones inválidas")
        # Preventivas sin implementación de filtrado detallado no pasan a IA.
        # Los ejercicios avanzados permanecen solo para consulta.
        if family["level"] == "avanzado" or family["restrictionTags"]:
            ensure(family["planningEligibility"] == "catalog_only",
                   "Ejercicio avanzado o con restricciones: solo catálogo hasta revisión")

        sources = family["sources"]
        ensure(isinstance(sources, list) and sources, "Familia sin fuente")
        for ref in sources:
            review_ref(ref, index, family, used_refs)

        variants = family["variants"]
        ensure(isinstance(variants, list), "Variantes deben ser lista")
        variant_ids: set[str] = set()
        for variant in variants:
            ensure(isinstance(variant, dict), "Variante mal formada")
            ensure(set(("id", "name", "equipment", "sources")) <= set(variant),
                   "Variante sin atributos esenciales")
            ensure(isinstance(variant["id"], str) and
                   re.fullmatch(r"[a-z0-9][a-z0-9_-]{2,80}", variant["id"]) is not None,
                   "ID de variante no válido")
            ensure(variant["id"] not in variant_ids, "Variante duplicada")
            variant_ids.add(variant["id"])
            ensure(isinstance(variant["name"], str) and variant["name"].strip(),
                   "Variante sin denominación")
            ensure(variant["equipment"] in EQUIPMENT, "Variante con equipo no normalizado")
            ensure(isinstance(variant["sources"], list) and variant["sources"],
                   "Variante sin procedencia")
            for ref in variant["sources"]:
                review_ref(ref, index, family, used_refs)

        # Campo revisado manualmente, NO se copian estructuras arbitrarias del staging.
        output.append({
            "id": family["id"],
            "localId": family["localId"],
            "name": family["name"],
            "category": family["category"],
            "subcategory": family["subcategory"],
            "patternKey": family["patternKey"],
            "equipment": family["equipment"],
            "level": family["level"],
            "reviewStatus": family["reviewStatus"],
            "planningEligibility": family["planningEligibility"],
            "restrictionTags": family["restrictionTags"],
            "sources": sources,
            "variants": variants
        })
    return {"schemaVersion": 1, "canonicalExercises": output}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="Actualizar asset tras validar")
    args = parser.parse_args()
    index = stage_index()
    reviewed = json.loads(REVIEW_FILE.read_text(encoding="utf-8"))
    exported = validated_export(reviewed, index)
    rendered = json.dumps(exported, ensure_ascii=False, indent=2) + "\n"
    if args.write:
        APPROVED_ASSET.parent.mkdir(parents=True, exist_ok=True)
        APPROVED_ASSET.write_text(rendered, encoding="utf-8")
        print(f"Asset exportado: {len(exported['canonicalExercises'])} familias")
    else:
        existing = json.loads(APPROVED_ASSET.read_text(encoding="utf-8"))
        ensure(existing == exported, "El asset Android no coincide con el catálogo aprobado")
        print(f"Gate OK: {len(index)} candidatos aislados; "
              f"{len(exported['canonicalExercises'])} familias aprobadas")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (CatalogReviewError, ValueError, KeyError) as exc:
        print(f"CATÁLOGO BLOQUEADO: {exc}", file=sys.stderr)
        sys.exit(1)
