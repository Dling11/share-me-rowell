# Card Duelist source — 1.2.5

Generated with the built-in image tool in edit/reference mode, using `pixel-special-source.png` as the pixel-style reference. Output was generated with transparency enabled. Original generation: `C:/Users/Administrator/.codex/generated_images/01a18536-a61e-7c62-b779-ab382dad7cbc/exec-6124080b-1451-40c8-a167-f92fd11e6288.png`; preserved as `pixel-yugi-source.png` beside this document.

The deterministic `compilePixelYugi` Gradle task samples this 4 × 4 source to a 256 × 256 atlas, thresholds alpha, and registers boot baselines. The same compiled bytes are embedded by Android and Windows. Cells 0–7 are Card Duelist (level 50); cells 8–15 are Pharaoh Duelist (level 95). These are complete fixed designs, independent of personal appearance settings.

Cel order per costume: idle, blink, left turn, right turn, card presentation, pendant glint, cheer, relaxed smirk. The renderer holds neutral boot pixels at the ground while selecting the authored upper-body cel.

The generated source remains unchanged: 1,752,991 bytes, SHA-256 `cbe212d01a94e00217a6c078995bc8d0867eec3cb44a3aee20a22b802ca3d9ca`. The compiled atlas is 79,221 bytes, SHA-256 `9b202752eb00da9117eb3148b72405730edf5b710892c1397593df2c78fc0058`, matching Android and Windows. Runtime animation uses selected authored cels plus one-pixel breathing and the existing frame/shadow renderer; the generated raster is not a video.

## Exact generation prompt

```text
Use case: stylized-concept. Asset type: transparent complete-character pixel sprite atlas for a fixed, collectible Yugi-inspired avatar in Share Mode.
Use the supplied full-character sheet only as the STYLE, PIXEL DENSITY, SCALE and FOOT REGISTRATION reference. Do not alter or include those existing characters. Generate one NEW square 4x4 atlas containing ONLY the Yugi-inspired card duelist. No text, labels, grid borders, logos, background, platforms or cast shadows. Genuine transparency.
Each of the16 equal cells represents64x64 logical pixels. A complete, coherent chibi character with his OWN distinctive head, face, iconic tall black spiky hair with magenta edges and blond lightning-shaped forelocks, large expressive violet eyes, pale warm skin, short naturally connected neck, navy school jacket over a dark shirt, leather belts and wristbands, dark fitted trousers, sturdy little boots and a small gold inverted-pyramid pendant. Cute, confident pixel-RPG look, readable at64px, hard pixel clusters and crisp outlines. No generic bald doll head; no interchangeable hair. All head, body, clothes and colours are one finished fixed design.
Every sprite fits its cell, centered aroundx32. Hair top aroundy2, boot soles always baseliney61. Consistent scale and anatomy through all cels. No huge card or hair obscuring the face. Keep foot positions stable; change posture, expression, hands and fabric internally rather than shifting the whole sprite.
TOP TWO ROWS (cells1..8 reading order): the ordinary Card Duelist, modest navy school jacket, one small fan of plain patterned cards; restrained pendant, no magic aura. This is a complete signature character but visibly less elaborate than the mastery version.
BOTTOM TWO ROWS (cells9..16 reading order): the SAME personal head/hair/face identity as the Pharaoh Duelist mastery outfit, flowing short navy/purple shoulder cape with tasteful gold trim, confidently presented plain card fan, slightly more elaborate gold pendant and a few small amber pendant sparks at the side. No giant glowing background or wings. Same body scale as the base.
For EACH eight-cell costume block, exact pose sequence:
1 neutral forward-facing idle, calm confident smile, cards held low;
2 gentle breathing and brief closed-eye blink, exact same head silhouette;
3 small weight/head turn toward viewer's left, cloth/hair follows, feet planted;
4 complementary turn toward viewer's right;
5 raises and presents the little card fan, playful focused eyes;
6 subtle pendant inspection/glint, one hand near the gold pyramid, a soft smile;
7 a happy cheer pose with card hand lifted and shoulders raised, still entirely in cell and boot baseline61;
8 returns to relaxed idle with a confident little smirk and lower card hand.
Cards have simple invented back patterns only, no copied card illustrations or text. Preserve a proper connected neck/chin/collar in every pose. Limited palette, no antialiasing, no soft painted texture or gradients. Precisely16 separate sprites on a fully transparent square sheet.
```
