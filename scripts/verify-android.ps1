param([string]$JavaDirectory='', [string]$SdkDirectory='')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
if(!$JavaDirectory){$JavaDirectory=(Get-ChildItem -LiteralPath (Join-Path $projectRoot '.local\tools\jdk17') -Directory | Select-Object -First 1).FullName}
if(!$SdkDirectory){$SdkDirectory=Join-Path $projectRoot '.local\tools\android-sdk'}
$apk=Join-Path $projectRoot 'dist\Share-Mode-1.0.4.apk'
$previousJava=$env:JAVA_HOME
try{
    $env:JAVA_HOME=$JavaDirectory
    $output=& (Join-Path $SdkDirectory 'build-tools\35.0.0\apksigner.bat') verify --verbose --print-certs $apk
    if($LASTEXITCODE -ne 0){throw 'Android signature verification failed'}
    $output+=& (Join-Path $SdkDirectory 'build-tools\35.0.0\aapt.exe') dump badging $apk
    if($LASTEXITCODE -ne 0){throw 'Android package verification failed'}
    $permissions=& (Join-Path $SdkDirectory 'build-tools\35.0.0\aapt.exe') dump permissions $apk
    if($LASTEXITCODE -ne 0 -or ($permissions -join "`n") -notmatch "android.permission.CHANGE_NETWORK_STATE"){throw 'APK lacks the permission required to join a hotspot'}
    $summary=@($output | Where-Object {$_ -match '^Verifies|^Verified|^Signer #1 certificate SHA-256|^package:|^sdkVersion:|^targetSdkVersion:|^application-label:'})
    $text=$summary -join [Environment]::NewLine
    if($text -notmatch "name='com.rowell.shareme'" -or $text -notmatch "versionCode='5'" -or $text -notmatch "versionName='1.0.4'" -or $text -notmatch "application-label:'Share Mode'" -or $text -notmatch "sdkVersion:'29'" -or $text -notmatch "targetSdkVersion:'35'"){throw 'Unexpected Android package metadata'}
    $certificate=[regex]::Match($text,'Signer #1 certificate SHA-256 digest: ([a-f0-9]{64})').Groups[1].Value
    $expected=(Get-Content -LiteralPath (Join-Path $projectRoot '.local\signing\certificate-sha256.txt') -Raw).Trim()
    if(!$certificate -or $certificate -ne $expected){throw 'Signing identity changed. Do not install or distribute this APK.'}
    $summary | Set-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.txt') -Encoding UTF8
    @{sha256=(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant();certificateSha256=$certificate;package='com.rowell.shareme';verified=$true;matchesPreviousRelease=$true;networkRequestPermission=$true} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $projectRoot '.local\apk-verify.json') -Encoding UTF8
    $summary
}finally{$env:JAVA_HOME=$previousJava}
