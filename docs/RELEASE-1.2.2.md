# Share Mode 1.2.2 — Simple beginnings, rare endgame gear

Android local candidate, version code 10. Contains the previous pixel-art, profile autosave, installed-app sharing, Android Share-menu and retry/resume improvements. Public release and website downloads are separate steps.

The matching [Windows 1.2.2 local candidate](RELEASE-WINDOWS-1.2.2.md) now implements the avatar editor and verified-transfer progression on PC. PC and phone XP remain separate.

## What's new

- Level 1 starts in plain clothes with empty hands. Training leather/wood gear arrives at 10, modest iron at 25 and the first ornate Courier/Mage/Ranger sets at 45.
- Shadow Reaper's full form arrives at 60, Demon Warden's horns/enchanted set at 75, Seraph's wings/halo at 85 and the final ascended Astral set at 100. The level-95 Astral unlock begins as an Astral Herald.
- Early class unlocks give their basic forms: Apprentice Mage (8), Forest Scout (18), Shadow Initiate (35), Warden Recruit (55) and Seraph Aspirant (75). Existing class unlock thresholds and stored XP are retained.
- The Ranks tab now contains a read-only gear roadmap. Looking at future gear does not equip it or award XP. Early outfits also have simpler frames and no ambient magical sparkles or advanced headpieces.
- Future new verified files earn 1 XP per complete 256 KiB, capped at 50 per file. Smaller files still share normally without XP. Repeated content, retries and previews do not farm rewards. Previously earned XP/levels are not recalculated.
- A new incoming batch resets its reward summary, so a repeat receive cannot display an old XP bonus.

Hair, skin, expressions, Boy/Girl looks and outfit/element colours remain free and autosaved. Sharing speed and features remain available at every level.

## Rarity

The existing increasingly expensive level curve remains: level 45 requires 332,992 XP and level 100 requires 3,302,937 XP. A new player needs at least **66,059 unique maximum-reward files** to reach 100 under the new reward rate. Progress remains device-local and cosmetic; it is not a competitive or tamper-proof score.

[Level 1–100 gear gallery](../assets/android-gear-progression.png) · [Actual app idle comparison](../assets/android-pixel-idle.gif) · [Artwork sources and generation prompts](../android/art/README.md)

## Size and validation

The signed APK is **2,836,298 bytes (about 2.84 MB)**, 67,804 bytes larger than 1.2.1. The extra progression art is compiled to a 67,642-byte atlas. Full-resolution art stays outside the APK; no runtime dependency, game engine or permission was added. Existing head/hair artwork is unchanged.

All **84 automated tests pass**. They cover gear/ornament gates, the exact reward floor/cap, retention of an old XP/receipt database, authored art, idle/freeze behavior, autosave, approvals, small-screen QR layout and positive XP after a resumed verified file. Release lint reports no errors and 48 warnings. APK and AAB signatures match the original signer.

The API36 Android lab retained its old 65 XP/level2 and original installation identity after upgrade. A verified 32 MiB file earned 50 XP, a 64 KiB file earned none, an interrupted 1 MiB file earned nothing until it resumed at byte 19,001 and verified for 4 XP, and an empty file earned nothing. Reused completed files produced no duplicates. All four payload hashes matched; repeating the large file awarded nothing and showed no stale bonus. Gear previews also left XP unchanged.

Package/signing identity, the local download folder, QR format, v1 transfer messages and pairing/TLS behavior are retained. Older peers may display their older outfits; transfers remain compatible. Physical radio/battery and owner visual/feel approval are outside these lab results.

Phone installation reported Success, and Android package metadata confirms 1.2.2/code10 with the original installation date. USB disconnected before the independent installed-APK hash and launch checks, which remain pending. No source push, public release or website pointer change was made.
