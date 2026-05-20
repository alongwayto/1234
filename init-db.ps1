param(
  [string]$MysqlUser = "root",
  [Parameter(Mandatory = $true)]
  [string]$MysqlPassword
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$mysql = "D:\Program Files\MySQL\bin\mysql.exe"

if (-not (Test-Path $mysql)) {
  throw "mysql.exe not found at $mysql"
}

& $mysql "-u$MysqlUser" "-p$MysqlPassword" --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS campus_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
& $mysql "-u$MysqlUser" "-p$MysqlPassword" --default-character-set=utf8mb4 campus_equipment -e "source $root\backend\src\main\resources\sql\schema.sql"
& $mysql "-u$MysqlUser" "-p$MysqlPassword" --default-character-set=utf8mb4 campus_equipment -e "source $root\backend\src\main\resources\sql\data.sql"
