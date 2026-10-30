$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dist=Join-Path $projectRoot 'dist'
$version='1.0.4'
$packagingRevision='standard'
$releaseTag="v$version"
$apk=Join-Path $dist "Share-Mode-$version.apk"
if($version -eq '1.0.4'){
    $tlsProof=Get-Content -LiteralPath (Join-Path $projectRoot '.local\phone-verification\secure-listener-check.json') -Raw | ConvertFrom-Json
    if(!$tlsProof.passed -or !$tlsProof.fingerprintMatched -or !$tlsProof.invalidPairingRejected -or $tlsProof.phoneVersion -ne $version -or $tlsProof.apkSha256 -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()){throw 'Real-phone TLS regression check must pass for this exact APK before packaging 1.0.4'}
}
$windows=Join-Path $dist 'Windows'
foreach($required in @($apk,(Join-Path $windows 'Share-Mode.exe'),(Join-Path $windows 'QRCoder.dll'))){if(!(Test-Path -LiteralPath $required)){throw "Build first: missing $required"}}
$testReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-integration.json') -Raw | ConvertFrom-Json
$qrReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\qr-check.json') -Raw | ConvertFrom-Json
$windowsChecks=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-checks.json') -Raw | ConvertFrom-Json
$uiChecks=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-ui-checks.json') -Raw | ConvertFrom-Json
$verify=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.txt') -Raw
$verifiedApk=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.json') -Raw | ConvertFrom-Json
if(!$verifiedApk.verified -or !$verifiedApk.matchesPreviousRelease -or !$verifiedApk.networkRequestPermission -or $verifiedApk.sha256 -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()){throw 'APK verification is missing or stale. Run verify-android.ps1.'}
$certificate=[regex]::Match($verify,'Signer #1 certificate SHA-256 digest: ([a-f0-9]{64})').Groups[1].Value
if(!$certificate){throw 'Verify APK signing before packaging.'}
$jvmPassed=0
foreach($result in Get-ChildItem -LiteralPath (Join-Path $projectRoot 'android\app\build\test-results\testDebugUnitTest') -Filter 'TEST-*.xml'){
    [xml]$xml=Get-Content -LiteralPath $result.FullName -Raw
    if([int]$xml.testsuite.failures -ne 0 -or [int]$xml.testsuite.errors -ne 0){throw 'Android tests did not pass'}
    $jvmPassed += [int]$xml.testsuite.tests
}
[xml]$lint=Get-Content -LiteralPath (Join-Path $projectRoot 'android\app\build\reports\lint-results-release.xml') -Raw
$lintErrors=@($lint.issues.issue | Where-Object {$_.severity -in @('Error','Fatal')}).Count
if($lintErrors -ne 0 -or $jvmPassed -lt 31 -or $testReport.passed -lt 18 -or $windowsChecks.passed -lt 5 -or $uiChecks.passed -lt 7 -or !$qrReport.passed){throw 'Required local checks are incomplete'}
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'Android') | Out-Null
Copy-Item -LiteralPath $apk -Destination (Join-Path $windows 'Android\Share-Mode.apk')
Copy-Item -LiteralPath (Join-Path $projectRoot 'scripts\Allow-Local-Network.ps1') -Destination $windows
Copy-Item -LiteralPath (Join-Path $projectRoot 'THIRD_PARTY_NOTICES.md') -Destination $windows
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'licenses') | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'licenses\*.txt') -Destination (Join-Path $windows 'licenses')
$androidGuide=@'
SHARE MODE - Android

1. Extract this ZIP, open Share-Mode-Android, then tap Share-Mode-1.0.4.apk on Android 10 or newer.
2. Allow your browser/file manager to install this APK if Android asks.
3. Connect both devices to the same Wi-Fi or a phone hotspot.
4. Sender: Send > Choose files. Select one or several; review names/sizes.
Receiver: Receive shows a QR. Sender: Scan receiver to send.
5. The sender scans the receiver code. For PC transfers the phone scans the PC.
6. Approve the files. Received files appear in Downloads/SHARE-ME-ROWELL.

For PC transfers, run the Windows companion and scan its Send/Receive code.
For offline phone transfers, Receiver: No Wi-Fi? Create a hotspot.
The sender scans the QR and approves Android's request to join.
Bluetooth is optional under More connection options > Android sharing.
Mobile data is not required. Tap file rows for readable details/previews.
An interrupted transfer can resume when you send the same files again.
Transfer progress stays above the file list on all three phone tabs.
Disconnect stops that connection; Stop sharing & hotspot also closes your hotspot.

This release passed local protocol/build checks and an Android 15 update/launch
check. Two-phone Wi-Fi/hotspot, Bluetooth, background and speed tests remain pending.
See README.md for complete instructions, supported features and limits.
'@
$windowsGuide=@'
SHARE MODE - Windows 10/11, 64-bit

Extract the Windows ZIP, open Share-Mode, then open Share-Mode.exe.
Keep QRCoder.dll and the Android folder beside the executable.

PC to phone: Send files > Add files. Phone: Receive > Scan sender code.
Phone to PC: Receive files. Phone: Send > Choose files > Scan receiver to send.
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

Local transfer tests and Android update/launch passed. Two-phone testing is pending.
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
    foreach($name in @("Share-Mode-$version.apk","Share-Mode-Android-$version.zip","Share-Mode-Windows-$version.zip","Share-Mode-Complete-$version.zip")){
        $file=Join-Path $dist $name
        $artifacts+=@{file=$name;bytes=(Get-Item -LiteralPath $file).Length;sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()}
    }
    $phoneVerified=$false
    $phoneTests='Two-phone Wi-Fi/hotspot and mobile UI tests pending'
    $phoneProofPath=Join-Path $projectRoot '.local\device-check.json'
    if(Test-Path -LiteralPath $phoneProofPath){
        $phoneProof=Get-Content -LiteralPath $phoneProofPath -Raw | ConvertFrom-Json
        $phoneVerified=$phoneProof.version -eq $version -and $phoneProof.apkSha256 -eq $apkHash -and $phoneProof.updateInstalled -and $phoneProof.nativeLaunchVerified -and $phoneProof.sendScreenVerified
        if($phoneVerified){$phoneTests='Android 15 update, native launch, Send screen and secure listener verified; full two-phone Wi-Fi/hotspot file transfers pending'}
    }
    $metadata=[ordered]@{name='Share Mode';version=$version;packagingRevision=$packagingRevision;releaseTag=$releaseTag;published=$false;archiveFolders=@{windows='Share-Mode/';android='Share-Mode-Android/';complete='Share-Mode/'};builtUtc=[DateTime]::UtcNow.ToString('o');android=@{package='com.rowell.shareme';minimumAndroid=10;targetSdk=35;signerCertificateSha256=$certificate};validation=@{androidJvmTestsPassed=$jvmPassed;androidLintErrors=$lintErrors;windowsIntegrationChecksPassed=$testReport.passed;windowsUnitChecksPassed=$windowsChecks.passed;windowsUiChecksPassed=$uiChecks.passed;renderedQrDecoded=$qrReport.passed;installedOnPhone=$phoneVerified;nativePhoneLaunchVerified=$phoneVerified;physicalPhoneTlsListenerVerified=[bool]$tlsProof.passed;phoneTests=$phoneTests;multiGigabytePhysicalTransfer='Not benchmarked; 64-bit metadata and streaming tested locally'};artifacts=$artifacts}
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
