#!/bin/sh
set -eu
cd /workspace
mkdir -p .local/signing
if [ ! -f .local/signing/shareme.jks ]; then
  python3 - <<'PY'
import pathlib, secrets
p = pathlib.Path('.local/signing/password.txt')
if not p.exists():
    p.write_text(secrets.token_hex(32))
    p.chmod(0o600)
PY
  keytool -genkeypair -noprompt -keystore .local/signing/shareme.jks -alias shareme \
    -storepass:file .local/signing/password.txt -keypass:file .local/signing/password.txt \
    -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Share-Me-Rowell'
fi
cd android
/opt/rowell-gradle/bin/gradle --no-daemon --max-workers=3 --console=plain testDebugUnitTest assembleRelease lintRelease
