# Shared pixel artwork

These small transparent atlases are byte-for-byte copies of the Android 1.2.2 generated game-resolution resources:

- `pixel_armory.png`: 256 × 256, equipment.
- `pixel_progression.png`: 256 × 256, novice, training and final gear.
- `pixel_heads.png`: 192 × 192, independent faces and hair layers.

The original generated images, exact generation prompts, attribution and atlas compilation live in [android/art](../../android/art/README.md). No artwork was regenerated for the Windows port. The C# build embeds these images; System.Drawing composites and recolours their layers with nearest-neighbour sampling, a bounded 24-image cache and the same head/body registration as Android. The full-resolution source images are not shipped with the executable.

1.2.3 adds pixel_anime.png (256x256) and pixel_focus.png (96x96), compiled from the [new source/prompt record](../../android/art/PIXEL-1.2.3.md). The three original atlases remain byte-identical. New equipment normalization preserves the same neck and foot registration on both platforms.



1.2.4 adds pixel_special.png (256x384), a complete fixed-character sheet with unique character heads and four cels per costume. It bypasses generic face/hair and palette composition for roles8-10. Source and exact built-in prompt: [FIXED-1.2.4.md](../../android/art/FIXED-1.2.4.md). Original RPG resources remain editable and unchanged.
