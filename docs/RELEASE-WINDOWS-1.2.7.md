# Share Mode for Windows 1.2.7 — Avatar studio redesign

The PC preview used a long single-column character list, a cramped portrait and appearance dropdowns. Its older modular fantasy art and pixel frames also differed from the complete anime character artwork. This local Windows update replaces that interface with a searchable collection, a dedicated portrait and direct visual appearance controls.

- **Characters:** a three-column collection with search and filters. Featured starts with your customizable avatar and complete signature characters. Select a card to preview; use the single Equip action when it is unlocked. Starter and Mastery previews never grant progress.
- **Style:** Boy/Girl looks, six hairstyle previews, hair/skin/outfit/accent swatches and four expressions. Names and available choices save immediately. Complete default-character cels keep the head, neck and body together while runtime colour masks preserve the authored shading.
- **Preview:** larger complete portraits, correct character/outfit labels and clear unlock information. Desktop art uses 128-pixel cells with preserved alpha. The old procedural pixel frame and head/body cuts are absent from this preview.
- **Journey:** the former Ranks page now focuses on character unlocks and mastery outfits. Earned XP, content receipts, IDs, unlock levels and the difficult level curve remain intact. This changes the presentation of leveling; it does not reset or rebalance progress.
- **Fantasy characters:** Heavenly Seraph and Astral Sovereign have newly generated complete starter/mastery artwork in the same clean chibi direction. They equip as fixed designs on PC, preserving your personal appearance for switching back. Their existing unlock/mastery levels remain 75/85 and 95/100.
- **Desktop polish:** custom dark scrollbars support wheel, thumb dragging, track clicks and keyboard navigation. Search and name inputs, spacing, a slim XP bar and footer sizing are updated. Send/Receive are more compact and their QR codes render at whole module sizes; network selection stays under Advanced.

The featured collection has your personal avatar and 18 fixed characters. Earlier customizable RPG styles remain available through All characters or Customizable. Free appearance choices, sharing speed and file features remain available independently of level.

## Validation and compatibility

The final candidate passes **68 Windows checks**: 18 core/render/persistence, 23 loopback transfers, 10 existing UI, eight fixed-character UI/save/restart and nine studio checks. Rendered QR decoding also passes on the main receive screen and at both default/minimum window sizes, including expanded network controls and maximum-length Unicode device names. Tests use isolated profiles and labelled fixture progress, never real XP or receipts.

The **Android 1.2.6 APK is unchanged**, as are its 37 runtime source files, build configuration and the Windows transfer core. TLS identity, pairing format, file approvals, save paths, resume and verified-file rewards are retained. No Android rebuild or physical-phone performance test was needed for this Windows UI change; no phone installation is included. Physical radio behavior and the owner's deferred live focused-window animation review are separate from these checks.

All art is bundled locally; no engine, account, network artwork download or extra app runtime was introduced. The high-resolution original sources stay outside runtime resources. The optional authoring utility is `scripts/CompilePortraitArt.cs`; normal Windows builds use the precompiled portrait PNGs.

This is a local PC candidate. GitHub releases, public ZIPs and website downloads are unchanged; the existing public packaging guard remains in place.
