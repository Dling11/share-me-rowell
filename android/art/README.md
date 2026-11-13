# Pixel RPG artwork

Created for Share Mode with the built-in image generation tool on November 7, 2026 (Asia/Taipei), using the owner-approved chibi pixel sample as a style reference. Source images are preserved unchanged. They are production layer atlases, not complete character screenshots. No third-party game sprite pack or runtime SDK is used.

The build compiles equipment to a 256x256 atlas (4x4 cells of 64px) and head layers to a 192x192 atlas (4x4 cells of 48px), with nearest-neighbor sampling and hard alpha. Only compiled resources are bundled. Head layers are cropped to their opaque bounds and registered at runtime, independently of equipment.

Equipment order is class*2+look (Knight, Mage, Ranger, Reaper, Warden, Seraph, Sovereign); the final two cells are empty. Head cells 0..3 are Bright, Happy, Wink and Blink. Cells 4..8 are Soft, Tuft, Fluffy, Long and Buns; None has no fringe.

Purple equipment cloth is recoloured to the chosen outfit, cyan magic to the element. Head skin, cyan irises and purple hair use separate palettes. The two recolour caches are bounded to 24 images each.

## Equipment

Source: [pixel-armory-source.png](pixel-armory-source.png)

SHA-256: 31bc57bbe873aa86c0a896b403665f05ee71c8fb69450f7d17a7d58d617f9afc

### Final generation prompt

```text
Use case: stylized-concept.
Asset type: production modular PIXEL ART armor sprite atlas for a customizable chibi RPG avatar system.
Reference image 1 is a STYLE REFERENCE: keep its crisp chunky pixels, cute two-head-tall proportions, strong RPG equipment silhouettes, dark outlines, and readable clustered shading. Create a NEW asset atlas, not a presentation board.
CRITICAL OUTPUT: a SQUARE 1024 x 1024 PNG with a GENUINELY TRANSPARENT background. Divide the canvas into an exact invisible 4-column by 4-row grid of equal 256 x 256 cells, no gaps, no lines, no labels. Each cell is a 64 x 64 logical-pixel sprite enlarged 4x with nearest-neighbor hard square pixels.
These are HEADLESS BODY AND EQUIPMENT layers: DO NOT DRAW HEADS, FACES, HAIR, HATS, HELMETS, HORNS, HALOS OR ANY HUMAN SKIN. The app will draw a separate customizable oversized chibi head. Reserve the CENTRAL HEAD AREA in every cell (logical x=18..46, y=0..35) as completely transparent. Neck opening at logical (32,36). Tiny armored torso roughly x=23..41,y=36..49; legs/boots y=49..61; boots bottom y=61, same baseline in all cells. Full tiny body, hands in gloves, weapons at sides. Capes/wings can extend behind the head only OUTSIDE the reserved central head area. Maintain the same body position/size/ground line for all 14 sprites.
Use richly shaded neutral iron/silver/obsidian armor with clear dark pixel outlines. All cloth/cape panels are MAGENTA/PURPLE (a reserved recoloring channel). Magical gems/weapon energy are CYAN/TURQUOISE (another reserved recoloring channel). The app recolors those channels. Metal can use neutral white/gray/black and restrained warm gold. Keep these sprites clearly pixel art, limited palette, no blur, no gradients, no painted/photographic shading, no background, no floor shadows, no antialiasing.
Exact cell order, left to right then top to bottom:
Row 1: Courier Knight Boy, Courier Knight Girl, Rune Mage Boy, Rune Mage Girl.
Row 2: Forest Ranger Boy, Forest Ranger Girl, Shadow Reaper Boy, Shadow Reaper Girl.
Row 3: Demon Warden Boy, Demon Warden Girl, Heavenly Seraph Boy, Heavenly Seraph Girl.
Row 4: Astral Sovereign Boy, Astral Sovereign Girl, EMPTY transparent cell, EMPTY transparent cell.
Class equipment:
Courier Knight: friendly compact steel breastplate, short cape, sword at one side, small round shield at the other.
Rune Mage: tiny layered wizard robe, belt, gloved hands, crystal staff beside the body, hanging scroll pouch.
Forest Ranger: small leather/iron tunic, hooded cape body without the hood, bow at one side, quiver low on back.
Shadow Reaper: jagged dark plate armor, ragged cloak, crescent scythe beside the body, cyan pixel soul-flames.
Demon Warden: heavy angular dark armor, armored gauntlets, short cape, huge rune-forged greatsword beside the body.
Heavenly Seraph: pearl-white/gold plate armor, compact feather wings spreading to left/right, tall gold spear at the side with cyan tip.
Astral Sovereign: regal layered dark-gold star armor, short ornate cape, floating small cyan crystals at sides, jeweled scepter at one side.
Boy and Girl versions are equally armored and powerful. Girl armor may have a short armored skirt over covered legs. No exposed skin, no head, no eyes. Every sprite and weapon fits inside its own cell without touching another. Leave the last two cells empty.
Primary priorities: exact 4x4 grid, genuinely transparent alpha, EMPTY CENTRAL HEAD REGIONS, matching 64-pixel logical scale, and detailed cool RPG ARMOR bodies that support the separate giant chibi heads.
```

## Faces and hair

Source: [pixel-heads-source.png](pixel-heads-source.png)

SHA-256: 3973b2a33c605ed729e9f8e4a901fbb11f8ec6573da2dbe678f1dd0ae686ca2e

### Final generation prompt

```text
Use case: stylized-concept.
Asset type: modular chibi PIXEL ART FACE and HAIR atlas, matching the reference RPG sprites.
Reference image 1 is the approved STYLE reference. Match its cute but badass expressive anime PIXEL faces, big sparkling eyes, chunky clusters and clean pixel outlines. This is a production layering asset, not a character presentation.
Create a square 1024x1024 PNG with GENUINELY TRANSPARENT BACKGROUND and an exact invisible 4x4 grid of equal cells. No frames, no text, no labels. Each cell represents a 48x48 logical-pixel canvas, enlarged as crisp nearest-neighbor square pixel blocks. No blur, no painted gradients, no antialiasing.
All faces/hair share this registration: head centered at logical x=24, y=22. Bald head outline roughly x=10..38,y=8..37; ears x=8..12 and x=36..40 at y=23..29; eyes centered near x=18 and x=30,y=25; mouth centered at (24,32). Big chibi head with a small tapered chin and rounded square cheeks, matching the reference's oversized heads. Reserve room for the separate hair layers. The images will be stacked at exactly the same origin.
Cell order, left to right then top to bottom:
Row1: BALD FACE bright eyes and confident mischievous smile; SAME BALD FACE happy curved closed eyes and smile; SAME BALD FACE one eye winking; SAME BALD FACE normal brief blink (both eyelids closed).
Faces only: no hair, no helmets, no necklaces, no armor, no body, no shoulders. Use warm peach skin with rich pixel-cluster shading, black/dark brown eyebrows/lashes, tiny mouth, subtle cheek pixels. Open eyes must be big beautiful angular anime pixel eyes, CYAN/TURQUOISE irises with tiny white highlights, NOT glasses, not rectangular spectacle frames. Same facial geometry/position/scale in all four expressions.
Row2: HAIR ONLY soft side-swept short fringe; HAIR ONLY short sharp heroic tuft; HAIR ONLY fluffy layered chibi spikes; HAIR ONLY long side-swept front fringe with temple locks.
Row3: HAIR ONLY two round buns and a soft fringe; EMPTY TRANSPARENT CELL; EMPTY TRANSPARENT CELL; EMPTY TRANSPARENT CELL.
Row4: ALL FOUR CELLS EMPTY TRANSPARENT.
Hair layers only: NO FACE, NO SKIN, NO EYES, NO HEAD DUMMY, NO EARS, NO HELMET. Only crisp dark-outlined purple hair sprites with lighter purple/lavender pixel highlights. Scalp/front hair is roughly x=8..40,y=3..24, and lower fringe tips can reach y=26; leave central eye area around x=14..34,y=25..31 transparent. Hair aligns onto the bald faces above. Bun hairstyle extends to x=4..44, but its center stays x=24. The long hairstyle includes front fringe and side locks down to y=38, with a transparent face opening. Back hair is handled separately by the app.
No hats or accessories in hair layers. Hair primary palette PURPLE only, for later recoloring. Neutral near-black outlines, light lavender highlights, carefully stepped limited palette; do not introduce skin colours into hair. Every face/hair element must fit its own cell. Leave the seven specified empty cells genuinely transparent.
Primary priorities: approved expressive chibi pixel face style, genuine crisp 2D pixel art, exact 4x4 grid, consistent face/hair alignment, transparent blank areas for layering, and NO GLASSES.
```


## Progression equipment (1.2.2)

Source: [pixel-progression-source.png](pixel-progression-source.png)

SHA-256: 3265664c2cd92ab3fc767fd5a27589d6d42fa02c1ef1e96d6fca3388067830af

Another 4x4, 64px-per-cell atlas. Pairs 0..7 are Novice, Trainee, Iron Courier, Apprentice Mage, Forest Scout, Shadow Initiate, Warden Recruit and level-100 Ascended Astral gear. Adventure.gear selects the proper source/pair from earned level; future previews do not persist an equipment override. Heads and hairstyles are unchanged. Early bodies do not use advanced headpieces or ambient magical effects.

### Final generation prompt

```text
Use case: stylized-concept.
Asset type: progression equipment atlas for a modular chibi pixel RPG avatar app.
Reference image 1 provides the pixel style and exact headless equipment layout. Create a NEW atlas of WEAK STARTER outfits progressing toward ONE EXTREMELY RARE LEVEL-100 COSMIC SET. Preserve clean chibi pixel art quality at every tier; early gear is charming and simple.
OUTPUT: square 1024x1024 PNG, genuinely transparent alpha, exact invisible 4x4 equal grid, no gaps, no text, no labels. Each 256x256 cell is a 64x64 logical-pixel equipment sprite enlarged 4x nearest-neighbor. NO HEADS, FACES, HAIR, HATS, HALOS, HORNS OR CROWNS: the app adds the existing customized heads. CENTRAL HEAD SOCKET logical x18..46,y0..35 must remain transparent. Neck at (32,36), tiny torso y36..49, legs/boots to baseline y61. Side equipment stays outside the head socket. All sprites share the same tiny body scale and same baseline. Boy/Girl pairs use equally practical fully covered outfits; Girl may have a short tunic/skirt over covered leggings. Brown or metal gloves: NO exposed skin.
Use limited-palette crisp pixel clusters with dark outlines, no antialiasing, no blur, no gradients, no painted texture. Purple/magenta cloth is a reserved outfit recolour channel. Cyan/turquoise magic is a reserved element recolour channel, and is allowed ONLY IN THE FINAL TWO LEVEL-100 CELLS. Wood/leather are natural brown; ordinary iron is dull neutral gray, not shiny gold.
EXACT CELL ORDER:
Row1 cell1: LEVEL1 NOVICE BOY: very plain purple linen shirt, simple brown trousers, rope belt, worn brown boots, little canvas messenger pouch. Empty hands. NO armor, weapons, shields, capes, metal trim, jewels, runes, wings, aura or magic.
Row1 cell2: same LEVEL1 NOVICE GIRL, equally simple plain tunic, covered legs, rope belt, little pouch, empty hands. No heroic equipment.
Row1 cell3: LEVEL10 TRAINEE BOY: plain stitched brown leather vest over purple shirt, wrist wraps, modest boots, small wooden buckler and short blunt wooden training sword. No metal plate, no gem, no magic, no ornate cape.
Row1 cell4: same LEVEL10 TRAINEE GIRL.
Row2 cell1: LEVEL25 IRON COURIER BOY: modest dull iron vest/chain mail and leather belt, small ordinary iron shortsword and plain round iron shield, no gold, no gems, no glowing blade, no giant shoulder plates. Simple practical novice guard.
Row2 cell2: same LEVEL25 IRON COURIER GIRL.
Row2 cell3: APPRENTICE MAGE BOY: plain purple cloth robe, rope belt, small leather pouch, entirely ordinary brown wooden staff with a simple round wooden tip. No gems, runes, metal armor or glow.
Row2 cell4: same APPRENTICE MAGE GIRL.
Row3 cell1: FOREST SCOUT BOY: simple brown leather vest, plain purple hoodless short cloak, simple wooden bow and ordinary quiver. No glowing gems, no silver armor plates, no magical arrows.
Row3 cell2: same FOREST SCOUT GIRL.
Row3 cell3: SHADOW INITIATE BOY: plain dark leather and modest dull iron pieces, short ragged purple cloth, small simple iron crescent scythe. No soul flames, no gems, no ornate plate armor, no purple magic clouds.
Row3 cell4: same SHADOW INITIATE GIRL.
Row4 cell1: WARDEN RECRUIT BOY: sturdy but plain dark iron armor, brown leather straps, short purple cloth tabard, ordinary broad iron sword. No lava, horns, jewels, enchanted runes, glowing sword, gigantic shoulders or gold filigree.
Row4 cell2: same WARDEN RECRUIT GIRL.
Row4 cell3: LEVEL100 ASCENDED ASTRAL BOY: spectacular late-endgame white/obsidian/gold cosmic plate armor, intricate layered breastplate, luminous cyan star-shaped core, floating small star crystals at the sides, magnificent compact celestial wings extending outside the head socket, ornate scepter and cosmic trim. This must be dramatically more elaborate and powerful than all prior cells, while remaining a tiny pixel sprite. Head socket stays empty.
Row4 cell4: same LEVEL100 ASCENDED ASTRAL GIRL, equal power, full armor over covered legs.
Every sprite fits its own cell without crossing borders. Make the first two novice bodies VISIBLY WEAK AND UNARMORED; make ONLY the last two bodies extravagant, magical, winged and god-tier.
```
