"""Pruebas de formato y uso correcto de recursos HD en los cinco pasos."""
from __future__ import annotations

from pathlib import Path
import struct
import unittest

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "app/src/main/res/drawable-nodpi"
UI = ROOT / "app/src/main/java/com/gymbreaker/app/OnboardingActivity.kt"
GEN = ROOT / "scripts/build_profile_png_assets.py"

STEPS = ["identity", "appearance", "week", "goals", "restrictions"]
PNG_MAGIC = b"\x89PNG\r\n\x1a\n"


def png_header(path: Path):
    with path.open("rb") as file:
        header = file.read(29)
    if header[:8] != PNG_MAGIC or header[12:16] != b"IHDR":
        raise AssertionError(f"Archivo no válido PNG: {path}")
    return struct.unpack(">IIBBBBB", header[16:29])


class OnboardingPngAssetsTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.ui = UI.read_text(encoding="utf-8")
        cls.generator = GEN.read_text(encoding="utf-8")

    def test_exactly_five_unique_full_hd_backgrounds(self):
        files = sorted(RES.glob("bg_onboarding_*_fullhd.png"))
        self.assertEqual(len(files), 5, files)
        self.assertEqual(
            [f.name for f in files],
            sorted(f"bg_onboarding_{step}_fullhd.png" for step in STEPS)
        )
        for f in files:
            width, height, bits, color, *_ = png_header(f)
            self.assertEqual((width, height), (1080, 1920), f)
            self.assertEqual(bits, 8)
            self.assertIn(color, (2, 6))
            self.assertIn(f"R.drawable.{f.stem}", self.ui)
        self.assertIn("ImageView.ScaleType.CENTER_CROP", self.ui)

    def test_icons_and_portrait_frames_are_transparent_png(self):
        icons = sorted(RES.glob("icon_profile_*.png"))
        frames = sorted(RES.glob("frame_profile_*.png"))
        self.assertEqual(len(icons), 9)
        self.assertEqual(len(frames), 3)
        for path in icons + frames + [RES / "badge_rank_e.png"]:
            width, height, bits, color, *_ = png_header(path)
            self.assertGreaterEqual(width, 512)
            self.assertGreaterEqual(height, 512)
            self.assertEqual(bits, 8)
            self.assertEqual(color, 6, path)  # PNG RGBA con canal alfa
        for theme in ["violet", "cyan", "gold"]:
            self.assertIn(f"R.drawable.frame_profile_{theme}", self.ui)

    def test_steps_and_theme_order_unchanged(self):
        expected = (
            "1 -> information()",
            "2 -> appearance()",
            "3 -> calendar()",
            "4 -> goals()",
            "5 -> restrictions()",
            "else -> success()",
        )
        for fragment in expected:
            self.assertIn(fragment, self.ui)
        self.assertIn('listOf("Misiones", "Rutinas diarias")', self.ui)
        self.assertIn("(1..7).toList().chunked(4)", self.ui)
        self.assertIn('"Básico", "Intermedio", "Avanzado"', self.ui)
        self.assertIn("ActivityResultContracts.GetContent()", self.ui)
        self.assertIn("framedPortrait(186)", self.ui)
        self.assertIn("R.drawable.icon_profile_theme", self.ui)
        self.assertIn("R.drawable.icon_profile_difficulty", self.ui)

    def test_renderer_does_not_upscale_a_preview(self):
        self.assertIn("1080, 1920", self.generator)
        self.assertIn('output(name, im.convert("RGB"))', self.generator)
        self.assertNotIn("resize(", self.generator)
        self.assertNotIn("Image.open(", self.generator)


if __name__ == "__main__":
    unittest.main()
