param(
    [string] $SourcePath = "Bootstrap/build/libs",
    [string] $DestinationPath = "build/publish"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $ScriptRoot "../..")).Path
Set-Location -LiteralPath $RepositoryRoot

function Resolve-RepoPath {
    param([Parameter(Mandatory = $true)][string] $Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }

    return [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($RepositoryRoot, $Path))
}

$source = Resolve-RepoPath $SourcePath
$destination = Resolve-RepoPath $DestinationPath

New-Item -ItemType Directory -Force -Path $destination | Out-Null

$jars = Get-ChildItem -LiteralPath $source -Filter "CustomSkinLoader_Universal-*.jar" -File |
    Where-Object { $_.Name -notlike "*-sources.jar" }

if (-not $jars) {
    throw "No Universal jar found under $SourcePath."
}

$jars | Copy-Item -Destination $destination -Force
