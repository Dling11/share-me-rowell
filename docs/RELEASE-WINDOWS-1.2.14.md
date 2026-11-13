# Share Mode for Windows 1.2.14

Transfers now show the sender's avatar on the left, progress in the middle and the receiver's avatar on the right. Tap either avatar to animate it inside its own frame. Supported mastery characters use their existing skill; other characters use their tap animation. Animation Off and foreground/accessibility settings apply to both.

Avatar details are optional cosmetic fields in the existing encrypted transfer handshake. Older versions still transfer normally and show a device placeholder. Peer appearance never equips a character, changes local progress or grants XP. The QR format, pairing checks, file verification and resume protocol are retained.

The active view expands only while connected; idle and completed layouts keep the original QR space at minimum window sizes. No new artwork, animation engine or DLL is added. Zoro's previously accepted idle-to-skill scale transition remains a known visual issue.

104 Windows checks pass, including actual left/right layouts, in-frame tapping, motion controls, older-peer fallback, transfer/resume, XP receipts and QR decoding. Tests use isolated profiles and loopback connections. Physical phone/radio, high-DPI hardware and the deferred focused owner animation review remain separate.
