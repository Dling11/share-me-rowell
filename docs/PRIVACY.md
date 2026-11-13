# Share Mode privacy policy

Last updated: November 7, 2026

Share Mode is a local file-transfer app for Android and Windows, developed by Rowell. It has no account system, advertising, analytics, or cloud file-storage service.

## Files and installed apps

Files you select are read on your device and sent to the device you choose. Incoming transfers require approval. Transfers use encrypted connections, and completed files are checked for integrity. Transfer history and unfinished downloads are stored on your device.

Choosing an installed app reads its installer APK and any installed APK parts. A temporary installer copy is kept in Android's app cache for sharing. App accounts, private application data and saves are not copied. Android checks received installers and asks for permission before installation.

## Nearby name and avatar

Your display name and avatar choices save automatically on your device. While Share Mode is visible on its Receive screen, it advertises your chosen name, avatar settings, your cosmetic level, a randomly generated app identifier, and public connection information to devices on the same local network. Other devices on that network may see this information. Use a nickname if you prefer.

Discovery does not advertise your selected filenames, QR pairing secret, hotspot password, phone number, contacts, or precise location. Names and avatars are not proof of identity; compare the connection-specific transfer code on both devices before sending or accepting a nearby transfer. QR pairing is also available.

Nearby searching and visibility stop when the app leaves the foreground. A transfer already in progress may continue with a visible notification. Idle sharing shuts down after two minutes in the background. The notification's Stop sharing action ends the service and releases an app-created hotspot.

## Cosmetic progress (Android 1.2.0)

Character and frame unlocks use an offline XP ledger on your device. A completed, verified new file can earn XP once. From 1.2.2, files earn 1 XP per complete 256 KiB, capped at 50 XP; smaller files still share normally without XP. Existing XP and level thresholds are retained. The private ledger retains a content SHA-256 digest and byte count to prevent repeat rewards, together with total XP, file count and bytes. It stores no filenames or peer identities. No past transfer history is imported. Your level may be shown to nearby devices with your avatar; your XP ledger is not shared. Clearing app storage or uninstalling removes progress. There is no account or cross-device progress synchronization.

Avatar animation runs only while the app is visible and idle, respects Android's animation setting and can be disabled in the profile editor.

## Android permissions

- Camera: scans a pairing QR when you choose that action.
- Nearby Wi-Fi: creates or joins an offline hotspot. Android 10–12 use Android's Location permission for these Wi-Fi operations; Share Mode does not derive or store your geographic location.
- Notifications and foreground service: show ongoing sharing and keep a transfer running while you switch apps.
- Install packages: opens Android's approval process when you explicitly choose to install a received app.
- Network access: discovers and connects to devices on your local network. App transfers do not require internet access.

Files are selected using Android's system file picker. Installed-app selection uses Android's launcher-app query; Share Mode does not request unrestricted access to every installed package.

## Controls and deletion

Change your nearby name and avatar using the avatar button. Use Stop sharing to end a session. You can remove unfinished downloads from the menu. Android's Clear storage or uninstall removes app preferences, app cache and transfer history. Received files in Downloads remain until you delete them yourself.

Opening an external support page or sharing files through Android's sharing screen passes control to the selected browser or app, whose privacy practices apply.

## Contact

Questions or deletion assistance: [Share Mode support](https://github.com/Dling11/share-me-rowell/issues). Do not include private files, pairing codes or passwords in a public issue.

Windows 1.2.2 local candidate adds matching on-device cosmetic profiles and progression. The existing Windows local app-data folder holds appearance settings and a flushed, checksummed ledger of content SHA-256, size and reward. It stores no filenames, paths or peer identities in that ledger. There is no account or phone/PC progress synchronization, and no telemetry or new network service was introduced for avatars. The existing local installer endpoint and transfer listener keep their original purposes.
