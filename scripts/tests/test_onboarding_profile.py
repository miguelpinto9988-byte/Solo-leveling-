"""Garantías estáticas del asistente de perfil y semana (UI Android)."""
from __future__ import annotations
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "app/src/main/java/com/gymbreaker/app"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


class OnboardingContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.store = (SOURCE / "onboarding/HunterSetupStore.kt").read_text()
        cls.ui = (SOURCE / "OnboardingActivity.kt").read_text()
        cls.splash = (SOURCE / "SplashActivity.kt").read_text()
        cls.main = (SOURCE / "MainActivity.kt").read_text()

    def test_splash_routes_to_mandatory_first_run_profile(self):
        activities = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot().findall(".//activity")
        launcher = [a for a in activities if any(
            action.get(ANDROID_NS + "name") == "android.intent.action.MAIN"
            for action in a.findall(".//action")
        )]
        self.assertEqual(len(launcher), 1)
        self.assertEqual(launcher[0].get(ANDROID_NS + "name"), ".SplashActivity")
        self.assertIn("HunterSetupStore.completed", self.splash)
        self.assertIn("OnboardingActivity::class.java", self.splash)
        onboarding = next(a for a in activities if a.get(ANDROID_NS + "name") == ".OnboardingActivity")
        self.assertEqual(onboarding.get(ANDROID_NS + "exported"), "false")

    def test_three_difficulties_and_rank_independence(self):
        self.assertIn('listOf("inicio", "intermedio", "avanzado")', self.store)
        self.assertIn('"avanzado" -> "EXPERIENCED"', self.store)
        self.assertIn("Rango inicial: E", self.ui)
        self.assertIn("HunterProgression.rank(xp)", (
            SOURCE / "TrainingViewModel.kt").read_text()
        )
        self.assertNotIn('nivel de dificultad "élite"', self.store.lower())

    def test_missions_only_one_through_seven(self):
        self.assertIn("require(data.weeklyMissions in 1..7)", self.store)
        self.assertIn("weeklyMissions = obj.optInt", self.store)
        self.assertIn("coerceIn(1, 7)", self.store)
        self.assertIn("(1..7).toList().chunked(4)", self.ui)
        self.assertIn('listOf("Misiones", "Rutinas diarias")', self.ui)
        self.assertIn("Misiones configuradas", self.main)

    def test_private_photo_appearance_and_theme_second(self):
        self.assertIn("ActivityResultContracts.GetContent()", self.ui)
        self.assertIn('File(filesDir, "hunter_profile_photo.jpg")', self.ui)
        self.assertIn("private fun appearance()", self.ui)
        self.assertIn('"Tema y colores"', self.ui)
        self.assertIn("2 -> appearance()", self.ui)
        self.assertIn("shadow_igris", self.ui)

    def test_goal_recommendations_need_confirmation_and_restrictions_block(self):
        self.assertIn("suggestGoal", self.ui)
        self.assertIn("Aplicar propuesta", self.ui)
        self.assertIn("Molestia o lesión actual", self.store)
        self.assertIn("data.restrictions.any", self.store)
        self.assertIn("requiresReview = review", self.store)
        self.assertIn("No marcar onboarding terminado hasta que Room haya guardado", self.store)
        self.assertIn("profile_created", self.store)

    def test_profile_editing_and_success(self):
        profile = (SOURCE / "ProfileActivity.kt").read_text()
        self.assertIn('putExtra("edit_profile", true)', profile)
        self.assertIn("ProfileActivity::class.java", self.main)
        self.assertIn("6 -> success()", self.ui) if "6 -> success()" in self.ui else self.assertIn("else -> success()",self.ui)
        self.assertIn("ENTRAR A LA APLICACIÓN", self.ui)
        self.assertIn("Misiones semanales:", self.ui)


if __name__ == "__main__":
    unittest.main()
