#!/usr/bin/env python3
"""Auditoría técnica de PNG HD/FHD específicos del perfil (sin Pillow)."""
from __future__ import annotations

import hashlib
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "app/src/main/res/drawable-nodpi"
MANIFEST = ROOT / "art/profile_asset_manifest.json"
BACKGROUNDS = {
    "bg_profile_setup_dark_fhd", "bg_profile_setup_light_fhd",
    "bg_profile_summary_dark_fhd", "bg_profile_summary_light_fhd",
}
FRAMES = {"frame_profile_violet", "frame_profile_cyan", "frame_profile_gold"}
RANKS = {"badge_rank_" + x for x in "edcbas"}
ICONS = {"icon_profile_" + x for x in (
    "camera", "calendar", "target", "shield", "theme", "edit", "difficulty"
)}
ALL = BACKGROUNDS | FRAMES | RANKS | ICONS


def inspect_png(path: Path):
    blob = path.read_bytes()
    assert blob[:8] == bytes.fromhex("89504e470d0a1a0a"), f"PNG inválido: {path}"
    assert blob[12:16] == b"IHDR", f"IHDR faltante: {path}"
    width, height = struct.unpack(">II", blob[16:24])
    depth, mode = blob[24], blob[25]
    assert depth == 8, f"Profundidad no soportada: {path}"
    assert b"IEND" in blob[-28:], f"PNG truncado: {path}"
    return blob, width, height, mode


def validate() -> dict:
    assert len(ALL) == 20
    assert MANIFEST.is_file(), "Falta el manifiesto de recursos PNG del perfil"
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    assert manifest["schema"] == "profile-assets-1.0"
    assert manifest["no_monarch_backgrounds"] is True
    actual = {x["name"]: x for x in manifest["assets"]}
    assert set(actual) == ALL, f"Recursos inesperados: {set(actual) ^ ALL}"
    for name in sorted(ALL):
        path = ART / (name + ".png")
        assert path.is_file(), f"Falta {path}"
        blob, width, height, mode = inspect_png(path)
        expected = (1080, 1920, 2) if name in BACKGROUNDS else (512, 512, 6)
        assert (width, height, mode) == expected, f"{name}: {(width, height, mode)} != {expected}"
        data = actual[name]
        assert data["role"] == ("background" if name in BACKGROUNDS else "transparent")
        assert data["mode"] == ("RGB" if name in BACKGROUNDS else "RGBA")
        assert data["width"] == width and data["height"] == height
        assert hashlib.sha256(blob).hexdigest() == data["sha256"], f"SHA-256 inválido: {name}"
    assert not any("monarch" in name for name in ALL)
    return {"backgrounds": len(BACKGROUNDS), "transparent": len(ALL) - len(BACKGROUNDS)}


if __name__ == "__main__":
    result = validate()
    print("PNG del perfil aprobados:", result)
