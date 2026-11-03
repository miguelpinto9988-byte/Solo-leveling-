"""Regresiones estáticas de pantalla de carga y entrada Android."""
from __future__ import annotations
import pathlib
import struct
import unittest
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main"
ANDROID = "{http://schemas.android.com/apk/res/android}"


class SplashScreenTests(unittest.TestCase):
    def test_splash_is_only_launcher_and_main_is_internal(self):
        xml = ET.parse(APP / "AndroidManifest.xml").getroot()
        activities = xml.findall(".//activity")
        launchers = [
            a for a in activities
            if any(
                intent.find(f"action[@{ANDROID}name='android.intent.action.MAIN']")
                is not None
                and intent.find(
                    f"category[@{ANDROID}name='android.intent.category.LAUNCHER']"
                ) is not None
                for intent in a.findall("intent-filter")
            )
        ]
        self.assertEqual(len(launchers), 1)
        self.assertEqual(launchers[0].get(ANDROID + "name"), ".SplashActivity")
        main = next(a for a in activities if a.get(ANDROID + "name") == ".MainActivity")
        self.assertEqual(main.get(ANDROID + "exported"), "false")

    def test_artwork_is_valid_portrait_png(self):
        path = APP / "res/drawable-nodpi/loading_poster.png"
        raw = path.read_bytes()
        self.assertEqual(raw[:8], bytes.fromhex("89504e470d0a1a0a"))
        width, height = struct.unpack(">II", raw[16:24])
        self.assertGreaterEqual(width, 120)
        self.assertGreater(height, width)
        self.assertAlmostEqual(width / height, 992 / 1585, delta=0.02)

    def test_real_loading_and_shadow_animation_are_connected(self):
        activity = (APP / "java/com/gymbreaker/app/SplashActivity.kt").read_text()
        drawable = (APP / "java/com/gymbreaker/app/SplashArtView.kt").read_text()
        engine = (APP / "java/com/gymbreaker/app/ai/ShadowLocalLanguageModel.kt").read_text()
        self.assertIn("ShadowLocalLanguageModel.prepare", activity)
        self.assertIn("TrainingDatabase.get", activity)
        self.assertIn("poster.updateProgress(100)", activity)
        self.assertIn("drawRunner(canvas, runnerX, runnerY)", drawable)
        self.assertIn("ValueAnimator.ofFloat", drawable)
        self.assertIn("(finish - start) * progress", drawable)
        self.assertIn("MODEL_SHA256", engine)
        self.assertIn("onProgress(percent)", engine)


if __name__ == "__main__":
    unittest.main()
