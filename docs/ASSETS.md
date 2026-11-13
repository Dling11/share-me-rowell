# Build assets and package contents

Keep the 47 production Windows PNGs, 23 selected Android/desktop source PNGs and ten selected skill-source PNGs required by the build tools. Full-resolution authoring sources stay outside runtime packages.

Release packages contain only the executable/APK, required DLL, installation guide, licenses and privacy/release notes. Never copy assets, authoring sources, local test captures, caches or old archives into a runtime package. The explicit image allowlist in build-windows.ps1 is authoritative.
