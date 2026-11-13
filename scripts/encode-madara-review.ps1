param([switch]$Inline)
# Compatibility entry point: all current skill reviews play in the portrait card.
& (Join-Path $PSScriptRoot 'encode-skill-review.ps1') -Character Madara