# Build a GitHub Actions test matrix for Minecraft loaders from build.info.json.

$ErrorActionPreference = "Stop"
$ProgressPreference    = "SilentlyContinue"

# --- Remote metadata endpoints -------------------------------------------------
$GameVersionManifestUrl     = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
$FabricGameVersionsUrl      = "https://meta.fabricmc.net/v2/versions/game"
$QuiltGameVersionsUrl       = "https://meta.quiltmc.org/v3/versions/game"
$ForgeMetadataUrl           = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml"
$NeoForgeMetadataUrl        = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"
$FabricInstallerMetadataUrl = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml"
$QuiltInstallerMetadataUrl  = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-installer/maven-metadata.xml"

# --- Helpers -------------------------------------------------------------------
# Timestamped progress message.
function Log([string]$Message) { Write-Host "[$(Get-Date -Format s)] $Message" }

# Download a URL with 5 attempts and linear backoff; -AsJson returns parsed JSON instead of raw text.
function Get-Remote([string]$Url, [switch]$AsJson) {
    for ($attempt = 1; $attempt -le 5; $attempt++) {
        try {
            if ($AsJson) { return Invoke-RestMethod -Uri $Url -TimeoutSec 120 }
            return (Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 120).Content
        } catch {
            if ($attempt -ge 5) { throw }
            Start-Sleep -Seconds ($attempt * 2)
        }
    }
}

# Newest release from Maven metadata: release, then latest, then last listed.
function Get-MavenLatestVersion([string]$MetadataUrl) {
    $versioning = ([xml](Get-Remote $MetadataUrl)).metadata.versioning
    $version = [string]$versioning.release
    if (-not $version) { $version = [string]$versioning.latest }
    if (-not $version) { $version = [string]@($versioning.versions.version)[-1] }
    return $version
}

# Pick, per game version, the newest loader build whose id matches the given prefix.
function Resolve-LoaderBuilds {
    param([string[]]$Versions, [string[]]$GameVersions, [string]$Loader, [scriptblock]$Prefix)
    $result = @{}
    foreach ($gameVersion in $GameVersions) {
        $buildPrefix = & $Prefix $gameVersion
        $hits = @($Versions | Where-Object { $_.StartsWith($buildPrefix) })
        if ($hits.Count -eq 0) { Log "${Loader}: no build for $gameVersion, skipped"; continue }
        $result[$gameVersion] = [string]@($hits | Sort-Object {
            $key = ($_.Substring($buildPrefix.Length) -split "-")[0] -split "\." | ForEach-Object {
                $n = 0; if ($_ -match "^\d+$") { $n = [int]$_ }
                $n.ToString("D10")
            }
            ($key -join "").PadRight(80, "0")
        })[-1]
    }
    return $result
}

# --- Read inputs ---------------------------------------------------------------
$info         = Get-Content -LiteralPath "build.info.json" -Raw | ConvertFrom-Json
$loaders      = @($info.loaders)
$gameVersions = @($info.game_versions)

# --- Fetch version metadata ----------------------------------------------------
Log "Fetching version metadata"
$mojangManifest   = Get-Remote $GameVersionManifestUrl -AsJson
$fabricGame       = Get-Remote $FabricGameVersionsUrl -AsJson
$quiltGame        = Get-Remote $QuiltGameVersionsUrl -AsJson
$forgeVersions    = @(([xml](Get-Remote $ForgeMetadataUrl)).metadata.versioning.versions.version)
$neoForgeVersions = @(([xml](Get-Remote $NeoForgeMetadataUrl)).metadata.versioning.versions.version)

# Game versions supported by the Fabric / Quilt loaders.
$fabricSupported = @{}; $fabricGame | ForEach-Object { $fabricSupported[[string]$_.version] = $true }
$quiltSupported  = @{}; $quiltGame  | ForEach-Object { $quiltSupported[[string]$_.version]  = $true }

# Resolve installer versions only for the loaders that were requested.
$installerVersion = @{}
if ($loaders -contains "fabric") { $installerVersion.fabric = Get-MavenLatestVersion $FabricInstallerMetadataUrl }
if ($loaders -contains "quilt")  { $installerVersion.quilt  = Get-MavenLatestVersion $QuiltInstallerMetadataUrl }

# Newest Forge / NeoForge build per game version.
$forgeByGameVersion    = @{}
$neoForgeByGameVersion = @{}
if ($loaders -contains "forge") {
    $forgeByGameVersion = Resolve-LoaderBuilds $forgeVersions $gameVersions "Forge" { param($v) "$v-" }
}
if ($loaders -contains "neoforge") {
    $neoForgeByGameVersion = Resolve-LoaderBuilds $neoForgeVersions $gameVersions "NeoForge" {
        param($v); $n = $v -replace "^1\.", ""
        if (($v -split "\.").Count -le 2) { "$n.0." } else { "$n." }
    }
}

# --- Build the test matrix -----------------------------------------------------
Log "Generating test matrix"
$manifestEntries = @{}
foreach ($entry in $mojangManifest.versions) { $manifestEntries[[string]$entry.id] = $entry }

# Per loader: game-version support lookup, installer version resolver, download URL template.
$loaderSpecs = [ordered]@{
    fabric   = @{ Supported = $fabricSupported;       Version = { $installerVersion.fabric };                Jar = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/{0}/fabric-installer-{0}.jar" }
    forge    = @{ Supported = $forgeByGameVersion;    Version = { param($gv) $forgeByGameVersion[$gv] };     Jar = "https://maven.minecraftforge.net/net/minecraftforge/forge/{0}/forge-{0}-installer.jar" }
    neoforge = @{ Supported = $neoForgeByGameVersion; Version = { param($gv) $neoForgeByGameVersion[$gv] };  Jar = "https://maven.neoforged.net/releases/net/neoforged/neoforge/{0}/neoforge-{0}-installer.jar" }
    quilt    = @{ Supported = $quiltSupported;        Version = { $installerVersion.quilt };                 Jar = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-installer/{0}/quilt-installer-{0}.jar" }
}

$matrixInclude = @()
foreach ($gameVersion in $gameVersions) {
    $entry = $manifestEntries[$gameVersion]
    if (-not $entry) { throw "Version not found in Mojang manifest: $gameVersion" }

    # Keep only the requested loaders that support this game version.
    $entryLoaders = @(foreach ($name in $loaderSpecs.Keys) {
        $spec = $loaderSpecs[$name]
        if ($loaders -notcontains $name -or -not $spec.Supported.ContainsKey($gameVersion)) { continue }
        $version = & $spec.Version $gameVersion
        [pscustomobject]@{ name = $name; version = $version; url = $spec.Jar -f $version }
    })
    if ($entryLoaders.Count -eq 0) { Log "No loader available for $gameVersion, skipped"; continue }

    $matrixInclude += [pscustomobject]@{
        version   = $gameVersion
        json_url  = [string]$entry.url
        json_sha1 = [string]$entry.sha1
        loaders   = $entryLoaders
    }
}

# --- Emit results --------------------------------------------------------------
$matrixJson = @{ include = $matrixInclude } | ConvertTo-Json -Compress -Depth 10
@("matrix<<EOF", $matrixJson, "EOF") | Out-File -FilePath $env:GITHUB_OUTPUT -Append
Log $matrixJson
Log "Done. Generated $($matrixInclude.Count) matrix entry(ies)"
