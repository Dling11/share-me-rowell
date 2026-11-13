# Share Mode for Windows 1.2.13 - Zoro mastery correction

Zoro's sword skill now belongs to his **Mastery outfit**, with black bandana, dark green robe and red sash. Starter keeps its existing idle animation. Open **Your adventurer > Characters > Zoro > Mastery > Use skill**, or click the mastery portrait. Locked mastery can be demonstrated without equipping it or receiving XP. Automatic use follows the existing **Level 82 mastery**; the starter character still unlocks at Level 28.

The replacement art has **72 whole-body poses plus 24 effect cels**. Wado Ichimonji's white handle is held in the mouth between pommel and guard, with one sword in each hand using Enma and Sandai Kitetsu design cues. Sources were reviewed for three blades and attached grips; a spurious blade-like arc was corrected. Whole-pose registration prevents sword tips from being cut by generated tile spacing.

The move now takes **2.8 seconds**, retaining the full-body view with no zoom. Cuts appear in 20 ms, with larger emerald trails and sharp crosscuts. The finishing impact splits the preview diagonally by up to 22 pixels, lights its frame and settles before returning to idle. Effects stay inside the character preview and never cover sharing controls or alter the desktop. Use skill/Stop, Auto, HD/Pixel and motion/accessibility settings remain in place. Zoro draws at the existing 25 ms interval; Madara and Sukuna remain unchanged at 33 ms.

The same four production art resources are replaced, keeping the explicit 47-image allowlist. No new engine or runtime was added. Executable size is **55,023,104 bytes**; draft generations and source/review media remain outside the installed app. The byte-identical Android 1.2.6/code14 APK is unchanged.

**98 Windows checks passed**, including transfer/resume, verified XP/hash receipts, persistence, QR, studio layout, old skill behavior and six revised Zoro cases. New checks cover mastery-only controls and Auto at the existing Level 82 gate, no starter activation, all 96 reachable cels in 2.8 seconds, instant onset/stronger final cut, strict clear body-cell boundaries, HD/Pixel rendering, clipping, stop/switch and locked preview without XP/equipment mutation. Progression fixtures are isolated; owner data is preserved.

The [actual animation](../assets/zoro-skill-1.2.13.gif) has 141 native-renderer frames at 20 ms, including the resting endpoint. See the [card screenshot](../assets/windows-zoro-skill-1.2.13.png) and [saved sources, references and exact prompts](../windows/art-source/ZORO-1.2.13.md). Rendering/timing uses fixtures; focused owner review remains deferred, and physical phone/radio and high-DPI hardware were not retested. The source candidate is ready for GitHub; public downloads and the phone remain unchanged.

Known visual issue accepted for this version: Zoro changes apparent scale when switching between the idle portrait and attack. Matching the idle/attack registration is deferred to the next visual polish pass.
