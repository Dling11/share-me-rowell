$ErrorActionPreference='Stop'
$root=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$folder=Join-Path $root ('.local/transfer-showcase-checks-'+[Guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $folder | Out-Null
Copy-Item -LiteralPath (Join-Path $root 'dist/Windows/Share-Mode.exe'),(Join-Path $root 'dist/Windows/QRCoder.dll') -Destination $folder
$exe=Join-Path $folder 'TransferShowcaseChecks.exe'
& 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe' /nologo /platform:x64 /reference:System.dll /reference:System.Core.dll /reference:System.Drawing.dll /reference:System.Windows.Forms.dll /reference:System.Web.Extensions.dll ('/reference:'+(Join-Path $folder 'Share-Mode.exe')) ('/out:'+$exe) (Join-Path $root 'tests/TransferShowcaseChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Transfer showcase checks did not compile'}
& $exe $folder
if($LASTEXITCODE -ne 0){throw 'Transfer showcase checks failed'}
Copy-Item -LiteralPath (Join-Path $folder 'result.json') -Destination (Join-Path $root '.local/transfer-showcase-checks.json')
