# Build and package

All commands run inside this project. Leave the work Docker environment and Tri/MH4 projects and caches unchanged. The former shared Docker/MH4-cache workflow is superseded by this Windows workflow.

## Android on Windows

For the Android 1.1.0 development candidate, use JDK 17, Gradle 8.11.1, Android platform 36 and build-tools 35.0.0 with AGP 8.10.1. Android Studio and Docker are unnecessary. The current machine has these tools in the ignored `.local/tools/` directory:

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

The build runs JVM and Robolectric/Compose UI tests, assembles the optimized signed APK and AAB, and runs release lint. Outputs: `dist/Share-Mode-1.1.0.apk` and `dist/Share-Mode-1.1.0.aab`. UI tests use Android 15 in Robolectric and save rendered previews under `.local/ui-1.1.0`; this is not a physical-device check. Test dependencies are excluded from the release APK. Java's temporary socket directory is scoped to `.local/nio-temp` because this machine's normal temp directory rejects Java domain-socket connections. Process environment settings are restored afterward.

The existing private `.local/signing/shareme.jks` and password file are required. The build does not replace or regenerate them. Verification checks package `com.rowell.shareme`, version code 6, version name 1.1.0, label Share Mode, Android 10 minimum and SDK 36 target, then compares the signer with the previous release's certificate in `.local/signing/certificate-sha256.txt`. This text is a public certificate fingerprint; the keystore and password remain private. Restore the original private backup if signing files are missing.

## Windows

```powershell
.\scripts\build-windows.ps1
node .\tests\windows-integration.mjs
.\scripts\check-windows.ps1
.\scripts\check-qr.ps1
```

The installed .NET Framework compiler builds `dist/Windows/Share-Mode.exe`. The QRCoder 1.8.1 NuGet archive is SHA512-verified before extraction. Keep `QRCoder.dll` and its licence beside the executable.

The integration harness uses loopback servers and isolated dummy files. It checks transfer framing, integrity failures, resume, cancellation, safe names, existing files and 64-bit sizes. Network/preparation checks and seven actual WinForms selection/Advanced-control checks run separately. `check-qr.ps1` captures the app off-screen and decodes its displayed QR using ZXing 3.5.3 with the project-local JDK. It uses no Docker or phone.

## Packaging

The existing combined ZIP packaging scripts are pinned to the published 1.0.4 release and its physical-listener proof. Do not use them to publish Android 1.1.0. Its APK/AAB are development candidates pending the physical checks in [PLAY-RELEASE.md](PLAY-RELEASE.md). Windows remains 1.0.4.

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
