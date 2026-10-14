$ErrorActionPreference = 'Stop'

$exe = 'C:\Users\user\AppData\Roaming\Orama Interactive\Pixelorama\Pixelorama.exe'
$inputFile = 'C:\Projects\KelMagic\art\pixelorama\bridge_test.pxo'
$outputDir = 'C:\Projects\KelMagic\art\generated\bridge-test'

if (-not (Test-Path -LiteralPath $exe -PathType Leaf)) {
    Write-Host "ERROR: Pixelorama executable not found: $exe" -ForegroundColor Red
    exit 2
}
if (-not (Test-Path -LiteralPath $inputFile -PathType Leaf)) {
    Write-Host "ERROR: Pixelorama project not found: $inputFile" -ForegroundColor Red
    exit 3
}

New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
$output = Join-Path $outputDir 'pixelorama_cli_export.png'
if (Test-Path -LiteralPath $output) {
    $stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
    $output = Join-Path $outputDir "pixelorama_cli_export_$stamp.png"
}

Write-Host "Pixelorama: $exe"
Write-Host "Input project: $inputFile"
Write-Host "Output PNG: $output"
Write-Host 'Starting one headless export attempt...'

$cliArgs = @('--headless', '--quit', '--', '--export', '--output', $output, '--scale', '1', $inputFile)
& $exe @cliArgs
$exitCode = $LASTEXITCODE
Write-Host "Pixelorama exit code: $exitCode"

if ($exitCode -ne 0) {
    Write-Host 'ERROR: Pixelorama reported a non-zero exit code. No retry was attempted.' -ForegroundColor Red
    exit $exitCode
}
if (-not (Test-Path -LiteralPath $output -PathType Leaf)) {
    Write-Host 'ERROR: Pixelorama exited but did not create the expected PNG.' -ForegroundColor Red
    exit 4
}

$file = Get-Item -LiteralPath $output
if ($file.Length -lt 24) {
    Write-Host "ERROR: Output file is too small to be a valid PNG ($($file.Length) bytes)." -ForegroundColor Red
    exit 5
}
$bytes = [System.IO.File]::ReadAllBytes($output)
$expected = @(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
$signatureOk = $true
for ($i = 0; $i -lt 8; $i++) {
    if ($bytes[$i] -ne $expected[$i]) { $signatureOk = $false; break }
}
if (-not $signatureOk) {
    Write-Host 'ERROR: Output exists but does not have the PNG file signature.' -ForegroundColor Red
    exit 6
}

$width = ([uint32]$bytes[16] * 16777216) + ([uint32]$bytes[17] * 65536) + ([uint32]$bytes[18] * 256) + [uint32]$bytes[19]
$height = ([uint32]$bytes[20] * 16777216) + ([uint32]$bytes[21] * 65536) + ([uint32]$bytes[22] * 256) + [uint32]$bytes[23]
Write-Host ''
Write-Host 'SUCCESS: PNG export verified.' -ForegroundColor Green
Write-Host "Output: $output"
Write-Host "Size: $($file.Length) bytes"
Write-Host "Dimensions: ${width}x${height}"
Write-Host 'PNG signature: 89 50 4E 47 0D 0A 1A 0A'
exit 0
