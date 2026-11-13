# Share Mode for Windows 1.2.10 — Skills in the character preview

Madara's skill now plays **inside the existing portrait card**. The separate cinematic window and Preview move action are removed.

- Click Madara's portrait or **Use skill** to activate Crimson Awakening in place. During playback, the button becomes **Stop**; clicking the animated portrait also stops it. Completion restores the normal idle portrait.
- **Auto**, beside HD and Pixel, plays the skill once when an unlocked Madara is selected or equipped. If Madara is currently selected when verified XP reaches Level 96, it plays once at that transition too. Routine XP refreshes, name edits and HD/Pixel changes never restart playback.
- Auto saves independently of appearance and Animation Off. Turning Auto off leaves manual activation available. Locked Madara can still demonstrate the skill manually; this never equips him or grants XP.
- Switching characters or tabs stops playback. Inactive, hidden and minimized previews pause, and motion/accessibility settings remain respected. Closing the editor disposes the scene and its timer.

The existing 64 cels and 9.4-second sequence are reused. No new art, engine or download is needed. The skill's first crimson-outfit design is unchanged, including when viewing mastery; it never alters equipped gear. The Windows executable is 41,581,056 bytes, only 2 KB larger than 1.2.9. Android remains the exact 1.2.6/code14 APK.

## Validation

**86 Windows checks pass:** 18 core, 23 transfer/resume, 10 existing UI, eight fixed-character UI/save/restart, nine studio, six HD/Pixel, six cinematic and six inline-skill cases. New checks exercise the actual portrait slot, manual button and portrait click, stop/character switching, default/minimum layout, HD/Pixel changes during playback, Auto persistence, old profile defaults and a labelled isolated fixture crossing Level 95 to 96 with one verified XP award. No real progress is seeded or edited.

Playback time is tested deterministically through the same state machine used by the UI timer. Native card screenshots and the 95-frame review GIF use fixture time; no window focus was forced. The owner's deferred live focused-window review remains separate. Phone/radio and high-DPI hardware were not retested. Existing identity, history, XP receipts and appearance are preserved; Android runtime/build/APK, transfer core and original artwork remain unchanged.

This update is installed locally on PC. GitHub releases and website downloads have not been changed.
