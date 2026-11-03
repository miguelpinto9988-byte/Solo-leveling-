#!/usr/bin/env python3
"""Regresión: los fondos Monarca no se filtran a las pantallas normales."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[2]
JAVA=ROOT/"app/src/main/java/com/gymbreaker/app"
ART=JAVA/"ui/MissionBackgroundCatalog.kt"
SCREEN=JAVA/"MainActivity.kt"
DRAWABLE=ROOT/"app/src/main/res/drawable-nodpi"


class MissionBackdropTests(unittest.TestCase):
    def test_monarchs_resolved_exclusively_in_monarch_catalog(self):
        source=ART.read_text()
        left=source.split("fun normalBackground(",1)[1].split("fun monarchBackground(",1)[0]
        self.assertNotIn("mission_bg_monarch_",left)
        self.assertEqual(len(re.findall(r'R\.drawable\.mission_bg_monarch_[a-z_]+',source)),9)
        self.assertIn("fun monarchBackground",source)

    def test_daily_missions_keep_non_monarch_art(self):
        screen=SCREEN.read_text()
        self.assertIn('currentScreen == Screen.MISSIONS',screen)
        self.assertIn('MissionBackgroundCatalog.companionBackground(state.companion)',screen)
        self.assertIn('MONARCA SEMANAL',screen)
        self.assertIn('No aumenta la dificultad ni altera el entrenamiento',screen)

    def test_image_pack_integrity(self):
        backgrounds=list(DRAWABLE.glob("mission_bg_*.webp"))
        self.assertEqual(34,len(backgrounds),"Importar los 34 fondos antes de compilar")
        self.assertEqual(9,len(list(DRAWABLE.glob("mission_bg_monarch_*.webp"))))
        self.assertLess(sum(path.stat().st_size for path in backgrounds),2_000_000)
        self.assertGreater(min(path.stat().st_size for path in backgrounds),2000)


if __name__=="__main__":
    unittest.main()
