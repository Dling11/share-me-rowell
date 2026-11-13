$ErrorActionPreference='Stop'
$project=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$folder=Join-Path $project ('.local\studio-checks-'+[Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $folder | Out-Null
Copy-Item -LiteralPath (Join-Path $project 'dist\Windows\Share-Mode.exe'),(Join-Path $project 'dist\Windows\QRCoder.dll') -Destination $folder
$compiler='C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$exe=Join-Path $folder 'StudioChecks.exe'
& $compiler /nologo /target:exe /platform:x64 /optimize+ ('/out:'+$exe) /reference:System.dll /reference:System.Core.dll /reference:System.Drawing.dll /reference:System.Windows.Forms.dll /reference:System.Web.Extensions.dll ('/reference:'+(Join-Path $folder 'Share-Mode.exe')) (Join-Path $project 'tests\StudioChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Studio checks did not compile'}
& $exe $folder
if($LASTEXITCODE -ne 0){throw 'Studio checks failed'}
$java=(Get-ChildItem -LiteralPath (Join-Path $project '.local\tools\jdk17') -Directory | Select-Object -First 1).FullName
Get-ChildItem -LiteralPath $folder -Directory -Filter 'main-*' | ForEach-Object { foreach($suffix in @('','-expanded')){ & (Join-Path $java 'bin\java.exe') '-Djava.awt.headless=true' -cp ((Join-Path $project '.local\qr-classes')+';'+(Join-Path $project '.local\zxing-core.jar')) QrImageCheck (Join-Path $_.FullName ('receive'+$suffix+'.png')) (Join-Path $_.FullName ('pairing'+$suffix+'.txt'));if($LASTEXITCODE -ne 0){throw 'Receive QR failed at a supported window size'} } }
$result=Get-Content -LiteralPath (Join-Path $folder 'result.json') -Raw | ConvertFrom-Json
$result | Add-Member -NotePropertyName qrDefaultAndMinimumDecoded -NotePropertyValue $true -Force
$result | Add-Member -NotePropertyName artifactDirectory -NotePropertyValue $folder -Force
$result | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $project '.local\studio-checks.json') -Encoding UTF8
