$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (Test-Path -LiteralPath '.env') { Write-Host '.env already exists. Kept your settings.'; exit 0 }
$email = Read-Host 'Administrator email (Enter for admin@smartbus.tn)'
if ([string]::IsNullOrWhiteSpace($email)) { $email = 'admin@smartbus.tn' }
if ($email -notmatch '^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') { throw 'Enter a valid email address.' }
$secure = Read-Host 'Choose administrator password (12+ ASCII letters/numbers/symbols, no spaces or quotes)' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
if ($password.Length -lt 12 -or $password.Length -gt 72 -or $password -notmatch '^[A-Za-z0-9!@#%&*+,.?:_=/-]+$') { throw 'Use 12-72 characters from the suggested letters, digits and symbols.' }
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$internal = ([BitConverter]::ToString($bytes)).Replace('-','').ToLowerInvariant()
@"
ADMIN_EMAIL=$email
ADMIN_PASSWORD='$password'
INTERNAL_TOKEN=$internal
WEB_PORT=4200
API_PORT=3000
MONGO_PORT=27019
SEED_DATA=true
"@ | Set-Content -LiteralPath '.env' -Encoding ASCII
$password = $null
Write-Host 'Created .env. Keep this file private.'
