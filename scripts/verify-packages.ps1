$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dist=Join-Path $projectRoot 'dist'
$metadata=Get-Content -LiteralPath (Join-Path $dist 'VERSION.json') -Raw | ConvertFrom-Json
$apkHash=(Get-FileHash -LiteralPath (Join-Path $dist ('Share-Mode-'+$metadata.version+'.apk')) -Algorithm SHA256).Hash.ToLowerInvariant()
Add-Type -AssemblyName System.IO.Compression.FileSystem
$reports=@()
foreach($artifact in $metadata.artifacts){
    $artifactPath=Join-Path $dist $artifact.file
    if((Get-FileHash -LiteralPath $artifactPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $artifact.sha256){throw "Artifact checksum mismatch: $($artifact.file)"}
    if(!$artifact.file.EndsWith('.zip')){continue}
    $archive=[System.IO.Compression.ZipFile]::OpenRead($artifactPath)
    try{
        $names=@($archive.Entries | ForEach-Object {$_.FullName.Replace('\','/')})
        $outer=@($names | ForEach-Object {($_ -split '/')[0]} | Sort-Object -Unique)
        if($outer.Count -ne 1){throw 'Archive needs exactly one outer folder'}
        if($names | Where-Object {$_ -match '\.jks$|password\.txt|\.local/|\.git/|\.keystore$|local.properties$'}){throw 'Private/development file found in package'}
        if($names | Where-Object {$_ -match 'Share-Me-Rowell.*\.(apk|exe)$'}){throw 'Old application build found in renamed package'}
        $embedded=0
        foreach($entry in $archive.Entries | Where-Object {$_.FullName.EndsWith('.apk')}){
            $stream=$entry.Open();$hasher=[Security.Cryptography.SHA256]::Create()
            try{
                $actual=[BitConverter]::ToString($hasher.ComputeHash($stream)).Replace('-','').ToLowerInvariant()
                if($actual -ne $apkHash){throw 'Embedded APK differs from standalone build'}
                $embedded++
            }finally{$stream.Dispose();$hasher.Dispose()}
        }
        $expected=if($artifact.file.Contains('-Complete-')){2}else{1}
        if($embedded -ne $expected){throw "Unexpected installer count: $($artifact.file)"}
        $reports+=@{archive=$artifact.file;outerFolder=$outer[0];matchingApks=$embedded;privateFiles=$false;sha256=$artifact.sha256}
    }finally{$archive.Dispose()}
}
@{passed=$true;artifacts=$reports} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $projectRoot '.local\package-verify.json') -Encoding UTF8
$reports | ForEach-Object { 'Verified '+$_.archive+': one outer folder, current APK only, no private files' }
