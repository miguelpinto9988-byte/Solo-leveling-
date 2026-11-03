"""Regresión de recursos PNG Full HD y sus usos en el perfil independiente."""
from __future__ import annotations
import pathlib
import sys
import unittest
ROOT=pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/"scripts"))
from validate_profile_png_assets import validate

class ProfilePngAssetsTests(unittest.TestCase):
    def test_dimensions_transparency_and_hashes(self):
        self.assertEqual(validate(),{"backgrounds":4,"transparent":16})

    def test_profile_uses_native_full_hd_png_and_composable_frames(self):
        base=ROOT/"app/src/main/java/com/gymbreaker/app"
        layout=(base/"OnboardingActivity.kt").read_text(encoding="utf8")
        profile=(base/"ProfileActivity.kt").read_text(encoding="utf8")
        artwork=(base/"onboarding/ProfileArtwork.kt").read_text(encoding="utf8")
        scene = (base/"onboarding/ProfileSceneArtwork.kt").read_text(encoding="utf8")
        self.assertIn("ProfileSceneArtwork.apply", layout)
        self.assertIn("ProfileSceneArtwork.veil", layout)
        self.assertIn("setupBackground", scene)
        self.assertIn("summaryBackground", scene)
        self.assertIn("portrait(this, setup", layout)
        self.assertIn("ProfileArtwork.summaryBackground",profile)
        self.assertIn("ProfileArtwork.rankBadge",profile)
        self.assertIn("hunter_profile_photo.png",artwork)
        self.assertIn("hunter_profile_photo.jpg",artwork)
        self.assertNotIn("mission_bg_monarch_",layout+profile+artwork)

    def test_profile_does_not_enter_bottom_navigation(self):
        layout=(ROOT/"app/src/main/res/layout/activity_main.xml").read_text(encoding="utf8")
        self.assertIn('android:id="@+id/profileButton"',layout)
        nav=layout.split('android:id="@+id/bottomNavigation"',1)[1]
        self.assertNotIn('android:id="@+id/navProfile"',nav)

if __name__=="__main__":
    unittest.main()
