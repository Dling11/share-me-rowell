# Build assets and package contents

Keep the 47 production Windows PNGs, 23 selected Android/desktop source PNGs and ten selected skill-source PNGs required by the build tools. Full-resolution authoring sources stay outside runtime packages.

Android 1.2.8 includes 648 compiled WebP cels from 28 approved portrait/skill atlases. HD and Pixel share this one asset set. The build verifies its source hashes against the Windows artwork, and unused legacy pixel resources are stripped from the release APK. Source PNGs stay outside the APK; no full-resolution authoring atlases or unused draft generations are bundled.

Release ZIPs contain only the executable/APK, required DLL, installation guide, licenses and privacy/release notes. Keep repository screenshots, authoring sources, local test captures, caches and old archives outside runtime packages. The explicit image allowlist in build-windows.ps1 is authoritative for Windows.
