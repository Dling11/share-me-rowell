# Fixed signature character artwork, 1.2.4

The three anime-inspired roles use finished character cels with their own head shapes, hair, face, body, clothes and colours. The normal RPG avatar retains its modular editable renderer. No generic head, hair, palette, expression or Boy/Girl variation is composited onto a signature character.

Generated using the built-in image_gen tool, not the CLI. The unchanged project-bound source is [pixel-special-source.png](pixel-special-source.png), a transparent 1024x1536 sheet. The compilePixelSpecial build task samples it at game resolution into a 256x384 hard-alpha PNG with four pose columns and six costume rows. Each cel's boots are aligned to logical baseline61. Windows embeds the same compiled PNG. Runtime idle draws the current upper cel with the neutral cel's planted boots; animation-off, background and transfer guards remain.

Rows are Gojo-inspired basic/mastery, Saitama-inspired basic/mastery and Naruto-inspired basic/mastery. The four columns are neutral, blink/breath, left weight/head shift and right weight/head shift. Gojo's blindfold remains part of his design. The roles keep indices8..10 and the same 40/65/85 first unlocks and 80/90/100 mastery levels.

## Exact generation prompt

```text
Use case: stylized-concept. Asset type: transparent COMPLETE fixed-character sprite sheet, for a lightweight chibi pixel RPG avatar app.
The provided lineup is the style reference and shows the OLD modular shared heads. Create BETTER, dedicated, recognizable character-specific full-body designs. Do NOT reuse that generic head on all heroes. Each hero must have its own distinctive face/head silhouette, hair, proportions, clothes and naturally integrated short neck/collar. No interchangeable head/hair pieces: every cell is ONE finished complete character.
EXACT CANVAS: portrait 2:3 aspect ratio, FOUR equal columns and SIX equal rows (24 full sprites). No labels, text, grid lines, platforms or background. Genuine transparent background. Each cell corresponds to 64x64 logical pixels. Tiny compact pixel RPG sprites, crisp clusters, limited palette, no antialiasing, no painterly blur. Dark outlines. Every cell is centered at x32, sole baseline y61, hair/head top around y2..5, all artwork fits within its cell with transparent gutters. The same character keeps exactly the same scale and registration across its four poses. Feet stay planted.
ROWS:
Row1 = GOJO-inspired academy look, full character: very spiky silver-white hair, distinctive slim angular face with black wraparound blindfold, pale skin, fitted navy high-collar uniform, dark boots, calm cool personality. Integrated collar, no long exposed neck. No magic aura.
Row2 = SAME GOJO-inspired teacher mastery look: same exact personal head identity/hair/blindfold, stylish longer dark navy coat, one hand in a relaxed two-finger pose and a restrained tiny electric-blue orb/effects at the side. His angular head must look distinct from the other two heroes.
Row3 = SAITAMA-inspired hero, full character: a genuinely bald smooth OVAL head with a broad rounded chin (not the other heroes' hairless head), tiny relaxed half-lidded deadpan eyes, small neutral mouth, light warm skin, yellow jumpsuit, short white shoulder cape, red gloves and boots, plain dark belt with round buckle. Charming understated comedy, not angry.
Row4 = SAME SAITAMA-inspired mastery look: exact same unique bald oval head/deadpan identity, more dramatic flowing white cape, confident compact clenched red gloves, yellow suit/red boots, stronger ready stance. Still a small chibi hero, no huge muscles.
Row5 = NARUTO-inspired ninja, full character: distinctive rounder youthful face with cheek whisker marks, bright blue eyes, short spiky BLOND hair different in shape from Gojo's, dark forehead band with plain silver plate, orange-and-charcoal jacket, dark trousers, wrapped ankles, dark ninja shoes, little utility pouch. Energetic determined expression. No text or official emblems.
Row6 = SAME NARUTO-inspired sage mastery look: exact same blond head/face identity and headband, dramatic orange/gold sage cloak over dark ninja clothing, small restrained amber chakra accents at the sides, energetic brave demeanor. No official emblems or text.
FOUR COLUMNS in EVERY row are carefully authored idle cels, not copies shifted wholesale:
Column1 = neutral idle facing mostly forward, both feet planted.
Column2 = a subtle breathing/blink cel, chest/shoulders slightly change and face briefly blinks; Gojo's blindfold stays on, so his hand/coat changes slightly instead of exposed eyes.
Column3 = a tiny natural weight shift and head/eyes looking slightly toward viewer's left, cloth/cape follows; feet stay exactly on baseline.
Column4 = the complementary tiny weight shift and head/eyes slightly toward viewer's right, cloth/cape follows; feet stay exactly on baseline.
Keep the same 3 distinct identities through all 24 cells. This is fixed character artwork: no girl versions, no palette variations, no generic doll faces, no detached heads, no floating hair. Original generic RPG characters remain elsewhere in the app. These special anime-inspired heroes need coherent unique silhouettes and neck/head/body anatomy, readable at 64px.
```


Source SHA-256:
034239a9c3715c03694cdbf0038af3bce5dff439cc5eb530d7089597a0665a63
Compiled PNG SHA-256:
39b7e75db7b4423d10ce0da28059e57522bd1160e4a503566a2ccf62447267f5

The owner approved this art direction on November 13, 2026. That does not establish physical-device animation approval.
