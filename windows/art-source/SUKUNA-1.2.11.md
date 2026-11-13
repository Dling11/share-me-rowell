# Sukuna skill artwork — Windows 1.2.11

Created with the built-in image-generation tool, one subject per call. Three selected outputs provide 64 authored cels: 24 full-body hand-sign/cast/recovery poses, 24 shrine emergence frames, and 16 facial close-up frames. The original source outputs are preserved unchanged here; only compiled production PNGs are embedded. No draft, authoring source, GIF or screenshot belongs in the installed runtime.

| Asset | Selected project source | Original generation |
| --- | --- | --- |
| Gesture | sukuna-cast-source-1.2.11.png | exec-41547e6f-b991-439b-b476-d47305042de1.png |
| Malevolent Shrine | sukuna-shrine-source-1.2.11.png | exec-54584dde-cf4a-43d5-90d5-11e4559beeae.png |
| Face cut-in | sukuna-eye-source-1.2.11.png | exec-12f5650b-faf1-40e3-80d0-42177f75ebcd.png |

The reference was android/art/pixel-sukuna-source.png. This first skill uses the school-uniform design, including when viewing mastery, and returns to the selected outfit after playback. Existing idle/mastery sprites and XP gates are unchanged. The 4.8-second sequence is shared by manual activation and Auto. Source sizes are sampled at whole-grid boundaries by scripts/CompileSukunaArt.cs; runtime face framing uses a deliberate central close-up. Visible RGBA values in compiled PNGs are preserved by the optional compression step.

## Exact prompt set

### Hand-sign sheet

```text
Use case: stylized-concept. Asset type: production transparent 24-frame Sukuna casting animation atlas for Share Mode's character card.
Reference image: supplied Sukuna sheet is a character/style reference, NOT an edit target. Generate a NEW single-character animation sheet. Exact4 columns by6 rows of square256x256 cels,1024x1536 canvas. No labels, numbers, text or grid lines. Real alpha background, not painted grey/brown.
Subject: adult Ryomen Sukuna in his pink-haired vessel form, narrow intense crimson eyes, black forehead and cheek curse markings, dark navy school uniform with red collar and gold buttons. Match the reference's clean detailed chibi pixel-RPG illustration, strong crisp dark outlines, connected head/neck/body and correctly planted feet. More sinister confident smirk, never a childish open-mouth happy grin. SAME anatomy, face, costume, scale and body anchors throughout24cels.
24 genuinely progressive animation drawings, read left-to-right then top-to-bottom. Complete full body fits every cell with at least10px transparent margins, including hair and both feet:
0-3 calm dominant stance, both hands lift quickly from hips;
4-7 elbows bend, hands approach chest, fingers align into a two-handed domain-expansion mudra;
8-11 hands join into a clear interlaced clasp with a pair of upright index fingers, stern powerful gaze;
12-15 holds the domain hand sign, head tilts slightly forward, smirk intensifies, tiny crimson cursed-energy wisps begin at shoulders; fine incremental finger/sleeve/hair movement;
16-19 releases the seal into a commanding open-palm cast, black facial markings and crimson eyes remain clear, controlled red glints;
20-23 lowers both hands progressively back to the exact initial neutral stance.
Every cel must be a different useful in-between, not repeated idle. No giant shrine, eye closeups or screen-wide effects in this sheet, just Sukuna's full-body pose motion and tiny restrained crimson wisps. No blood, injury, detached neck, floating head, extra arms or cropped limbs. Keep all24 square cells registered and transparent.
```

### Shrine sheet

```text
Use case: stylized-concept. Asset type: transparent 24-frame Domain Expansion environment/VFX atlas for Sukuna's skill in a chibi pixel-RPG app.
Reference: supplied Sukuna sheet is STYLE reference only. No human character in this new asset.
Create EXACT4 columns by6 rows of equal square256x256 cels on1024x1536 transparent canvas. No grid lines, text, labels, numbers or watermark.
Subject: Sukuna's MALEVOLENT SHRINE, a sinister Japanese shrine apparition with a broad dark-red curved roof, ivory ornamental fang-like gable, black wooden pillars, a large open temple entrance and small horned roof ornaments. Distinct architecture, NOT a humanoid ghost or samurai. Detailed crisp chibi pixel-RPG illustration, strong dark outlines, clean crimson highlights, smoky black-red cursed energy. No blood, gore, corpses or injuries. Stylized ivory ornaments may suggest teeth/bone but remain clean architectural decoration.
24 progressive in-betweens read left-to-right then top-to-bottom:
0-3: a thin crimson domain ring and a few dark-red wisps gather low in the cel;
4-7: the shadowed shrine foundation and pillars form gradually, a red roof silhouette assembles above;
8-11: the roof and ivory gable materialize clearly, the central opening becomes a dark crimson portal;
12-15: the complete shrine rises into place, controlled red energy ribbons climb its pillars and a faint circular cursed boundary forms around it;
16-19: shrine fully awakened, animated crimson slash-shaped energy streaks orbit the sides, roof ornaments glow subtly;
20-23: fierce but steady full shrine, red-black energy pulses and settles in clear sequential variations.
The full structure, roof tips, ring and all effects stay INSIDE each cell with at least8px margins. Fixed camera, roof and base anchors consistent. Leave the center lower third partially open and transparent so a separate Sukuna sprite can stand in front. Genuine transparent background and sparse energy fade, never opaque fog rectangles, checkerboard, grey/brown tile backdrops or solid square glow panels. ONE shrine design per cell, no extra shrines or characters.
```

### Face cut-in sheet

```text
Use case: stylized-concept. Asset type: transparent 16-frame Sukuna cinematic face cut-in atlas for a chibi pixel-RPG character skill.
Reference: supplied Sukuna sheet is a character and art-style reference only. Generate a NEW close-up sequence.
EXACT4 columns by4 rows,16 equal square256x256 cels on1024x1024 canvas. No labels, text, numbers, border rectangles or grid lines. Real transparent background and clean transparent cell margins, no opaque grey/brown backdrop and no stray colored pixels in gutters.
Each cel is a large CLOSE-UP of the SAME adult Sukuna face: pink swept-back spiky hair, narrow crimson eyes, strong black forehead and cheek curse tattoos, angular confident face, intimidating controlled smirk. Preserve the reference's crisp detailed chibi pixel-RPG illustration and strong clean dark outlines. The head/face fills the central80percent of each cell; all features registered at the same scale and position. This is a deliberate cinematic close-up, not a full body.
16 progressive frames left-to-right then top-to-bottom:
0-3: eyelids rise from a cold closed stare to half-open;
4-7: eyes open fully, crimson irises intensify, mouth curls slowly into a sinister restrained smirk;
8-11: black curse markings grow slightly darker and tiny red glints gather beside the eyes, brows tighten;
12-15: holds the kingly confident smirk and narrow open eyes, red glints settle and fade slightly.
No laughing baby face, oversized round eyes, extra eyes or mouths, no blood, injuries or gore. Keep face anatomy, hair shape and tattoos consistent; movement comes from lids, irises, mouth corners and tiny glints, not camera jitter. Clean deliberate face cutout edges, genuine alpha outside the close-up head. ONE face per tile.
```
