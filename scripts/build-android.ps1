param(
    [string]$DockerImage = 'monster-hunter-tri-android:rowell',
    [string]$GradleDirectory = 'D:\rowell_files\MH_4\MH4U_Rowell\.local\tools\gradle-8.9',
    [string]$GradleCache = 'D:\rowell_files\MH_4\MH4U_Rowell\.local\gradle'
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
if (!(Test-Path -LiteralPath (Join-Path $GradleDirectory 'bin\gradle'))) { throw 'Pass -GradleDirectory pointing to an extracted Gradle 8.9 distribution. See docs/BUILD.md.' }
New-Item -ItemType Directory -Force -Path $GradleCache | Out-Null
& docker run --rm --cap-drop ALL --security-opt no-new-privileges --pids-limit 512 --memory 6g --cpus 4 --mount "type=bind,source=$projectRoot,target=/workspace" --mount "type=bind,source=$GradleDirectory,target=/opt/rowell-gradle,readonly" --mount "type=bind,source=$GradleCache,target=/gradle-cache" --env GRADLE_USER_HOME=/gradle-cache --workdir /workspace --entrypoint /bin/sh $DockerImage /workspace/scripts/bootstrap-android.sh
if ($LASTEXITCODE -ne 0) { throw 'Android build failed.' }
$dist = Join-Path $projectRoot 'dist'
New-Item -ItemType Directory -Force -Path $dist | Out-Null
Copy-Item -LiteralPath (Join-Path $projectRoot 'android\app\build\outputs\apk\release\app-release.apk') -Destination (Join-Path $dist 'Share-Me-Rowell-1.0.1.apk')
