# Share Mode 1.2.5 — Card Duelist

Local Android and Windows candidate adding a Yugi-inspired signature character in the approved full-character pixel style.

- **Card Duelist unlocks at Level 50.** Its Pharaoh Duelist mastery outfit appears at Level 95. Unlocking enables Equip; it does not automatically change the selected character.
- Both outfits have eight authored full-character cels: idle, blink, left/right turns, card presentation, pendant glint, cheer and smirk. Tap the large avatar to cheer. Existing motion preferences, reduced-motion, background and sharing guards are retained.
- Yugi has his own fixed head, face, hair, outfit and colours. Personal appearance controls are hidden while equipped; device name and animation settings remain editable. Switching back to a normal RPG character restores the saved personal appearance.
- Gojo, Saitama and Naruto remain available with their existing artwork and unlock/mastery levels. Existing role IDs, saved progress, content receipts, XP curve and file-sharing features are retained.
- Older peers can still transfer files. A new optional `guest` cosmetic key carries Yugi's role without expanding the old bounded `role` or `hero` fields. Nearby discovery and hello now use the same cosmetic metadata.

See the actual-renderer lineup (retired preview/source), Yugi's animation preview (retired preview/source) and [the original source and exact prompt](../android/art/YUGI-1.2.5.md). Only a 256×256 compiled atlas is added to application resources; the full-resolution source stays outside the APK. Android and Windows embed matching atlas bytes. No engine or extra runtime was added.

All **95 Android tests** pass with zero failures/errors; release lint has zero errors and 48 warnings. Windows passes **16 core/render/persistence checks, 23 loopback transfer checks, 10 UI checks and six signature UI/save/restart checks**, plus rendered QR decoding. Yugi's eight cels are reachable; every personal appearance field leaves fixed-character pixels unchanged. The actual picker equips Yugi at Level50 and retains the choice and personal style after restart. Test progress is isolated and labelled; no real installation receives XP or unlocks.

The signed APK is **3,084,162 bytes** (about 3.08 MB), the AAB 3,850,364 bytes and the native Windows executable 561,664 bytes. Android package/signing, TLS identity, QR format, approval and save paths are retained. Public release, website downloads, physical-phone installation and the owner's deferred focused-window animation check remain separate from this local candidate.
