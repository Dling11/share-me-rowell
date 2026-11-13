# Build and package

All commands run inside this project. Leave the work Docker environment and Tri/MH4 projects and caches unchanged. The former shared Docker/MH4-cache workflow is superseded by this Windows workflow.

## Android on Windows

The current local 1.2.6 candidate includes Android Share-menu receiving, clearer app sending and transfer retry/resume, plus customizable animated chibi pixel RPG avatars and verified-transfer cosmetic progression. Build/verification output is `dist/Share-Mode-1.2.6.apk` and `.aab` with version code 14. Public downloads and the combined packaging script remain pinned to 1.1.0 until a separate release is published; do not run that packaging script against the 1.2.6 verification record.

For the Android 1.2.6 development candidate, use JDK 17, Gradle 8.11.1, Android platform 36 and build-tools 35.0.0 with AGP 8.10.1. Android Studio and Docker are unnecessary. The current machine has these tools in the ignored `.local/tools/` directory:

- `.local/tools/jdk17/<jdk-directory>/bin/javac.exe`
- `.local/tools/gradle-8.11.1/bin/gradle.bat`
- `.local/tools/android-sdk/platforms/android-36/android.jar`
- `.local/tools/android-sdk/build-tools/35.0.0/`
- `.local/tools/android-sdk/platform-tools/` (optional adb for connected devices)

On a new machine, obtain a Windows JDK from [Eclipse Adoptium](https://adoptium.net/temurin/releases/?version=17), [Gradle 8.11.1](https://services.gradle.org/distributions/gradle-8.11.1-bin.zip), and the listed SDK packages from [Android's official tools](https://developer.android.com/tools). Verify published checksums before extraction. The tools used here were checked against the JDK and Gradle publishers' SHA256 and Google's SDK repository archive checksums. Tool and dependency writes stay in this project.

```powershell
.\scripts\build-android.ps1
.\scripts\verify-android.ps1
```

For another local toolchain, pass `-JavaDirectory`, `-SdkDirectory` and `-GradleDirectory` to the build; verification accepts the first two. Dependencies and project state stay in `.local/gradle-local` and `.local/android-project-cache`. The ignored `android/local.properties` points to the chosen SDK.

The build runs JVM and Robolectric/Compose UI tests, assembles the optimized signed APK and AAB, and runs release lint. Outputs: `dist/Share-Mode-1.2.6.apk` and `dist/Share-Mode-1.2.6.aab`. UI tests use Android 15 in Robolectric and save rendered previews under `.local/ui-1.2.6`; this is not a physical-device check. Test dependencies are excluded from the release APK. Java's temporary socket directory is scoped to `.local/nio-temp` because this machine's normal temp directory rejects Java domain-socket connections. Process environment settings are restored afterward.

The existing private `.local/signing/shareme.jks` and password file are required. The build does not replace or regenerate them. Verification checks package `com.rowell.shareme`, version code 14, version name 1.2.6, label Share Mode, Android 10 minimum and SDK 36 target, then compares the signer with the previous release's certificate in `.local/signing/certificate-sha256.txt`. This text is a public certificate fingerprint; the keystore and password remain private. Restore the original private backup if signing files are missing.

## Windows

```powershell
.\scripts\build-windows.ps1
node .\tests\windows-integration.mjs
.\scripts\check-windows.ps1
.\scripts\check-qr.ps1
```

The installed .NET Framework compiler builds `dist/Windows/Share-Mode.exe`. The QRCoder 1.8.1 NuGet archive is SHA512-verified before extraction. Keep `QRCoder.dll` and its licence beside the executable.

The current local Windows candidate is 1.2.12, with the avatar/XP port described in [the Windows notes](RELEASE-WINDOWS-1.2.2.md). Its nineteen game-resolution PNGs in `windows/art/` are embedded by the C# compiler; building Windows does not require building Android or adding an emulator/runtime. The shared original art and prompts remain in `android/art/`. The public 1.1.0 packaging script requires the old Windows 1.0.4 baseline and will reject this new executable; do not bypass that gate to publish a candidate.

The integration harness uses loopback servers and isolated dummy files. It checks transfer framing, integrity failures, resume, cancellation, safe names, existing files and 64-bit sizes. Network/preparation checks and seven actual WinForms selection/Advanced-control checks run separately. `check-qr.ps1` captures the app off-screen and decodes its displayed QR using ZXing 3.5.3 with the project-local JDK. It uses no Docker or phone.

## Packaging

The ZIP packaging script targets preview 1.1.0. It requires signed APK/AAB verification, all 51 local Android tests, release lint, Windows checks and the two-emulator native checks for the exact APK. Every embedded phone installer must match the standalone APK. The Windows application is unchanged from the 1.0.4 release; its new 1.1.0 bundle includes the updated phone installer. Physical checks in [PLAY-RELEASE.md](PLAY-RELEASE.md) remain separate from this GitHub preview.

```powershell
.\scripts\package.ps1
```

Packaging requires successful test, lint, signature and QR reports. It creates separate Android, Windows and complete ZIPs with one outer folder each, plus `SHA256SUMS.txt` and `VERSION.json`. `verify-packages.ps1` checks archive checksums, outer folders, matching APK bytes, and exclusion of old installers and private/development files. Windows includes the same Android APK as the standalone download. Signing keys and caches are excluded.

The 1.0.4 TLS hotfix also requires a successful real-phone listener report for the exact installed APK. With the updated app's Receive screen open, privately capture its QR under `.local/` and run:

```text
node tests/android-listener-check.mjs --serial <adb-device> --qr-image <private-screenshot> --apk dist/Share-Mode-1.0.4.apk --version 1.0.4 --version-code 5
```

Repeat with `--legacy-signature` to check RSA PKCS1/SHA256 compatibility. The check verifies installed APK bytes, the QR certificate fingerprint and rejection of an invalid pairing code. It sends no files and creates no transfer-history entry. QR contents remain in process memory; only the result is saved in ignored `.local/phone-verification/`. Do not publish the QR screenshot.

The repository is still [Dling11/share-me-rowell](https://github.com/Dling11/share-me-rowell). Build and packaging commands do not commit, push or publish. The owner-authorized 1.0.4 release uses the Share-Mode filenames linked in README; earlier releases retain their old names.

The compilePixelArmory preBuild task reads android/art/pixel-armory-source.png and compiles a 256x256 transparent equipment atlas into the generated resources. Only that game-resolution image is packaged. The full generated source stays outside res/ and is retained unchanged. The compilePixelHeads task also packs the generated face/expression and hairstyle layers into a 192x192 atlas. Heads, six hairstyles, six hair colours, expressions and pixel rank frames are composited separately in the existing Compose renderer.

The compilePixelProgression task packs the novice/training/iron/class-apprentice/level-100 equipment source from android/art/pixel-progression-source.png as another 256x256 atlas. High-resolution art sources remain outside the APK.


The 1.2.3 compilePixelAnime task fits new headless outfits to the existing collar/boot anchors before masking the customizable head socket; compilePixelFocus packs four focused expression cels into a 96x96 atlas. Both outputs are copied unchanged into windows/art and embedded by the Windows compiler. Original full-resolution sources and exact built-in prompts stay in android/art/PIXEL-1.2.3.md, outside the application resources.


The compilePixelSpecial task compiles the dedicated fixed-character 4x6 source into a 256x384 hard-alpha sheet. The full source stays under android/art; only the compiled PNG is embedded on both platforms. Run scripts/check-special-characters.ps1 after the Windows build for the separate eight-case signature UI/save/restore regression fixture. It uses private labelled Level40, Level50 and Level100 data, not the installed user's XP.

The compilePixelYugi task packs the dedicated 4x4 source into a 256x256 atlas containing eight complete cels for each of two costumes. Copy the compiled pixel_yugi.png unchanged into windows/art before building Windows after an art-source change. The original and exact built-in generation prompt are recorded in android/art/YUGI-1.2.5.md.

The twelve compilePixel<Slug> tasks compile each dedicated character's 4x6 source into a 256x384 transparent sheet (12 starter cels and12 mastery cels). Copy their compiled PNGs unchanged into windows/art before a Windows build after source-art changes. Source and exact built-in prompts: android/art/SOLO-COLLECTION-1.2.6.md. New runtime poses are bounded to11 idle cels and a separate tap reaction; existing 4/8-cel signature layouts remain unchanged.

Windows 1.2.7 builds AdventurerStudio.cs, StudioControls.cs, PortraitCatalog.cs and CustomPortraits.cs in addition to the existing native sources. It embeds18 desktop portrait atlases alongside the19 original atlases. Source portrait PNGs are128 pixels per cel and are precompiled under windows/art. The optional CompilePortraitArt.cs authoring utility regenerates them from unchanged sources; ordinary builds need neither JDK nor Android tooling. Run check-studio.ps1 after check-qr.ps1 for appearance/search/scroll/layout checks and QR decoding at default/minimum sizes. This is a Windows-only candidate; Android build/verification remains1.2.6/code14. The old public packaging baseline is still protected.


Windows 1.2.8 embeds the same18 portrait atlases at256 pixels per cel. Regenerate only when authoring art: compile scripts/CompilePortraitArt.cs with System.Drawing.dll, run it with the absolute project root, then run node scripts/optimize-portrait-art.mjs for lossless visible-pixel PNG compression. The original source images are unchanged. Ordinary builds still use the checked-in compiled PNGs and need no Node runtime. After build-windows.ps1, check-portrait-sharpness.ps1 verifies HD/Pixel sampling, whole-pixel breathing, cache churn, selector layout and preference persistence. The optional before/after image uses the private prior-art backup when available; regression checks do not need it. Android remains1.2.6/code14.


Windows1.2.9 adds MadaraCinematic.cs and3embedded atlases (madara_cast, madara_eye, madara_guardian). Sources and exact prompts: windows/art-source/MADARA-1.2.9.md. Optional authoring: compile scripts/CompileMadaraArt.cs with System.Drawing.dll, run with the absolute project root, then node scripts/optimize-portrait-art.mjs madara_cast.png madara_guardian.png madara_eye.png. Ordinary Windows builds use the compiled PNGs. Run check-madara-cinematic.ps1 after build-windows.ps1. To export the reviewGIF, run encode-madara-review.ps1; its Windows WPF encoder is build-time only and is not linked into the app. Android stays1.2.6/code14.


Windows 1.2.10 replaces the cinematic Form with MadaraSkillPreview, a control hosted in the existing portrait slot, and SkillPlayback, its deterministic time state. Existing art resources are reused. Run check-inline-skill.ps1 after the Windows build in addition to the existing suites. Run encode-madara-review.ps1 -Inline to export the in-card review GIF from that suite. HeroProfile.AutoSkills defaults true when absent and saves locally; no Android schema or build change is needed. MainForm forwards verified-XP UI updates to the open editor for the selected character's unlock transition.


## Production artwork and local installation rule (1.2.11)

Always keep unused generations, source images, review GIFs, screenshots and historical ZIPs outside the installed application. The Windows compiler uses an explicit production-image allowlist; do not reintroduce wildcard portrait inclusion. Keep all original authoring material in the source project. Copy only the executable, QRCoder runtime/license, current signed Android installer and useful text documentation to the app folder. Run trim-windows-install.ps1 with the verified installation and full backup directories to archive old clutter and deduplicate the legacy launcher via NTFS hard link. Use -PlanOnly first; all archive source/destination paths are checked and owner AppData is untouched. Do not copy the whole assets or windows/art-source directory into the runtime.

CharacterSkills.cs adds Sukuna's Level 88 skill and its 4.8-second timeline. CharacterSkillPreview and CharacterSkillScene share rendering/lifecycle code with Madara, whose timeline is now 5.2 seconds. New authoring sources and exact prompts: windows/art-source/SUKUNA-1.2.11.md. Compile CompileSukunaArt.cs with System.Drawing, run with the absolute repo root, then optimize only sukuna_cast.png, sukuna_shrine.png and sukuna_eye.png using optimize-portrait-art.mjs. Normal builds use compiled PNGs. Run check-sukuna-skill.ps1 along with the existing suites; encode-skill-review.ps1 -Character Sukuna or Madara exports 50 ms review frames outside the runtime. Android remains 1.2.6/code14.


Windows 1.2.12 adds ZoroSkill.cs: a fixed-camera 72-pose renderer, 24 effects and a small reusable local screen-cut buffer. The build embeds exactly 47 production PNGs. CompileZoroArt.cs explicitly selects ready/combo v2, finish v3 and original slashes sources, preserving generated originals outside runtime. Run its authoring compiler, optimize only zoro_ready/combo/finish/slashes.png, then build Windows. check-zoro-skill.ps1 verifies the new move; encode-skill-review.ps1 -Character Zoro exports its 20 ms review outside the app. No eye atlas or source variant is bundled. Continue using the slim local installer and idempotent trim-windows-install.ps1; never copy assets/art-source/GIFs into the runtime. Android remains 1.2.6/code14.


Windows 1.2.13 replaces Zoro's four production atlases with mastery artwork, retaining 47 resources. CompileZoroArt.cs selects the 1.2.13 ready/combo/finish v2 sources and original slashes. Its authoring-only transparent-gutter registration preserves complete opaque poses and fits them into 256px runtime cells; crop/target logs stay under .local. Source originals are not changed. Optimize only the four zoro_*.png files and build normally. HasForOutfit/UnlockLevel separate Zoro's mastery skill from his starter outfit, preserving Level 28/82 gates. Zoro is 2.8 seconds with instant 20ms cut onset and a stronger final frame split. Export source-only review via encode-skill-review.ps1 -Character Zoro; continue the slim install rule above.
