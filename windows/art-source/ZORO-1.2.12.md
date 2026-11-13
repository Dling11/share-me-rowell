# Zoro artwork — Windows 1.2.12

Cleanup note (November 13, 2026): only selected current production sources remain in the project. Unused drafts, superseded Zoro 1.2.12 sheets and historical review images were deleted at the owner's request. Generation names and prompts below remain as provenance, including references to retired files.

Built-in image generation supplied 96 authored cels: three 24-cel full-body banks and a 24-cel slash bank. The move keeps a fixed full-body view with no eye/head cut-in. Its open eye glows green while the scarred left eye stays closed. The first costume remains the white shirt, green waist wrap and black trousers; the selected idle/mastery outfit is restored after playback.

| Production bank | Selected source | Original generated output |
| --- | --- | --- |
| Ready | zoro-ready-source-1.2.12-v2.png | exec-9539be6e-795b-475d-a68a-0ca5b78dc007.png |
| Combo | zoro-combo-source-1.2.12-v2.png | exec-3ba9512d-b4b0-418b-abba-a6fc97c4d104.png |
| Finisher/recovery | zoro-finish-source-1.2.12-v3.png | exec-23b7c09a-cf25-4806-8e74-843398b910ab.png |
| Slash effects | zoro-slashes-source-1.2.12.png | exec-7ef6f00e-2911-4363-885b-80123e2fecf6.png |

Four initial generations were followed by three targeted blade-tip refinements and one final border cleanup. Earlier source versions remain in this authoring folder; they are never embedded or copied to the installed app. Original generated files are preserved byte-for-byte. Only the four selected compiled atlases enter the production allowlist. CompileZoroArt.cs names those selections explicitly.

Validation rejects visible blade bands or dense artwork crossing cell borders. A single nearly black boundary sample is tolerated as raster antialiasing; body poses remain complete. The actual renderer was inspected at the eye flash, crosscuts, finisher and sheathing phases. Native effects use a reusable small buffer to briefly offset halves of the preview; they never move or obscure other app controls. No camera zoom or face atlas is used.

## Exact prompt set

### Ready

```text
Use case: stylized-concept. Asset type: production transparent full-body Zoro attack-animation atlas, first of three linked body phases.
Reference image is a character/style reference only, NOT an edit target. Generate a NEW single-character sheet matching the first costume of that reference: adult Roronoa Zoro, short spiky green hair, scarred LEFT eye closed, RIGHT eye open, three gold earrings, WHITE short-sleeve V-neck shirt, broad green haramaki waist wrap, black trousers and black boots. No bandana, green robe, jacket, armour or costume change.
EXACT4 columns by6 rows =24 equal square256px cels on1024x1536 canvas. Genuine transparent background, no painted backdrop, tile edges, grid lines, labels, numbers or text. Crisp detailed chibi pixel-RPG illustration, mature stern angular face, clean strong outlines. Same whole head/neck/body anatomy, costume and scale throughout.
This sheet is a COILING WIND-UP WITH GREEN EYE AWAKENING, not idle variants. Three-sword style: EXACTLY THREE katanas in every cel, ONE gripped HORIZONTALLY by the mouth with handle between teeth, ONE held in each hand. No extra arms, fingers, blades or floating sword pieces. Sword blades must be straight, distinct and correctly attached to hilts; the mouth sword stays anatomically aligned with the jaw. Entire character INCLUDING every sword tip fits inside its own square cell with10px margins. Make the character about65percent of cell height to leave generous room for swords, with both boots visible near the same ground line.
Read left-to-right, top-to-bottom,24 genuinely progressive useful in-between drawings:
0-3: low three-sword ready stance, right open eye begins glowing vivid emerald green, left scarred eye stays firmly closed;
4-7: knees compress, torso leans forward, hand-held blades pull back;
8-11: shifts weight onto rear foot, torso rotates into a deep coil, shoulders/hair follow;
12-15: both arms wind up into a powerful crossing guard, intense emerald glint at the open right eye;
16-19: torso starts uncoiling, leading sword begins a wide sweeping arc, boots push off;
20-23: strong first forward slash initiation with clear successive shoulder/elbow/wrist positions, ending just before the wide sweeping strike.
Exaggerated martial-arts anticipation, visibly different poses and coherent weight shifts, never a static PNG merely rotated. No green aura hiding the face/body, only a tiny sharp local emerald eye glint; large sword trails will be a separate asset. No head zooms, cropped body, detached neck, gore or injury. Keep the complete connected character, all3 sword tips and boots inside EVERY cell.
```

### Combo

```text
Use case: stylized-concept. Asset type: production transparent Zoro full-body attack atlas, phase2 COMBO.
Reference1: original Zoro character/style. Reference2: newly approved first wind-up atlas; preserve its EXACT character size, WHITE shirt, broad green waist wrap, black pants/boots, spiky green hair, three gold earrings, closed scarred left eye, glowing GREEN open right eye, strong clean outlines and complete connected anatomy. These are REFERENCES, not edit targets. Create a NEW sheet continuing the LAST pose of Reference2.
EXACT4 columns x6 rows =24 uniform square256x256 cells on1024x1536 canvas. Genuine alpha backdrop, no grid/text/labels/painted background. Same detailed clean chibi pixel-RPG art, mature stern expression. No camera zoom, no head closeup.
The same Zoro performs a FAST EXAGGERATED THREE-SWORD COMBO. Exactly3katanas: one held horizontally between teeth and one in each hand. Never add extra arms, swords or floating blade fragments. Both hands stay connected to the wrists and swords stay connected to hilts. Every sword tip, green eye, hair and boot must fit inside its cell with10px margins. Character about65percent cell height, same scale and ground as Reference2.
Read left-to-right/top-to-bottom with24 DIFFERENT successive in-betweens:
0-3 continue the last wind-up: drive the first hand-held blade through a huge forward horizontal sweep, torso and hips rotate, knees push off;
4-7 follow through low across the body, both shoulders rotate naturally, mouth sword follows the head;
8-11 reverses into a rising diagonal counter-slash, rear blade arcs upward with distinct elbow/wrist positions;
12-15 a sharp crossing double-cut, arms open from crossed guard, both boots pivot;
16-19 a compact full-body twisting lunge, torso turns into three-quarter profile and begins turning back, no disappearing swords or duplicated limbs;
20-23 brakes into a deep crossed-sword stance ready for a final strike, ends with knees compressed and blades drawn back diagonally.
Actual martial-arts movement with strong readable silhouettes, not a static picture tilted sideways. Keep all24 full-body poses registered, no motion blur obscuring anatomy, no large sword trails covering him (trails will be separate). The ONLY subtle glow is the open RIGHT eye, vivid emerald green; scarred left eye CLOSED in all poses. No bandana or costume change. No gore or injuries.
```

### Finisher and recovery

```text
Use case: stylized-concept. Asset type: transparent Zoro full-body animation atlas, final phase3 FINISHER AND RECOVERY.
Reference1 original Zoro defines the final neutral idle outfit and anatomy. Reference2 approved combo atlas defines exact active character style and LAST crossed-sword pose to continue. Generate a NEW sheet, don't edit the references.
EXACT4columns x6rows =24 square256px cels on1024x1536 transparent canvas, no labels/grid/text. Same clean detailed chibi pixel-RPG illustration, white short-sleeve shirt, wide green haramaki, black trousers/boots, green hair, three gold earrings, scarred left eye closed, open RIGHT eye glowing emerald. No bandana, robe or costume change. Same registered scale and ground as reference2, character about65percent cell height with10px margins around ALL sword tips and boots. Full connected head, neck and body in every cel.
24 distinct sequential drawings left-to-right/top-to-bottom:
0-3: from the last crossed-sword crouch in Reference2, drives upward into an exaggerated final strike, arms rise, hips and shoulders uncoil;
4-7: powerful downward crossing double slash, complete sword trajectories with separate in-betweens, mouth katana follows his stern face, boots land in a broad stance;
8-11: momentum follows through, blades sweep outward low and then slow, shoulders/hips recover together;
12-15: torso rises upright, wrists turn the blades down safely, green open-eye glint settles;
16-19: progressively SHEATHES the TWO hand-held swords into their hip scabbards, one then the other, hands become empty as each blade is stowed; mouth katana remains held correctly until the next step;
20-22: one empty hand takes the mouth katana BY ITS HILT and lowers it into the third scabbard; mouth returns to normal, no duplicated sword or hand;
23: matches the neutral standing first-costume Zoro in Reference1, both hands empty at sides, EXACTLY THREE sheathed katana hilts at hip, calm stern face, closed scarred left eye and open right eye.
Exactly3 swords total whether drawn or sheathed, never a drawn sword plus a duplicate of it in a scabbard. Correct grips/hilts, no extra limbs/floating pieces/cropped blades, no large glows or trails hiding anatomy. This is fluid martial-arts follow-through and readable recovery, not24 repeated poses or camera zooms. No blood or injuries. Transparent gutters and complete sword tips.
```

### Slash effects

```text
Use case: stylized-concept. Asset type: transparent 24-frame emerald sword-slash VFX atlas for Zoro's chibi pixel-RPG skill.
Reference is STYLE AND GREEN PALETTE reference only. NO Zoro, human, body, face, weapon or background in this sheet. ONLY energy trails and tiny fragments.
EXACT4 columns by6 rows of uniform square256x256 cels,1024x1536 total. Real transparent background including between ribbons; no opaque fog rectangles, checkerboard, labels, text, numbers or grid lines. All effects fit within8px cell margins.
Crisp high-energy EMERALD GREEN sword-cut effects, sharp luminous pale-green leading edges and deep-green tapered tails, a few pixel-like sparks. Clean detailed pixel-RPG finish; no large blurry cloud or solid opaque splash. Center stays mostly open so a character can be seen beneath the trails.
24 useful sequential drawings, read left-to-right/top-to-bottom:
0-3 a thin green horizontal cut grows from left to right, with a curved tapered tail;
4-7 a huge diagonal crescent sweeps from upper-left to lower-right, progressing clearly through the arc;
8-11 an opposite diagonal crescent crosses the first into a strong luminous X, with clearly different intersections;
12-15 three successive broad green crescents form a compact spinning three-sword vortex, edges remain thin and sharp, center open;
16-19 an exaggerated final green cross-cut bursts outward with small angular emerald shards and short streaks;
20-23 the cross-cut breaks into smaller trailing ribbons and fragments which rapidly fade, ending nearly transparent.
Animation, not24 unrelated symbols. Consistent emerald palette and center anchors; no giant runes, dragons, extra creatures, purple/red effects or full-screen white flashes. Never fill whole square with glow. One effect composition per cel, all24 square cells aligned.
```

### Ready tip refinement

```text
Use case: precise-object-edit. Correct ONLY sword-tip packing on this EXACT transparent Zoro sprite atlas. Preserve its4-column by6-row grid,1024x1536 size,24poses, complete character anatomy, face, green eye, clothing, every hand and grip, pixel-RPG rendering, scale and registration. No new design, no camera zoom and no added frames. Do NOT shrink or move the heads/bodies. All cells not named below must remain unchanged.
ONE-BASED positions: ROW5 COLUMN3 and ROW6 COLUMN3: the hand-held sword extends through the RIGHT cell edge. Shorten/curve ONLY its last few pixels so the tip ends at least8pixels inside the same cell. ROW5 COLUMN4 and ROW6 COLUMN4: remove the stray incoming blade-tip pixels on the LEFT boundary that leaked from the previous cell.
Every katana must remain a complete sharp blade with its actual tip visible, correctly connected to its original hilt. Exactly3 swords total per character, no blade crossing into another cell. Clean all gutters back to genuinely transparent alpha, keeping8pixels of clear margin around these corrected tips. No painted background, grid lines, text, glow, new sword or extra limb. This is a tiny targeted tip/stray-pixel correction, not a redraw of the characters.
```

### Combo tip refinement

```text
Use case: precise-object-edit. Correct ONLY sword-tip packing on this EXACT transparent Zoro sprite atlas. Preserve its4-column by6-row grid,1024x1536 size,24poses, complete character anatomy, face, green eye, clothing, every hand and grip, pixel-RPG rendering, scale and registration. No new design, no camera zoom and no added frames. Do NOT shrink or move the heads/bodies. All cells not named below must remain unchanged.
ONE-BASED positions: ROW4 COLUMN3: one hand-held sword extends through the RIGHT cell edge. Shorten/curve ONLY its last few pixels so its complete tip ends at least8pixels inside that same cell. ROW4 COLUMN4: remove the stray incoming blade-tip pixels at the LEFT boundary that leaked from the previous cell.
Every katana must remain a complete sharp blade with its actual tip visible, correctly connected to its original hilt. Exactly3 swords total per character, no blade crossing into another cell. Clean all gutters back to genuinely transparent alpha, keeping8pixels of clear margin around these corrected tips. No painted background, grid lines, text, glow, new sword or extra limb. This is a tiny targeted tip/stray-pixel correction, not a redraw of the characters.
```

### Finisher tip refinement

```text
Use case: precise-object-edit. Correct ONLY sword-tip packing on this EXACT transparent Zoro sprite atlas. Preserve its4-column by6-row grid,1024x1536 size,24poses, complete character anatomy, face, green eye, clothing, every hand and grip, pixel-RPG rendering, scale and registration. No new design, no camera zoom and no added frames. Do NOT shrink or move the heads/bodies. All cells not named below must remain unchanged.
ONE-BASED positions: ROW3 COLUMN2: a hand-held sword extends through the RIGHT cell edge. Shorten/curve ONLY its last few pixels so its complete tip ends at least8pixels inside the same cell. ROW3 COLUMN3: remove the stray incoming blade-tip pixels at the LEFT boundary that leaked from the previous cell.
Every katana must remain a complete sharp blade with its actual tip visible, correctly connected to its original hilt. Exactly3 swords total per character, no blade crossing into another cell. Clean all gutters back to genuinely transparent alpha, keeping8pixels of clear margin around these corrected tips. No painted background, grid lines, text, glow, new sword or extra limb. This is a tiny targeted tip/stray-pixel correction, not a redraw of the characters.
```

### Final finisher border cleanup

```text
Use case: precise-object-edit. Edit this EXACT 4-column by6-row transparent Zoro animation sheet. Keep1024x1536 size, all24 poses, every actual character/sword, costume, face, scale, anatomy and genuine alpha unchanged. There is ONE TINY ARTIFACT to remove: at ROW3 COLUMN3 (one-based), on the EXTREME LEFT edge of that cell, about216pixels down within that256pixel cell, a small dark stray blade fragment has leaked in from the previous cell. It is disconnected from the actual character's left-pointing sword, which already ends well inside the tile. Remove ONLY that stray fragment and make the leftmost8pixels of this cell fully transparent around that y-position. Do not remove or shorten the real sword, hand, boot or figure. Also ensure the neighbouring ROW3 COLUMN2 sword tip ends fully INSIDE its cell and does not cross that border. All other cels/pixels stay unchanged. No new background, marks, labels, poses or effects. This is a tiny border cleanup, not a new drawing.
```
