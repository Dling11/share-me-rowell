# Share Mode for Windows 1.2.12 — Zoro: Three-Sword Tempest

Zoro now has a fast in-card sword skill with **96 authored cels**: 72 complete body poses and 24 emerald slash-effect cels. His open eye flashes green, he coils into a three-sword combo, cuts across the preview with three sharp impact beats, then sheathes his blades and returns to idle. There is **no zoom or face cut-in**.

The sequence takes **3.6 seconds**. Zoro alone uses a 25 ms active drawing interval to keep the quick poses readable; Madara and Sukuna retain their existing 33 ms interval and 5.2/4.8-second timing. Motion pauses when inactive or hidden, and Animation Off/accessibility rules remain in effect. Local screen cuts use a small reusable buffer confined to the portrait area, leaving the rest of the app interactive.

Open **Your adventurer → Characters → Zoro**, then click the portrait or **Use skill**. **Stop** returns to idle. Auto follows his original **Level 28** unlock and plays once on selection or a selected unlock transition. Locked manual demonstrations still never equip or grant XP. The move uses his first white-shirt costume; it does not change selected mastery gear or saved personal appearance.

Only four new production PNG atlases are bundled. The explicit image allowlist now contains 47 entries; source variants, unused generations, screenshots and review GIFs remain outside the installed app. The executable is **53,765,632 bytes**. Android remains the byte-identical **1.2.6/code14 APK** at 4,538,498 bytes. Existing runtime cleanup and the shared legacy launcher are retained.

## Validation

**98 Windows checks pass:** the prior 92 plus six Zoro cases. New checks cover all 96 reachable cels, constant full-body framing, three impact beats, source resolution and blade-boundary quality, actual HD/Pixel rendering, fixed appearance, clipping to the preview, no-popup controls, default/minimum layouts, stop/switch behavior, a labelled isolated Level 27-to-28 award, Auto persistence, motion guards and the role-specific drawing interval. Source tip refinements removed neighbouring-cell blade fragments; validation permits only one near-black antialias sample at a boundary rather than a visible clipped band.

The review GIF contains 181 native-renderer frames at 20 ms each, including the resting endpoint. Screenshots and timing tests use isolated profiles and deterministic time. No owner focus was forced; the previously deferred live visual review, physical phone/radio and high-DPI hardware checks remain separate. Transfer/resume, identity/history/XP receipts, old artwork and phone runtime/build/APK are preserved.

This is a local PC update. No GitHub push, public release, website update or phone installation is included.
