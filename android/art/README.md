# Android artwork inputs

The selected pixel-*-source.png files are build inputs. Gradle compiles them into small resources under the generated build directory; full-resolution sources are excluded from the APK.

The four pc-*-source-1.2.7.png files rebuild the desktop personal/fantasy portraits through [CompilePortraitArt.cs](../../scripts/CompilePortraitArt.cs). Keep selected source files unchanged unless intentionally updating their production artwork.

[Build instructions](../../docs/BUILD.md)
