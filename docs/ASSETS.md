# Current artwork and generated-file retention

The Windows build explicitly embeds 47 production PNG atlases from windows/art. Android builds consume the selected pixel-*-source.png files in android/art. The four pc-*-source-1.2.7.png files rebuild desktop personal/fantasy portraits. These sources are required authoring inputs and are kept outside the installed app and APK.

windows/art-source retains ten selected skill sources: three Madara, three Sukuna and four Zoro mastery sheets. CompileMadaraArt.cs, CompileSukunaArt.cs and CompileZoroArt.cs select them explicitly. Discarded drafts and superseded Zoro 1.2.12 sheets are removed. Historical generation records can name files no longer retained; they are provenance, not build inputs.

assets retains 21 current documentation files: app icons, phone screenshots/default-avatar reference, desktop Send/Receive/Studio screenshots and the latest three skill screenshots/GIFs. [Zoro](../assets/zoro-skill-1.2.13.gif), [Sukuna](../assets/sukuna-skill-1.2.11.gif) and [Madara](../assets/madara-skill-1.2.11.gif) previews are documentation only. Old preview links in historical notes are marked retired instead of pointing to missing images.

Do not copy assets, authoring sources, draft images, review GIFs or historical archives into the installed application. Keep the explicit production allowlist; do not replace it with an image wildcard. Generated test captures, temporary executables and build caches belong under ignored .local or build directories. Retain only the current APK/AAB and Windows build in ignored dist; public GitHub release assets are managed separately.

The November 13 cleanup removed 25,171 disposable files totaling 18,724,425,909 bytes (17.44 GiB), including the old emulator workspace, project caches, repeated test captures and superseded install/archive backups. Current builds, 47 runtime images, selected sources, signing files, installed build tools and owner data were preserved. Dependency caches will be recreated when future builds need them. The installed app's executable and APK did not change.
