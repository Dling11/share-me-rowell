# Share Mode for Windows 1.2.9 — Madara: Crimson Awakening

Madara now has a replayable signature cinematic: a progressive hand sign, a close-up Sharingan reveal, a purple Susanoo emerging behind him, a commanding cast and a controlled return to idle. It uses **64 authored cels** across three new atlases: 24 gesture, 16 eye and 24 guardian frames. A 30 Hz native drawing timer adds camera framing, fades and restrained particles across the 9.4-second sequence.

Open **Your adventurer → Characters**, search **Madara**, select his card, then click the large portrait or **Preview move**. Unlocking Madara at the existing Level 96 changes that action to **Play move**. Locked previews never equip the character or grant XP. **Skip / Escape** closes the scene; **Replay** becomes available when it ends. The first movie uses his crimson outfit, including when viewing the mastery character, and never changes equipped gear.

The scene respects the saved HD/Pixel setting, Animation Off, Windows reduced motion, high contrast and the existing explicit app-only motion opt-in. It pauses while hidden, minimized or inactive. Animation Off shows a still with an explanation. New cinematic atlases load only while the scene is open and are disposed when it closes. Existing idle animation and transfer behavior remain intact; no engine, account or online artwork download was added.

## Validation and delivery

**80 Windows checks pass:** 18 core, 23 loopback transfer/resume, 10 existing UI, eight fixed-character UI/save/restart, nine studio, six HD/Pixel and six cinematic checks. The new suite covers all authored frame bounds/reachability, return to idle, motion policies, alpha/art availability, distinct actual-renderer scenes, fixed appearance, real preview entry/close, default/minimum scene controls and saved Off/no-XP/no-equipment changes. Test profiles are isolated.

The Windows executable is **41,579,008 bytes**, about 6.3 MB larger than 1.2.8 for the three compressed atlases. Android remains the byte-identical **1.2.6/code14 APK** at 4,538,498 bytes. Its 37 runtime files and build configuration, Windows TransferCore, original art and existing profile/XP/identity/history are retained. This is a local PC experiment, with no phone installation, GitHub push, public release or website update.
