# Share Mode for Windows 1.2.2 — Your adventurer

Local Windows candidate. The PC application now includes the pixel adventurer and cosmetic progression previously available only on Android. This is an application update, separate from the earlier refresh of the bundled phone installer. GitHub and website downloads have not been published with this candidate.

## What changed

- A sidebar avatar card shows your name, level, rank and segmented XP bar. Click **Your adventurer** to open **Style**, **Characters** or **Ranks**.
- Boy/Girl looks, six hairstyles, silver and other hair colours, skin/outfit colours, expressions, elements and nearby name save automatically. **Done** closes the editor. **Surprise me** keeps the chosen Boy/Girl look and equipped character.
- The approved phone equipment, face and hair artwork is bundled at game resolution. No emulator, account, new runtime, avatar service or game engine is required. The executable is about 325 KB; QRCoder.dll remains its existing dependency.
- Visible idle heroes breathe and blink, and the large editor avatar cheers when clicked. Timers stop for hidden/inactive screens, disabled animation, high contrast, disabled Windows menu animations and active file sharing. Collection/gear previews remain static.
- The seven classes, rank frames, novice gear and Level 100 ultimate set follow the Android 1.2.2 unlocks. Previewing a locked character or future outfit does not equip it or award XP.

## Earned progress

Each new verified file earns 1 XP per complete 256 KiB, capped at 50 XP. Sending earns credit only after receiver acknowledgement; receiving earns credit after the hash matches and the file is saved. Empty, corrupt, failed, interrupted and declined transfers do not earn XP. A resumed file earns its credit after verified completion. Renamed files, repeat transfers and the same content sent back cannot grant another reward on that PC. Starting a new batch clears the previous batch's XP summary.

The level costs match Android: Level 100 costs 3,302,937 XP. Plain unarmed clothes start at 1; training gear at 10; iron at 25; ornate Courier/Mage/Ranger at 45; Shadow at 60; Demon at 75; winged Seraph at 85; Herald at 95; ultimate Sovereign at 100. All sharing features remain available at every level.

PC progress starts at Level 1. It is local to this Windows user and does not synchronize with a phone. Existing transfer history is not converted to XP. Appearance and a flushed, checksummed digest/size receipt ledger live under the existing `%LOCALAPPDATA%\Share-Me-Rowell` folder. Receipts contain no filenames, paths or peer identities. A torn final write is preserved before recovery; a damaged complete receipt disables new rewards and keeps the original ledger. Cosmetic storage failure does not fail a verified transfer.

## Compatibility and validation

The existing Windows TLS identity, QR scheme, v1 transfer messages, approvals, receive directory and history remain in place. Nearby receiver discovery remains an Android feature; the phone scans the Windows QR in both transfer directions. The Windows **Get phone app** action includes the exact signed Android 1.2.2 candidate.

Validation: 11 Windows core/render/persistence checks, 23 loopback protocol/integration checks, 10 actual WinForms UI checks and successful decoding of the rendered receive QR. Checks include sender verification failure, resumed rewards, repeated-file rewards, restart persistence, receipt recovery and damage, locked previews and authored face/idle differences. These are local checks, not a physical phone-to-PC radio or user visual/feel approval.
