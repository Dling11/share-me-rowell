# Share Mode 1.1.0 — What's new

## Android

- Pick a nearby receiver by their name and avatar on the same Wi-Fi or hotspot. Compare the transfer code on both phones, then approve the files. QR pairing remains available for Windows and Android 1.0.4.
- Create your local chibi profile: choose a name, Boy/Girl look, hairstyle, skin tone, hair/outfit colours and expression. Surprise me keeps your selected look. Avatars are drawn in code and work offline.
- Choose installed apps directly. Standalone apps share as APKs; split apps include their installed APK parts in a bundle. Android asks before installation. App accounts, private data and saves are not included; device compatibility and store requirements still apply.
- Clearer hotspot errors and sharper pairing QR codes on small screens. Choosing a receiver after Stop restarts discovery while keeping your files selected.
- Stop sharing from the notification. Nearby discovery stops when the app leaves the foreground; active transfers can continue, while idle sharing shuts down after two minutes.

The signed APK is about **2.59 MB**. Install it over your existing Share Mode app; the package, signer, download folder and transfer-history paths are preserved. No uninstall is needed.

## Windows package

The Windows and complete ZIPs now include the same Android 1.1.0 installer as the standalone download. The Windows application itself remains 1.0.4, with its existing QR workflow and features. Extract the new portable ZIP to refresh the bundled phone installer.

## Downloads and validation

Use the APK to install on Android. The AAB is a developer artifact for a future Play submission and cannot be installed by tapping it. Every ZIP has one outer folder; SHA256SUMS.txt and VERSION.json describe the release files.

51 local Android tests, release lint with zero errors and original APK/AAB signatures pass. Two Android 16 virtual phones exercise the release APK's nearby confirmation, exact file hashes on shared Wi-Fi and an app-created hotspot, discovery restart and native split-app installation. Earlier candidate checks also covered a multi-file batch, a 128 MiB background transfer, notification Stop and idle hotspot cleanup.

This remains a public preview. Physical two-phone radio range, speed, camera scanning, battery/heat and manufacturer background restrictions still need device testing. This is a GitHub release, not a Play Store launch. Bluetooth remains optional through Android sharing.
