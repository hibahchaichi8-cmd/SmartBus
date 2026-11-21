$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
mvn -B -f services/pom.xml verify
if ($LASTEXITCODE -ne 0) { throw 'Java checks failed' }
npm --prefix services/gateway test
if ($LASTEXITCODE -ne 0) { throw 'Gateway checks failed' }
npm --prefix services/tracking-service test
if ($LASTEXITCODE -ne 0) { throw 'Tracking checks failed' }
npm --prefix frontend run build
if ($LASTEXITCODE -ne 0) { throw 'Angular build failed' }
