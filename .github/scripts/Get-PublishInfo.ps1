param(
    [string]$ArtifactDir = '',
    [string]$Type = ''
)

$ErrorActionPreference = "Stop"

# ---- Output build.info.json to GITHUB_OUTPUT ----
$info = Get-Content "build.info.json" -Raw | ConvertFrom-Json

$info.PSObject.Properties | ForEach-Object {
    $name = $_.Name
    $value = $_.Value
    if ($value -is [array]) {
        "$name<<EOF" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
        $value | Out-File -FilePath $env:GITHUB_OUTPUT -Append
        "EOF" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
    } else {
        "$name=$value" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
    }
}

# ---- Generate metadata JSONs ----
if (-not $ArtifactDir) { return }
if (-not (Test-Path -LiteralPath $ArtifactDir)) { throw "ArtifactDir not found: $ArtifactDir" }

$jar = Get-ChildItem -Path $ArtifactDir -Filter "*.jar" | Where-Object { $_.Name -notlike "*-sources.jar" } | Select-Object -First 1
if (-not $jar) { throw "No jar found in $ArtifactDir" }
$JarFilename = $jar.Name

$version = $info.mod_version
$isSnapshot = ($Type -ne "release")
$shortVersion = if ($isSnapshot) { "$version-s$env:GITHUB_RUN_NUMBER" } else { $version }

"short_version=$shortVersion" | Out-File -FilePath $env:GITHUB_OUTPUT -Append

$mcVersionList = $info.game_versions | ForEach-Object {
    $parts = $_ -split '\.'
    "$($parts[0]).$($parts[1])"
} | Sort-Object -Unique | Sort-Object { 
    $v = $_ -split '\.'
    [int]$v[0] * 100000 + [int]$v[1]
}

$suffix = if ($isSnapshot) { "-beta" } else { "" }
$jarKey = "mods/$JarFilename"
$baseUrl = "https://csl.3-3.dev"

@{
    version     = $shortVersion
    downloads   = @{ Universal = "$baseUrl/$jarKey" }
    launchermeta = @{}
} | ConvertTo-Json -Depth 10 | Set-Content -Path "$ArtifactDir/latest$suffix.json" -NoNewline

$details = [ordered]@{}
foreach ($mcVer in $mcVersionList) {
    $details[$mcVer] = @{
        Fabric   = "$baseUrl/$jarKey"
        Forge    = "$baseUrl/$jarKey"
        NeoForge = "$baseUrl/$jarKey"
        Quilt    = "$baseUrl/$jarKey"
    }
}
@{
    version   = $shortVersion
    timestamp = [System.DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    details   = $details
} | ConvertTo-Json -Depth 10 | Set-Content -Path "$ArtifactDir/detail$suffix.json" -NoNewline
