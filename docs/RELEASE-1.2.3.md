# Share Mode 1.2.3 — Connected characters and livelier idle

Local Android and Windows candidate. Fixes the head/collar attachment and expands idle animation, with a serious warrior and three anime-inspired character families. Public release and website download updates are separate steps.

## Art and movement

- A short skin-matched attachment sits behind the chin, with the head seated inside the existing collar. The original head/hair artwork is retained; a new optional **Focused** expression adds a rounded chin, confident eyes, a matching blink and left/right eye glances.
- The 9.6-second idle coordinates the chest, two arm/weapon groups, head and trailing hair. Boots stay planted during idle. The rhythm includes breathing, weight changes, head turns and a brief double blink. Tap/click the large avatar for a short cheer with raised arms and a changing ground shadow.
- All motion uses discrete pixel poses. Hidden/inactive/background avatars and active transfers pause their animations. Existing animation-off and system motion guards remain in place. There is no video, emulator, game engine or extra runtime dependency.
- The old faces, free colours, Boy/Girl looks, saved names, XP, receipts and original character unlock levels remain intact.

## New characters

| Character | First unlock | Mastery outfit |
| --- | ---: | ---: |
| Silent Ronin | 12, ordinary training gear | 55, dark layered armor |
| Gojo-inspired | 40, silver-haired academy look | 80, blindfolded teacher and blue effects |
| Saitama-inspired | 65, yellow training suit | 90, white cape and hero gloves |
| Naruto-inspired | 85, orange ninja outfit | 100, sage cloak and chakra effects |

Both Boy/Girl body variants are available. Equipping a new class applies its signature hair/expression/colour choices; these can be customized afterward. Locked previews never equip a character or change XP. New classes are appended without changing existing saved role indices. All sharing features stay available at every level. The XP curve and 50-XP maximum per new verified file are unchanged. Phone and PC progress remain separate.

## Compatibility and artifacts

Android package/signing identity, data paths, QR scheme, TLS, approvals and v1 transfer messages remain unchanged. New discovery metadata uses optional `hero` and `mood` fields while keeping the old `role` and five-part avatar fields readable by older peers. Older apps display a basic fallback appearance. New app versions display the new characters and focused mood. No discovery metadata authorizes a transfer.

Original full-resolution art, exact built-in generation prompts and resource compilation notes are in [the art revision record](../android/art/PIXEL-1.2.3.md). Only small game-resolution atlases are embedded in the applications. Rendered lineup and motion previews use sample profiles; they do not grant progress to a real installation.

Validation and final artifact metadata are recorded privately under `.local/verification-1.2.3.json`. Device installation and physical-transfer coverage must be reported separately from local rendering/tests. Owner visual and motion approval remains separate from automated checks.

## Final local validation

Android: 88 tests pass, zero failures/errors, release lint zero errors and 48 warnings. The signed APK is 2,878,334 bytes (about 2.88 MB), only 42,036 bytes larger than 1.2.2. The APK and AAB match the original signing certificate. Windows: 14 core/render/persistence/motion checks, 23 loopback protocol checks, 10 actual UI checks and a successfully decoded rendered QR. The native Windows executable is 369,664 bytes. Checks include every class/Look neck contact and planted feet across idle poses, mastery gates, new/legacy cosmetic round trips, focused blink/glance cels, paused Android animation, uniform locked ninja shadows and retained XP/receipts.

Windows used to gate idle on the menu-animation setting, which could leave an enabled avatar switch still. The update reads the client-area motion preference and allows an explicit **Share Mode only** opt-in; disabled animation, inactive/hidden screens, transfers and high contrast keep their guards. No Windows system setting is changed. The platform flag is documented in [Microsoft's client-area animation reference](https://learn.microsoft.com/en-us/windows/win32/winauto/client-area-animation).

The PC installation is backed up and updated in place, with the original connection identity, history and XP ledger retained. The included phone APK is checked against the signed artifact through the installed Windows executable's local download endpoint. The physical phone was not connected for this update; its 1.2.3 installation and physical radio/owner motion review remain separate. No commit, push, public ZIP release or website update was performed.
