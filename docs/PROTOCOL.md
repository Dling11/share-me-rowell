# Local transfer protocol, version 1

Normal transfers use TLS 1.2. Each receiver/sender host has a self-signed RSA certificate. Its SHA256 fingerprint is carried in the QR; the Android client validates that exact identity before sending the pairing secret or file details. Windows keeps its identity encrypted for the current Windows user using DPAPI. Android uses AndroidKeyStore. Pairing secrets are random 256-bit values and expire after ten minutes. Hosts accept private/loopback/link-local IPv4 peers and require batch approval. QR contents include sensitive pairing/hotspot details; they are not uploaded or added to transfer history.

The QR is `shareme-rowell://pair/<base64url-json>`. JSON fields: `v=1`, `hosts` (private IPv4 strings), `port`, `fp` (64 lower-case hex characters), `code`, `mode` (`send`/`receive`), `name`. App-created hotspots add `ssid` and `password`.

Control frames are UTF-8 JSON followed by LF, at most 65,536 bytes. No text reader may buffer past LF into a binary payload. File metadata: `id`, sanitized `name`, `size` (64-bit integer, 0 through 1 TB), `sha256`. A batch has 1–16 files with unique IDs. Paths, file URIs and local source filenames beyond the display name never travel in an offer.

Push: client sends `{v:1, op:"push", code, name, files:[...]}`. Receive host checks/approves, then responds `{ok:true, offsets:[...]}`. Pull: client sends `{v:1, op:"pull", code, name}` to a send host. After host approval, it responds `{ok:true, sender, files:[...]}`; the client receiver approves and responds with offsets.

The sender streams exactly `size-offset` raw bytes for each file in offer order. The receiver hashes the completed partial, publishes/renames only on a match, then sends `{ok:true,index:N}` before the next file. Errors use `{ok:false,error:"..."}` where a control response is expected. Socket EOF is an interrupted transfer, never completion. The sender waits for verification with a longer timeout. Resume offsets must be within 0..size; partials are keyed by expected hash and size. Repeated identical files in one batch resume only the first occurrence.

Android writes to pending MediaStore Downloads entries and clears `IS_PENDING` after successful verification. Windows stages in `.shareme-parts` and renames on success. Windows uses unique final filenames; Android relies on MediaStore's unique-name allocation. SHA256 is also checked for zero-length files. Receivers do not execute incoming files. Cancellation closes sockets and retains partials; signature verification and installation of APKs remain Android system actions.

The optional installer bootstrap is separate: a short-lived random local HTTP URL serves a small HTML page and the host's public Android APK. It cannot browse folders or serve user files. Regular transfer JSON and file payloads never use that HTTP channel.
