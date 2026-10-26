$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$compiler='C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$privateDir=Join-Path $projectRoot '.local'
$testExe=Join-Path $privateDir 'ShareMe-WindowsChecks.exe'
& $compiler /nologo /target:exe /platform:x64 /optimize+ ('/out:'+$testExe) /reference:System.dll /reference:System.Core.dll /reference:System.Security.dll /reference:System.Web.Extensions.dll (Join-Path $projectRoot 'windows\TransferCore.cs') (Join-Path $projectRoot 'windows\NetworkChoice.cs') (Join-Path $projectRoot 'tests\WindowsChecks.cs')
if($LASTEXITCODE -ne 0){throw 'Windows checks did not compile'}
$testFolder=Join-Path $privateDir ('windows-checks-'+[Guid]::NewGuid().ToString('N'))
& $testExe $testFolder
if($LASTEXITCODE -ne 0){throw 'Windows checks failed'}
Copy-Item -LiteralPath (Join-Path $testFolder 'result.json') -Destination (Join-Path $privateDir 'windows-checks.json')
