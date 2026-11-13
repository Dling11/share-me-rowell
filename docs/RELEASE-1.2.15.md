# Share Mode preview - Android artwork update

Release bundle 1.2.15 contains Android **1.2.8** (version code 16) and the unchanged Windows **1.2.14** application. The Windows ZIP includes the new Android installer for Get phone app.

- Android now displays the same approved whole-character artwork as Windows throughout the profile editor, collection, nearby devices and left/right transfer cards. HD is the default; Pixel uses crisp sampling of the same artwork.
- Redesigned Boy/Girl defaults retain hair, expression, skin, outfit and element colour customization. Every change saves immediately.
- The character collection offers the personal avatar and eighteen fixed designs. Retired class choices and the old rank/gear gallery are replaced by the character Journey. An equipped retired class returns to the personal avatar while keeping its appearance settings, identity and earned progress.
- Starter and Mastery previews show distinct outfits at their existing unlock levels. Fixed characters, including Seraph and Sovereign, keep their own complete designs. Viewing a locked outfit or skill cannot equip it or grant XP.
- Madara's Crimson Awakening, Sukuna's Malevolent Shrine and Zoro's mastery Three-Sword Tempest play inside the preview card. Tap either transfer avatar to animate it independently. Automatic skills apply only to earned characters and can be disabled separately.
- Motion stops when the app leaves the foreground or animation is disabled. Skills use the existing sprite artwork with a bounded frame cache; no video player, account or additional animation engine is required.
- Compiled WebP cels replace the old low-resolution artwork in the release APK. Build verification rejects a mobile asset set that no longer matches the approved shared artwork.

Install the new APK over the existing app; do not uninstall first. Files, history, device identity, original signing identity, XP thresholds and earned unlocks are preserved. QR pairing, nearby discovery, resume and verified-transfer XP use the existing protocol.

Zoro retains the previously accepted idle-to-attack scale transition. This preview uses automated Android rendering/interaction and transfer tests; physical phone performance and a real two-phone radio retest are separate checks.
