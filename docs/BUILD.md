# Build and publish

## Android

The current PC reuses the existing `monster-hunter-tri-android:rowell` Docker image and Gradle 8.9/Gradle dependency cache from the MH4 project. It does not change MH4 source, APKs or saves. The Docker image supplies Java 21, Android SDK 35 and build-tools 35.0.0. No additional Windows Android Studio install is required for this build.

```powershell
.\scripts\build-android.ps1
```

On another machine, supply an Android build image with those tools and an extracted official Gradle 8.9 distribution:

```powershell
.\scripts\build-android.ps1 -DockerImage your-android-image -GradleDirectory C:\tools\gradle-8.9 -GradleCache C:\build-cache\shareme
```

The first build generates the persistent private key `.local/signing/shareme.jks` and its password file. Back up both privately. Later builds use the same identity. Do not include them in GitHub source, ZIPs or error reports. The app ID is `com.rowell.shareme`, separate from both Monster Hunter apps.

## Windows

Windows 10/11 with .NET Framework 4.8 and PowerShell:

```powershell
.\scripts\build-windows.ps1
```

The script checks the SHA512 of QRCoder 1.8.1 before extracting its net40 DLL, then compiles using the installed .NET Framework C# compiler. QRCoder's MIT notice is included. The executable is a portable x64 app; extract its ZIP with the dependency DLL beside it.

## Checks and packaging

Android builds run JVM tests, assemble the release APK and run Android lint. On Windows:

```powershell
node .\tests\windows-integration.mjs
.\scripts\check-windows.ps1
.\scripts\check-qr.ps1
.\scripts\verify-android.ps1
.\scripts\package.ps1
```

The integration harness uses isolated dummy files and loopback-only headless servers. It exercises transfers, integrity failures, resume, safe names, existing files and 64-bit sizes. A size metadata test is not a physical multi-gigabyte throughput benchmark. Complete phone and hotspot tests before claiming broad device compatibility.

`check-qr.ps1` captures this app's own window off-screen, then checks the displayed receiver QR against its pairing session with ZXing 3.5.3. It uses the existing Docker image and the decoder cached by the Android build; it does not connect to a phone.

Packaging puts the exact APK into Android, Windows and complete ZIPs, along with instructions, checksums, licence and third-party notices. `dist/VERSION.json` records actual validation status. Source ZIPs exclude `.local/`, `dist/`, build caches and keys.

## GitHub download links

The source repository is [Dling11/share-me-rowell](https://github.com/Dling11/share-me-rowell). For version 1.0.2, create a release tagged `v1.0.2` and upload the APK, three ZIPs, `SHA256SUMS.txt` and `VERSION.json` from `dist/`. The README links directly to these release assets. No repository is created or pushed automatically by these build commands.
