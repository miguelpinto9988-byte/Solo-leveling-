#!/usr/bin/env python3
"""Verifica índice de 29 referencias y binarios opcionales de la biblioteca visual.

Por defecto, los binarios ausentes se informan, pero no bloquean el resto de la
aplicación. --strict exige los 29 archivos y su SHA-256 correspondiente.
"""
import argparse
import csv
import hashlib
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
INDEX = ROOT / "personajes/v1/manifest_referencias.tsv"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--strict", action="store_true", help="Exigir que todos los JPEG estén importados")
    args = parser.parse_args()
    with INDEX.open(encoding="utf-8", newline="") as stream:
        entries = list(csv.DictReader(stream, delimiter="\t"))
    if len(entries) != 29:
        raise ValueError(f"Se esperaban 29 referencias, recibidas {len(entries)}")
    ids = set()
    missing = []
    for record in entries:
        record_id = record["id"]
        if record_id in ids:
            raise ValueError(f"ID duplicado: {record_id}")
        ids.add(record_id)
        route = Path(record["ruta_prevista"])
        if route.is_absolute() or ".." in route.parts or route.suffix != ".jpg":
            raise ValueError(f"Ruta insegura: {route}")
        sha = record["sha256"]
        if len(sha) != 64 or any(c not in "0123456789abcdef" for c in sha):
            raise ValueError(f"SHA-256 inválido: {record_id}")
        path = ROOT / route
        if not path.exists():
            missing.append(str(route))
            continue
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if digest != sha:
            raise ValueError(f"SHA-256 no coincide: {route}")
    verified = len(entries) - len(missing)
    print(f"Referencias indexadas: {len(entries)} | verificadas en GitHub: {verified} | pendientes de subir: {len(missing)}")
    if missing and args.strict:
        for path in missing:
            print(f"FALTA: {path}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
