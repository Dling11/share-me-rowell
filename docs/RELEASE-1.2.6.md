# Share Mode 1.2.6 — Favourite signature collection

Twelve new fixed signature characters join Android and Windows. Each was generated individually in the approved pixel style: one character per source image, 24 authored cels comprising 12 starter and 12 mastery frames. Personal RPG characters remain customizable; signature characters equip as complete designs with their own heads, hair, faces and outfits.

| Character | First unlock | Mastery |
| --- | ---: | ---: |
| Luffy | 20 | 80 |
| Zoro | 28 | 82 |
| Boa Hancock | 32 | 84 |
| Shanks | 48 | 90 |
| Eren | 36 | 86 |
| Levi | 58 | 92 |
| Mikasa | 46 | 88 |
| Sukuna | 88 | 99 |
| Itachi | 68 | 94 |
| Rock Lee | 24 | 76 |
| Might Guy | 78 | 98 |
| Madara | 96 | 100 |

The character picker orders entries by earned unlock level, preserving saved IDs. Every new family begins with its starter outfit and gains its mastery outfit only at the listed later level. Existing characters, their old first/mastery levels, XP curve and verified-content deduplication remain intact. There is no currency, account or sharing-feature gate.

The 9.6-second idle sequence plays breathing, blink, left/right glance transitions, a signature gesture and recovery. Tap selects a separate reaction cel. The new characters use authored in-betweens with neutral planted feet; there is no generic customizable head or extra body bob layered over them. Madara specifically uses a cold narrow-eyed expression, heavy crimson armour and a commanding power reaction, with a separate Six-Paths-inspired mastery design at Level100.

Appearance edits remain hidden for fixed signatures; names and animation preferences remain available. Switching back restores personal appearance. Off, reduced-motion, inactive-window, background and active-transfer guards are retained. Runtime art uses compact 256x384 sheets; high-resolution sources are excluded from APK resources.

Older peers retain their basic-avatar fallback through an optional bounded `collect` display field (12–23), while existing `role`, `hero` and `guest` fields retain their previous bounds. TLS, QR, transfer approvals, receipt credit and file messages are unchanged.

Source art and exact prompts: [one-character generation record](../android/art/SOLO-COLLECTION-1.2.6.md). Validation and installed/public delivery status are recorded separately in the local verification report; a local candidate is not a public release.

All **99 Android tests** pass with zero failures/errors. Release lint has zero errors and 48 warnings. Windows passes **18 core/render/persistence checks, 23 loopback transfer checks, 10 UI checks and eight signature UI/save/restart checks**, plus displayed QR decoding. Tests cover all twelve new families' first/mastery gates and reachable authored frames, fixed-image invariance to personal style, saved-style restoration, actual picker equip and restart, old-role compatibility and optional metadata bounds. Fixtures are isolated; no real player XP or unlocks are granted.

The APK is **4,538,498 bytes** (about4.54MB), the AAB5,309,197bytes and the Windows executable2,020,352bytes. The original Android certificate/package are retained. The new artwork is bundled locally; no account, online art download, engine or additional runtime is needed. Collection preview (retired preview/source), Madara animation (retired preview/source), Luffy animation (retired preview/source) and Windows signature screen (retired preview/source) use labelled preview levels. Physical-phone installation/radio tests and the owner's deferred focused-window idle check are separate from these checks. This candidate has not been committed, pushed or published to website downloads.
