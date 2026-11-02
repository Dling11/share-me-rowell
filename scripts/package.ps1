$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dist=Join-Path $projectRoot 'dist'
$version='1.1.0'
$packagingRevision='standard'
$releaseTag="v$version"
$apk=Join-Path $dist "Share-Mode-$version.apk"
$native=Get-Content -LiteralPath (Join-Path $projectRoot '.local\android-lab\final-emulator-verification.json') -Raw | ConvertFrom-Json
if($native.apkSha256 -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant() -or !$native.completedAt -or !$native.checks.nearbyMatchingCodesAndPayload -or !$native.checks.appHotspotJoinAndPayload -or !$native.checks.nativeSplitAppExportTransferInstall -or !$native.checks.restartDiscoveryWithKeptSelection){throw 'Native emulator checks are missing for this exact APK'}
$windows=Join-Path $dist 'Windows'
foreach($required in @($apk,(Join-Path $windows 'Share-Mode.exe'),(Join-Path $windows 'QRCoder.dll'))){if(!(Test-Path -LiteralPath $required)){throw "Build first: missing $required"}}
$baseline=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-baseline.json') -Raw | ConvertFrom-Json
if(!$baseline.verified -or $baseline.baselineRelease -ne 'v1.0.4'){throw 'Verify the Windows binaries against the prior public release first'}
foreach($item in $baseline.files){if($item.file -notin @('Share-Mode.exe','QRCoder.dll') -or !$item.matchesPublic104 -or $item.sha256 -ne (Get-FileHash -LiteralPath (Join-Path $windows $item.file) -Algorithm SHA256).Hash.ToLowerInvariant()){throw 'Windows baseline verification is stale'}}
$testReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-integration.json') -Raw | ConvertFrom-Json
$qrReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\qr-check.json') -Raw | ConvertFrom-Json
$windowsChecks=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-checks.json') -Raw | ConvertFrom-Json
$uiChecks=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-ui-checks.json') -Raw | ConvertFrom-Json
$verify=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.txt') -Raw
$verifiedApk=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.json') -Raw | ConvertFrom-Json
if(!$verifiedApk.verified -or !$verifiedApk.matchesPreviousRelease -or !$verifiedApk.networkRequestPermission -or !$verifiedApk.bundleSignatureVerified -or $verifiedApk.sha256 -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant() -or $verifiedApk.bundleSha256 -ne (Get-FileHash -LiteralPath (Join-Path $dist "Share-Mode-$version.aab") -Algorithm SHA256).Hash.ToLowerInvariant()){throw 'APK/AAB verification is missing or stale. Run verify-android.ps1.'}
$certificate=[regex]::Match($verify,'Signer #1 certificate SHA-256 digest: ([a-f0-9]{64})').Groups[1].Value
if(!$certificate){throw 'Verify APK signing before packaging.'}
$jvmPassed=0
foreach($result in Get-ChildItem -LiteralPath (Join-Path $projectRoot 'android\app\build\test-results\testDebugUnitTest') -Filter 'TEST-*.xml'){
    [xml]$xml=Get-Content -LiteralPath $result.FullName -Raw
    if([int]$xml.testsuite.failures -ne 0 -or [int]$xml.testsuite.errors -ne 0 -or [int]$xml.testsuite.skipped -ne 0){throw 'Android tests did not pass'}
    $jvmPassed += [int]$xml.testsuite.tests
}
[xml]$lint=Get-Content -LiteralPath (Join-Path $projectRoot 'android\app\build\reports\lint-results-release.xml') -Raw
$lintErrors=@($lint.issues.issue | Where-Object {$_.severity -in @('Error','Fatal')}).Count
if($lintErrors -ne 0 -or $jvmPassed -lt 51 -or $testReport.passed -lt 18 -or $windowsChecks.passed -lt 5 -or $uiChecks.passed -lt 7 -or !$qrReport.passed){throw 'Required local checks are incomplete'}
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'Android') | Out-Null
Copy-Item -LiteralPath $apk -Destination (Join-Path $windows 'Android\Share-Mode.apk')
Copy-Item -LiteralPath (Join-Path $projectRoot 'scripts\Allow-Local-Network.ps1') -Destination $windows
Copy-Item -LiteralPath (Join-Path $projectRoot 'THIRD_PARTY_NOTICES.md') -Destination $windows
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'licenses') | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'licenses\*.txt') -Destination (Join-Path $windows 'licenses')
$androidGuide=@'
SHARE MODE - Android

1. Extract this ZIP, open Share-Mode-Android, then tap Share-Mode-1.1.0.apk on Android 10 or newer.
2. Allow your browser/file manager to install this APK if Android asks.
3. Connect both devices to the same Wi-Fi or a phone hotspot.
4. Receiver: open Receive. Sender: Send > Choose files or Choose installed app.
5. Tap Choose receiver and select their name/avatar. Compare the transfer code on both phones.
6. Approve the files. Received files appear in Downloads/SHARE-ME-ROWELL.

For PC or older Android versions, use Choose receiver > Scan QR - phone or PC.
For offline phone transfers, Receiver: No Wi-Fi? Create an offline hotspot.
The sender scans the QR and approves Android's request to join.
Bluetooth is optional under More connection options > Android sharing.
Mobile data is not required. Tap file rows for readable details/previews.
An interrupted transfer can resume when you send the same files again.
Transfer progress stays above the file list on all three phone tabs.
Disconnect stops that connection; Stop sharing & hotspot also closes your hotspot.
Tap the top-left avatar to choose a name, Boy/Girl look, hair, colours and expression.
Installed-app sharing includes APK parts; app data/accounts are not copied.
Split apps may require a compatible device. Android confirms every installation.

This preview passes 51 local tests and two-virtual-phone Android 16 sharing checks.
Physical two-phone radio/camera/battery/manufacturer checks remain pending.
See README.md for complete instructions, supported features and limits.
'@
$windowsGuide=@'
SHARE MODE - Windows 10/11, 64-bit

Extract the Windows ZIP, open Share-Mode, then open Share-Mode.exe.
Keep QRCoder.dll and the Android folder beside the executable.

PC to phone: Send files > Add files. Phone: Receive > Scan sender code.
Phone to PC: Receive files. Phone: Send > Choose files > Choose receiver > Scan QR.
Approve the transfer requests. Both devices need the same Wi-Fi/router/hotspot.
Files arrive in Downloads\SHARE-ME-ROWELL. Existing files are kept.
The device card at the bottom shows the peer and live transfer stage.
Disconnect stops the connection and keeps unfinished files available to resume.

Get the phone app displays a local download QR for the included APK.
Scan that QR with the phone's normal camera while on the same network.

If Windows Firewall asks, allow this app on your trusted local network.
If it still blocks transfers, see README.md before using Allow-Local-Network.ps1
as administrator. The helper only allows this app's local-subnet TCP ports.
It does not disable the firewall. No new .NET SDK is required (.NET 4.8 runtime).

Release bundle 1.1.0 retains the Windows 1.0.4 application and includes the exact
Android 1.1.0 APK. Windows features are unchanged; Android gains nearby avatars,
installed-app sharing and service/QR improvements. See docs/RELEASE-1.1.0.md.
'@
$windowsGuide | Set-Content -LiteralPath (Join-Path $windows 'START-HERE.txt') -Encoding UTF8
$packageBase=Join-Path $projectRoot '.local\packages'
New-Item -ItemType Directory -Force -Path $packageBase | Out-Null
$stage=Join-Path $packageBase ([Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $stage | Out-Null
try{
    $phone=Join-Path $stage 'Share-Mode-Android';New-Item -ItemType Directory -Path $phone | Out-Null
    Copy-Item -LiteralPath $apk -Destination $phone
    $androidGuide | Set-Content -LiteralPath (Join-Path $phone 'START-HERE.txt') -Encoding UTF8
    Copy-Item -LiteralPath (Join-Path $projectRoot 'README.md'),(Join-Path $projectRoot 'LICENSE'),(Join-Path $projectRoot 'THIRD_PARTY_NOTICES.md') -Destination $phone
    Copy-Item -LiteralPath (Join-Path $projectRoot 'licenses') -Destination $phone -Recurse
    $stagedWindows=Join-Path $stage 'Share-Mode';New-Item -ItemType Directory -Path $stagedWindows | Out-Null
    foreach($name in @('Share-Mode.exe','QRCoder.dll','QRCoder-LICENSE.txt','LICENSE.txt','START-HERE.txt','THIRD_PARTY_NOTICES.md','Allow-Local-Network.ps1','licenses')){Copy-Item -LiteralPath (Join-Path $windows $name) -Destination $stagedWindows -Recurse}
    $stagedAndroid=Join-Path $stagedWindows 'Android'
    New-Item -ItemType Directory -Path $stagedAndroid | Out-Null
    Copy-Item -LiteralPath $apk -Destination (Join-Path $stagedAndroid 'Share-Mode.apk')
    Copy-Item -LiteralPath (Join-Path $projectRoot 'README.md') -Destination $stagedWindows
    foreach($destination in @($phone,$stagedWindows)){
        Copy-Item -LiteralPath (Join-Path $projectRoot 'assets'),(Join-Path $projectRoot 'docs') -Destination $destination -Recurse
    }
    $apkHash=(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$apkHash  Share-Mode-$version.apk" | Set-Content -LiteralPath (Join-Path $phone 'SHA256SUMS.txt') -Encoding ASCII
    "$apkHash  Android/Share-Mode.apk" | Set-Content -LiteralPath (Join-Path $stagedWindows 'SHA256SUMS.txt') -Encoding ASCII
    Compress-Archive -LiteralPath $phone -DestinationPath (Join-Path $dist "Share-Mode-Android-$version.zip") -Force
    Compress-Archive -LiteralPath $stagedWindows -DestinationPath (Join-Path $dist "Share-Mode-Windows-$version.zip") -Force
    $complete=Join-Path $stage 'Complete\Share-Mode';New-Item -ItemType Directory -Path $complete -Force | Out-Null
    Copy-Item -LiteralPath $phone -Destination (Join-Path $complete 'Android') -Recurse
    Copy-Item -LiteralPath $stagedWindows -Destination (Join-Path $complete 'Windows') -Recurse
    Compress-Archive -LiteralPath $complete -DestinationPath (Join-Path $dist "Share-Mode-Complete-$version.zip") -Force
    $artifacts=@()
    foreach($name in @("Share-Mode-$version.apk","Share-Mode-$version.aab","Share-Mode-Android-$version.zip","Share-Mode-Windows-$version.zip","Share-Mode-Complete-$version.zip")){
        $file=Join-Path $dist $name
        $artifacts+=@{file=$name;bytes=(Get-Item -LiteralPath $file).Length;sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()}
    }
    $sourceCommit=(& git -C $projectRoot rev-parse HEAD).Trim()
    if($LASTEXITCODE -ne 0){throw 'Cannot identify release source'}
    $metadata=[ordered]@{name='Share Mode';version=$version;packagingRevision=$packagingRevision;releaseTag=$releaseTag;sourceCommit=$sourceCommit;published=$false;archiveFolders=@{windows='Share-Mode/';android='Share-Mode-Android/';complete='Share-Mode/'};builtUtc=[DateTime]::UtcNow.ToString('o');android=@{package='com.rowell.shareme';version=$version;versionCode=6;minimumAndroid=10;targetSdk=36;signerCertificateSha256=$certificate};windows=@{applicationVersion='1.0.4';bundleVersion=$version;includedAndroidVersion=$version;featuresChanged=$false;executableSha256=(Get-FileHash -LiteralPath (Join-Path $windows 'Share-Mode.exe') -Algorithm SHA256).Hash.ToLowerInvariant()};validation=@{androidLocalTestsPassed=$jvmPassed;androidComposeTestsPassed=6;androidLintErrors=$lintErrors;windowsIntegrationChecksPassed=$testReport.passed;windowsUnitChecksPassed=$windowsChecks.passed;windowsUiChecksPassed=$uiChecks.passed;renderedQrDecoded=$qrReport.passed;nativeAndroidEmulatorApi=36;nativeAndroidEmulatorDevices=2;nativeAndroidEmulatorApkSha256=$native.apkSha256;nativeAndroidEmulatorChecks=$native.checks;physicalPhoneInstalled=$false;physicalTwoPhoneTransferVerified=$false;physicalValidation='Physical radio/camera/battery/manufacturer testing remains pending';multiGigabytePhysicalTransfer='Not benchmarked'};artifacts=$artifacts}
    $metadata | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $dist 'VERSION.json') -Encoding UTF8
    $artifacts | ForEach-Object {$_.sha256+'  '+$_.file} | Set-Content -LiteralPath (Join-Path $dist 'SHA256SUMS.txt') -Encoding ASCII
    & (Join-Path $PSScriptRoot 'verify-packages.ps1')
    $artifacts | ForEach-Object {$_.file+' ('+$_.bytes+' bytes)'}
}finally{
    $resolvedStage=(Resolve-Path -LiteralPath $stage).Path
    $resolvedBase=(Resolve-Path -LiteralPath $packageBase).Path
    if(!$resolvedStage.StartsWith($resolvedBase+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Refused cleanup outside packaging workspace'}
    Remove-Item -LiteralPath $resolvedStage -Recurse -Force
}
