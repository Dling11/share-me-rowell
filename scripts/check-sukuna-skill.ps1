$ErrorActionPreference='Stop'
$project=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$folder=Join-Path $project ('.local\sukuna-checks-'+[Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $folder | Out-Null
Copy-Item -LiteralPath (Join-Path $project 'dist\Windows\Share-Mode.exe'),(Join-Path $project 'dist\Windows\QRCoder.dll') -Destination $folder
$exe=Join-Path $folder 'SukunaSkillChecks.exe'
& 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe' /nologo /target:exe /platform:x64 /optimize+ ('/out:'+$exe) /reference:System.dll /reference:System.Core.dll /reference:System.Drawing.dll /reference:System.Windows.Forms.dll /reference:System.Web.Extensions.dll ('/reference:'+(Join-Path $folder 'Share-Mode.exe')) (Join-Path $project 'tests\SukunaSkillChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Sukuna checks did not compile'}
& $exe $folder
if($LASTEXITCODE -ne 0){throw 'Sukuna skill checks failed'}
Copy-Item -LiteralPath (Join-Path $folder 'result.json') -Destination (Join-Path $project '.local\sukuna-skill-checks.json')
