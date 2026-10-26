$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$privateDir = Join-Path $projectRoot '.local'
$package = Join-Path $privateDir 'qrcoder.1.8.1.nupkg'
$packageHash = '3Mg+aSoCNKz7w3OQ6KxpiNxEu6AgOE5lqsORNNu63heRdau9wM34IUCmtvkKDCCC2oraxxlOVTKX/HsXYAxHUw=='
New-Item -ItemType Directory -Force -Path $privateDir | Out-Null
if (!(Test-Path -LiteralPath $package)) { Invoke-WebRequest 'https://api.nuget.org/v3-flatcontainer/qrcoder/1.8.1/qrcoder.1.8.1.nupkg' -OutFile $package }
$algorithm = [System.Security.Cryptography.SHA512]::Create()
try { $actualHash = [Convert]::ToBase64String($algorithm.ComputeHash([System.IO.File]::ReadAllBytes($package))) } finally { $algorithm.Dispose() }
if ($actualHash -ne $packageHash) { throw 'QRCoder package hash mismatch' }
$output = Join-Path $projectRoot 'dist\Windows'
New-Item -ItemType Directory -Force -Path $output | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($package)
try {
    [System.IO.Compression.ZipFileExtensions]::ExtractToFile($zip.GetEntry('lib/net40/QRCoder.dll'),(Join-Path $output 'QRCoder.dll'),$true)
    [System.IO.Compression.ZipFileExtensions]::ExtractToFile($zip.GetEntry('LICENSE.txt'),(Join-Path $output 'QRCoder-LICENSE.txt'),$true)
} finally { $zip.Dispose() }
$compiler = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (!(Test-Path -LiteralPath $compiler)) { throw 'Windows .NET Framework compiler was not found. .NET Framework 4.8 is required.' }
$iconPath = Join-Path $privateDir 'shareme.ico'
$references = @('/reference:System.dll','/reference:System.Core.dll','/reference:System.Drawing.dll','/reference:System.Windows.Forms.dll','/reference:System.Security.dll','/reference:System.Web.Extensions.dll',('/reference:' + (Join-Path $output 'QRCoder.dll')))
$sources = @('TransferCore.cs','NetworkChoice.cs','ModernUi.cs','Program.cs','AssemblyInfo.cs') | ForEach-Object { Join-Path $projectRoot ('windows\' + $_) }
$builder = Join-Path $privateDir 'ShareMe-IconBuilder.exe'
Copy-Item -LiteralPath (Join-Path $output 'QRCoder.dll') -Destination (Join-Path $privateDir 'QRCoder.dll')
& $compiler /nologo /target:winexe /platform:x64 /optimize+ /utf8output ('/out:' + $builder) @references @sources
if ($LASTEXITCODE -ne 0) { throw 'Icon renderer build failed' }
$render = Start-Process -FilePath $builder -ArgumentList @('--render-icon',('"' + $privateDir + '"')) -WindowStyle Hidden -PassThru
if (!$render.WaitForExit(15000)) { $render.Kill(); throw 'Icon render timed out' }
if ($render.ExitCode -ne 0) { throw 'Icon render failed' }
Copy-Item -LiteralPath (Join-Path $privateDir 'icon-preview.png') -Destination (Join-Path $projectRoot 'assets\icon-preview.png')
& $compiler /nologo /target:winexe /platform:x64 /optimize+ /utf8output ("/win32icon:" + $iconPath) ("/win32manifest:" + (Join-Path $projectRoot 'windows\app.manifest')) ("/out:" + (Join-Path $output 'Share-Me-Rowell.exe')) @references @sources
if ($LASTEXITCODE -ne 0) { throw 'Windows build failed' }
Copy-Item -LiteralPath (Join-Path $projectRoot 'LICENSE') -Destination (Join-Path $output 'LICENSE.txt')
'Windows app built: ' + (Join-Path $output 'Share-Me-Rowell.exe')
