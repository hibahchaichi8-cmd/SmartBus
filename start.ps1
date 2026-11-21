$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not (Test-Path -LiteralPath '.env')) { & "$PSScriptRoot/init-env.ps1" }
# Windows PowerShell 5.1 converts native stderr warnings into error records.
# Check Docker's exit code instead of stopping on harmless warnings.
$previousPreference = $ErrorActionPreference
try {
    $ErrorActionPreference = 'Continue'
    docker info *> $null
    $dockerExitCode = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $previousPreference
}
if ($dockerExitCode -ne 0) { throw 'Open Docker Desktop and wait until its Linux engine is running.' }
try {
    $ErrorActionPreference = 'Continue'
    docker compose up -d --build
    $composeExitCode = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $previousPreference
}
if ($composeExitCode -ne 0) { throw 'Build/start failed. Read the first Docker error above. A Maven download error usually means a network/DNS issue; retry the same command when connected.' }
Write-Host 'Containers started. View status: docker compose ps. View logs: docker compose logs -f.'
Write-Host 'Open http://localhost:4200 (or WEB_PORT in .env). Java services may need a few more seconds on first start.'
