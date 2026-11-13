# One-character animation sheets — 1.2.6

Cleanup note (November 13, 2026): only selected current production sources remain in the project. Unused drafts, superseded Zoro 1.2.12 sheets and historical review images were deleted at the owner's request. Generation names and prompts below remain as provenance, including references to retired files.

The owner requested one character per generation, following the approved Yugi art, with more frames. Each of twelve separate 4x6 portrait sources contains exactly one complete fixed character: twelve starter cels and twelve mastery cels. No generic head or personal palette is applied. Sources remain unchanged after selection, outside APK resources; the build compiles them deterministically to 256x384 hard-alpha atlases shared by Android and Windows.

Generated with the **built-in image tool** using `pixel-yugi-source.png` as the style/anatomy reference, transparency enabled. Generation is one tool call per character, with targeted correction calls for Luffy's breathing eyes and Levi's background artifact. Earlier versions are retained as `pixel-luffy-source-v1.png` and `pixel-levi-source-v1.png`. No CLI fallback was used.

The runtime plays eleven authored idle cels with timed holds and returns, plus a separate twelfth tap reaction, over the existing 9.6-second loop. It holds neutral foot pixels at the ground and uses no extra generic body bob for these new sheets. Madara overrides the cheerful tap reaction with a stern commanding power stance at the owner's request.

| Character | First unlock | Mastery | Selected source |
| --- | ---: | ---: | --- |
| Luffy-inspired | 20 | 80 | [luffy](pixel-luffy-source.png) |
| Zoro-inspired | 28 | 82 | [zoro](pixel-zoro-source.png) |
| Boa Hancock-inspired | 32 | 84 | [boa](pixel-boa-source.png) |
| Shanks-inspired | 48 | 90 | [shanks](pixel-shanks-source.png) |
| Eren-inspired | 36 | 86 | [eren](pixel-eren-source.png) |
| Levi-inspired | 58 | 92 | [levi](pixel-levi-source.png) |
| Mikasa-inspired | 46 | 88 | [mikasa](pixel-mikasa-source.png) |
| Sukuna-inspired | 88 | 99 | [sukuna](pixel-sukuna-source.png) |
| Itachi-inspired | 68 | 94 | [itachi](pixel-itachi-source.png) |
| Rock Lee-inspired | 24 | 76 | [rock-lee](pixel-rock-lee-source.png) |
| Might Guy-inspired | 78 | 98 | [might-guy](pixel-might-guy-source.png) |
| Madara-inspired | 96 | 100 | [madara](pixel-madara-source.png) |

## Exact generation prompts

Each prompt is the shared specification followed by `CHARACTER DESIGN:` and the corresponding character text below. Luffy's initial generation used the shared specification before the final eye-continuity sentence was added, then received the targeted eye correction recorded afterward. All other initial generations use the complete shared specification.

### Shared specification

```text
Use case: stylized-concept. Asset type: transparent complete-character pixel sprite atlas for Share Mode.
REFERENCE: the supplied Yugi sheet establishes ONLY the pixel-RPG art quality, chibi scale, full-character anatomy and clean short connected neck. Generate ONLY the ONE new character described below, never Yugi or any other character.
LAYOUT: portrait 2:3 aspect, EXACTLY 4 columns x 6 rows, 24 equal square cells, no grid lines. Each cell is a full finished character at 64x64 logical game pixels. TOP THREE ROWS = 12 sequential cels of the basic outfit. BOTTOM THREE ROWS = the same 12-cel sequence in the earned mastery outfit. One character identity, two complete coherent outfits, twelve animation frames for EACH.
STYLE: crisp appealing chibi pixel-RPG character, distinctive own face/head/hair, expressive eyes and carefully connected short neck/chin/collar; not a generic doll head. Match the approved reference's detail and cute badass mood. Strong readable silhouette, limited palette, no painterly shading or blur. Pixel clusters, not vector outlines. Full head and feet inside every cell; center x32, top near y3, planted foot/sandal soles at y61. Identical head/body scale, costume construction and foot registration across a costume's twelve cels.
Make a COHERENT stepped idle sequence, with intentional in-between poses rather than 12 random portraits. Read each block LEFT TO RIGHT then TOP TO BOTTOM:
0 relaxed front idle;
1 inhale: chest/shoulders rise slightly, hands relax;
2 exhale: chest settles, hair/cloth tips trail;
3 a brief blink in the neutral pose;
4 begins small leftward head/shoulder glance;
5 gentle leftward glance held, cloth follows;
6 passes back through center;
7 small rightward glance with corresponding hair/cloth motion;
8 winds up the character-specific gesture described below;
9 completes the gesture, eyes expressive;
10 gesture settles back toward neutral, cloth/prop follows;
11 proud cheerful reaction, lifted hand/shoulders, feet still at baseline (tap-to-cheer).
Design frames0..10 as a readable loop, with small anatomy-consistent movements, no abrupt different face. Frame11 is the tap reaction. Do not crop hair or hands. Feet planted throughout. Keep props small and safely inside the cell.
BACKGROUND: genuinely TRANSPARENT alpha, no scenic backdrop, no opaque colour, no gradients/glows behind sprite, no cast shadow, no floor. No text, labels, borders or watermarks. Small themed spark pixels beside a hand are allowed. Every sprite is one fixed complete design; do not create separate head/body parts.
Eye continuity: frames0,1,2 and4..10 have OPEN eyes, including the breathing in-betweens. Only frame3 is the closed-eye blink; frame11 may smile with closed eyes. Frame1 must not duplicate the blink. Carefully follow the numeric pose ordering.
```

### Luffy-inspired

```text
CHARACTER DESIGN:
Monkey D. Luffy, adult cheerful straw-hat pirate: messy black hair, straw hat with red band, warm dark eyes, tiny scar under left eye, own wide friendly smile. BASIC: simple red sleeveless vest, yellow waist sash, blue cuffed shorts and sandals, no aura. MASTERY: Gear-5-inspired white cloudlike curly hair and eyebrows, white open jacket and white shorts, purple waist sash, sandals, straw hat on his back, tiny white cloud curls kept close to shoulders; bright playful eyes, same face identity, no huge aura. GESTURE frames8..10: basic reaches toward straw-hat brim, tips it with a grin, then lowers hand; mastery raises a little clenched fist with a playful grin and two tiny cloud curls, then relaxes. Cheer frame11: happy lifted fist, broad smile.
```

### Zoro-inspired

```text
CHARACTER DESIGN:
Adult Roronoa Zoro, focused swordsman with short spiky green hair, stern eyes with one closed scarred eye, three tiny gold earrings on one ear. BASIC: white short-sleeve V-neck shirt, green haramaki waist wrap, dark trousers and boots, three small sheathed swords neatly at waist. MASTERY: dark green long robe, red waist sash, black bandana tied around hair, same short green hair/face identity, sword hilts and a very restrained green glint. GESTURE8..10: hand moves to a sheathed sword hilt, small focused hilt salute/glint without attacking, then relaxes. Cheer11: confident fist/salute.
```

### Boa Hancock-inspired

```text
CHARACTER DESIGN:
Adult Boa Hancock, elegant confident woman with long sleek black hair, distinct feminine chibi face and blue-gray eyes, tiny gold snake earrings. BASIC: tasteful fully covering long red high-collar dress with restrained gold snake motif, modest side fold and dark shoes. MASTERY: fully covering deep-purple royal dress with gold trim, short pale shoulder mantle and small gold snake hair ornament, same sleek hair/head and face. GESTURE8..10: brushes a lock of hair, extends a graceful palm with one tiny pink heart sparkle beside it, then settles. Cheer11: regal cheerful wave. No revealing or sexualized clothing or pose.
```

### Shanks-inspired

```text
CHARACTER DESIGN:
Adult Shanks: tousled short crimson-red hair, three fine scars over the left eye, subtle reddish stubble, calm charismatic adult chibi face. Respectfully has ONLY HIS RIGHT ARM; left sleeve rests empty beneath the cape, never draw a left hand. BASIC: open white shirt, dark cropped trousers with restrained pattern, sandals, a small sheathed sword at right hip, simple dark shoulder cape. MASTERY: dramatic but cell-sized black captain cape with red lining, white shirt and richer dark trousers, golden sword hilt, tiny red spark near right hand. GESTURE8..10: right hand rests on sword hilt, offers a calm open-palm greeting, returns. Cheer11: relaxed raised right-hand wave.
```

### Eren-inspired

```text
CHARACTER DESIGN:
Adult human Eren Yeager, serious green-gray eyes and distinct brown hair/face. BASIC: short tousled brown hair, tan cropped scout jacket over a light shirt, neat brown harness straps, dark trousers and boots, no oversized equipment. MASTERY: longer dark brown hair tied in a short low bun with loose facial strands, dark long coat over charcoal shirt, dark trousers and boots, same recognizable face, no Titan/monster/gore. GESTURE8..10: looks at a tiny key pendant in hand, closes fist with resolve near chest, relaxes. Cheer11: determined raised fist with restrained smile.
```

### Levi-inspired

```text
CHARACTER DESIGN:
Adult Levi Ackerman, short straight black undercut with long parted fringe, narrow cool gray eyes, own sharp serious chibi face. BASIC: tan cropped scout jacket, white cravat, light shirt, tidy leather harnesses, dark trousers and tall boots, small sheathed twin blades. MASTERY: deep green short scout cloak with original simple winglike badge (not a copied logo), darker refined harness uniform, two compact downward-pointing blades kept within cell, same face/hair. GESTURE8..10: adjusts white cravat, flicks a blade hilt with a tiny steel glint, then settles; no attack. Cheer11: subtle proud salute, still serious.
```

### Mikasa-inspired

```text
CHARACTER DESIGN:
Adult Mikasa Ackerman, short straight black bob, thoughtful dark gray eyes, own strong feminine chibi face, recognizable red scarf in every cel. BASIC: tan cropped scout jacket, light shirt, neat harness straps, dark trousers and brown boots, small sheathed blades. MASTERY: dark charcoal fitted scout coat and harness, flowing but compact red scarf, small silver blade hilts and muted green shoulder cape, same bob and face. GESTURE8..10: gently adjusts scarf, hand settles near a sheathed blade with small steel glint, relaxes. Cheer11: calm warm smile with a small lifted fist.
```

### Sukuna-inspired

```text
CHARACTER DESIGN:
Adult two-armed human-form Ryomen Sukuna, short swept spiky pink hair, sharply confident own face, small distinctive black tattoo markings across forehead, cheeks and hands, red eyes. BASIC: dark high-collar navy/black uniform with red collar trim, dark trousers and boots, no large aura. MASTERY: elegant white robe with dark lapels and a dark violet waist sash, same pink hair and black markings, small restrained red ember pixels near one hand. Keep two arms and coherent anatomy, no gore. GESTURE8..10: raises two fingers in a confident gesture, tiny crimson spark, lowers hand with a smirk. Cheer11: proud amused raised hand.
```

### Itachi-inspired

```text
CHARACTER DESIGN:
Adult Itachi Uchiha, long straight dark hair framing his face and tied low behind, distinctive calm melancholy face with fine under-eye lines, red eyes, dark headband with simple abstract metal mark. BASIC: high-collar black cloak with small red cloud motifs, dark trousers and sandals, quiet silhouette. MASTERY: same iconic identity with cloak slightly open to show a dark fitted shirt and necklace, stronger crimson lining, a couple tiny black crow-feather pixels beside the shoulder but no giant bird/aura. GESTURE8..10: raises two fingers near chest, a subtle red eye/feather glint, lowers hand. Cheer11: quiet knowing smile and small salute.
```

### Rock Lee-inspired

```text
CHARACTER DESIGN:
Adult Rock Lee, glossy black bowl-cut hair, thick expressive eyebrows, round determined dark eyes, own endearing chibi face. BASIC: simple green fitted training jumpsuit, orange leg warmers, blue sandals, white hand bandages and a small red waist belt. MASTERY: polished darker green training suit with refined pale wrist/forearm bandages, open small training vest and gold ankle weights, tiny green energy glint near a fist; same bowl cut and eyebrows. GESTURE8..10: winds up a neat thumbs-up, holds it with a bright grin, settles; feet never kick. Cheer11: enthusiastic clenched fist.
```

### Might Guy-inspired

```text
CHARACTER DESIGN:
Adult Might Guy, glossy black bowl-cut hair, thick eyebrows, older confident own chibi face with smile lines, broad energetic smile. BASIC: green jumpsuit, dark green flak training vest, orange leg warmers, blue sandals, white hand wraps. Clearly distinguish from Rock Lee with older face, broader shoulders and vest. MASTERY: Night-Guy-inspired deep crimson/dark red suit with compact orange-red hair tips and tiny warm ember strokes around shoulders, clear same adult face and strong eyebrows; no huge aura, no injury or gore. GESTURE8..10: thumbs-up rises, brilliant proud smile and one tiny glint, settles. Cheer11: energetic lifted fist.
```

### Madara-inspired

```text
CHARACTER DESIGN:
Adult Madara Uchiha: make him visibly MORE INTIMIDATING and mature than the cute friendly characters. Own angular stern chibi face with smaller narrow crimson eyes, strong shadowed brow, no round baby eyes, no happy grin. Long wild layered black hair partly frames one eye. Broad imposing shoulder armour, upright commanding stance, restrained serious expression in EVERY cel. BASIC: heavy deep-crimson segmented samurai shoulder/chest armour over dark blue-black clothes, dark trousers and sandals; compact dark war fan at back, strong imposing silhouette, subtle controlled dark-red glint near hand. No huge aura obscuring body. MASTERY (Level100): SAME mature head/face structure, long pale-white hair, cold lavender concentric-ring eyes, white high-collar Six-Paths-inspired robes with restrained black markings, black sash, compact dark staff, at most two small dark orbs near shoulders. More powerful but still inside64px cell. GESTURE8..10: slowly raises one commanding hand, dark crimson or violet spark gathers, controlled return to neutral. IMPORTANT OVERRIDE of generic cheerful reaction: frame11 is a frighteningly calm power stance/raised palm with cold eyes and a very subtle confident smirk, NEVER a happy open-mouth smile, laughing face or cute cheering fist. Keep properly attached head/neck, planted feet and consistent face across all24cels. Intimidating anime final-boss mood in the approved crisp chibi pixel-RPG art style, no gore or injury.
```

### Targeted Luffy correction

```text
Use case: precise-object-edit. Edit this exact transparent 4-column by6-row Luffy sprite sheet. Preserve the entire sheet, every cell position, proportions, pixel art, colours, costumes and genuine alpha background. Change ONLY the EYES of the breathing-in frames: ROW1 COLUMN2 and ROW4 COLUMN2 (one-based positions). Both currently incorrectly show closed eyes. Open the eyes in these two cells to match their neighbouring neutral open-eyed Luffy of the same costume; keep the subtle inhale shoulder/hand position. The blink cells ROW1COLUMN4 and ROW4COLUMN4 must stay closed. Keep all other cels, hats, hair, faces, expressions, chest/neck connections and gestures unchanged. No new background. Do not crop, add frames or alter the4x6 grid. Exact same character art with just the two inhale eyes corrected.
```

### Targeted Levi correction

```text
Use case: precise-object-edit / background-extraction. Correct ONE artifact on this exact transparent pixel sprite sheet, preserving all24 Levi cels,4-column by6-row layout, both costumes, pixel style, body/head/feet registration, poses, faces and genuine alpha. At ROW3 COLUMN3 (one-based), to the RIGHT of the character's hand/hip, there is an unwanted bright WHITE RECTANGULAR BLOCK plus a faint light haze. Remove only this white rectangle and surrounding haze and make that little BACKGROUND area fully transparent to match the rest of the sheet. Do not remove or alter his hand, blade or clothes. No other new pixels, shapes, labels, effects or background. Keep every other sprite unchanged, same image size and grid.
```

Original generation locations, selected source SHA-256 hashes and matching compiled atlas hashes are recorded in the local verification report. No key, private device information or pairing code is included in these art assets.
