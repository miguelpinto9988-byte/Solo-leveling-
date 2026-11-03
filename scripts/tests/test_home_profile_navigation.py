"""Regresión de navegación: Perfil exclusivamente vía fotografía de Inicio."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[2]
APP=ROOT/"app/src/main"
JAVA=APP/"java/com/gymbreaker/app"
LAYOUT=APP/"res/layout/activity_main.xml"
ANDROID="{http://schemas.android.com/apk/res/android}"


class HunterHomeTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root=ET.parse(LAYOUT).getroot()
        cls.main=(JAVA/"MainActivity.kt").read_text()
        cls.profile=(JAVA/"ProfileActivity.kt").read_text()
        cls.home=(JAVA/"ui/HunterHomeDashboard.kt").read_text()
        cls.catalog=(JAVA/"ui/MissionBackgroundCatalog.kt").read_text()

    def test_bottom_has_five_destinations_without_profile(self):
        bar=next(e for e in cls_walk(self.root)
                 if e.get(ANDROID+"id")=="@+id/bottomNavigation")
        labels=[e.get(ANDROID+"text") for e in list(bar)]
        self.assertEqual(labels,["Inicio","Rutinas","Misiones","Progreso","Más"])
        self.assertNotIn("Perfil",labels)
        ids=[e.get(ANDROID+"id") for e in list(bar)]
        self.assertNotIn("@+id/profileButton",ids)

    def test_profile_button_only_in_header_and_main_screen(self):
        header=next(e for e in cls_walk(self.root)
                    if e.get(ANDROID+"id")=="@+id/homeHeader")
        self.assertTrue(any(x.get(ANDROID+"id")=="@+id/profileButton" for x in header))
        self.assertIn("if (currentScreen == Screen.SYSTEM)", self.main)
        self.assertIn("ProfileActivity::class.java", self.main)
        menu=self.main.split("private fun showMoreMenu()",1)[1].split("private fun navigate(",1)[0]
        self.assertNotIn("Perfil",menu)
        self.assertNotIn("OnboardingActivity::class.java",self.main)
        self.assertIn('putExtra("edit_profile", true)',self.profile)
        self.assertIn("HunterOnboardingBackdrop",self.profile)

    def test_profile_is_internal_and_without_footer(self):
        manifest=ET.parse(APP/"AndroidManifest.xml").getroot()
        profile=next(x for x in manifest.findall(".//activity")
                     if x.get(ANDROID+"name")==".ProfileActivity")
        self.assertEqual(profile.get(ANDROID+"exported"),"false")
        self.assertNotIn("bottomNavigation",self.profile)

    def test_home_uses_actual_xp_schedule_companion_and_ai(self):
        self.assertIn("state.xp",self.home)
        self.assertIn("state.level",self.home)
        self.assertIn("state.rank",self.home)
        self.assertIn("state.weeklySessions",self.home)
        self.assertIn("setup.groups",self.home)
        self.assertIn("setup.hunterName",self.home)
        self.assertIn("SOMBRA ACOMPAÑANTE",self.home)
        self.assertIn("SHADOW CORE",self.home)
        self.assertIn("PREPARAR RUTINA",self.home)
        self.assertIn("HunterHomeDashboard.Actions(",self.main)

    def test_34_assets_with_monarch_exclusivity(self):
        assets=list((APP/"res/drawable-nodpi").glob("mission_bg_*.webp"))
        self.assertEqual(len(assets),34)
        self.assertEqual(len([a for a in assets if a.name.startswith("mission_bg_monarch_")]),9)
        self.assertEqual(len(re.findall(r"R\\.drawable\\.mission_bg_monarch_",self.catalog)),9)
        self.assertNotIn("mission_bg_monarch_",self.home)
        self.assertIn("MissionBackgroundCatalog.monarchBackground(selectedMonarch())",self.main)
        self.assertIn("MissionBackgroundCatalog.companionBackground(state.companion)",self.main)


def cls_walk(node):
    yield node
    for child in node:
        yield from cls_walk(child)


if __name__=="__main__":
    unittest.main()
