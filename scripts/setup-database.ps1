param([string]$Server = 'localhost')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$localDir = Join-Path $projectRoot '.local'
New-Item -ItemType Directory -Force -Path $localDir | Out-Null
$passwordFile = Join-Path $localDir 'db-password.txt'
if (!(Test-Path -LiteralPath $passwordFile)) {
    [IO.File]::WriteAllText($passwordFile, ('Tl!' + [guid]::NewGuid().ToString('N') + 'a7Z'))
}
$dbPassword = [IO.File]::ReadAllText($passwordFile).Trim()
if ($dbPassword -notmatch '^[A-Za-z0-9!]+$') { throw 'Formato de credencial local inválido.' }
$sql = @"
IF DB_ID(N'TechLink') IS NULL CREATE DATABASE [TechLink];
GO
IF SUSER_ID(N'techlink_local') IS NULL
    CREATE LOGIN [techlink_local] WITH PASSWORD = N'$dbPassword', CHECK_POLICY = ON;
GO
USE [TechLink];
IF USER_ID(N'techlink_local') IS NULL CREATE USER [techlink_local] FOR LOGIN [techlink_local];
IF IS_ROLEMEMBER(N'db_owner', N'techlink_local') <> 1 ALTER ROLE [db_owner] ADD MEMBER [techlink_local];
GO
"@
$sqlFile = Join-Path $localDir 'setup.sql'
try {
    [IO.File]::WriteAllText($sqlFile, $sql)
    & sqlcmd -S $Server -E -C -b -l 10 -i $sqlFile
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar SQL Server. Revisa la instancia y tus permisos de Windows.' }
    Write-Host 'Base TechLink y usuario local preparados. La contraseña queda en .local/db-password.txt (ignorada por Git).'
} finally {
    if (Test-Path -LiteralPath $sqlFile) { Remove-Item -LiteralPath $sqlFile }
}
