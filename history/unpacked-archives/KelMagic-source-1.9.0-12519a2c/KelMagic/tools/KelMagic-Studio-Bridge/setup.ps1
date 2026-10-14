$ErrorActionPreference = 'Stop'
$bridge = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = (Resolve-Path (Join-Path $bridge '..\..')).Path
Write-Host "KelMagic Studio Bridge setup" -ForegroundColor Cyan
Write-Host "Project root: $root"

$folders = @(
  'art\generated', 'art\approved', 'art\references', 'art\pixelorama',
  'art\blockbench\models', 'art\blockbench\exported', 'art\workflows\comfyui', 'art\bridge'
)
foreach ($folder in $folders) { New-Item -ItemType Directory -Force -Path (Join-Path $root $folder) | Out-Null }

$workflowSource = Join-Path $bridge 'workflows\KelMagic_Crystal_LCM.json'
$workflowTarget = Join-Path $root 'art\workflows\comfyui\KelMagic_Crystal_LCM.json'
if (-not (Test-Path $workflowTarget)) { Copy-Item $workflowSource $workflowTarget }

$configPath = Join-Path $bridge 'config.json'
if (Test-Path $configPath) { $config = Get-Content $configPath -Raw | ConvertFrom-Json }
else { $config = Get-Content (Join-Path $bridge 'config.example.json') -Raw | ConvertFrom-Json }

function Find-AppExe([string]$name, [string[]]$candidates) {
  foreach ($candidate in $candidates) { if ($candidate -and (Test-Path $candidate -PathType Leaf)) { return (Resolve-Path $candidate).Path } }
  try {
    $roots = @((Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs'), (Join-Path $env:ProgramData 'Microsoft\Windows\Start Menu\Programs'))
    $wsh = New-Object -ComObject WScript.Shell
    foreach ($r in $roots) {
      if (-not (Test-Path $r)) { continue }
      foreach ($lnk in (Get-ChildItem $r -Filter '*.lnk' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -match $name })) {
        $shortcut = $wsh.CreateShortcut($lnk.FullName)
        if ($shortcut.TargetPath -and (Test-Path $shortcut.TargetPath -PathType Leaf)) { return $shortcut.TargetPath }
      }
    }
  } catch {}
  return ''
}

if (-not $config.blockbenchExe) {
  $config.blockbenchExe = Find-AppExe 'Blockbench' @(
    (Join-Path $env:LOCALAPPDATA 'Programs\Blockbench\Blockbench.exe'),
    (Join-Path $env:LOCALAPPDATA 'Blockbench\Blockbench.exe'),
    (Join-Path $env:ProgramFiles 'Blockbench\Blockbench.exe'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Blockbench.exe')
  )
}
if (-not $config.pixeloramaExe) {
  $config.pixeloramaExe = Find-AppExe 'Pixelorama' @(
    (Join-Path $env:LOCALAPPDATA 'Programs\Pixelorama\Pixelorama.exe'),
    (Join-Path $env:LOCALAPPDATA 'Pixelorama\Pixelorama.exe'),
    (Join-Path $env:ProgramFiles 'Pixelorama\Pixelorama.exe'),
    (Join-Path $env:ProgramFiles 'Steam\steamapps\common\Pixelorama\Pixelorama.exe')
  )
}
$config.comfyUIBaseUrl = 'http://127.0.0.1:8188'
$config.bridgeHost = '127.0.0.1'
$config.bridgePort = 18765
$config | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 $configPath

$snippetPath = Join-Path $root 'art\bridge\cline-mcp-server-snippet.json'
$serverPath = Join-Path $bridge 'server.js'
$snippet = [ordered]@{
  mcpServers = [ordered]@{
    kelmagicStudio = [ordered]@{
      command = 'node'
      args = @($serverPath)
      env = [ordered]@{ KELMAGIC_ROOT = $root; KELMAGIC_CONFIG = $configPath }
      disabled = $false
      autoApprove = @()
    }
  }
}
$snippet | ConvertTo-Json -Depth 10 | Set-Content -Encoding UTF8 $snippetPath

Write-Host ""
Write-Host "Detected applications:" -ForegroundColor Cyan
Write-Host "  ComfyUI:    $($config.comfyUIBaseUrl)"
Write-Host "  Blockbench: $($config.blockbenchExe)"
Write-Host "  Pixelorama: $($config.pixeloramaExe)"
Write-Host ""
Write-Host "Installing MCP dependencies (npm install)..." -ForegroundColor Cyan
Push-Location $bridge
try { npm install --no-audit --no-fund }
finally { Pop-Location }
Write-Host ""
Write-Host "Setup files ready." -ForegroundColor Green
Write-Host "1) In Cline > MCP Servers > Configure, MERGE the mcpServers.kelmagicStudio entry from:"
Write-Host "   $snippetPath"
Write-Host "   Do not replace your existing MCP server entries."
Write-Host "2) If an app path above is blank, edit: $configPath"
Write-Host "3) Restart the KelMagic Studio MCP server in Cline."
Write-Host "4) Load the Blockbench plugin from: $(Join-Path $bridge 'blockbench\kelmagic_studio_bridge.js')"
