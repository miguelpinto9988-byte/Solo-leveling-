#!/usr/bin/env python3
"""Obtiene pesos GGUF verificables para la APK offline; sin archivos ficticios."""
from __future__ import annotations

import hashlib
import pathlib
import sys
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[1]
MODEL_NAME = "SmolLM2-135M-Instruct-Q2_K.gguf"
RELATIVE_PATH = "shadow_core/" + MODEL_NAME
DESTINATION = ROOT / "app/src/main/assets" / RELATIVE_PATH
MODEL_URL = (
    "https://huggingface.co/tensorblock/SmolLM2-135M-Instruct-GGUF/"
    "resolve/32db44d/" + MODEL_NAME
)
EXPECTED_SHA256 = "478cca924101ed67bb2751f597be41532b2f4817ee2ab1b0d7cc379c76e79faf"
MAX_MODEL_BYTES = 89_000_000
MIN_MODEL_BYTES = 80_000_000


def verify(path: pathlib.Path) -> int:
    if not path.is_file():
        raise ValueError(f"Pesos GGUF ausentes: {path}")
    size = path.stat().st_size
    if not MIN_MODEL_BYTES <= size < MAX_MODEL_BYTES:
        raise ValueError(f"Longitud GGUF inesperada: {size}")
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        if stream.read(4) != b"GGUF":
            raise ValueError("Cabecera GGUF no válida")
        stream.seek(0)
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    if digest.hexdigest() != EXPECTED_SHA256:
        raise ValueError(f"SHA-256 distinto del publicado: {digest.hexdigest()}")
    return size


def download() -> None:
    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    if DESTINATION.exists():
        print(f"Modelo existente verificado: {verify(DESTINATION):,} bytes")
        return

    temporary = DESTINATION.with_suffix(".gguf.partial")
    temporary.unlink(missing_ok=True)
    request = urllib.request.Request(
        MODEL_URL, headers={"User-Agent": "SoloLevelingGymBreaker-CI/1.0"}
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as source, temporary.open("wb") as output:
            total = 0
            while chunk := source.read(1024 * 1024):
                total += len(chunk)
                if total > MAX_MODEL_BYTES:
                    raise ValueError("El archivo excede el presupuesto permitido")
                output.write(chunk)
        size = verify(temporary)
        temporary.replace(DESTINATION)
        print(f"Modelo GGUF independiente y verificado: {size:,} bytes")
    finally:
        temporary.unlink(missing_ok=True)


if __name__ == "__main__":
    try:
        if "--check" in sys.argv[1:]:
            print(f"Pesos incluidos y verificados: {verify(DESTINATION):,} bytes")
        else:
            download()
    except (OSError, ValueError) as error:
        print(f"MODELO NO VERIFICADO: {error}", file=sys.stderr)
        sys.exit(1)
