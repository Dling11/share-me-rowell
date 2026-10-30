# Run as administrator only if the normal Windows Firewall prompt did not enable local transfers.
$ErrorActionPreference = 'Stop'
$appExe = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'Share-Mode.exe')).Path
$name = 'ShareMeRowell.LocalTcp'
if (Get-NetFirewallRule -Name $name -ErrorAction SilentlyContinue) { Remove-NetFirewallRule -Name $name }
New-NetFirewallRule -Name $name -DisplayName 'Share Mode - local transfers' -Direction Inbound -Action Allow -Protocol TCP -LocalPort 48290,48291 -Program $appExe -RemoteAddress LocalSubnet -Profile Private,Public -EdgeTraversalPolicy Block | Out-Null
'Enabled this app on TCP 48290/48291 for local-subnet devices. The firewall remains enabled.'
