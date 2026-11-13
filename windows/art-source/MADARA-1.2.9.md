# Madara cinematic artwork — Windows 1.2.9

Cleanup note (November 13, 2026): only selected current production sources remain in the project. Unused drafts, superseded Zoro 1.2.12 sheets and historical review images were deleted at the owner's request. Generation names and prompts below remain as provenance, including references to retired files.

Three selected assets were generated with the built-in image tool, one subject per call. A fourth call refined the eye atlas; the first eye draft is retained separately. No external API runner or key was used. Originals remain unchanged in the generation folder; the selected sources are copied here for reproducible project use.

| Asset | Selected source | Original generation |
| --- | --- | --- |
| 24 whole Madara gesture cels | madara-cast-source-1.2.9.png | exec-50bc38d7-6bcc-4ea1-ba2d-3df09735c823.png |
| 24 Susanoo emergence cels | madara-guardian-source-1.2.9.png | exec-4172d814-b4c0-409e-8ba2-6fc49fd21095.png |
| 16 eye close-up cels | madara-eye-source-1.2.9.png | exec-008b11ff-89c6-4768-a764-8d3c1b9919cb.png |
| Eye draft, superseded | madara-eye-draft-1.2.9.png | exec-e450dd3c-2f6b-4904-9fd7-dd1c7f401da8.png |

The existing android/art/pixel-madara-source.png supplied the character/style reference. Existing idle and mastery artwork were retained. This first signature movie uses the crimson outfit at every level and does not change equipped gear. Runtime effects load only when its scene is opened.

The compiler preserves complete gesture/guardian cels and their alpha. The selected eye sheet is 1254 pixels square; its uniform four-by-four grid is sampled at rounded boundaries to 313-pixel square cels. Runtime eye framing takes the central 80 percent for an intentional close-up, excluding stray pixels in the source gutters. No source PNG was painted over. Compiled atlases live in windows/art; optional PNG compression preserves visible RGBA values. The application needs neither the source images nor authoring tools at runtime.

## Exact prompt set

### Gesture sheet

```text
Use case: stylized-concept. Asset type: production transparent character-animation sprite atlas for Share Mode.
Reference image: the supplied Madara sprite sheet is a STYLE AND CHARACTER reference, not an edit target. Generate a NEW single-character sheet.
Create exactly 4 columns by 6 rows = 24 equally sized square animation cels, strict regular grid, 1024x1536 canvas (256x256 cells). No visible grid lines, no text, no numbers. Genuine transparent background throughout, no grey/black backdrop.
ONLY adult Madara Uchiha in his deep crimson segmented samurai armour and black clothing. Preserve the reference's intimidating angular stern chibi face, narrow crimson eyes, long wild layered black hair, mature proportions, strong clean dark outlines and detailed pixel-RPG illustration finish. No smile or baby face. Anatomically connected head, neck and shoulders. Same character, face, proportions, costume and scale in all 24 cels.
This is a FLUID HAND-SIGN CASTING SEQUENCE, not 24 idle variants. Read left to right, top to bottom. All cels show his COMPLETE full body, including hair tips and both feet, with generous 12px margins. Head center and feet stay registered; face stays at the same height. 24 genuine progressive in-between drawings:
frames 0-3: stern neutral stance, both hands start lifting gradually from waist;
4-7: elbows bend, palms approach at chest, fingers turn inward gradually;
8-11: hands meet into a clear ninja hand sign, fingers articulate, hair begins lifting from power;
12-15: holds the hand sign, chin lowers slightly then visible eye opens with intense crimson glint, sleeves/hair have subtle progressive outward motion;
16-19: hands separate gradually, right palm rotates forward into a commanding casting gesture, gaze severe;
20-23: slowly lowers arms back to the exact starting neutral stance so the sequence can return smoothly to idle.
No giant effects, no Susanoo, no eye closeups in this sheet; only a tiny restrained crimson glint around the eye in frames12-19. The body motion must be clear and sequential. No weapons crossing the face, no cropped heads, no mismatched scale, no detached body pieces. Entire costumes drawn as complete cels. ONE character per cell. Keep genuine alpha.
```

### Susanoo sheet

```text
Use case: stylized-concept. Asset type: transparent VFX animation atlas for a chibi pixel-RPG character cinematic.
Input reference: supplied Madara sheet is STYLE/PALETTE reference only. Do NOT include the human Madara character in this new effect atlas.
Generate exactly 4 columns x 6 rows =24 uniform square cels on a 2048x3072 image (512x512 cells if possible). Strict aligned regular grid, no labels/grid lines/text/watermark. Genuine transparent background, including the interior negative spaces. No grey/black opaque tile backgrounds.
Subject: ONE intimidating Madara Susanoo guardian apparition, luminous deep-violet spectral samurai, a fierce horned angular mask, narrow glowing pale lavender eyes, huge broad armour shoulders, ribcage-like lower torso, and two powerful ghostly arms. Detailed clean chibi pixel-RPG sprite illustration, precise dark-violet outlines, controlled purple rimlight, clear readable anatomy. No unrelated people, weapons or objects.
Animation runs left-to-right then top-to-bottom across24 distinct progressive drawings of a guardian EMERGING, not24 identical completed busts:
0-3: sparse curling purple wisps gather, small rib-like energy arcs materialize low in the frame, faint eye shapes begin high up;
4-7: translucent spectral ribs and shoulders build progressively upward, mask begins forming between emerging short angular horns;
8-11: face/mask becomes solid and intimidating, eyes open progressively, large upper arms assemble from energy;
12-15: complete towering spectral samurai bust forms, shoulders spread, chin tilts down imperiously, elbows bend outward;
16-19: one huge spectral hand reaches forward slightly, energy mane/armour ribbons rise and flow, eyes intensify;
20-23: full guardian remains in a powerful protective stance while flame ribbons smoothly ripple and settle.
Keep the same guardian proportions, face location and registered lower-torso anchor throughout. Entire apparition fits each square cell, with clear margins at horn tips and sides. The lower torso tapers into sparse transparent energy curls, never into a solid rectangular bottom. Center lower third has clear negative space for the separate Madara character to stand in front. Full luminous silhouette stays INSIDE each cell. Transparent violet body, sharp clean pixel-RPG detail, not blurry smoke, no giant flat glow covering the whole tile. This sheet contains ONLY the guardian and its restrained aura.
```

### Eye sheet

```text
Use case: stylized-concept. Asset type: transparent 16-frame cinematic eye close-up sprite atlas, for Madara's signature attack in Share Mode.
Reference image: supplied Madara sheet defines the exact mature, intimidating chibi pixel-RPG art style, skin tone, narrow eye shape, black fringe and crimson eye. Generate a new CLOSE-UP asset, not an edit of that sheet.
Exactly4 columns by4 rows,16 equal square256x256 cels,1024x1024 total. Strict regular grid, no visible lines, labels, numbers, text or watermark. Transparent background.
Every cel is a LARGE close-up of ONE SAME visible crimson Sharingan eye with its stern brow, light warm skin, a small part of the nose bridge and black hair fringe at the edge. Same camera angle and facial anatomy in all16cels. This is a deliberately cinematic face detail, NOT a complete character. The eye fills most of its cel width, but do not touch the tile edges. Crisp detailed pixel-RPG illustration, clean black outlines, not photorealism.
Animation reads left to right, top to bottom:0-3 eyelid slowly opens from an intimidating closed eye to half-open;4-7 eye opens fully and the crimson iris gradually brightens;8-11 the black tomoe iris markings subtly rotate through clear progressive in-betweens and a small restrained crimson energy glint forms at the outer corner;12-15 eye holds its cold fully open stare, the iris settles, the glint diminishes. No smiling eye. No red blood or injury. Not a generic round cute eye, no duplicate eye, no whole head, no body, no halos or giant circular symbols around the face. Keep the skin/brow/fringe crop identical and registered across all16 frames; only eyelid/iris/tiny glint animate. Real alpha outside the face-detail cutout, no black or grey rectangular background.
```

### Eye refinement

```text
Use case: precise-object-edit / background-extraction. Correct this EXACT Madara eye animation atlas. Keep all16 eye poses and their ordering, facial anatomy, crisp art, black fringe, brow and crimson Sharingan eye. Change ONLY atlas alignment and background artifacts.
Output a clean1024x1024 PNG in EXACT4 columns x4 rows of256x256 square cels. All16 face close-ups centered and registered at identical scale in their own cells. Each face-detail rectangle has at least12px of genuinely transparent padding around it. Remove every stray WHITE, YELLOW and RED speckle, noisy edge, irregular halo and thin divider line outside the16 face rectangles. Outer padding and all gutters must be genuinely transparent, not painted black/grey. Maintain clean intentional face edges. Do not add glows outside the tiny intentional eye-corner glints. No text, labels, grid lines, extra eyes, new frames or changed expressions. Keep the same16-frame progressive closed-to-open Sharingan sequence, one eye detail per cell. This is background/packing cleanup only, not new design.
```
