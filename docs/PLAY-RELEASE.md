# Android 1.1.0 release preparation

Version 1.1.0 is a GitHub public preview, separate from a Play Store submission. Its Windows bundle includes Android 1.1.0 and the unchanged Windows application from the 1.0.4 release.

## Included

- Local name and customizable chibi avatar, drawn in code without downloaded images or an avatar dependency. Boy/Girl appearance presets keep all hair/colour/expression choices available; Surprise me retains the selected style. Existing saved avatars and older nearby peers remain compatible.
- Android nearby receivers over platform DNS-SD on the same Wi-Fi/hotspot. Only an open, visible Receive screen advertises. A connection-specific 64-bit comparison code binds the actual TLS certificate and a fresh client nonce. The sender checks this code before selected-file metadata is sent; the receiver checks it and approves before payload transfer. Public discovery metadata contains no QR secret or Wi-Fi password.
- QR pairing and the existing Windows protocol remain compatible. Windows 1.0.4 uses QR; it does not advertise in the Android receiver list.
- Searchable launcher-app selection; standalone APK export and split APK bundles. Explicit received-bundle installation through Android's PackageInstaller. Sharing Share Mode itself also includes all installed parts; a split/Play installation offers its store link and does not advertise an incomplete base APK through local download QR. Standalone installations retain the offline APK bootstrap.
- Hidden manual hotspot details, explicit notification Stop sharing, no repeated identical notifications, and two-minute background-idle cleanup. Active transfers continue.
- Dense QR codes render with crisp integer-sized modules and a locally checked mask. After Stop/idle cleanup, Choose receiver restarts discovery for the existing selection. Hotspot errors provide readable guidance rather than an Android numeric code.
- Android 16 target/compile SDK 36, signed APK and signed Android App Bundle output. Package, signing key, downloads and history remain compatible.

## Before a Play submission

1. Validate this exact signed candidate on two physical phones: discovery appearance/loss and same-name avatars; matching-code rejection and acceptance; real files on router Wi-Fi, app hotspot and system hotspot; app switching, screen lock, cancellation, idle shutdown and notification Stop sharing.
2. Check Android 10/12 permission flow and Android 15/16 behavior, large fonts, a small display, TalkBack, and denied camera/notification/Nearby Wi-Fi permissions. Restricted/guest networks can block discovery and transfers; QR does not bypass client isolation.
3. Test received standalone and split app installation, denial/cancellation, and an incompatible device. Split parts exported from one device may not suit another device's architecture or required resources. Store licensing and downloaded assets are not transferred.
4. Upload the signed AAB to an internal test track first. Preserve the existing signing key and choose Play App Signing settings deliberately so existing sideloaded users can update. Do not replace the signing identity casually.
5. Host the privacy policy at a stable public URL, add it to the app and Play listing, and complete Data safety based on the actual shipped app. Review foreground-service and install-package declarations. File-sharing is a listed permitted install-package use; all installation must remain user initiated.
6. Prepare real screenshots, icon/feature graphic, support contact, content rating and store copy. Complete any account-specific identity/testing requirements shown in Play Console. Builds alone do not establish approval or production readiness.

Official requirements checked November 2, 2026:

- [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en): new phone apps/updates target API 36 from August 31, 2026.
- [Install-package permission](https://support.google.com/googleplay/android-developer/answer/12085295?hl=en).
- [Android network service discovery](https://developer.android.com/develop/connectivity/wifi/use-nsd).
- [Local network protections](https://developer.android.com/privacy-and-security/local-network-permission): API 36 uses existing network permission by default; API 37 introduces additional permission/picker requirements. Do not claim API 37 readiness from an API 36 build.

Native phone validation and Play Console submission are separate from local build checks.
