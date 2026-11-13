# Your adventurer

Tap the avatar beside **Share Mode** on Android, or click **Your adventurer** in the Windows 1.2.13 sidebar, to edit your character. Names and available style choices save automatically, including when you leave with Back, dismiss the editor or close its window. **Done** closes the editor. On the original RPG characters, Boy/Girl looks, hair, colours, expressions and elements remain freely customizable. Signature characters equip as complete fixed designs.

Characters use a stepped idle cycle to breathe, blink and sparkle. Tap the large preview to cheer. Animation respects Android's animator setting, can be switched off under Style, and stops while sharing or when the app leaves the foreground. The chibi pixel art combines bundled generated equipment and face/hair atlases with separately rendered pixel rank frames. The build packs the equipment at its game resolution; the full-resolution art source stays outside the APK. There is no image download, account, avatar SDK or extra permission.

## Character collection

Locked characters have silhouettes. Tap one to preview its full design and unlock level. A preview does not equip it or change progress. An unlocked character has an **Equip character** button.

| Character | Unlock level |
| --- | ---: |
| Courier | 1 |
| Rune Mage | 8 |
| Forest Ranger | 18 |
| Shadow Reaper | 35 |
| Demon Warden | 55 |
| Heavenly Seraph | 75 |
| Astral Sovereign | 95 |
| Silent Ronin | 12 |
| Gojo-inspired | 40 |
| Saitama-inspired | 65 |
| Naruto-inspired | 85 |

Frames advance through Novice (1), Adventurer (8), Vanguard (18), Elemental (30), Shadow (45), Demonic (60), Heavenly (80) and God-tier (100). The editor shows your XP, the next character and the complete rank path. Unlocks are cosmetic: every sharing feature and transfer speed stays available.

## Progress

Progression began in 1.2.0 at level 1, without importing earlier transfer history. Later updates retain existing XP and levels. From 1.2.2, a new verified file earns 1 XP per complete 256 KiB, capped at 50 XP per file. Files smaller than 256 KiB share normally but earn no XP. Awards occur after verification on receipt or the receiver's acknowledgement after sending. Incomplete, cancelled, corrupt and empty files earn nothing. A completed file in a partially failed batch can earn its reward; retrying it does not earn another reward. Renaming, selecting again or sending back the same content does not generate extra XP on that device.

Progress belongs to this installation. Android uses a private SQLite transaction; Windows uses a flushed, checksummed append-only receipt ledger in its existing local application-data folder. Both store a content SHA-256/size receipt to prevent duplicate rewards. The ledger contains no filenames or peer identities, and is not advertised. Removing the local app data removes progress; there is no account recovery or cross-device synchronization. A PC begins at level 1 independently of a phone. Local progress and nearby cosmetic levels are not a competitive or tamper-proof score.

For developers: total XP to reach level `L` is `40n² + 3n³`, where `n = L - 1`; the maximum is level 100. From 1.2.2 a file earns `min(50, floor(bytes / 262144))`. Existing XP and the level curve are retained; historical receipts are never recalculated. This deliberately makes later levels much slower. No daily streaks, notifications or sharing restrictions are attached to progression.

The five-field discovery avatar and v1 transfer protocol remain compatible with older peers. Silver is a sixth free hair colour; its five-field fallback uses the existing gold index, while new peers receive optional hairTone=5. New peers exchange optional, bounded look/role/element/level/hairTone fields. These fields are display metadata and do not authorize a transfer. QR secrets, Wi-Fi passwords, approval, TLS identity and transfer verification are unchanged.

## Pixel renderer (1.2.1)

The eight customizable classes support Boy/Girl bodies, six independent hairstyles, five skin tones, six hair colours including silver, five outfit colours, four expressions and six elements. The head, hair and eyes remain independent of generated equipment, so changing a style does not require a separate full character image. Existing style indices and progress are retained. The sixteen signature characters use their own complete artwork.

The hero uses a 9.6-second discrete idle cycle. Breathing moves by a logical pixel, boots stay grounded, eyes briefly blink and small elemental pixels pulse. Tap triggers a short cheer hop and sparks. Only visible idle heroes animate; collection/nearby previews are static. Draw state changes at the pose boundaries, and reduced-motion and background/transfer guards apply. Equipment images have a bounded 24-entry recolour cache.

## Novice-to-endgame gear (1.2.2)

A character unlock gives access to the class and its novice outfit; it does not grant every advanced outfit in that family. Gear evolves automatically from the saved earned level. Hair, skin, outfit/element colour, expressions and Boy/Girl looks remain free.

| Appearance | Level |
| --- | ---: |
| Novice Courier, plain clothes and no weapon | 1 |
| Courier Trainee, leather and wooden training gear | 10 |
| Iron Courier, modest iron gear | 25 |
| Ornate Courier Knight / Rune Mage / Forest Ranger | 45 |
| Full Shadow Reaper | 60 |
| Full Demon Warden with horns | 75 |
| Full Heavenly Seraph with wings and halo | 85 |
| Astral Herald | 95 |
| Ascended Astral Sovereign | 100 |

Rune Mage first unlocks at 8 as an Apprentice Mage; Ranger at 18 as a Scout; Reaper at 35 as an Initiate; Warden at 55 as a Recruit; Seraph at 75 as an Aspirant. Class unlock thresholds are unchanged. Basic forms do not emit auras/sparkles or receive advanced headpieces. The Ranks tab includes a preview-only gear roadmap. Previewing never equips future gear or changes XP. Older peers may draw their older outfits while transfers remain compatible.

Level 100 requires 3,302,937 total XP: a new player needs at least 66,059 maximum-XP unique files. Repeating, renaming, resuming or sending the same content back cannot earn another reward. The lower future reward rate does not remove any previously earned XP, levels or character unlocks.


## Head attachment and new characters (1.2.3)

The shared rig seats the chin inside the collar with a short skin-matched connection. During the idle loop, the feet remain planted while chest, arm groups, head and trailing hair move around shared anchors. Focused eyes have authored blink and left/right glance cels. Existing cute expressions remain available. See [the 1.2.3 changes](RELEASE-1.2.3.md) for the serious Ronin and anime-inspired families, their independent first/mastery unlocks, and backwards-compatible cosmetic metadata. The original seven class unlocks and all saved XP are retained.



## Fixed signatures and personal avatars (1.2.4)

Gojo-, Saitama- and Naruto-inspired heroes have complete fixed artwork, including their own heads and idle cels. Equip them after unlocking, without a customization step. Hair, palette, expression and Boy/Girl controls are hidden while they are equipped. Name and animation settings remain global preferences. Switching back to any normal RPG character restores the saved personal appearance and its editing controls. Silent Ronin remains customizable. Existing unlock/mastery levels, XP and saved role IDs are retained. See [the fixed-character update](RELEASE-1.2.4.md).

## Card Duelist (1.2.5)

Yugi-inspired joins the fixed signatures at **Level 50** as Card Duelist, with the Pharaoh Duelist mastery outfit at **Level 95**. Unlocking enables Equip; it never changes your choice automatically. Both outfits have eight authored cels: idle, blink, left/right turns, card presentation, pendant glint, cheer and smirk. Tap the large avatar for the cheer. The same motion/background/transfer guards apply. All previous character IDs and unlock levels are retained, and personal appearance survives switching to and from this character. See [the update](RELEASE-1.2.5.md) and actual-renderer animation (retired preview/source).

## Favourite collection (1.2.6)

Twelve individually generated characters join the fixed signatures, each with12 cels for its first outfit and12 for earned mastery. The picker is ordered by unlock level. Luffy, Zoro, Boa Hancock, Shanks, Eren, Levi, Mikasa, Sukuna, Itachi, Rock Lee, Might Guy and Madara each have an individual design, locked preview and equip action. Madara uses a cold-eyed, imposing stance and controlled power reaction. New first/mastery gates are listed in [the collection notes](RELEASE-1.2.6.md); old characters and XP remain unchanged. Switching away restores the saved personal style. See all new outfits (retired preview/source) and Madara's actual-renderer animation (retired preview/source).

## Windows avatar studio (1.2.7)

On PC, Your adventurer opens a searchable collection beside a complete-character preview. Style offers visual hairstyle choices and skin/hair/outfit/accent swatches, with autosave. The personal default uses whole Boy/Girl character cels instead of separate head/body layers. Featured contains this default and18 fixed signatures, including the newly drawn Heavenly Seraph and Astral Sovereign; older custom RPG styles remain under All characters or Customizable. Personal choices are retained when equipping a fixed character. Starter/Mastery previews are separate from Equip. Journey presents character unlocks while keeping earned XP and existing unlock gates. The clean desktop portrait renderer replaces the old pixel rank frame. [Full Windows notes](RELEASE-WINDOWS-1.2.7.md). Android1.2.6 retains its existing editor/art and unchanged APK.


## Sharper desktop previews (1.2.8)

Above the large portrait, choose HD for original 256-pixel detail or Pixel for crisp blocks. The choice saves automatically, including on fixed characters, and applies to your sidebar portrait. HD avoids enlargement beyond native size; Pixel enlarges only by whole multiples. These display choices do not change equipment, personal colours, XP or unlocks. See the comparison (retired preview/source).


## Madara signature cinematic (Windows1.2.9)

Search for Madara in Characters, select his card and click the portrait or Preview move. Crimson Awakening uses64authored gesture/eye/Susanoo cels over9.4seconds. Play move unlocks with Madara atLevel96; a locked preview never equips or grantsXP. Replay follows completion; Skip or Escape closes it. It uses the crimson outfit without changing equipped mastery gear. Saved HD/Pixel and motion settings apply; hidden/inactive playback pauses, and Off shows a still. [Full notes](RELEASE-WINDOWS-1.2.9.md).


## In-card skills (Windows 1.2.10)

Madara now performs Crimson Awakening inside the portrait card. Click his portrait or Use skill; click Stop or the animated portrait to return to idle. Auto beside HD/Pixel saves automatically and plays once when unlocked Madara is selected or equipped. A selected Madara also activates once when verified progress reaches Level 96. Auto Off keeps manual use available; Animation Off prevents both. Locked manual previews never equip or grant XP. There is no separate movie window. [Full notes](RELEASE-WINDOWS-1.2.10.md).


## Sukuna and quicker skills (Windows 1.2.11)

Sukuna now has Domain Expansion: Malevolent Shrine in the portrait card. Click the portrait or Use skill; Auto follows his existing Level 88 unlock. His 64-cel sequence takes 4.8 seconds. Madara now takes 5.2 seconds instead of 9.4, with the same artwork and controls. Both retain Stop, HD/Pixel and saved motion preferences. A mastery preview returns to its selected outfit after the skill's initial costume. [Details](RELEASE-WINDOWS-1.2.11.md).


## Zoro mastery: Three-Sword Tempest (Windows 1.2.13)

Choose Zoro > Mastery > Use skill, or click the mastery portrait. Starter keeps its idle and has no skill activator. Mastery uses the black bandana, green robe and red sash with Wado's white mouth grip and Enma/Sandai sword cues. The 2.8-second move has 72 body poses, 24 effect cels, instant crosscuts and a strong final diagonal split of the preview, with no zoom. Stop returns to the selected outfit. Auto follows the existing Level 82 mastery; starter still unlocks at 28. Locked manual mastery demonstrations never equip or grant XP. [Details](RELEASE-WINDOWS-1.2.13.md).
