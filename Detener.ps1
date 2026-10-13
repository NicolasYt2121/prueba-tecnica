$ErrorActionPreference='Stop'
$taskWork=Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) 'work'
$pidFile=Join-Path $taskWork 'app.pid'
if(Test-Path $pidFile) {
    $appProcess=Get-Process -Id ([int](Get-Content $pidFile)) -ErrorAction SilentlyContinue
    $expectedJava=Get-ChildItem (Join-Path $taskWork 'tools') -Directory -Filter 'jdk-*' | Select-Object -First 1
    if($appProcess -and $appProcess.Path -eq (Join-Path $expectedJava.FullName 'bin/java.exe')) {Stop-Process -Id $appProcess.Id;Write-Host 'Aplicación detenida.'}
}
& (Join-Path $taskWork 'tools/pgsql/bin/pg_ctl.exe') -D (Join-Path $taskWork 'pgdata') -m fast -w stop
Write-Host 'Los datos se conservan.'
