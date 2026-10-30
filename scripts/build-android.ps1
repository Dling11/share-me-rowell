param([string]$JavaDirectory='', [string]$SdkDirectory='', [string]$GradleDirectory='')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
if(!$JavaDirectory){$JavaDirectory=(Get-ChildItem -LiteralPath (Join-Path $projectRoot '.local\tools\jdk17') -Directory | Select-Object -First 1).FullName}
if(!$SdkDirectory){$SdkDirectory=Join-Path $projectRoot '.local\tools\android-sdk'}
if(!$GradleDirectory){$GradleDirectory=Join-Path $projectRoot '.local\tools\gradle-8.9'}
foreach($required in @((Join-Path $JavaDirectory 'bin\javac.exe'),(Join-Path $SdkDirectory 'platforms\android-35\android.jar'),(Join-Path $SdkDirectory 'build-tools\35.0.0\aapt.exe'),(Join-Path $GradleDirectory 'bin\gradle.bat'),(Join-Path $projectRoot '.local\signing\shareme.jks'))){if(!(Test-Path -LiteralPath $required)){throw "Missing local build prerequisite: $required. See docs/BUILD.md."}}
$previousJava=$env:JAVA_HOME
$previousSdk=$env:ANDROID_HOME
$previousCache=$env:GRADLE_USER_HOME
$previousJavaOptions=$env:JAVA_TOOL_OPTIONS
try{
    $socketTemp=Join-Path $projectRoot '.local\nio-temp'
    New-Item -ItemType Directory -Force -Path $socketTemp | Out-Null
    $env:JAVA_TOOL_OPTIONS='"-Djdk.net.unixdomain.tmpdir='+$socketTemp+'"'
    $env:JAVA_HOME=$JavaDirectory
    $env:ANDROID_HOME=$SdkDirectory
    $env:GRADLE_USER_HOME=Join-Path $projectRoot '.local\gradle-local'
    ('sdk.dir='+$SdkDirectory.Replace('\','/')) | Set-Content -LiteralPath (Join-Path $projectRoot 'android\local.properties') -Encoding ASCII
    & (Join-Path $GradleDirectory 'bin\gradle.bat') --no-daemon --max-workers=3 --console=plain --stacktrace --project-dir (Join-Path $projectRoot 'android') --project-cache-dir (Join-Path $projectRoot '.local\android-project-cache') '-Pkotlin.compiler.execution.strategy=in-process' testDebugUnitTest assembleRelease lintRelease
    if($LASTEXITCODE -ne 0){throw 'Android build failed.'}
    $dist=Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Force -Path $dist | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot 'android\app\build\outputs\apk\release\app-release.apk') -Destination (Join-Path $dist 'Share-Mode-1.0.4.apk')
}finally{$env:JAVA_HOME=$previousJava;$env:ANDROID_HOME=$previousSdk;$env:GRADLE_USER_HOME=$previousCache;$env:JAVA_TOOL_OPTIONS=$previousJavaOptions}
