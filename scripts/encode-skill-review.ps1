param([ValidateSet('Madara','Sukuna','Zoro')][string]$Character='Sukuna')
$ErrorActionPreference='Stop'
$project=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$framework='C:\Windows\Microsoft.NET\Framework64\v4.0.30319'
$encoder=Join-Path $project '.local\EncodeReviewGif.exe'
& (Join-Path $framework 'csc.exe') /nologo /target:exe /reference:System.dll /reference:System.Core.dll /reference:System.Xaml.dll ('/reference:'+(Join-Path $framework 'WPF\PresentationCore.dll')) ('/reference:'+(Join-Path $framework 'WPF\WindowsBase.dll')) ('/out:'+$encoder) (Join-Path $project 'scripts\EncodeReviewGif.cs')
if($LASTEXITCODE -ne 0){throw 'Review encoder did not compile'}
$name=$Character.ToLowerInvariant();$file=if($Character -eq 'Zoro'){'.local\zoro-skill-checks.json'}elseif($Character -eq 'Sukuna'){'.local\sukuna-skill-checks.json'}else{'.local\inline-skill-checks.json'}
$proof=Get-Content -LiteralPath (Join-Path $project $file) -Raw | ConvertFrom-Json
$version=if($Character -eq 'Zoro'){'1.2.13'}else{'1.2.11'}
$gif=Join-Path $project ('assets\'+$name+'-skill-'+$version+'.gif')
$delay=if($Character -eq 'Zoro'){2}else{5}
& $encoder (Join-Path $proof.artifactDirectory 'frames') $gif $delay
if($LASTEXITCODE -ne 0){throw 'Review encoding failed'}
Add-Type -AssemblyName System.Drawing
$image=[Drawing.Image]::FromFile($gif)
try{
    $frames=$image.GetFrameCount([Drawing.Imaging.FrameDimension]::Time);$delays=$image.GetPropertyItem(0x5100).Value;$duration=0
    for($i=0;$i -lt $delays.Length;$i+=4){$duration+=[BitConverter]::ToUInt32($delays,$i)*10}
    if($frames -ne $proof.reviewFrames -or $duration -ne $frames*$delay*10){throw 'Skill review timing is incorrect'}
    [ordered]@{character=$Character;frames=$frames;durationMilliseconds=$duration;frameDelayMilliseconds=$delay*10;width=$image.Width;height=$image.Height;bytes=(Get-Item -LiteralPath $gif).Length;actualRenderer=$true;fixtureTimesUsed=$true;includedInInstalledApp=$false} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $project ('.local\'+$name+'-review-check.json')) -Encoding UTF8
}finally{$image.Dispose()}
