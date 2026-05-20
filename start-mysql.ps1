$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$mysqlHome = "D:\Program Files\MySQL"
$mysqld = Join-Path $mysqlHome "bin\mysqld.exe"
$mysql = Join-Path $mysqlHome "bin\mysql.exe"
$data = Join-Path $root ".mysql-data"
$logDir = Join-Path $root ".logs"
$port = 3307

if (-not (Test-Path $mysqld)) {
  throw "mysqld.exe not found at $mysqld"
}

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

if (-not (Test-Path $data) -or -not (Get-ChildItem -Force $data -ErrorAction SilentlyContinue)) {
  New-Item -ItemType Directory -Force -Path $data | Out-Null
  & $mysqld --initialize-insecure "--basedir=$mysqlHome" "--datadir=$data" --console
}

$existing = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
  Where-Object { $_.State -eq "Listen" } |
  Select-Object -First 1

if (-not $existing) {
  $out = Join-Path $logDir "mysql3307.out.log"
  $err = Join-Path $logDir "mysql3307.err.log"
  $args = "--basedir=`"$mysqlHome`" --datadir=`"$data`" --port=$port --bind-address=127.0.0.1 --console"
  Start-Process -FilePath $mysqld -ArgumentList $args -WorkingDirectory $root -WindowStyle Hidden -RedirectStandardOutput $out -RedirectStandardError $err | Out-Null
  Start-Sleep -Seconds 5
}

& $mysql "-h127.0.0.1" "-P$port" -uroot --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS campus_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

$tableCount = & $mysql "-h127.0.0.1" "-P$port" -uroot --batch --skip-column-names -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='campus_equipment';"
if ([int]$tableCount -eq 0) {
  Push-Location (Join-Path $root "backend")
  try {
    & $mysql "-h127.0.0.1" "-P$port" -uroot --default-character-set=utf8mb4 -e "source src/main/resources/sql/schema.sql"
    & $mysql "-h127.0.0.1" "-P$port" -uroot --default-character-set=utf8mb4 campus_equipment -e "source src/main/resources/sql/data.sql"
  } finally {
    Pop-Location
  }
}

Write-Host "MySQL is running on 127.0.0.1:$port"
