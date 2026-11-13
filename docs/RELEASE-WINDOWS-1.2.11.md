# Share Mode for Windows 1.2.11 — Sukuna and faster skills

Sukuna now performs **Domain Expansion: Malevolent Shrine** inside the existing character preview. His 64 authored cels cover the hand sign, a curse-marked face cut-in, the shrine emerging with crimson energy, and a return to idle. Select Sukuna, then click his portrait or **Use skill**. **Stop** cancels it. Auto plays once after the existing Level 88 unlock; locked manual demonstrations still never equip or grant XP. The first animation uses his school-uniform design without changing selected mastery gear.

Madara now plays in **5.2 seconds instead of 9.4**, approximately 1.8 times faster. Sukuna's sequence takes **4.8 seconds**. Both retain all authored frames, HD/Pixel choices, saved Auto, Animation Off and accessibility/background pause rules. The native drawing timer remains 33 ms; shortening the pose holds improves speed without adding a faster background loop.

## Keeping the app lean

The Windows build now embeds an explicit allowlist of 43 production PNG atlases. Source sheets, unused generations, review GIFs and screenshots are excluded. Only three new Sukuna production atlases enter the executable, which is 48,118,784 bytes. The Android 1.2.6/code14 APK stays byte-identical at 4,538,498 bytes.

Local installation cleanup archives old review/source folders, old ZIPs and the unused legacy APK outside the installed app. It retains the current Android installer, runtime libraries, licenses and personal state. The two executable names share one physical file where NTFS hard links are available, preserving the legacy launcher without storing a second full binary. Future installations should follow the same rule: keep authoring and review material in the source project, never copy the project assets folder into the runtime.

## Validation

**92 Windows checks pass:** 18 core, 23 loopback transfer/resume, 10 existing UI, eight fixed-character UI/save/restart, nine studio, six HD/Pixel, six Madara cinematic, six in-card skill and six Sukuna cases. New cases cover all 64 Sukuna cels, real in-card controls, character switching, supported sizes, fixed appearance, no manual XP/equip change, a labelled isolated Level 87-to-88 award, Auto persistence, motion guards and the actual embedded resource list. Faster Madara playback and paused/finished/replay state are also checked.

Review GIFs use 50 ms frame timing: 105 frames for Madara and 97 for Sukuna, including a final resting frame. Screenshots and playback-state tests use isolated profiles and deterministic time. No owner focus was forced; deferred live visual review, phone/radio and high-DPI hardware testing remain separate. Transfer core, existing XP receipts/identity/history, old artwork and Android runtime/build/APK are preserved.

See the [Sukuna preview](../assets/windows-sukuna-skill-1.2.11.png), [Domain Expansion animation](../assets/sukuna-skill-1.2.11.gif), [faster Madara](../assets/madara-skill-1.2.11.gif), and [source art with exact prompts](../windows/art-source/SUKUNA-1.2.11.md). These previews remain in the source project and are not installed runtime files.

This is a local PC update. No GitHub push, public release, website change or phone installation is included.
