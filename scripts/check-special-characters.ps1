$ErrorActionPreference='Stop'
$project=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$folder=Join-Path $project ('.local\special-character-checks-'+[Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $folder | Out-Null
Copy-Item -LiteralPath (Join-Path $project 'dist\Windows\Share-Mode.exe'),(Join-Path $project 'dist\Windows\QRCoder.dll') -Destination $folder
$compiler='C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$exe=Join-Path $folder 'SpecialCharacterChecks.exe'
& $compiler /nologo /target:exe /platform:x64 /optimize+ ('/out:'+$exe) /reference:System.dll /reference:System.Core.dll /reference:System.Drawing.dll /reference:System.Windows.Forms.dll /reference:System.Web.Extensions.dll ('/reference:'+(Join-Path $folder 'Share-Mode.exe')) (Join-Path $project 'tests\SpecialCharacterChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Fixed-character checks did not compile'}
& $exe $folder
if($LASTEXITCODE -ne 0){throw 'Fixed-character checks failed'}
Copy-Item -LiteralPath (Join-Path $folder 'result.json') -Destination (Join-Path $project '.local\special-character-checks.json')
Copy-Item -LiteralPath (Join-Path $folder 'fixed-character-style.png'),(Join-Path $folder 'personal-character-restored.png'),(Join-Path $folder 'yugi-character-style.png'),(Join-Path $folder 'luffy-character-style.png'),(Join-Path $folder 'madara-character-style.png') -Destination (Join-Path $project '.local\ui-verified')
