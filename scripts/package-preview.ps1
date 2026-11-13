param([string]$WindowsVersion='1.2.14',[string]$AndroidVersion='1.2.8',[string]$ReleaseTag='v1.2.15')
$ErrorActionPreference='Stop'
if($WindowsVersion -notmatch '^\d+\.\d+\.\d+$' -or $AndroidVersion -notmatch '^\d+\.\d+\.\d+$' -or $ReleaseTag -notmatch '^v\d+\.\d+\.\d+$'){throw 'Invalid release versions'}
$root=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dist=Join-Path $root 'dist';$tag=$ReleaseTag;$notes='docs/RELEASE-'+$tag.Substring(1)+'.md'
$exe=Join-Path $dist 'Windows/Share-Mode.exe';$apk=Join-Path $dist ('Share-Mode-'+$AndroidVersion+'.apk');$aab=Join-Path $dist ('Share-Mode-'+$AndroidVersion+'.aab')
function Hash([string]$file){(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()}
$windows=Get-Content -LiteralPath (Join-Path $root ('.local/verification-'+$WindowsVersion+'.json')) -Raw | ConvertFrom-Json
$android=Get-Content -LiteralPath (Join-Path $root '.local/apk-verify.json') -Raw | ConvertFrom-Json
if((Get-Item -LiteralPath $exe).VersionInfo.FileVersion -ne ($WindowsVersion+'.0') -or (Hash $exe) -ne $windows.windows.sha256 -or $windows.totalWindowsChecks -lt 104){throw 'Windows package must match the verified executable'}
if(!$android.verified -or !$android.bundleSignatureVerified -or $android.version -ne $AndroidVersion -or $android.package -ne 'com.rowell.shareme' -or (Hash $apk) -ne $android.sha256 -or (Hash $aab) -ne $android.bundleSha256 -or $android.certificateSha256 -ne 'c944651d33fc164747075203c219b38773efcef45bb93b3dcc78bffc0b1ee8b8'){throw 'Android signature/version verification is missing or stale'}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stage=Join-Path $root ('.local/package-current-'+[Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage | Out-Null
try{
    $pc=Join-Path $stage 'Share-Mode';$phone=Join-Path $stage 'Share-Mode-Android'
    New-Item -ItemType Directory -Path $pc,$phone | Out-Null
    foreach($name in @('Share-Mode.exe','QRCoder.dll','QRCoder-LICENSE.txt','LICENSE.txt','THIRD_PARTY_NOTICES.md','Allow-Local-Network.ps1')){Copy-Item -LiteralPath (Join-Path $dist ('Windows/'+$name)) -Destination $pc}
    Copy-Item -LiteralPath (Join-Path $root 'licenses') -Destination $pc -Recurse
    $embedded=Join-Path $pc 'Android';New-Item -ItemType Directory -Path $embedded | Out-Null
    Copy-Item -LiteralPath $apk -Destination (Join-Path $embedded 'Share-Mode.apk')
    Copy-Item -LiteralPath $apk -Destination $phone
    Copy-Item -LiteralPath (Join-Path $root 'LICENSE'),(Join-Path $root 'THIRD_PARTY_NOTICES.md') -Destination $phone
    Copy-Item -LiteralPath (Join-Path $root 'licenses') -Destination $phone -Recurse
    foreach($folder in @($pc,$phone)){New-Item -ItemType Directory -Path (Join-Path $folder 'docs') | Out-Null;Copy-Item -LiteralPath (Join-Path $root 'docs/PRIVACY.md'),(Join-Path $root $notes) -Destination (Join-Path $folder 'docs')}
    $pcGuide=@"
SHARE MODE - Windows $WindowsVersion / included Android $AndroidVersion

Extract the entire Share-Mode folder and open Share-Mode.exe.
Keep QRCoder.dll and the Android folder beside the executable.
The Android APK is offered by Get phone app through a local download QR.
Use the same Wi-Fi or hotspot. Receiver: Receive. Sender: choose files, then pair by QR and approve.
Files arrive in Downloads\SHARE-ME-ROWELL. Reconnect and send the same files to resume.
Your existing profile, identity, history and progress stay in their existing locations.
Your adventurer opens appearance, characters and progress. Mastery skills are optional.
Known preview issue: Zoro changes apparent scale between idle and his mastery attack.
See $notes and docs/PRIVACY.md.
"@
    $phoneGuide=@"
SHARE MODE - Android $AndroidVersion

Install the included APK as an update without uninstalling the previous app.
Requires Android 10 or newer. Approve installation from your file manager if Android asks.
Put both devices on the same Wi-Fi/hotspot. Receiver: Receive. Sender: choose files/apps and a receiver or scan QR.
Compare the code and approve the files. Reconnect and resend the same files to resume.
Android's system Share menu can open selected files in Share Mode.
App sharing copies installers only; accounts, private app data and saves are excluded.
Existing received files, profile, history and progress are preserved.
HD and Pixel portraits, the curated character collection and in-card skills now match the approved Windows artwork.
Your personal Boy/Girl look saves automatically. Signature characters equip as fixed designs.
Use Starter/Mastery to preview outfits. Previewing never equips a locked character or earns XP.
Tap Madara/Sukuna, or Zoro's mastery preview, to play a skill within the card.
See $notes and docs/PRIVACY.md.
"@
    [IO.File]::WriteAllText((Join-Path $pc 'START-HERE.txt'),$pcGuide,[Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText((Join-Path $phone 'START-HERE.txt'),$phoneGuide,[Text.UTF8Encoding]::new($false))
    $zipNames=@(('Share-Mode-Windows-'+$WindowsVersion+'.zip'),('Share-Mode-Android-'+$AndroidVersion+'.zip'))
    for($i=0;$i -lt 2;$i++){
        if($i -ge $zipNames.Count -or !$zipNames[$i]){throw 'Missing package filename'}
        $destination=[IO.Path]::GetFullPath((Join-Path $dist $zipNames[$i]))
        if(!$destination.StartsWith($dist+'\',[StringComparison]::OrdinalIgnoreCase) -or [IO.Path]::GetExtension($destination) -ne '.zip'){throw 'Invalid package destination'}
        if(Test-Path -LiteralPath $destination){if((Get-Item -LiteralPath $destination).PSIsContainer){throw 'Package destination must be a file'};Remove-Item -LiteralPath $destination -Force}
        $folder=if($i -eq 0){$pc}else{$phone}
        Compress-Archive -LiteralPath $folder -DestinationPath $destination -CompressionLevel Optimal
        $zip=[IO.Compression.ZipFile]::OpenRead($destination)
        try{
            $names=@($zip.Entries | ForEach-Object {$_.FullName.Replace('\','/')});$outer=@($names | ForEach-Object {($_ -split '/')[0]} | Sort-Object -Unique)
            if($outer.Count -ne 1 -or @($names | Where-Object {$_ -match '(^|/)(\.local|\.git|assets|art-source|AGENTS\.md)|\.(png|gif|jks|keystore|pfx)$|password|local\.properties|Share-Me-Rowell.*\.exe$'}).Count){throw 'Unwanted private, source or image content in release package'}
            foreach($extension in @('.apk','.exe')){
                $entries=@($zip.Entries | Where-Object {$_.FullName.EndsWith($extension)})
                $expected=if($extension -eq '.exe' -and $i -eq 1){0}else{1}
                if($entries.Count -ne $expected){throw 'Duplicate or missing installer in package'}
                foreach($entry in $entries){$stream=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create();try{$actual=[BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-','').ToLowerInvariant();$wanted=if($extension -eq '.apk'){$android.sha256}else{$windows.windows.sha256};if($actual -ne $wanted){throw 'Embedded installer differs from tested build'}}finally{$stream.Dispose();$sha.Dispose()}}
            }
        }finally{$zip.Dispose()}
    }
    $artifacts=@();foreach($name in @((Split-Path -Leaf $apk),(Split-Path -Leaf $aab))+$zipNames){$file=Join-Path $dist $name;$artifacts+=[ordered]@{file=$name;bytes=(Get-Item -LiteralPath $file).Length;sha256=(Hash $file)}}
    $source=(& git -C $root rev-parse HEAD).Trim();if($LASTEXITCODE -ne 0){throw 'Cannot identify release source'}
    $metadata=[ordered]@{name='Share Mode';releaseTag=$tag;sourceCommit=$source;packagedAtUtc=[DateTime]::UtcNow.ToString('o');prerelease=$true;windows=@{version=$WindowsVersion;exeSha256=$windows.windows.sha256;automatedChecksPassed=104};android=@{version=$AndroidVersion;versionCode=$android.versionCode;package=$android.package;certificateSha256=$android.certificateSha256;sha256=$android.sha256;bundleSha256=$android.bundleSha256};validation=@{archiveContentsVerified=$true;signaturesVerified=$true;sourceAndReviewImagesExcluded=$true;physicalTwoPhoneRadioRetested=$false;focusedOwnerAnimationReviewDeferred=$true};knownIssues=@('Zoro idle-to-mastery-attack scale transition needs further polish');artifacts=$artifacts}
    $metadata | ConvertTo-Json -Depth 7 | Set-Content -LiteralPath (Join-Path $dist 'VERSION.json') -Encoding UTF8
    $sums=@($artifacts | ForEach-Object {$_.sha256+'  '+$_.file});$sums+=((Hash (Join-Path $dist 'VERSION.json'))+'  VERSION.json')
    $sums | Set-Content -LiteralPath (Join-Path $dist 'SHA256SUMS.txt') -Encoding ASCII
    $metadata | ConvertTo-Json -Depth 7 | Set-Content -LiteralPath (Join-Path $root '.local/current-package-verification.json') -Encoding UTF8
    $artifacts | ForEach-Object {[pscustomobject]@{file=$_.file;bytes=$_.bytes}}
}finally{
    $absolute=[IO.Path]::GetFullPath($stage);$allowed=Join-Path $root '.local'
    if(!$absolute.StartsWith($allowed+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Packaging cleanup escapes workspace'}
    if(Test-Path -LiteralPath $absolute){Remove-Item -LiteralPath $absolute -Recurse -Force}
}
