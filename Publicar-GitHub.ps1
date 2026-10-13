param([string]$Repository='NicolasYt2121/prueba-tecnica')
$ErrorActionPreference='Stop'
$projectRoot=$PSScriptRoot
$workspaceRoot=Split-Path (Split-Path $projectRoot -Parent) -Parent
$ghTask=Join-Path $workspaceRoot 'work/tools/gh/bin/gh.exe'
if(!(Test-Path $ghTask)) {
    $ghCommand=Get-Command gh -ErrorAction SilentlyContinue
    if(!$ghCommand){throw 'No se encuentra GitHub CLI. Instala gh desde https://cli.github.com/ y vuelve a ejecutar este script.'}
    $ghTask=$ghCommand.Source
}
if(!(Get-Command git -ErrorAction SilentlyContinue)) {
    $bundledGit=Join-Path $env:USERPROFILE '.cache/codex-runtimes/codex-primary-runtime/dependencies/native/git/cmd'
    if(Test-Path (Join-Path $bundledGit 'git.exe')){$env:Path="$bundledGit;$env:Path"}
    else {throw 'No se encuentra Git. Instala Git para Windows desde https://git-scm.com/.'}
}
if($Repository -notmatch '^NicolasYt2121/[A-Za-z0-9_.-]+$'){throw 'Este script publica únicamente en la cuenta NicolasYt2121.'}
function Invoke-GhProbe {
    $ErrorActionPreference='Continue'
    $probeOutput=& $ghTask @args 2>$null
    [pscustomobject]@{ExitCode=$LASTEXITCODE;Output=$probeOutput}
}
$authTask=Invoke-GhProbe auth status --hostname github.com
if($authTask.ExitCode -ne 0) {
    Write-Host 'Inicia sesión en GitHub cuando se abra el navegador. No compartas tu contraseña ni tokens en el chat.'
    & $ghTask auth login --hostname github.com --git-protocol https --web --skip-ssh-key
    if($LASTEXITCODE -ne 0){throw 'No se completó el inicio de sesión.'}
}
$loginTask=& $ghTask api user --jq .login
if($LASTEXITCODE -ne 0 -or $loginTask.Trim() -ine 'NicolasYt2121'){throw 'La sesión activa no corresponde a NicolasYt2121. Cambia de cuenta con gh auth switch antes de publicar.'}
& $ghTask auth setup-git --hostname github.com
if($LASTEXITCODE -ne 0){throw 'No se pudo configurar Git con la sesión de GitHub.'}

# La preparación local debe completarse antes de crear el repositorio remoto.
& (Join-Path $projectRoot 'Finalizar-Git.ps1')
if(!$?){throw 'No se completó el historial local.'}
$remoteTask="https://github.com/$Repository.git"
$existingTask=Invoke-GhProbe repo view $Repository --json nameWithOwner --jq .nameWithOwner
if($existingTask.ExitCode -eq 0) {
    $remoteRefs=& git ls-remote $remoteTask
    if($LASTEXITCODE -ne 0){throw 'No se pudo verificar el repositorio existente.'}
    if($remoteRefs){throw "El repositorio $Repository ya contiene historial. No se alteró. Revisa su contenido o usa otro nombre con -Repository NicolasYt2121/otro-nombre."}
} else {
    & $ghTask repo create $Repository --private --description 'Prueba técnica: Java, Spring Boot, PostgreSQL, frontend, Docker y pruebas JUnit.'
    if($LASTEXITCODE -ne 0){throw 'No se pudo crear el repositorio privado. Revisa la conexión y los permisos de GitHub.'}
}
& (Join-Path $projectRoot 'Finalizar-Git.ps1') -RemoteUrl $remoteTask
if(!$?){throw 'No se pudo completar la publicación.'}
$actualTask=& git -c "safe.directory=$($projectRoot.Replace('\','/'))" -C $projectRoot rev-parse main
$publishedTask=& git ls-remote $remoteTask refs/heads/main
if($LASTEXITCODE -ne 0 -or !$publishedTask.StartsWith($actualTask)){throw 'No se confirmó que main remoto coincida con el commit local.'}
Write-Host "Publicación confirmada: https://github.com/$Repository"
Write-Host 'Repositorio PRIVADO. Puedes conceder acceso al evaluador desde GitHub.'
