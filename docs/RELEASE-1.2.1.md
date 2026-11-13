# Share Mode 1.2.1 — Chibi pixel RPG update

Android development candidate, version code 9. Includes the preceding 1.2.0 avatar/XP update and unpublished 1.1.1 Share-menu/app-picker/resume improvements. Publishing and website download-pointer changes are separate steps.

## What's new

- All seven classes now use chibi pixel RPG equipment, generated pixel faces and separate hairstyles. The initial boxy head prototype was replaced before installation.
- Six hairstyles, five skin tones, six hair colours including silver, five outfit colours, three expressions, six elements and Boy/Girl bodies remain freely selectable. Profile changes still save automatically.
- A stepped idle loop adds breathing, blinking and elemental sparkle. Tap the large preview to cheer. Animation stops during transfers or in the background, respects Android's motion setting and can be disabled.
- Pixel rank frames, a segmented XP bar, cut-corner cards/buttons and the character collection bring the RPG styling into the app. Send and Receive retain their existing actions.
- Existing levels, XP, profile settings, installation identity and saved files are retained. No sharing feature is gated by progression.

## Size and artwork

The APK is **2,768,494 bytes (about 2.77 MB)**, 113,388 bytes larger than 1.2.0. There is no new runtime dependency or game engine. The build compiles generated source art into a 256x256 equipment atlas (71,494 bytes) and a 192x192 face/hair atlas (24,912 bytes). Full-resolution source images stay outside the APK. Recolour caches are bounded.

## Validation and compatibility

All **79 automated tests pass**, including the generated layer registration, expressions, skin/hair recolouring, discrete idle movement and animation-off freeze. Existing small-phone QR layout, autosave, character gates, XP receipts and resume checks remain covered. Release lint has **0 errors and 48 warnings**. APK and AAB signatures match the original signer.

Two API 36 virtual phones tested **1.2.1 receiving from 1.2.0** over native nearby discovery. The older peer found the silver-haired receiver, both transfer codes matched, filenames stayed withheld until sender confirmation and the new received file matched its expected SHA-256. Existing XP and installation identity survived upgrade. Repeated content and character previews earned no extra XP. Native profile autosave, silver hair, locked preview and cheer interaction were exercised.

Silver uses a bounded optional hairTone attribute; older peers receive the existing gold hair index as a display fallback. The v1 transfer format, QR scheme, approval flow, TLS handling, package/signing identity and Downloads/SHARE-ME-ROWELL location are unchanged.

The connected Android 15 phone was updated in place. Its installed APK was pulled and matched the tested build exactly; version 1.2.1/code 9 and the original first-install identity were verified, and the app launched. Remote taps were not used. The virtual phones were shut down after testing. Physical two-phone radio/battery and owner visual/feel approval are outside these automated checks.
