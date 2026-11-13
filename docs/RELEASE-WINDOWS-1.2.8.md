# Share Mode for Windows 1.2.8 — Sharper portraits

The PC studio enlarged 128-pixel portrait frames with smooth filtering, which softened faces and outlines. This update uses 256-pixel frames from the existing original artwork and adds an **HD / Pixel** choice above the large character preview.

- **HD** is the default for profiles without a saved preference. It retains the larger source detail and centers portraits at native size instead of enlarging them. Smaller previews use filtered reduction.
- **Pixel** samples complete characters into 128-pixel frames and enlarges them by whole multiples with nearest-neighbor drawing. Outlines remain crisp blocks. The choice saves automatically and also applies to your sidebar portrait.
- Personal-avatar breathing uses integer movement instead of stretching the image. Existing idle timing, motion preferences, reduced-motion guards and tap reactions remain in place.
- Atlas, recoloured-cell and Pixel caches have fixed limits. Fixed character thumbnails are reused when personal colours change. Only one HD artwork set is embedded; Pixel frames are derived locally.

Original art, fixed-character designs, personal customization, XP receipts, unlock gates and transfer behavior are retained. This changes how portraits are sampled; it does not generate replacement characters or add new unlocks. The desktop executable is 35,249,152 bytes (about 33.6 MiB), larger than 1.2.7 because it contains four times as many pixels per portrait frame. PNG compression preserves all visible pixels. The bundled Android 1.2.6/code14 APK remains byte-identical at 4,538,498 bytes.

## Validation

**74 Windows checks pass:** 18 core, 23 loopback transfer/resume, 10 existing UI, eight signature UI/save/restart, nine studio and six portrait-sharpness cases. New checks compare source detail at native and larger bounds, inspect uniform Pixel blocks, verify integer breathing, exercise cache eviction, check both buttons at default/minimum sizes and restart both saved preferences without granting XP or equipping a locked preview. GDI alpha rounding is allowed in source comparisons; the original art contains partially transparent pixels.

Actual QR images decode at the normal and minimum window sizes, with expanded network options and long Unicode names. Tests use isolated profiles. Android runtime files, build configuration, APK and Windows transfer core are checked against the prior baseline. Physical radio tests, high-DPI hardware and the owner's deferred focused-window idle review were not repeated.

This is a local Windows update. GitHub releases, public packages and website downloads have not been changed.
