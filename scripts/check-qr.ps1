param([string]$DockerImage='monster-hunter-tri-android:rowell',[string]$GradleCache='D:\rowell_files\MH_4\MH4U_Rowell\.local\gradle')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$preview=Join-Path $projectRoot '.local\ui-verified'
New-Item -ItemType Directory -Force -Path $preview | Out-Null
$capture=Start-Process -FilePath (Join-Path $projectRoot 'dist\Windows\Share-Me-Rowell.exe') -ArgumentList @('--capture',('"'+$preview+'"')) -WindowStyle Hidden -PassThru
if(!$capture.WaitForExit(20000)){$capture.Kill();throw 'Preview capture timed out'}
if($capture.ExitCode -ne 0){throw 'Preview capture failed'}
$decoder=Get-ChildItem -LiteralPath (Join-Path $GradleCache 'caches\modules-2\files-2.1\com.google.zxing\core\3.5.3') -Filter 'core-3.5.3.jar' -Recurse | Select-Object -First 1
if(!$decoder){throw 'Build Android first to obtain the pinned ZXing decoder.'}
Copy-Item -LiteralPath $decoder.FullName -Destination (Join-Path $projectRoot '.local\zxing-core.jar')
& docker run --rm --cap-drop ALL --security-opt no-new-privileges --memory 512m --cpus 2 --mount "type=bind,source=$projectRoot,target=/workspace" --workdir /workspace --entrypoint /bin/sh $DockerImage -c 'mkdir -p .local/qr-classes && javac -cp .local/zxing-core.jar -d .local/qr-classes tests/QrImageCheck.java && java -Djava.awt.headless=true -cp .local/qr-classes:.local/zxing-core.jar QrImageCheck .local/ui-verified/receive.png .local/ui-verified/pairing.txt'
if($LASTEXITCODE -ne 0){throw 'Displayed QR validation failed'}
@{passed=$true;decoder='ZXing 3.5.3';checked='Actual Windows receive-screen image';phoneUsed=$false} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $projectRoot '.local\qr-check.json') -Encoding UTF8
