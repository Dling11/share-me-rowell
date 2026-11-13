# Share Mode 1.2.0 — Your adventurer

Android development candidate, version code 8. Public GitHub and website downloads remain 1.1.0. Windows application features are unchanged. This update includes the unpublished 1.1.1 improvements.

## What's new

- **Autosave:** names, Boy/Girl looks, hair, colours, expressions and elements save immediately. Back, swiping away and Done keep your choices. Editing a profile updates its QR name without resetting the listener or losing an interrupted transfer.
- **Animated RPG avatars:** code-drawn chibi characters breathe, blink and wave when tapped. Animation can be disabled, respects Android's animator setting and stops during transfers and in the background.
- **Seven unlockable characters:** Courier Knight, Rune Mage, Forest Ranger, Shadow Reaper, Demon Warden, Heavenly Seraph and Astral Sovereign. The collection shows locked silhouettes, full previews and the next unlock. Every character supports Boy/Girl customization.
- **An offline journey:** start at Novice and progress through increasingly demanding levels to God-tier. XP bars and ornamental frames show your rank. A verified new file earns 10–100 XP; repeat content, incomplete transfers and failed verification cannot farm rewards. Rewards and unlocks are cosmetic.
- **Android Share menu:** send a file or a batch from Files, Gallery and other apps into Share Mode. Existing selections offer Add or Replace; active transfers keep incoming files waiting.
- **Clearer app sending:** Send apps remains available with selected files and can prepare installed APKs or complete split APK bundles.
- **Retry/resume:** a visible retry action, actual resumed byte offsets and verified reuse of files already completed in an interrupted batch.

Hair, colours, expressions, Boy/Girl looks and elemental styles remain free at every level. Speed and sharing features are never locked. Progress starts with this update, stays on the installation and is removed by Clear storage/uninstall. There is no account or leaderboard. See [the avatar guide](AVATARS.md) for thresholds and XP rules.

## Compatibility and delivery

Existing Android package/signing identity, local download folder, TLS identity handling, QR pairing and v1 transfer messages are retained. Nearby cosmetics use optional display fields; older peers keep the basic avatar and can still transfer files. The private XP ledger is never sent to another device.

The optimized APK and signed App Bundle are built locally. The previous 1.1.1 APK and published 1.1.0 artifacts are retained separately. Publishing, website download-pointer changes and Play submission are separate steps.

## Validation

The APK is 2,655,106 bytes (about 2.66 MB), 65,540 bytes larger than the 1.1.1 candidate. No runtime dependency or image pack was added. APK and AAB signatures match the original signing identity.

The full 72-test suite passed, followed by a successful targeted rerun after strengthening the past-history regression. Release lint reports no errors and 46 warnings. Rendered checks cover all seven characters in both looks, locked/equipped states, autosave, small screens and the existing Receive QR layout.

An API 36 Android emulator verified actual external-file grants, installed-app export, exact Android/Windows payload hashes, sender XP, repeat suppression, persistence after process restart, partial-file non-rewards and a receive resumed from byte 32,768. Reusing a completed file produced neither another download nor another XP reward. Native name/look/hair autosave survived Back and process restart; locked preview and avatar tap were exercised. The emulator was shut down afterward.

The connected Android 15 phone was updated in place to this exact APK and launched successfully; the original installation was retained. Physical two-phone radio, battery and owner visual/feel checks remain outside these results.
