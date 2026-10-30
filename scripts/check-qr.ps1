param([string]$JavaDirectory='',[string]$GradleCache='')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
if(!$JavaDirectory){$JavaDirectory=(Get-ChildItem -LiteralPath (Join-Path $projectRoot '.local\tools\jdk17') -Directory | Select-Object -First 1).FullName}
if(!$GradleCache){$GradleCache=Join-Path $projectRoot '.local\gradle-local'}
$preview=Join-Path $projectRoot '.local\ui-verified'
New-Item -ItemType Directory -Force -Path $preview | Out-Null
$capture=Start-Process -FilePath (Join-Path $projectRoot 'dist\Windows\Share-Mode.exe') -ArgumentList @('--capture',('"'+$preview+'"')) -WindowStyle Hidden -PassThru
if(!$capture.WaitForExit(20000)){$capture.Kill();throw 'Preview capture timed out'}
if($capture.ExitCode -ne 0){throw 'Preview capture failed'}
Copy-Item -LiteralPath (Join-Path $preview 'ui-checks.json') -Destination (Join-Path $projectRoot '.local\windows-ui-checks.json')
$decoder=Get-ChildItem -LiteralPath (Join-Path $GradleCache 'caches\modules-2\files-2.1\com.google.zxing\core\3.5.3') -Filter 'core-3.5.3.jar' -Recurse | Select-Object -First 1
if(!$decoder){throw 'Build Android first to obtain the pinned ZXing decoder.'}
Copy-Item -LiteralPath $decoder.FullName -Destination (Join-Path $projectRoot '.local\zxing-core.jar')
$classes=Join-Path $projectRoot '.local\qr-classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
& (Join-Path $JavaDirectory 'bin\javac.exe') -cp (Join-Path $projectRoot '.local\zxing-core.jar') -d $classes (Join-Path $projectRoot 'tests\QrImageCheck.java')
if($LASTEXITCODE -ne 0){throw 'QR decoder compilation failed'}
& (Join-Path $JavaDirectory 'bin\java.exe') '-Djava.awt.headless=true' -cp ($classes+';'+(Join-Path $projectRoot '.local\zxing-core.jar')) QrImageCheck (Join-Path $preview 'receive.png') (Join-Path $preview 'pairing.txt')
if($LASTEXITCODE -ne 0){throw 'Displayed QR validation failed'}
@{passed=$true;decoder='ZXing 3.5.3';checked='Actual Windows receive-screen image';phoneUsed=$false} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $projectRoot '.local\qr-check.json') -Encoding UTF8
