param([int]$Port=8080)
$ErrorActionPreference='Stop'
$projectRoot=$PSScriptRoot
$workspaceRoot=Split-Path (Split-Path $projectRoot -Parent) -Parent
$taskWork=Join-Path $workspaceRoot 'work'
$toolsRoot=Join-Path $taskWork 'tools'
$javaHomeTask=Get-ChildItem $toolsRoot -Directory -Filter 'jdk-*' | Select-Object -First 1
$pgBin=Join-Path $toolsRoot 'pgsql/bin'
$jar=Join-Path $projectRoot 'backend/target/banking-1.0.0.jar'
if(!$javaHomeTask -or !(Test-Path $jar)) {throw 'Falta Java portátil o el JAR compilado. Consulta README.md para instalar/compilar o usa Docker Compose.'}
$envFile=Join-Path $projectRoot '.env'
if(!(Test-Path $envFile)) {throw 'Crea .env a partir de .env.example con la contraseña de la base local.'}
$passwordLine=Get-Content -LiteralPath $envFile | Where-Object {$_ -match '^DB_PASSWORD='} | Select-Object -First 1
if(!$passwordLine) {throw 'Falta DB_PASSWORD en .env'}
$env:DB_PASSWORD=$passwordLine.Substring('DB_PASSWORD='.Length)
$env:PGPASSWORD=$env:DB_PASSWORD
$env:DB_USER='nexo'
$env:DB_URL='jdbc:postgresql://127.0.0.1:55432/nexo'
$env:PORT=[string]$Port
$env:BIND_ADDRESS='127.0.0.1'
& (Join-Path $pgBin 'pg_isready.exe') -h 127.0.0.1 -p 55432 -U nexo | Out-Null
if($LASTEXITCODE -ne 0) {
    $data=Join-Path $taskWork 'pgdata'
    if(!(Test-Path (Join-Path $data 'PG_VERSION'))) {throw 'No existe la base local preparada. Usa Docker Compose o configura PostgreSQL según README.md.'}
    Start-Process -FilePath (Join-Path $pgBin 'postgres.exe') -ArgumentList @('-D',('"'+$data+'"'),'-p','55432','-h','127.0.0.1') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $taskWork 'postgres-stdout.log') -RedirectStandardError (Join-Path $taskWork 'postgres-stderr.log') | Out-Null
    for($attempt=0;$attempt -lt 30;$attempt++) {& (Join-Path $pgBin 'pg_isready.exe') -h 127.0.0.1 -p 55432 -U nexo | Out-Null;if($LASTEXITCODE -eq 0){break};Start-Sleep -Seconds 1}
}
try {$health=Invoke-RestMethod "http://localhost:$Port/actuator/health" -TimeoutSec 2;if($health.status -eq 'UP'){Write-Host "Aplicación disponible: http://localhost:$Port";return}} catch {}
$process=Start-Process -FilePath (Join-Path $javaHomeTask.FullName 'bin/java.exe') -ArgumentList @('-jar',('"'+$jar+'"')) -WindowStyle Hidden -RedirectStandardOutput (Join-Path $taskWork 'app.log') -RedirectStandardError (Join-Path $taskWork 'app-error.log') -PassThru
Set-Content -LiteralPath (Join-Path $taskWork 'app.pid') -Value $process.Id
for($attempt=0;$attempt -lt 60;$attempt++) {
    Start-Sleep -Seconds 1
    try {$health=Invoke-RestMethod "http://localhost:$Port/actuator/health" -TimeoutSec 2;if($health.status -eq 'UP'){Write-Host "Aplicación disponible: http://localhost:$Port";return}} catch {}
    if($process.HasExited){throw "La aplicación se detuvo. Revisa $taskWork\app.log"}
}
throw "No se confirmó el arranque. Revisa $taskWork\app.log"
