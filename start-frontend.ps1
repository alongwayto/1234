$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend = Join-Path $root "frontend"
$bundledNode = "C:\Users\Lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe"

Set-Location $frontend

$nodeExe = "node"
try {
  $nodeVersionText = & $nodeExe -v
  $major = [int]($nodeVersionText.TrimStart("v").Split(".")[0])
} catch {
  $nodeVersionText = ""
  $major = 0
}

if ($major -lt 18) {
  if (Test-Path $bundledNode) {
    $nodeExe = $bundledNode
    $nodeVersionText = & $nodeExe -v
    $major = [int]($nodeVersionText.TrimStart("v").Split(".")[0])
  }
}

if ($major -lt 18) {
  throw "Node 18+ is required. Current Node is $nodeVersionText."
}

if (-not (Test-Path "node_modules\vite\bin\vite.js")) {
  npm install
}

& $nodeExe "node_modules\vite\bin\vite.js" --host 127.0.0.1
