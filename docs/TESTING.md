# Share Mode 1.0.3 validation

## Scope

This preview release renames the app, simplifies mobile sending/receiving, adds bounded thumbnails/details, and hardens Android hotspot/Wi-Fi routing and TLS setup. The package, app signing identity, pairing protocol and existing data paths are preserved.

## Required local checks

Build/package scripts record current results in `.local/` and `dist/VERSION.json`:

- Android JVM protocol, peer-state, connection-error and local-routing regression tests.
- Release lint and optimized APK assembly.
- APK signature, package/version/label and previous-release signer comparison.
- 18 Windows loopback transfer/installer checks.
- Five Windows network/preparation checks.
- Seven actual Windows selection/Advanced-control checks and decoding of the displayed QR.

These checks do not establish physical phone-to-phone compatibility or measured throughput. See the packaged VERSION.json for exact counts and completed checks.

## Physical testing to complete

Version 1.0.3 was installed as an in-place update from 1.0.1 on an Android 15 phone. The unchanged original install date, version code 4, successful native launch and actual Send-screen capture were verified. The signing certificate matches the previous release. This establishes installation and visible UI on that device; two-phone Wi-Fi/hotspot transfers remain pending.

The owner reports earlier phone-to-PC transfers and offline installer sharing work. The earlier phone-to-phone test failed on both Wi-Fi and hotspot; its exact failure stage has not yet been established.

Install 1.0.3 on **both** phones as an update. Keep the older APK if needed, but Android may prevent downgrading without uninstalling. Uninstalling removes app data, so do not uninstall just to roll back.

| Test | Steps | Expected result |
| --- | --- | --- |
| Same Wi-Fi | Receiver opens Receive; sender chooses files and scans receiver | Device names appear; receiver approves; files finish and open |
| Offline hotspot | Mobile data off on both; receiver creates hotspot; sender scans and approves Android join | Transfer without internet; correct hotspot name shown |
| Overlapping subnets | Start sender on another Wi-Fi using the same private subnet, then scan a hotspot QR | Android requests the named hotspot instead of reusing the unrelated router |
| Reverse scan | Sender uses More connection options, Show sender code; receiver scans it | Approval and transfer also work in this direction |
| Multiple files | Select a photo, video, document and empty file; add/remove files | Order, count and total stay clear; each file is verified |
| Cancel/resume | Cancel during a large file; reconnect and send the same selection | Completed files remain; compatible partial resumes |
| Mobile UI | Small screen, long filenames, larger font, switch tabs during transfer | Active file/progress stays at top; details expose full names; controls stay usable |
| PC regression | Phone scans Windows Receive files, then Windows Send files | Both directions work with the renamed builds |
| Background | Switch apps and return during a larger transfer | Accurate notification/status; interruption explained if power management stops it |
| Optional Bluetooth | Use Android sharing if desired | Android owns pairing/progress; Wi-Fi does not require Bluetooth |

For a failed connection, record both models/Android versions, which phone hosted, scan direction, visible stage/error, and whether mobile data/VPN was active. A screenshot of the error is sufficient for the next pass. Do not share pairing QR contents publicly.

## Connection changes

The previous manifest omitted `CHANGE_NETWORK_STATE`, which is required by `ConnectivityManager.requestNetwork`. The permission is now declared and checked in the built APK. This is a concrete hotspot-joining defect; it does not alone establish the cause of the separate same-Wi-Fi report.

Hotspot QR addresses exclude the concurrent router. Cellular/VPN/virtual interfaces are not advertised as local phone endpoints. A matching subnet does not establish hotspot identity. Hotspot joining waits for an IPv4 address from `onLinkPropertiesChanged` and binds its client socket to that Network. Host startup waits for its hotspot interface instead of a fixed delay. Reachable QR alternatives are tried first; TLS negotiation has a separate timeout. Android's signing-only keystore identity is served using ECDHE-RSA cipher suites.

Android documents the callback ordering race in [Read network state](https://developer.android.com/develop/connectivity/network-ops/reading-network-state). Permission requirements: [Use a local-only Wi-Fi hotspot](https://developer.android.com/develop/connectivity/wifi/localonlyhotspot).

Network-request permission: [ConnectivityManager.requestNetwork](https://developer.android.com/reference/android/net/ConnectivityManager#requestNetwork(android.net.NetworkRequest,%20android.net.ConnectivityManager.NetworkCallback,%20android.os.Handler,%20int)).
