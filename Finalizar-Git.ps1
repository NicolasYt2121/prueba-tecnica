param([string]$RemoteUrl)
$ErrorActionPreference='Stop'
Set-Location -LiteralPath $PSScriptRoot
function Invoke-ProjectGit {
    & git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" @args
    if($LASTEXITCODE -ne 0){throw 'Git no pudo completar la operación. Revisa el mensaje anterior.'}
}
if(!(Get-Command git -ErrorAction SilentlyContinue)){throw 'Instala Git para Windows o agrega git al PATH.'}
Invoke-ProjectGit add .
Invoke-ProjectGit update-index --chmod=+x backend/mvnw
$changes=& git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" diff --cached --name-only
if($changes){Invoke-ProjectGit commit -m 'test: verify banking flows and document delivery'}
Invoke-ProjectGit checkout develop
Invoke-ProjectGit merge --no-ff feature/banking-core -m 'merge: integrate banking application'
$release=& git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" branch --list release/1.0.0
if(!$release){Invoke-ProjectGit checkout -b release/1.0.0}else{Invoke-ProjectGit checkout release/1.0.0}
Invoke-ProjectGit checkout main
Invoke-ProjectGit merge --no-ff release/1.0.0 -m 'release: deliver version 1.0.0'
$tag=& git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" tag --list v1.0.0
if(!$tag){Invoke-ProjectGit tag -a v1.0.0 -m 'Nexo Finanzas 1.0.0'}
Invoke-ProjectGit checkout develop
Invoke-ProjectGit merge --no-ff release/1.0.0 -m 'merge: synchronize release with develop'
Invoke-ProjectGit checkout main
if($RemoteUrl){
    if($RemoteUrl -notmatch '^https://github\.com/[^/]+/[^/]+(?:\.git)?$'){throw 'Usa la URL HTTPS de tu repositorio de GitHub.'}
    $remotes=& git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" remote
    $origin=$null
    if($remotes -contains 'origin'){$origin=& git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" remote get-url origin}
    if(!$origin){Invoke-ProjectGit remote add origin $RemoteUrl}elseif($origin -ne $RemoteUrl){throw 'Ya existe otro remoto origin. No se reemplazó.'}
    Invoke-ProjectGit push -u origin main
    Invoke-ProjectGit push -u origin develop
    Invoke-ProjectGit push origin feature/banking-core release/1.0.0 --tags
}
Write-Host 'Historial local finalizado. Sin -RemoteUrl no se realiza ninguna publicación.'
