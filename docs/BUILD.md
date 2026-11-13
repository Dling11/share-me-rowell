# Build Share Mode

Current preview: Windows 1.2.14 and Android 1.2.8 (version code 16), in release bundle 1.2.15.

## Windows

Use Windows 10/11, 64-bit, with .NET Framework 4.8 and its C# compiler. Run scripts/build-windows.ps1 from PowerShell. Output is dist/Windows/Share-Mode.exe beside QRCoder.dll and license files.

The build explicitly embeds 47 production PNG resources. Selected high-resolution sources are authoring inputs; screenshots, prompts and drafts are excluded from the executable. Windows builds do not require an Android emulator or additional runtime.

Run scripts/check-windows.ps1 and node tests/windows-integration.mjs. The studio, portrait, skill and transfer-showcase checks live under scripts/check-*.ps1. QR checks use the pinned ZXing decoder obtained by the Android build. Tests use isolated profiles and loopback transfers.

## Android

Use JDK 17, Android SDK 36, build-tools 35.0.0 and Gradle 8.11.1. scripts/build-android.ps1 accepts explicit JavaDirectory, SdkDirectory and GradleDirectory paths; local defaults are under ignored .local/tools.

Configure your own private signing material according to android/app/build.gradle.kts. An existing installation must be updated with its original signing identity; never publish signing keys or passwords. Project-local signing files stay under ignored .local/signing.

Run scripts/build-android.ps1, then scripts/verify-android.ps1. The build runs unit/UI tests, release lint, APK and AAB generation. Verification checks the package, version, permissions and original signing certificate. Dependency caches are downloaded again when absent.

Android consumes precompiled 256-pixel WebP cels from the approved Windows artwork. The optional scripts/compile-android-portraits.mjs utility uses sharp 0.35.5 to regenerate them. Set SHARE_MODE_SHARP_MODULE to an existing sharp installation or install it in an ignored tools folder. The Gradle preBuild check rejects stale or missing shared artwork. Legacy pixel compiler checks remain available for compatibility; unused legacy art is removed from the release APK. Selected source images remain in android/art and windows/art-source for rebuilding assets. No emulator is needed for the JVM/UI checks; physical radio/camera/manufacturer testing is separate.

## Preview packages

scripts/package-preview.ps1 creates the current Android and Windows ZIPs, checksums and VERSION.json from already verified builds. It requires matching signature/hash reports and Windows test evidence under ignored .local. Packages contain only runtime files, the current Android installer, guides and licenses. The Windows ZIP includes the APK for Get phone app.

The older scripts/package.ps1 reproduces the 1.1.0 release and retains its historical baseline guards. Use the current preview packager for the new version; do not bypass the old guards. Publishing a GitHub release and updating website download approvals are separate actions from building packages.

See [validation](TESTING.md), [privacy](PRIVACY.md) and [release changes](RELEASE-1.2.15.md).
