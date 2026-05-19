param()

$ErrorActionPreference = "Stop"

$props = Get-Content "gradle.properties"

function Get-Prop($key) {
    ($props | Select-String "^$key=") -replace "^.*=", "" | ForEach-Object { $_.Trim() }
}

function Set-MultilineOutput($name, $value) {
    "$name<<EOF" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
    $value | Out-File -FilePath $env:GITHUB_OUTPUT -Append
    "EOF" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
}

$loaders = Get-Prop "loaders"
$gameVersions = Get-Prop "game_versions"
$java = Get-Prop "java"
$version = Get-Prop "mod_version"

$loadersList = $loaders -split "," | ForEach-Object { $_.Trim() }
$gameVersionsList = $gameVersions -split "," | ForEach-Object { $_.Trim() }
$javaList = $java -split "," | ForEach-Object { $_.Trim() }

Set-MultilineOutput "loaders" $loadersList
Set-MultilineOutput "game-versions" $gameVersionsList
Set-MultilineOutput "java" $javaList
"short-version=$version" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
