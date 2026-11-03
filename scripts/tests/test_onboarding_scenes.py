"""Regresión: cada etapa del onboarding tiene un fondo permitido y ningún Monarca."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[2]
SOURCE=ROOT/"app/src/main/java/com/gymbreaker/app"
CONFIG=SOURCE/"onboarding/ProfileSceneArtwork.kt"
SCREEN=SOURCE/"OnboardingActivity.kt"

class OnboardingScenesTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.scenes=CONFIG.read_text(encoding="utf-8")
        cls.ui=SCREEN.read_text(encoding="utf-8")

    def test_seven_distinct_non_monarch_scenes(self):
        entries=re.findall(r'Scene\("([^"]+)", "([^"]+)"\)',self.scenes)
        self.assertEqual(len(entries),7)
        self.assertEqual(len(set(path for _,path in entries)),7)
        self.assertTrue(all("monarca" not in name.lower() and "monarch" not in file.lower()
                            for name,file in entries))
        self.assertIn("bg_01_ciudad_moderna_fhd.png",self.scenes)
        self.assertIn("bg_02_gimnasio_del_cazador_fhd.png",self.scenes)
        self.assertIn("bg_09_cueva_de_cristales_fhd.png",self.scenes)
        self.assertEqual([p for _,p in entries],[
            "bg_01_ciudad_moderna_fhd.png",
            "bg_03_sede_de_la_asociacion_fhd.png",
            "bg_06_base_de_cazadores_fhd.png",
            "bg_02_gimnasio_del_cazador_fhd.png",
            "bg_04_sala_de_entrenamiento_fhd.png",
            "bg_09_cueva_de_cristales_fhd.png",
            "bg_24_interior_del_gate_fhd.png",
        ])
        self.assertIn("Paso 6 de 6",self.ui)
        self.assertIn("(1..6).joinToString",self.ui)

    def test_android_ui_keeps_native_controls_above_background(self):
        self.assertIn("ProfileSceneArtwork.apply",self.ui)
        self.assertIn("ProfileSceneArtwork.veil",self.ui)
        self.assertLess(self.ui.index("root.addView(decor"),self.ui.index("root.addView(veil"))
        self.assertLess(self.ui.index("root.addView(veil"),self.ui.index("root.addView(scroll"))
        self.assertIn("ImageView.ScaleType.CENTER_CROP",self.scenes)
        self.assertIn("options.outWidth < 1080 || options.outHeight < 1920",self.scenes)

    def test_missing_assets_have_fhd_fallback(self):
        self.assertIn("ProfileArtwork.setupBackground(darkTheme)",self.scenes)
        self.assertIn("ProfileArtwork.summaryBackground(darkTheme)",self.scenes)
        self.assertIn("context.assets.open(",self.scenes)
        self.assertIn("onboarding_backgrounds/",self.scenes)

if __name__=="__main__":
    unittest.main()
