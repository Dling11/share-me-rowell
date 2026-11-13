# Share Mode 1.2.4 — Personal avatars and signature characters

Local Android and Windows candidate. Anime-inspired heroes now have complete fixed designs instead of using the customizable avatar's generic head and palette. Public release and website updates remain separate.

## Choose a character, then equip

Gojo-, Saitama- and Naruto-inspired characters each have their own complete head shape, face, hair, outfit, colours and authored idle cels. Unlocking makes the **Equip** action available; it does not automatically change the player's selection. The same first-unlock and mastery levels remain: Gojo40/80, Saitama65/90 and Naruto85/100. Outfits evolve to their fixed mastery form at the existing earned level.

The **Characters** screen labels these **Fixed signature** and the original RPG characters **Customizable**. When a signature is equipped, **Style** hides hair, colours, expression, outfit, element and Boy/Girl controls and shows a fixed-design card with a route back to the character picker. Device name and animation preferences remain available. The large Windows title shows the signature character's name, separate from the nearby device name.

The Courier, Mage, Ranger, Reaper, Warden, Seraph, Sovereign and Silent Ronin keep their appearance controls. Personal hair, skin, colours, expression and Boy/Girl choice remain saved while using a signature. Switching back restores those settings. Both the persistence layer and renderer enforce the distinction: changing personal style fields cannot change a fixed character or overwrite the saved personal style while one is equipped.

## Art and progress

One transparent, compact sheet contains the three characters' fixed basic/mastery designs and four complete idle cels each. Their heads are part of those authored designs; the generic head/hair compositor is bypassed. Original artwork stays unchanged for customizable avatars. The unchanged generated source and exact built-in prompt are in the artwork record. Only the 256x384 game-resolution sheet enters application resources, with matching bytes on phone and PC. No engine, emulator, account or additional runtime dependency was introduced.

XP, permanent content receipts, class IDs, level costs, existing unlocks, data locations, Android package/signing, TLS, QR format and transfer approvals are retained. Phone and PC progress remain separate. Older versions may display their older interpretation of these cosmetics; this change introduces no transfer protocol fields.

## Validation

92 Android tests pass with zero failures/errors; release lint has zero errors and 48 warnings. Windows passes 15 core/render/persistence checks, 23 loopback transfer checks, 10 UI checks and four dedicated fixed-character UI/storage checks, plus rendered QR decoding. Tests verify immutable rendering under every personal appearance change, free editing on normal characters, hidden fixed-character controls, saved-style restoration, name edits, restart persistence, locked previews and unchanged XP. The fixed-character UI fixture uses labelled isolated progress; no real installation was granted XP or unlocks.

The signed APK is 3,004,778 bytes (about 3.00 MB). The native Windows executable is 482,304 bytes. Physical phone installation and the owner's focused-window animation review remain separate from the local tests. No commit, push, public binary release or website update is included in this local change.
