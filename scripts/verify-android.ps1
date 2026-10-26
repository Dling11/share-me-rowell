param([string]$DockerImage='monster-hunter-tri-android:rowell')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$apk=Join-Path $projectRoot 'dist\Share-Me-Rowell-1.0.0.apk'
$output=& docker run --rm --cap-drop ALL --security-opt no-new-privileges --memory 512m --cpus 2 --mount "type=bind,source=$projectRoot,target=/workspace,readonly" --entrypoint /bin/sh $DockerImage -c '/opt/android-sdk/build-tools/35.0.0/apksigner verify --verbose --print-certs /workspace/dist/Share-Me-Rowell-1.0.0.apk && /opt/android-sdk/build-tools/35.0.0/aapt dump badging /workspace/dist/Share-Me-Rowell-1.0.0.apk'
if($LASTEXITCODE -ne 0){throw 'Android signature/package verification failed'}
$summary=@($output | Where-Object {$_ -match '^Verifies|^Verified|^Signer #1 certificate SHA-256|^package:|^sdkVersion:|^targetSdkVersion:|^application-label:|^application-icon:'})
$text=$summary -join [Environment]::NewLine
if($text -notmatch "name='com.rowell.shareme'" -or $text -notmatch "versionName='1.0.0'" -or $text -notmatch "sdkVersion:'29'" -or $text -notmatch "targetSdkVersion:'35'"){throw 'Unexpected Android package metadata'}
$certificate=[regex]::Match($text,'Signer #1 certificate SHA-256 digest: ([a-f0-9]{64})').Groups[1].Value
if(!$certificate){throw 'Signing certificate digest not found'}
$summary | Set-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.txt') -Encoding UTF8
@{sha256=(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant();certificateSha256=$certificate;package='com.rowell.shareme';verified=$true} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.json') -Encoding UTF8
$summary
