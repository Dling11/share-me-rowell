param(
    [Parameter(Mandatory=$true)][string]$InstallationDirectory,
    [Parameter(Mandatory=$true)][string]$BackupDirectory,
    [switch]$PlanOnly
)
$ErrorActionPreference='Stop'
$root=(Resolve-Path -LiteralPath $InstallationDirectory).Path.TrimEnd('\')
$backup=(Resolve-Path -LiteralPath $BackupDirectory).Path.TrimEnd('\')
$primary=Join-Path $root 'Share-Mode.exe'
if($root.Length -lt 6 -or (Get-Item -LiteralPath $primary).VersionInfo.ProductName -ne 'Share Mode'){throw 'This is not a verified Share Mode installation'}
if($backup.StartsWith($root+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Cleanup backup must be outside the application folder'}
$archive=[IO.Path]::GetFullPath((Join-Path $backup 'removed-runtime-clutter'))
if(!$archive.StartsWith($backup+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Archive escaped the intended backup folder'}
$candidates=@('assets','windows','Android\art')
$candidates+=@(Get-ChildItem -LiteralPath $root -File | Where-Object {$_.Name -match '^Share-(Me-Rowell|Mode)-Windows-\d+\.\d+\.\d+\.zip$'} | ForEach-Object {$_.Name})
$candidates+=@(Get-ChildItem -LiteralPath (Join-Path $root 'Android') -File -ErrorAction SilentlyContinue | Where-Object {$_.Extension -eq '.apk' -and $_.Name -ne 'Share-Mode.apk'} | ForEach-Object {'Android\'+$_.Name})
$plan=@();foreach($relative in $candidates){
    $path=[IO.Path]::GetFullPath((Join-Path $root $relative));$target=[IO.Path]::GetFullPath((Join-Path $archive $relative))
    if(!$path.StartsWith($root+'\',[StringComparison]::OrdinalIgnoreCase) -or !$target.StartsWith($archive+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Archive path escaped its checked root'}
    if(!(Test-Path -LiteralPath $path)){continue}
    $item=Get-Item -LiteralPath $path
    if($item.Attributes -band [IO.FileAttributes]::ReparsePoint){throw ('Unexpected link in cleanup path: '+$relative)}
    $files=if($item.PSIsContainer){@(Get-ChildItem -LiteralPath $path -Recurse -File)}else{@($item)}
    if(@(Get-ChildItem -LiteralPath $path -Recurse -Force -ErrorAction SilentlyContinue | Where-Object {$_.Attributes -band [IO.FileAttributes]::ReparsePoint}).Count){throw ('Unexpected nested link: '+$relative)}
    $plan+=[pscustomobject]@{relative=$relative;source=$path;destination=$target;bytes=($files|Measure-Object Length -Sum).Sum}
}
if($PlanOnly){[ordered]@{installationDirectory=$root;archiveDirectory=$archive;candidates=$plan;bytesOutsideRuntime=($plan|Measure-Object bytes -Sum).Sum;wouldDeduplicateLegacyLauncher=(Test-Path -LiteralPath (Join-Path $root 'Share-Me-Rowell.exe'));planOnly=$true} | ConvertTo-Json -Depth 5;return}
if(!(Test-Path -LiteralPath (Join-Path $backup 'installation\Share-Mode.exe'))){throw 'A complete pre-update installation backup is required'}
New-Item -ItemType Directory -Path $archive -Force | Out-Null
foreach($item in $plan){
    if(Test-Path -LiteralPath $item.destination){throw 'Archive destination already exists; no historical backup will be overwritten'}
    New-Item -ItemType Directory -Path (Split-Path -Parent $item.destination) -Force | Out-Null
    # Both absolute targets were checked against the explicitly supplied roots above.
    Move-Item -LiteralPath $item.source -Destination $item.destination
}
$alias=Join-Path $root 'Share-Me-Rowell.exe';$hardlinked=$false
if(Test-Path -LiteralPath $alias){
    $running=@(Get-CimInstance Win32_Process -Filter "Name='Share-Me-Rowell.exe'" | Where-Object {$_.ExecutablePath -eq $alias})
    if($running.Count){throw 'Close the legacy launcher normally before deduplicating it'}
    $primaryHash=(Get-FileHash -LiteralPath $primary).Hash
    if((Get-FileHash -LiteralPath $alias).Hash -ne $primaryHash){throw 'Legacy launcher differs from the installed executable; it was preserved'}
    if((Get-Item -LiteralPath $alias).LinkType -eq 'HardLink'){
        $fsutil=Join-Path $env:SystemRoot 'System32\fsutil.exe'
        $primaryId=[regex]::Match((& $fsutil file queryfileid $primary | Out-String),'0x[0-9a-fA-F]+').Value
        $aliasId=[regex]::Match((& $fsutil file queryfileid $alias | Out-String),'0x[0-9a-fA-F]+').Value
        $hardlinked=$primaryId.Length -gt 0 -and $primaryId -eq $aliasId
    }
    if(!$hardlinked){Remove-Item -LiteralPath $alias;try{New-Item -ItemType HardLink -Path $alias -Target $primary -ErrorAction Stop | Out-Null;$hardlinked=$true}catch{Copy-Item -LiteralPath $primary -Destination $alias}}
    if((Get-FileHash -LiteralPath $alias).Hash -ne $primaryHash){throw 'Legacy launcher verification failed'}
}
$total=(Get-ChildItem -LiteralPath $root -Recurse -File | Measure-Object Length -Sum).Sum
$payload=$total;if($hardlinked){$payload-=(Get-Item -LiteralPath $alias).Length}
$report=[ordered]@{installationDirectory=$root;archiveDirectory=$archive;archived=$plan;archivedBytes=($plan|Measure-Object bytes -Sum).Sum;legacyLauncherHardlinked=$hardlinked;logicalFileBytes=$total;runtimePayloadBytesCountingBinaryOnce=$payload;authoringAndReviewMediaInRuntime=$false;personalDataTouched=$false;deletedSourceImages=$false}
$record=Join-Path $backup ('runtime-trim-'+[Guid]::NewGuid().ToString('N').Substring(0,8)+'.json')
$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $record -Encoding UTF8
$report | ConvertTo-Json -Depth 6
