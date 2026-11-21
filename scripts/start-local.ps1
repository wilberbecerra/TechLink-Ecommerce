param([int]$Port = 8080, [string]$DbHost = 'localhost', [int]$DbPort = 1433)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$passwordFile = Join-Path $projectRoot '.local/db-password.txt'
if (!(Test-Path -LiteralPath $passwordFile)) { throw 'Primero ejecuta: powershell -ExecutionPolicy Bypass -File scripts/setup-database.ps1' }
$jdkRelease = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'release' } else { '' }
$compatibleJdk = (Test-Path -LiteralPath "$env:JAVA_HOME/bin/javac.exe") -and (Test-Path -LiteralPath $jdkRelease)
if ($compatibleJdk) {
    $jdkInfo = [IO.File]::ReadAllText($jdkRelease)
    $compatibleJdk = $jdkInfo -match 'JAVA_VERSION="(?<major>\d+)' -and [int]$Matches.major -ge 17
}
if (!$compatibleJdk) {
    $jdkPaths = @('C:/Program Files/Java/jdk-17', "$env:USERPROFILE/.jdks/ms-17.0.17", "$env:USERPROFILE/.jdks/corretto-17.0.17")
    $jdk = $jdkPaths | Where-Object { Test-Path -LiteralPath "$_/bin/javac.exe" } | Select-Object -First 1
    if (!$jdk) { throw 'Configura JAVA_HOME con un JDK 17 o superior.' }
    $env:JAVA_HOME = $jdk
}
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
$env:DB_PASSWORD = [IO.File]::ReadAllText($passwordFile).Trim()
$env:DB_URL = "jdbc:sqlserver://${DbHost}:${DbPort};databaseName=TechLink;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = 'techlink_local'
$env:TECHLINK_DEMO_ENABLED = 'true'
$demoCredentialsFile = Join-Path $projectRoot '.local/demo-credentials.json'
if (!(Test-Path -LiteralPath $demoCredentialsFile)) {
    $demoCredentials = [ordered]@{
        adminEmail = 'admin@techlink.pe'
        adminPassword = ('Tl!' + [guid]::NewGuid().ToString('N') + 'A7')
        customerEmail = 'cliente@techlink.pe'
        customerPassword = ('Tl!' + [guid]::NewGuid().ToString('N') + 'C9')
    }
    [IO.File]::WriteAllText($demoCredentialsFile, ($demoCredentials | ConvertTo-Json))
}
$demoCredentials = [IO.File]::ReadAllText($demoCredentialsFile) | ConvertFrom-Json
$env:TECHLINK_DEMO_ADMIN_PASSWORD = $demoCredentials.adminPassword
$env:TECHLINK_DEMO_CUSTOMER_PASSWORD = $demoCredentials.customerPassword
Write-Host 'Consulta tus accesos demo en .local/demo-credentials.json. No se modifican cuentas existentes.'
$env:PORT = "$Port"
Push-Location $projectRoot
try {
    & ./mvnw.cmd spring-boot:run
} finally { Pop-Location }
