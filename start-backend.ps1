param(
  [string]$DbUrl = $env:DB_URL,
  [string]$DbUsername = $env:DB_USERNAME,
  [AllowEmptyString()]
  [string]$DbPassword = $env:DB_PASSWORD
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root "backend"
$jdk = "E:\JDK\Java\jdk-17.0.5"
$maven = "E:\tools\apache-maven-3.8.8\apache-maven-3.8.8\bin"

if (-not (Test-Path (Join-Path $jdk "bin\java.exe"))) {
  throw "JDK 17 not found at $jdk"
}

if (-not (Test-Path (Join-Path $maven "mvn.cmd"))) {
  throw "Maven not found at $maven"
}

$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$maven;$env:Path"

if (-not $DbUrl) {
  if (Test-Path (Join-Path $root ".mysql-data")) {
    $DbUrl = "jdbc:mysql://127.0.0.1:3307/campus_equipment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
  } else {
    $DbUrl = "jdbc:mysql://localhost:3306/campus_equipment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
  }
}

if (-not $DbUsername) { $DbUsername = "root" }
if ($null -eq $DbPassword -and (Test-Path (Join-Path $root ".mysql-data"))) { $DbPassword = "" }

$env:DB_URL = $DbUrl
$env:DB_USERNAME = $DbUsername
$env:DB_PASSWORD = $DbPassword

Set-Location $backend
mvn -s .mvn\local-settings.xml spring-boot:run
