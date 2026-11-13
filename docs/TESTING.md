# Share Mode validation

## Local Android 1.1.1 candidate

The candidate adds incoming ACTION_SEND/ACTION_SEND_MULTIPLE files, an explicit Send apps action and Retry / resume. All 60 local tests pass. On the owner's Android 15 phone, the update's exact signed bytes and version were checked, and the owner confirmed file-manager Share-menu import and installed-app selection. API 36 native checks separately cover temporary external URI read grants, warm multiple-file import, installed-APK export to a Windows peer and payload hashes. A two-file receive batch deliberately interrupted after 32 KB of its second file reused the first completed file, resumed the partial and produced two matching files without an extra copy. Physical two-phone interruption testing remains separate.

## Scope

This preview release renames the app, simplifies mobile sending/receiving, adds bounded thumbnails/details, and hardens Android hotspot/Wi-Fi routing and TLS setup. The package, app signing identity, pairing protocol and existing data paths are preserved.

## Required local checks

Build/package scripts record current results in `.local/` and `dist/VERSION.json`:

- Android JVM protocol, peer-state, connection-error and local-routing regression tests. Avatar coverage includes legacy saved profiles, Boy/Girl style bounds, optional nearby metadata, older-peer fallback, and saving/reloading the selected look after randomization.
- Release lint and optimized APK assembly.
- APK signature, package/version/label and previous-release signer comparison.
- 18 Windows loopback transfer/installer checks.
- Five Windows network/preparation checks.
- Seven actual Windows selection/Advanced-control checks and decoding of the displayed QR.

These checks do not establish physical phone-to-phone compatibility or measured throughput. See the packaged VERSION.json for exact counts and completed checks.

## Physical testing to complete

Version 1.0.3 was installed as an in-place update from 1.0.1 on an Android 15 phone. Native launch and the visible Send screen passed, but a subsequent PC-to-phone secure-listener check reproduced an AndroidKeyStore `INCOMPATIBLE_DIGEST` failure before pairing. This is a confirmed Android hosting defect, independent of Wi-Fi discovery or incoming file approval.

Version 1.0.4 generates a separate, versioned TLS identity with the digest/padding authorizations required by Conscrypt, and explicitly selects that identity even while the old entry remains in AndroidKeyStore. The app installation signer and package are unchanged. The existing download/history paths remain. Fresh pairing codes carry the new TLS certificate fingerprint.

The regression tests cover socket and engine alias selection with an old identity present, rejection of stale/unrelated aliases and absence of client authentication. Real-phone verification passed on the updated Android 15 device using both default TLS signature negotiation and RSA PKCS1/SHA256. Each check verified the exact installed APK bytes, migration to the new certificate, a TLS 1.2/ECDHE-RSA connection, the QR fingerprint, and rejection of an invalid pairing code. No files were sent or created. Two-phone Wi-Fi/hotspot file-transfer testing remains separate.

The owner reports earlier phone-to-PC transfers and offline installer sharing work. The earlier phone-to-phone test failed on both Wi-Fi and hotspot; its exact failure stage has not yet been established.

Install the current signed candidate on **both** phones as an update. Keep the older APK if needed, but Android may prevent downgrading without uninstalling. Uninstalling removes app data, so do not uninstall just to roll back. The 1.1.0 candidate also needs the nearby-receiver and approval checks in `PLAY-RELEASE.md`.

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

TLS key authorizations: [KeyGenParameterSpec.Builder](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.Builder). Conscrypt signs a digest/padded TLS message prepared by its TLS implementation; allowing that raw private-key operation does not remove TLS encryption or the QR certificate pin check.

Network-request permission: [ConnectivityManager.requestNetwork](https://developer.android.com/reference/android/net/ConnectivityManager#requestNetwork(android.net.NetworkRequest,%20android.net.ConnectivityManager.NetworkCallback,%20android.os.Handler,%20int)).

## Android 1.1.0 emulator lab

Two independent Android 16/API 36 AOSP virtual phones were installed with the signed 1.1.0 APK and connected through Android Emulator's shared virtual Wi-Fi and SoftAP networking. Nearby confirmation, Wi-Fi/hotspot payload hashes, discovery restart and native split-app installation were repeated for the final Boy/Girl release APK. Its local tests also cover avatar saving and older-peer fallback. The isolated tools, virtual disks, disposable split-app fixture, UI automation and private results are under `.local/android-lab/`; no existing Docker environment is used. Stop the virtual devices when the run ends.

The lab exercises actual Android framework services, application UIDs, AndroidKeyStore/TLS, NSD, Wi-Fi joining prompts, MediaStore downloads and PackageInstaller. Coverage includes receiver-name/avatar discovery, connection-code confirmation, withholding file details before sender approval, router Wi-Fi and app-created hotspot payloads, multi-file/empty-file/Unicode-content hashes, normal APK updates with retained history/identity, split-app export/installation, a 128 MiB transfer with the receiver on Home, background-idle shutdown and the native notification Stop action. Receiver discovery must also restart when Choose receiver is tapped after stopping, while keeping the selection.

Simulator testing exposed a dense QR rendering issue. QR images now use an integer number of display pixels per module, nearest-neighbour rendering and medium error correction; the encoder avoids masks the bundled reader cannot locate. A deterministic regression covers twelve pairing payloads at three small-phone sizes. This also avoids retaining an 800×800 intermediate bitmap for every code.

Emulator results do not establish physical radio range, throughput, thermal/battery behaviour, manufacturer power-management compatibility, Bluetooth operation or camera optics. The real-device matrix above remains the release gate for those behaviours. Official networking support: [interconnecting emulators](https://developer.android.com/studio/run/emulator-networking-interconnect).

## Android 1.2.0 avatar and journey candidate

The 72-test full build passes, including increasing XP costs and exact level boundaries, durable digest receipts, free style choices, locked-character gating, legacy avatar/optional-field parsing, verified resume rewards, corrupt/cancelled payload non-rewards and profile autosave. The past-history check was subsequently strengthened to use real transfer-history storage and its six-test Journey suite reran successfully. UI previews are in the ignored .local/ui-1.2.0 directory; the README gallery is rendered from the actual procedural avatar code, with sample colours and levels.

The signed APK passed API 36 native transfer checks: external single/multiple URI read grants, installed-app export, exact payloads to an isolated Windows peer, expected sender XP, no repeat-content reward, progress persistence after process restart, interrupted receive non-rewards, completed-file reuse, a 32,768-byte partial offset and exact resumed payloads. Native profile changes survived Back and process restart, locked previews could not be equipped, and avatar tap interaction completed without a crash. These do not quantify physical animation performance or battery usage.

The connected Android 15 phone received the same signed APK in place; installed bytes, version 1.2.0/code 8, original first-install identity and launch were verified. No remote taps were used. Private reports are .local/verification-1.2.0.json, .local/android-lab/verification-1.2.0.json and .local/phone-1.2.0/verification.json. The lab is shut down, and its tools/data remain isolated under .local/android-lab. Public release publication and physical two-phone checks remain separate.

## Android 1.2.1 pixel-art candidate

All 79 tests passed for the final generated face/hair build, including an actual-rendered idle sequence with distinct frames and a frozen image after animation is disabled. Test frames and the resulting preview are under .local/ui-1.2.1; the preview uses example character/level values and is not a record of earned owner progress. Resource checks cover all fourteen equipment bodies, a clear head socket, all expressions and hair layers, independent recolouring and strict optional silver-tone parsing with legacy fallback. Release lint: 0 errors, 48 warnings.

Two native API36 Android virtual phones verified a 1.2.1 receiver with a 1.2.0 sender, including silver-haired receiver discovery, matching connection codes, withholding filenames until sender confirmation, both approvals and a newly received exact-hash payload. XP and original installation identity survived upgrade, and repeat-content/preview actions left XP unchanged. Native profile autosave, locked preview and cheer were exercised. Private report: .local/android-lab/verification-1.2.1.json.

The connected Android15 phone was updated in place to the same APK, exact installed bytes/version/first-install identity were verified, and launch succeeded without remote taps. Private report: .local/phone-1.2.1/verification.json. The lab was shut down afterward; this does not establish physical radio, thermal/battery or owner art/feel approval. No source push, public release, website download-pointer or Windows binary change was made.

## Android 1.2.2 progression balance

The final build passes 84 tests, including the novice-to-100 roadmap, no ornate forms before mastery levels, strict future reward size floor/cap, preserved old XP/receipts and a larger positive-XP resume fixture. Old character-unlock thresholds and level costs are unchanged. Rendering fixtures use example levels and do not award player XP.

Native API36 upgrade retained 65 XP/level2 and installation identity. The real receiver verified a 32 MiB file for 50 XP, a 64 KiB file for zero, and a resumed 1 MiB+19-byte file for 4 XP only after verification. It resumed at byte19001, reused the completed large/small files, saved an empty file with zero XP and ended at119 XP. All payload hashes matched with exactly four output files. A repeat completed receive added no XP and did not show a stale bonus; future-gear previews also awarded none. Reports: .local/android-lab/verification-1.2.2.json and .local/verification-1.2.2.json. Lint:0errors/48warnings. The lab was shut down afterward.

## Windows 1.2.2 avatar and XP candidate

The Windows candidate passes 11 core/render/persistence checks, 23 loopback protocol checks, 10 actual WinForms UI checks and the rendered receive-QR decode. Run build-windows.ps1, check-windows.ps1, node tests/windows-integration.mjs and check-qr.ps1. Reports remain under ignored .local/. New tests cover Android-equivalent XP/gear gates, durable content receipts, restart persistence, torn-tail recovery, damaged-ledger preservation, free Girl/silver appearance autosave, locked previews, authored blink/idle pixels, resumed receive credit, sender acknowledgement failure and repeated-content zero rewards. Physical Android-to-Windows radio performance and owner visual/feel approval remain separate. The old 1.1.0 packaging baseline intentionally rejects this Windows executable until release preparation is updated and approved.

## Avatar attachment and motion revision 1.2.3

88 Android tests pass with no failures/errors; lint has no errors and 48 warnings. The Windows candidate passes 14 core/render/store/motion checks, 23 loopback protocol checks, 10 actual WinForms UI checks and QR decode. New tests cover all eleven class/Look neck contacts and planted boots through discrete poses, old XP/receipt retention, the four new classes' separate starter/mastery gates, Focused blink/glance cels, frozen Android animation and locked-silhouette skin independence. Legacy discovery keeps a bounded role0-6 field and adds optional hero/mood keys for new cosmetics. tests/AvatarMotionReview.cs renders the lineup and full 80-frame 9.6-second motion review; these are example profiles, with no real XP or unlock changes. Capturing the real PC idle additionally needs that app focused, because inactive-window motion is intentionally paused.


## Fixed signature characters 1.2.4

92 Android tests pass, including fixed-image invariance to personal style, protected saved appearance while equipped, original-style restoration, hidden signature controls and device-name edits. Lint has zero errors and 48 warnings. Windows passes 15 core/render/persistence cases, 23 protocol cases, 10 existing UI cases and four new fixed-character UI/save/restart cases, plus actual rendered QR decoding. Use check-special-characters.ps1 after build-windows.ps1. Its isolated old-progress fixture and screenshots never touch the real installation's ledger. Phone hardware installation was not performed. The owner deferred the live focused idle check; do not restart it without their return to that check.

## Card Duelist 1.2.5

The final Android build passes 95 tests with zero failures/errors; lint has zero errors and 48 warnings. New checks cover Level50/95 gates without changing prior unlocks, eight distinct reachable poses per costume, fixed-image invariance and personal-style preservation for role11, and backward-readable guest metadata in discovery and nearby hello. Windows passes 16 core/render/persistence, 23 loopback transfer, 10 UI and six signature UI/save/restart checks, plus QR decode. The two added signature checks equip Yugi through the real picker and restart the store using isolated labelled Level50 receipts. Neither real progress nor unlocks are changed by tests. tests/AvatarMotionReview.cs exports the five-character lineup, full 80-frame loop and separate two-outfit Yugi loop from the app renderer. No phone was connected; physical transfer/idle checks were not repeated.

## One-character sheets and favourite collection 1.2.6

99 Android tests pass; zero failures/errors and lint0errors/48warnings. SignatureCollectionTest checks appended IDs/gates, separate starter/mastery outfits, twelve full cels per outfit with hard alpha and distinct identities, reachable eleven-frame idle and tap-only reaction, recovery ordering and legacy-compatible optional collect metadata. Existing profile and render tests cover all sixteen fixed signatures. The gallery shows personal Boy/Girl variants and signature starter/mastery outfits without claiming that fixed characters are customizable.

Windows passes18core/render/persistence,23loopback,10UI and8signatureUI checks plus QRdecode. Added checks exercise every new family's artwork/gates/poses and all twelve through the actual picker in isolated labelled Level100 data, including restart and restoration to a personal avatar. Real XP/receipts are untouched. SignatureCollectionReview.cs exports the complete outfit gallery and 80-frame Luffy/Madara loops from the actual renderer. Native phone transfers and focused-window idle review were not repeated. Reports: .local/verification-1.2.6.json, .local/android-build-1.2.6-final.log and .local/pc-install-verification.json.

## Windows1.2.7 studio redesign

68 checks pass:18core,23loopback transfer,10UI,8signatureUI/save/restart and9studio. The studio suite checks48 complete default appearance cels, independent colours including both hands,18fixed portrait invariance, separate fantasy mastery artwork, search/filter/locked previews, custom-scrollbar wheel/drag/keyboard/limits, default+minimum layout and autosave restart. QR decoding passes at default/minimum outer sizes and with expanded networks plus24 Unicode characters in a device name. Tests use isolated profiles only. Phone runtime/source/build/APK and Windows TransferCore hashes are compared against the pre-redesign baseline. No native phone, focused-window idle or high-DPI hardware claim is made by these off-screen checks. Local reports: .local/studio-checks.json, .local/phone-unchanged-1.2.7.json and .local/verification-1.2.7.json.


## Windows1.2.8 portrait clarity

74Windows checks pass: the existing68 plus6portrait-sharpness checks. Run scripts/check-portrait-sharpness.ps1 after building Windows. It compares all18fixed portraits with the embedded256-pixel cells at native/larger bounds (allowing GDI alpha rounding), verifies exact2x2Pixel blocks on personal/fixed portraits, checks integer-only default breathing, exercises cache eviction, checks default/minimum selector layout and restarts HD/Pixel preferences without XP/equipment changes. Proof .local/portrait-checks.json; source/native preview comparison assets/windows-portrait-comparison-1.2.8.png. Existing QR/transfer/resume and appearance suites pass. No Android rebuild or physical phone/focused-window idle/high-DPI hardware claim is included.


## Windows1.2.9 Madara cinematic

80Windows checks pass: existing74plus6cinematic cases. check-madara-cinematic.ps1 validates64reachable cels, bounded9.4second playback phases, rest/end state, motion/accessibility/background guards, alpha art, distinct native HD/Pixel scenes and fixed appearance, actual locked preview/close and Off persistence with noXP/equip change. It exports95native review frames; encode-madara-review.ps1 checks9500ms GIF timing and looping. Focused timer/replay was not exercised because the offscreen fixture did not become active; no focus was forced, and the owner's deferred live animation review remains deferred. Existing transfer/resume/QR suites pass. Phone/source/APK/TransferCore baseline comparisons remain required. Reports .local/madara-checks.json, .local/madara-gif-check.json and .local/verification-1.2.9.json.


## Windows 1.2.10 in-card skills

86 checks pass: the existing 80, with popup UI cases updated for in-card Off/Auto behavior, plus six inline-skill cases. These verify monotonic active-time/paused/finished/replay state, the Level 96 Auto gate and legacy profile defaults, actual same-slot activation without another Form, portrait clicks and stop, character switching, default/minimum controls, persistence and a labelled isolated fixture crossing Level 95 to 96 with one verified XP award. HD/Pixel changes do not restart playback; routine XP refreshes do not auto-replay. Native screenshots/GIF use deterministic fixture time and no forced focus. Owner data and deferred live visual review remain separate. Reports: .local/inline-skill-checks.json, .local/inline-gif-check.json and .local/verification-1.2.10.json.


## Windows 1.2.11 Sukuna, faster timing and resource allowlist

92 Windows checks pass: the prior 86 plus six Sukuna cases. Existing Madara and inline tests now verify 5.2-second playback; the domain suite verifies all 64 cels and its 4.8-second duration, alpha/native resolution, fixed style, distinct scene phases, actual no-popup controls, size bounds, switching characters, Off and the original Level 88 Auto gate using an isolated 87-to-88 one-XP fixture. Reflection confirms exactly 43 embedded production images and no source/review files. Exported GIF timing is independently decoded at 50 ms per frame. Source/runtime PNGs, transfer core and phone hashes are compared before delivery. Runtime cleanup uses a full installation backup, a reviewed path plan, post-install launch/listener/APK checks and unchanged owner profile/XP/identity/history. No focus or physical-radio test is inferred from simulated playback captures.


## Windows 1.2.12 Zoro sword skill

98 Windows checks pass: prior 92 plus six Zoro cases. Tests cover 96 reachable cels, no zoom/face cut-in, three cut impacts and 3.6-second end state; complete silhouettes/native resolution without clipped blade bands; 47 actual embedded images with no source/review art; deterministic HD/Pixel phase/fixed-style comparisons; screen-cut clipping; real same-card controls, stop/switch/default/minimum layout; isolated Level 27-to-28 Auto and persistence; motion Off and Zoro-only 25 ms timing. A single near-black border antialias sample is tolerated after targeted source-tip cleanup. Other skill timers remain 33 ms. Review timing is independently decoded at 181 frames / 3620 ms. Live focused visual, phone/radio and high-DPI hardware review remains separate; fixture data never edits owner progress.


## Windows 1.2.13 Zoro mastery accuracy and instant finisher

98 Windows checks pass. Revised Zoro cases reach all 96 cels in 2.8 seconds, enforce no zoom and 20ms cut onset/stronger final split, require zero opaque contacts on body-cell edges, check HD/Pixel phase rendering and preview clipping, and exercise the actual Mastery-only controls with Starter rejection. An isolated 81-to-82 verified one-XP fixture verifies the original mastery Auto gate, repeat prevention and saved Auto; starter Level 28 is unchanged. Review GIF independently decodes to 141 frames at 20ms, 2820ms including the resting endpoint. Weapon identity/count and mouth-hilt placement were visually inspected; automated tests do not recognize semantic sword count. Other suites cover transfer/resume, XP/hash/receipts, persistence, QR, layouts and previous skills. Live owner focus, physical phone/radio and high-DPI hardware remain separate.
