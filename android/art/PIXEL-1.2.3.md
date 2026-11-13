# Pixel art and motion revision 1.2.3

The original equipment, progression, face and hair sources remain unchanged. This revision fixes the shared collar/chin attachment in the Android and Windows renderers and adds a 9.6-second discrete rig: breathing chest, planted boots, independent arm groups, weight shifts, head turns, trailing hair, focused eye glances, double blink and a grounded cheer. Animation remains optional and pauses for inactive/background/transfer states. No engine or avatar SDK was added.

New project-bound assets were generated with the built-in image_gen tool, not the CLI:

- `pixel-anime-source.png`: original transparent 4x4 equipment source. Build output `pixel_anime.png` is 256x256 and normalizes the body collar and boot anchors before applying the transparent head socket. Windows embeds the byte-identical game-resolution output.
- `pixel-focus-source.png`: original transparent 2x2 focused/blink/left-glance/right-glance face source. Build output `pixel_focus.png` is 96x96. Original cute faces and hair remain available.

All seven original role indices retain their meaning. New classes append indices 7-10; unlocks are Ronin12, Gojo-inspired40, Saitama-inspired65 and Naruto-inspired85. Mastery outfits require55/80/90/100 respectively. Appearance choices remain free; the Equip action applies a signature look which can be customized afterward. No XP or old unlock was reset.

## Equipment prompt

```text
Use case: stylized-concept. Asset type: a transparent 4 by 4 headless equipment sprite atlas for Share Mode's existing chibi pixel avatar renderer.
The provided equipment atlas is a STYLE AND REGISTRATION REFERENCE. The provided head atlas and app portrait show the existing customized heads; DO NOT redraw any of them. Generate a NEW equipment atlas only, not a screenshot or mockup. Preserve the reference's crisp miniature RPG pixel clusters, dark outlines, practical chibi body proportions, foot baseline and transparent head socket.
Canvas square, exact 4x4 equal cells, transparent background, no labels, borders or text. Each cell represents 64x64 logical pixels. All boot soles at logical y61. NO HEADS/FACES/HAIR/NECK SKIN/HATS. Central x18..46,y0..34 stays completely empty. Shoulders and collar begin at y35; neck opening centered x32,y35..38, torso y37..49, hips y49..53 and boots end61. Make collars naturally support a modular rounded head without a long neck. Side weapons may extend above shoulders outside the head socket.
Eight boy/girl pairs, fully clothed practical equally cool designs. Each ROW contains TWO adjacent boy/girl pairs, in this EXACT order:
ROW1: cells1-2 SILENT RONIN trainee boy/girl, serious subdued charcoal cloth and ordinary brown leather, simple wooden practice katana in scabbard, modest belt, no gold/magic. Cells3-4 SILENT RONIN master boy/girl: layered compact dark samurai armor, tasteful dull silver edging, short torn indigo cape, sheathed steel katana, composed powerful silhouette. No horns, wings or headgear.
ROW2: cells1-2 GOJO-inspired academy boy/girl: simple fitted dark navy high-collar student uniform, plain dark boots, empty gloved hands, no magical effects. Cells3-4 GOJO-inspired limitless teacher boy/girl: stylish dark navy high-collar coat, long slender covered sleeves, one gloved hand raised with two fingers, a few restrained cyan-blue magical particles to the SIDES. No head or blindfold (added by app).
ROW3: cells1-2 SAITAMA-inspired training boy/girl: simple golden-yellow tracksuit, ordinary belt and red gloves/boots, no cape or effects. Cells3-4 SAITAMA-inspired full hero boy/girl: iconic compact yellow jumpsuit, white flowing shoulder cape, dark round belt buckle, bright red gloves and boots, confident gloved fists. No hair, face or head.
ROW4: cells1-2 NARUTO-inspired young ninja boy/girl: practical orange and charcoal zip jacket, ordinary dark trousers, wrapped ankles and dark ninja shoes, small plain pouch, no effects. Cells3-4 NARUTO-inspired final sage boy/girl: ornate but compact orange/gold ninja cloak over covered dark clothing, dramatic short coat tails, small cyan/amber chakra sparks outside head socket, cool gloved ninja stance. NO emblems or text.
Use yellow, orange, red, leather and dark navy as stable identity colours, with only a few purple cloth accents reserved for user outfit recolouring. Cyan is the existing element colour channel. Extremely crisp pixel art, binary transparency, no antialiasing, no soft painting, no background, no shadows under feet. No giant weapons obscuring bodies. Feet and body scale aligned across ALL16 cells. The novice versions must look plainly weaker than their corresponding advanced versions.
```

## Focused expression prompt

```text
Use case: stylized-concept. Generate a NEW transparent 2x2 pixel face-expression atlas as a companion asset to the supplied face/hair atlas. The input is a STYLE reference only: keep the same compact rounded chibi head proportions, pixel density, peach skin shading and cyan eye colour so these faces fit the existing hairstyles. No body, neck, hair, clothes, accessories, text or background.
Canvas square with EXACTLY 2 columns x2 rows, four equal cells. Each cell represents 48x48 logical pixels. In every cell: the SAME bald rounded chibi head alone, identical silhouette and registration, centered x24, face approximately x10..37,y10..37; a gently rounded lower jaw with a small flat 6-pixel chin, not a sharp needle point. Crisp dark one-pixel outlines, limited palette, hard transparency, no antialiasing, no painted blur. Skin peach/orange hue is a recolour channel; cyan eyes are a separate recolour channel. Eyes are smaller and more confident than the cute reference, with restrained angled eyebrows. Make a cool serious RPG/anime warrior expression that is still charming chibi, not angry, cruel or old.
EXACT CELLS: top-left focused calm forward-facing face, both eyes open, small straight neutral mouth. Top-right SAME FACE with both eyes briefly closed for an idle blink, exact same head silhouette. Bottom-left SAME FACE eyes subtly glance to the viewer's left, head outline unchanged. Bottom-right SAME FACE eyes subtly glance to viewer's right, head outline unchanged. No cheek whiskers, symbols or blindfolds; app adds those for character-specific looks.
There must be only four separate clean head sprites, with completely transparent gutters and no cast shadows. Do not include reference hair sprites. These are game sprites for a tiny lightweight app, not illustrations.
```



## Saved source and resource digests

android/art/pixel-anime-source.png | 1144651 bytes | SHA-256 cb9a2972f8c4df8ebd749900ce26a842ab3cf179da63a2cfb762a4019384accc
android/art/pixel-focus-source.png | 608282 bytes | SHA-256 62eb69f8ce6e752eb71867ea17c609deec443059d67aaf1495dbfd07ed98c6e5
windows/art/pixel_anime.png | 35239 bytes | SHA-256 7d79602c587fba01c739e1fc71d6dbedeb4b98a16a5b784d73b2d1d162026ed7
windows/art/pixel_focus.png | 6471 bytes | SHA-256 3f67e608510ba5cdaf58db41add79629999b6173108f96b9bc9cc57481527a20
