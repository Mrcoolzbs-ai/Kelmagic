$ErrorActionPreference = 'Stop'

$root = 'C:\Projects\KelMagic'
$projectDir = Join-Path $root 'art\pixelorama'
$outputDir = Join-Path $root 'art\generated\bridge-test'
New-Item -ItemType Directory -Force -Path $projectDir, $outputDir | Out-Null

# Pick a fresh base name so the test never overwrites earlier work.
$base = 'piskel_cli_crystal_test'
$i = 1
while ($true) {
    $suffix = if ($i -eq 1) { '' } else { "_$i" }
    $name = "$base$suffix"
    $projectPath = Join-Path $projectDir "$name.piskel"
    $outputPath = Join-Path $outputDir "$name.png"
    $planPath = Join-Path $outputDir "$name-plan.json"
    if (!(Test-Path -LiteralPath $projectPath) -and !(Test-Path -LiteralPath $outputPath) -and !(Test-Path -LiteralPath $planPath)) { break }
    $i++
}

$steps = @(
    @{ command = 'project.create'; args = @{ width = 16; height = 16; output = $projectPath; name = 'KelMagic Crystal Test' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 7; y1 = 1; x2 = 9; y2 = 1; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 7; y1 = 1; x2 = 5; y2 = 3; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 9; y1 = 1; x2 = 11; y2 = 3; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 5; y1 = 3; x2 = 4; y2 = 5; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 11; y1 = 3; x2 = 12; y2 = 5; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 4; y1 = 5; x2 = 4; y2 = 8; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 12; y1 = 5; x2 = 12; y2 = 8; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 4; y1 = 8; x2 = 7; y2 = 12; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 12; y1 = 8; x2 = 9; y2 = 12; color = '#37205f' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 7; y1 = 12; x2 = 9; y2 = 12; color = '#37205f' } }
    @{ command = 'fill.area'; args = @{ project = $projectPath; x = 8; y = 6; color = '#28bfe8' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 6; y1 = 4; x2 = 6; y2 = 8; color = '#c1fbff' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 7; y1 = 3; x2 = 7; y2 = 6; color = '#e0ffff' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 8; y1 = 7; x2 = 10; y2 = 9; color = '#6b46c1' } }
    @{ command = 'draw.line'; args = @{ project = $projectPath; x1 = 8; y1 = 10; x2 = 8; y2 = 11; color = '#9a73ff' } }
    @{ command = 'export.png'; args = @{ project = $projectPath; output = $outputPath } }
)

$plan = @{ steps = $steps }
$planJson = $plan | ConvertTo-Json -Depth 20
[System.IO.File]::WriteAllText($planPath, $planJson, [System.Text.UTF8Encoding]::new($false))

Write-Host 'Piskel CLI test: create a 16x16 pixel-art crystal and export PNG' -ForegroundColor Cyan
Write-Host "Plan:    $planPath"
Write-Host "Project: $projectPath"
Write-Host "PNG:     $outputPath"
Write-Host 'Running one CLI plan...'

$cliOutput = & piskel-cli run $planPath --json 2>&1
$exitCode = $LASTEXITCODE
$cliOutput | ForEach-Object { Write-Host $_ }
Write-Host "CLI exit code: $exitCode"
if ($exitCode -ne 0) {
    throw 'Piskel CLI plan failed. No retry was attempted.'
}

if (!(Test-Path -LiteralPath $projectPath)) { throw "Project was not created: $projectPath" }
if (!(Test-Path -LiteralPath $outputPath)) { throw "PNG was not created: $outputPath" }
$bytes = [System.IO.File]::ReadAllBytes($outputPath)
if ($bytes.Length -lt 24) { throw 'PNG is unexpectedly small.' }
$signature = [BitConverter]::ToString($bytes[0..7])
if ($signature -ne '89-50-4E-47-0D-0A-1A-0A') { throw "Output does not have a PNG signature: $signature" }
$width = ([int64]$bytes[16] * 16777216) + ([int64]$bytes[17] * 65536) + ([int64]$bytes[18] * 256) + [int64]$bytes[19]
$height = ([int64]$bytes[20] * 16777216) + ([int64]$bytes[21] * 65536) + ([int64]$bytes[22] * 256) + [int64]$bytes[23]
if ($width -ne 16 -or $height -ne 16) { throw "Unexpected PNG dimensions: ${width}x${height}; expected 16x16." }

Write-Host ''
Write-Host 'SUCCESS: Piskel CLI created and exported the sprite.' -ForegroundColor Green
Write-Host "Project file: $projectPath"
Write-Host "PNG file:     $outputPath"
Write-Host "PNG size:     $($bytes.Length) bytes"
Write-Host "Dimensions:   ${width}x${height}"
Write-Host "Signature:    $signature"
Write-Host "To preview the PNG, open this file: $outputPath"
