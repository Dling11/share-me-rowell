$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$compiler='C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$privateDir=Join-Path $projectRoot '.local'
$testExe=Join-Path $privateDir 'ShareMe-WindowsChecks.exe'
$artResources=@('pixel_armory','pixel_progression','pixel_heads','pixel_anime','pixel_focus','pixel_special','pixel_yugi','pixel_luffy','pixel_zoro','pixel_boa','pixel_shanks','pixel_eren','pixel_levi','pixel_mikasa','pixel_sukuna','pixel_itachi','pixel_rock_lee','pixel_might_guy','pixel_madara') | ForEach-Object { '/resource:'+(Join-Path $projectRoot ('windows\art\'+$_+'.png'))+',ShareMeRowell.'+$_+'.png' }
$artResources += @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'windows\art') -Filter 'portrait_*.png' | ForEach-Object { '/resource:'+$_.FullName+',ShareMeRowell.'+$_.Name })
& $compiler /nologo /target:exe /platform:x64 /optimize+ ('/out:'+$testExe) /reference:System.dll /reference:System.Core.dll /reference:System.Security.dll /reference:System.Web.Extensions.dll /reference:System.Drawing.dll /reference:System.Windows.Forms.dll @artResources (Join-Path $projectRoot 'windows\TransferCore.cs') (Join-Path $projectRoot 'windows\NetworkChoice.cs') (Join-Path $projectRoot 'windows\Adventurer.cs') (Join-Path $projectRoot 'windows\PixelAvatar.cs') (Join-Path $projectRoot 'windows\ModernUi.cs') (Join-Path $projectRoot 'windows\PortraitCatalog.cs') (Join-Path $projectRoot 'windows\CustomPortraits.cs') (Join-Path $projectRoot 'tests\WindowsChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Windows checks did not compile'}
$testFolder=Join-Path $privateDir ('windows-checks-'+[Guid]::NewGuid().ToString('N'))
& $testExe $testFolder
if($LASTEXITCODE -ne 0){throw 'Windows checks failed'}
Copy-Item -LiteralPath (Join-Path $testFolder 'result.json') -Destination (Join-Path $privateDir 'windows-checks.json')
