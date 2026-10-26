$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dist=Join-Path $projectRoot 'dist'
$version='1.0.1'
$packagingRevision='r1'
$releaseTag="v$version-$packagingRevision"
$apk=Join-Path $dist "Share-Me-Rowell-$version.apk"
$windows=Join-Path $dist 'Windows'
foreach($required in @($apk,(Join-Path $windows 'Share-Me-Rowell.exe'),(Join-Path $windows 'QRCoder.dll'))){if(!(Test-Path -LiteralPath $required)){throw "Build first: missing $required"}}
$testReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\windows-integration.json') -Raw | ConvertFrom-Json
$qrReport=Get-Content -LiteralPath (Join-Path $projectRoot '.local\qr-check.json') -Raw | ConvertFrom-Json
$verify=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.txt') -Raw
$verifiedApk=Get-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.json') -Raw | ConvertFrom-Json
if(!$verifiedApk.verified -or $verifiedApk.sha256 -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()){throw 'APK changed since signature verification. Run verify-android.ps1.'}
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
if($lintErrors -ne 0 -or $jvmPassed -lt 18 -or $testReport.passed -lt 18 -or !$qrReport.passed){throw 'Required local checks are incomplete'}
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'Android') | Out-Null
Copy-Item -LiteralPath $apk -Destination (Join-Path $windows 'Android\Share-Me-Rowell.apk')
Copy-Item -LiteralPath (Join-Path $projectRoot 'scripts\Allow-Local-Network.ps1') -Destination $windows
Copy-Item -LiteralPath (Join-Path $projectRoot 'THIRD_PARTY_NOTICES.md') -Destination $windows
New-Item -ItemType Directory -Force -Path (Join-Path $windows 'licenses') | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'licenses\*.txt') -Destination (Join-Path $windows 'licenses')
$androidGuide=@'
SHARE-ME-ROWELL - Android

1. Extract this ZIP, open Share-Me-Rowell-Android, then tap Share-Me-Rowell-1.0.1.apk on Android 10 or newer.
2. Allow your browser/file manager to install this APK if Android asks.
3. Connect both devices to the same Wi-Fi or a phone hotspot.
4. Sender: Send > Choose files. Receiver: Receive.
5. Show a sender/receiver code on one device; scan it from the opposite screen.
6. Approve the files. Received files appear in Downloads/SHARE-ME-ROWELL.

For PC transfers, run the Windows companion and scan its Send/Receive code.
For offline phone transfers, use Create offline hotspot or Android's Hotspot settings.
Bluetooth uses Android's normal share sheet. Mobile data is not required.
An interrupted transfer can resume when you send the same files again.
The device card shows who you are sharing with and the current stage.
Disconnect stops that connection; Stop sharing & hotspot also closes your hotspot.

This release passed local protocol/build checks. Real-phone Wi-Fi, hotspot,
Bluetooth, background behavior and performance testing are still pending.
See README.md for complete instructions, supported features and limits.
'@
$windowsGuide=@'
SHARE-ME-ROWELL - Windows 10/11, 64-bit

Extract the Windows ZIP, open Share-Me-Rowell, then open Share-Me-Rowell.exe.
Keep QRCoder.dll and the Android folder beside the executable.

PC to phone: Send files > Choose files. Phone: Receive > Scan sender's code.
Phone to PC: Receive files. Phone: Send > Choose files > Scan receiver's code.
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

Local transfer tests passed. Real-phone testing is still pending.
'@
$windowsGuide | Set-Content -LiteralPath (Join-Path $windows 'START-HERE.txt') -Encoding UTF8
$packageBase=Join-Path $projectRoot '.local\packages'
New-Item -ItemType Directory -Force -Path $packageBase | Out-Null
$stage=Join-Path $packageBase ([Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $stage | Out-Null
try{
    $phone=Join-Path $stage 'Share-Me-Rowell-Android';New-Item -ItemType Directory -Path $phone | Out-Null
    Copy-Item -LiteralPath $apk -Destination $phone
    $androidGuide | Set-Content -LiteralPath (Join-Path $phone 'START-HERE.txt') -Encoding UTF8
    Copy-Item -LiteralPath (Join-Path $projectRoot 'README.md'),(Join-Path $projectRoot 'LICENSE'),(Join-Path $projectRoot 'THIRD_PARTY_NOTICES.md') -Destination $phone
    Copy-Item -LiteralPath (Join-Path $projectRoot 'licenses') -Destination $phone -Recurse
    $stagedWindows=Join-Path $stage 'Share-Me-Rowell';New-Item -ItemType Directory -Path $stagedWindows | Out-Null
    foreach($name in @('Share-Me-Rowell.exe','QRCoder.dll','QRCoder-LICENSE.txt','LICENSE.txt','START-HERE.txt','THIRD_PARTY_NOTICES.md','Allow-Local-Network.ps1','Android','licenses')){Copy-Item -LiteralPath (Join-Path $windows $name) -Destination $stagedWindows -Recurse}
    Copy-Item -LiteralPath (Join-Path $projectRoot 'README.md') -Destination $stagedWindows
    foreach($destination in @($phone,$stagedWindows)){
        Copy-Item -LiteralPath (Join-Path $projectRoot 'assets'),(Join-Path $projectRoot 'docs') -Destination $destination -Recurse
    }
    $apkHash=(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$apkHash  Share-Me-Rowell-$version.apk" | Set-Content -LiteralPath (Join-Path $phone 'SHA256SUMS.txt') -Encoding ASCII
    "$apkHash  Android/Share-Me-Rowell.apk" | Set-Content -LiteralPath (Join-Path $stagedWindows 'SHA256SUMS.txt') -Encoding ASCII
    Compress-Archive -LiteralPath $phone -DestinationPath (Join-Path $dist "Share-Me-Rowell-Android-$version.zip") -Force
    Compress-Archive -LiteralPath $stagedWindows -DestinationPath (Join-Path $dist "Share-Me-Rowell-Windows-$version.zip") -Force
    $complete=Join-Path $stage 'Complete\Share-Me-Rowell';New-Item -ItemType Directory -Path $complete -Force | Out-Null
    Copy-Item -LiteralPath $phone -Destination (Join-Path $complete 'Android') -Recurse
    Copy-Item -LiteralPath $stagedWindows -Destination (Join-Path $complete 'Windows') -Recurse
    Compress-Archive -LiteralPath $complete -DestinationPath (Join-Path $dist "Share-Me-Rowell-Complete-$version.zip") -Force
    $artifacts=@()
    foreach($name in @("Share-Me-Rowell-$version.apk","Share-Me-Rowell-Android-$version.zip","Share-Me-Rowell-Windows-$version.zip","Share-Me-Rowell-Complete-$version.zip")){
        $file=Join-Path $dist $name
        $artifacts+=@{file=$name;bytes=(Get-Item -LiteralPath $file).Length;sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()}
    }
    $metadata=[ordered]@{name='Share-Me-Rowell';version=$version;packagingRevision=$packagingRevision;releaseTag=$releaseTag;archiveFolders=@{windows='Share-Me-Rowell/';android='Share-Me-Rowell-Android/';complete='Share-Me-Rowell/'};builtUtc=[DateTime]::UtcNow.ToString('o');android=@{package='com.rowell.shareme';minimumAndroid=10;targetSdk=35;signerCertificateSha256=$certificate};validation=@{androidJvmTestsPassed=$jvmPassed;androidLintErrors=$lintErrors;windowsIntegrationChecksPassed=$testReport.passed;renderedQrDecoded=$qrReport.passed;installedOnPhone=$false;phoneTests='Pending; user deferred device testing';multiGigabytePhysicalTransfer='Not benchmarked; 64-bit metadata and streaming tested locally'};artifacts=$artifacts}
    $metadata | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $dist 'VERSION.json') -Encoding UTF8
    $artifacts | ForEach-Object {$_.sha256+'  '+$_.file} | Set-Content -LiteralPath (Join-Path $dist 'SHA256SUMS.txt') -Encoding ASCII
    $artifacts | ForEach-Object {$_.file+' ('+$_.bytes+' bytes)'}
}finally{
    $resolvedStage=(Resolve-Path -LiteralPath $stage).Path
    $resolvedBase=(Resolve-Path -LiteralPath $packageBase).Path
    if(!$resolvedStage.StartsWith($resolvedBase+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Refused cleanup outside packaging workspace'}
    Remove-Item -LiteralPath $resolvedStage -Recurse -Force
}
