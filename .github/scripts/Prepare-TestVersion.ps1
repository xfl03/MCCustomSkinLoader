# 1. downloads and runs the mod loader installers (fabric / quilt / forge / neoforge)
# 2. resolves the "inheritsFrom" chain of every installed version JSON
# 3. downloads client, server, library, native, logging and asset artifacts
# 4. emits one self-contained launch script (client/versions/<id>.ps1) per installed client

param(
    [Parameter(Mandatory)][string]$MinecraftVersion,
    [Parameter(Mandatory)][string]$MinecraftJsonUrl,
    [string]$MinecraftJsonSha1 = "",
    [string]$InstallersJson = $env:INSTALLERS
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"    # dropping the download progress bar speeds things up a lot
$ThrottleLimit = 32                         # max concurrent download workers

# ---- environment ----
$ServerAddress = "127.0.0.1"
$ServerPort = 25565
$WorkingDirectory = (Get-Location).Path
$TestJarPath = Join-Path $WorkingDirectory "Test/run/client/CustomSkinLoader-Test-1.0.0.jar"
$InstallerLogsDir = Join-Path $WorkingDirectory "installer-logs"
$JavaRuntimeIndexUrl = "https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json"
$ExternalArgs = Import-PowerShellDataFile -LiteralPath (Join-Path $PSScriptRoot "ExternalArgs.psd1")

$OsName = "windows"
$OsArch = "x86_64"
$OsVersion = [System.Environment]::OSVersion.Version
$NativeArch = "64"
# Feature flags the version JSON "rules" are evaluated against (see Test-Rules).
$Features = @{
    is_demo_user               = $false
    has_custom_resolution      = $false
    has_quick_plays_support    = $false
    is_quick_play_singleplayer = $false
    is_quick_play_multiplayer  = $true
    is_quick_play_realms       = $false
}

# ----- paths -----
$RunDir = Join-Path $WorkingDirectory "Test/run"
$ClientDir = Join-Path $RunDir "client"
$ServerDir = Join-Path $RunDir "server"
$VersionsDir = Join-Path $ClientDir "versions"
$LibrariesDir = Join-Path $ClientDir "libraries"
$AssetsDir = Join-Path $ClientDir "assets"
$AssetIndexesDir = Join-Path $AssetsDir "indexes"
$AssetObjectsDir = Join-Path $AssetsDir "objects"
$LogConfigsDir = Join-Path $AssetsDir "log_configs"
$JavaBaseDir = Join-Path $RunDir "java"
$MesaDir = Join-Path $RunDir "mesa"

# --- small helpers ---
# Timestamped progress line, so every step is easy to spot in the CI log.
function Write-Step { param([string]$Message) Write-Host "[$(Get-Date -Format s)] $Message" }

# Terse factory for the {Url,Path,Sha1,TimeoutSec} records consumed by Invoke-Downloads.
function New-Dl { param([string]$Url, [string]$Path, [string]$Sha1 = "", [int]$TimeoutSec = 10) [pscustomobject]@{ Url = $Url; Path = $Path; Sha1 = $Sha1; TimeoutSec = $TimeoutSec } }

# SHA-1 verified, parallel, retrying downloader. Files whose hash already matches are
# skipped; everything else is fetched as "<path>.download" and only moved into place
# after verification. 5 attempts with linear back-off, -WarnOnFailure turns the final
# failure into a warning instead of an error (used for the best-effort asset objects).
function Invoke-Downloads {
    param([object[]]$Downloads, [bool]$WarnOnFailure = $false)
    if (-not $Downloads) { return }
    $Downloads | ForEach-Object -Parallel {
        $url = [string]$_.Url
        if (-not $url) { return }
        $path = [string]$_.Path
        $expectedSha1 = if ($_.Sha1) { ([string]$_.Sha1).ToUpperInvariant() } else { "" }
        $timeoutSec = [int]$_.TimeoutSec
        $isValid = {
            param([string]$File)
            if (-not (Test-Path -LiteralPath $File -PathType Leaf)) { return $false }
            # A hash is authoritative (some Java runtime files are legitimately empty); the
            # non-empty fallback only applies to files downloaded without an expected hash.
            if ($expectedSha1) { return (Get-FileHash -LiteralPath $File -Algorithm SHA1).Hash -eq $expectedSha1 }
            return (Get-Item -LiteralPath $File -Force).Length -gt 0
        }
        if (& $isValid $path) { return }    # already cached and verified
        $directory = Split-Path -Parent $path
        if ($directory -and -not (Test-Path -LiteralPath $directory)) { New-Item -ItemType Directory -Force -Path $directory | Out-Null }
        $temp = "$path.download"
        for ($attempt = 1; $attempt -le 5; $attempt++) {
            try {
                if (Test-Path -LiteralPath $temp) { Remove-Item -LiteralPath $temp -Force }
                Invoke-WebRequest -Uri $url -OutFile $temp -TimeoutSec $timeoutSec
                if (-not (& $isValid $temp)) { throw "SHA1 verification failed" }
                Move-Item -LiteralPath $temp -Destination $path -Force
                return
            } catch {
                if (Test-Path -LiteralPath $temp) { Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue }
                if ($attempt -ge 5) {
                    if ($using:WarnOnFailure) { Write-Warning "Failed to download $url : $($_.Exception.Message)"; return }
                    throw "Failed to download $url : $($_.Exception.Message)"
                }
                Start-Sleep -Seconds ($attempt * 2)
            }
        }
    } -ThrottleLimit $ThrottleLimit
}

# Evaluates a Mojang launcher "rules" array against this host. Rules are applied in
# order and the LAST matching rule decides, so an unmatched rule keeps the previous
# verdict. No rules at all means "allowed".
function Test-Rules {
    param($Rules)
    if (-not $Rules) { return $true }
    $allowed = $false
    foreach ($rule in @($Rules)) {
        $matched = $true
        if ($rule.os) {
            if ($rule.os.name -and [string]$rule.os.name -ne $OsName) { $matched = $false }
            if ($rule.os.arch -and [string]$rule.os.arch -ne $OsArch) { $matched = $false }
            if ($matched -and $rule.os.version -and [string]$OsVersion -notmatch [string]$rule.os.version) { $matched = $false }
            if ($matched -and $rule.os.versionRange) {
                if ($rule.os.versionRange.min -and $OsVersion -lt [version][string]$rule.os.versionRange.min) { $matched = $false }
                if ($rule.os.versionRange.max -and $OsVersion -gt [version][string]$rule.os.versionRange.max) { $matched = $false }
            }
        }
        if ($matched -and $rule.features) {
            foreach ($featureFlag in $rule.features.PSObject.Properties) {
                if ($Features[$featureFlag.Name] -ne $featureFlag.Value) { $matched = $false; break }
            }
        }
        if ($matched) { $allowed = [string]$rule.action -eq "allow" }
    }
    return $allowed
}

# Maven base URL of a library: explicit "url", else the vanilla libraries host. Always returned with a trailing slash.
function Get-LibraryBaseUrl {
    param($Library, [string[]]$Parts)
    $url = if ($Library.url) { [string]$Library.url }
    else { "https://libraries.minecraft.net/" }
    if (-not $url.EndsWith("/")) { $url += "/" }
    return $url
}

# Normalizes an artifact (or natives classifier) node into {Path,Url,Sha1}. When the JSON
# has no such node the path is derived from the name instead of leaving it unresolved.
function New-ArtifactRecord {
    param($Library, [string[]]$Parts, $Artifact, [string]$Classifier)
    if ($Artifact -and $Artifact.path) {
        $url = if ($Artifact.url) { [string]$Artifact.url } else { (Get-LibraryBaseUrl $Library $Parts) + [string]$Artifact.path }
        return [pscustomobject]@{ Path = [string]$Artifact.path; Url = $url; Sha1 = [string]$Artifact.sha1 }
    }
    $childParts = if ($Classifier) { @($Parts[0], $Parts[1], $Parts[2], $Classifier) } else { $Parts }
    $suffix = if ($childParts.Count -ge 4 -and $childParts[3]) { "-$($childParts[3])" } else { "" }
    $path = "$($childParts[0] -replace '\.', '/')/$($childParts[1])/$($childParts[2])/$($childParts[1])-$($childParts[2])$suffix.jar"
    return [pscustomobject]@{ Path = $path; Url = ((Get-LibraryBaseUrl $Library $Parts) + $path); Sha1 = "" }
}

# Main artifact of a library, or $null for libraries that declare a "downloads" block but
# no artifact (those are provided by a mod loader instead) or have a malformed name.
function Get-LibraryArtifact {
    param($Library)
    $parts = @([string]$Library.name -split ":")
    if ($parts.Count -lt 3) { return $null }
    if ($Library.downloads.artifact -and $Library.downloads.artifact.path) { return New-ArtifactRecord $Library $parts $Library.downloads.artifact "" }
    if ($Library.downloads) { return $null }
    return New-ArtifactRecord $Library $parts $null ""
}

# Quotes a value for embedding into a generated .ps1. Launcher placeholders such as
# ${classpath} contain '$', so those values need double quotes with backtick escaping;
# everything else is cheaper and safer as a single quoted literal.
function ConvertTo-PowerShellLiteral {
    param([string]$Value)
    if (-not $Value.Contains('${')) { return "'" + $Value.Replace("'", "''") + "'" }
    $bt = [string][char]96
    return '"' + $Value.Replace($bt, $bt + $bt).Replace('"', $bt + '"') + '"'
}

# Expands a version JSON argument array: plain strings pass through, rule gated entries are
# kept only when their rules match, and array values are flattened into the result.
function Expand-ArgumentList {
    param($Arguments)
    $result = @()
    foreach ($entry in @($Arguments)) {
        if ($entry -is [string]) { $result += $entry; continue }
        if ($null -eq $entry.value -or -not (Test-Rules $entry.rules)) { continue }
        $result += if ($entry.value -is [System.Array]) { @($entry.value) } else { [string]$entry.value }
    }
    return $result
}

# Merges a parent version object into its child: missing properties are inherited, arrays
# (libraries, arguments) are appended child first, and "arguments" is merged recursively
# because it is an object holding two arrays.
function Merge-VersionObject {
    param($Child, $Parent)
    foreach ($property in $Parent.PSObject.Properties) {
        $name = $property.Name
        $childProperty = $Child.PSObject.Properties[$name]
        if ($null -eq $childProperty) { $Child | Add-Member -MemberType NoteProperty -Name $name -Value $property.Value; continue }
        $childValue = $childProperty.Value
        if ($childValue -is [System.Array]) { $Child.$name = @($childValue) + @($property.Value) }
        elseif ($name -eq "arguments" -and $childValue -is [System.Management.Automation.PSCustomObject] -and $property.Value -is [System.Management.Automation.PSCustomObject]) {
            Merge-VersionObject -Child $childValue -Parent $property.Value
        }
    }
}

# Depth-first resolution of "inheritsFrom": the parent is merged first, the result is cached
# in $mergedObjects, and $resolving turns a cyclic inheritance into a clear error.
function Resolve-VersionObject {
    param([string]$Id)
    if ($mergedObjects.Contains($Id)) { return $mergedObjects[$Id] }
    if (-not $versionObjects.Contains($Id)) { throw "Missing parent version JSON: $Id" }
    if (-not $resolving.Add($Id)) { throw "Circular inheritsFrom detected at: $Id" }
    $versionObject = $versionObjects[$Id]
    if ($versionObject.inheritsFrom) {
        $parent = Resolve-VersionObject -Id ([string]$versionObject.inheritsFrom)
        Merge-VersionObject -Child $versionObject -Parent $parent
    }
    [void]$resolving.Remove($Id)
    $mergedObjects[$Id] = $versionObject
    return $versionObject
}

# Template of the generated launcher. Tokens (@@NAME@@) are replaced per version; the here
# string is single quoted so the '$' of the generated PowerShell stays literal.
$LaunchTemplate = @'
$ErrorActionPreference = "Stop"

$version_name = @@VERSION_NAME@@
$game_directory = $PSScriptRoot
$assets_root = Join-Path $PSScriptRoot "assets"
$assets_index_name = @@ASSETS_INDEX@@
$quickPlayMultiplayer = @@QUICK_PLAY@@
$auth_player_name = "Player"
$auth_uuid = "00000000-0000-0000-0000-000000000000"
$auth_access_token = "0"
$clientid = "0"
$auth_xuid = "0"
$user_properties = "{}"
$user_type = "legacy"
$version_type = "release"
$launcher_name = "CustomSkinLoader"
$launcher_version = @@LAUNCHER_VERSION@@
$library_directory = Join-Path $PSScriptRoot "libraries"
$classpath_separator = [System.IO.Path]::PathSeparator
$primary_jar = Join-Path $PSScriptRoot @@PRIMARY_JAR@@
$natives_directory = Join-Path $PSScriptRoot @@NATIVES_DIR@@
@@LOGGING_CONFIG_LINE@@
$classpath = @(
@@CLASSPATH_ENTRIES@@
    $primary_jar
) -join $classpath_separator
$mainClass = @@MAIN_CLASS@@
$jvmArgs = @(
@@JVM_ARGUMENTS@@)
$gameArgs = @(
@@GAME_ARGUMENTS@@)

& java @jvmArgs $mainClass @gameArgs
exit $LASTEXITCODE
'@

# ======= main =======
foreach ($directory in @($RunDir, $ClientDir, $ServerDir, $VersionsDir, $LibrariesDir, $AssetIndexesDir, $AssetObjectsDir, $LogConfigsDir, $InstallerLogsDir, $JavaBaseDir)) {
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
}

$modVersion = [string](Get-Content -LiteralPath "build.info.json" -Raw | ConvertFrom-Json).mod_version

Write-Step "Downloading version JSON"
$versionJsonPath = Join-Path $VersionsDir $MinecraftVersion "$MinecraftVersion.json"
Invoke-Downloads -Downloads (New-Dl $MinecraftJsonUrl $versionJsonPath $MinecraftJsonSha1)
$versionJson = Get-Content -LiteralPath $versionJsonPath -Raw | ConvertFrom-Json

# --- Mojang Java runtime ---
# "javaVersion.component" names an entry of the Mojang java-runtime index; very old
# version JSONs predate the field and were always launched with jre-legacy.
$javaComponent = if ($versionJson.javaVersion.component) { [string]$versionJson.javaVersion.component } else { "jre-legacy" }
Write-Step "Downloading Java runtime $javaComponent"
$javaIndexPath = Join-Path $RunDir "java-runtime-index.json"
Invoke-Downloads -Downloads (New-Dl $JavaRuntimeIndexUrl $javaIndexPath)
$javaIndex = Get-Content -LiteralPath $javaIndexPath -Raw | ConvertFrom-Json
$javaRuntimes = @($javaIndex.'windows-x64'.$javaComponent | Where-Object { $_ })
if ($javaRuntimes.Count -eq 0) { throw "No Mojang Java runtime '$javaComponent' for windows-x64" }
$javaRuntime = @($javaRuntimes | Sort-Object { $_.version.released })[-1]
$javaManifestPath = Join-Path $JavaBaseDir "manifest.json"
$javaDir = Join-Path $JavaBaseDir $javaComponent
Invoke-Downloads -Downloads (New-Dl ([string]$javaRuntime.manifest.url) $javaManifestPath ([string]$javaRuntime.manifest.sha1))
$javaManifest = Get-Content -LiteralPath $javaManifestPath -Raw | ConvertFrom-Json
$javaDownloads = foreach ($file in $javaManifest.files.PSObject.Properties) {
    if ($file.Value.type -eq 'directory') { New-Item -ItemType Directory -Force -Path (Join-Path $javaDir $file.Name) | Out-Null }
    elseif ($file.Value.type -eq 'file') { New-Dl ([string]$file.Value.downloads.raw.url) (Join-Path $javaDir $file.Name) ([string]$file.Value.downloads.raw.sha1) 600 }
}
Write-Step "Downloading $($javaDownloads.Count) Java runtime file(s)"
Invoke-Downloads -Downloads $javaDownloads
Join-Path $javaDir "bin" | Out-File -FilePath $env:GITHUB_PATH -Append
$InstallerJava = Join-Path $env:JAVA_HOME_25_X64 "bin/java.exe"

# --- Mesa3D ---
# The runner has no GPU, so OpenGL is provided by Mesa's software renderer.
# The DLLs go next to java.exe so every JVM picks them up.
Write-Step "Setting up Mesa3D"
New-Item -ItemType Directory -Force -Path $MesaDir | Out-Null
gh release download --repo pal1000/mesa-dist-win --pattern "mesa3d-*-release-msvc.7z" --dir $MesaDir --clobber
if ($LASTEXITCODE -ne 0) { throw "Failed to download Mesa3D (exit code $LASTEXITCODE)" }
$mesaArchive = @(Get-ChildItem -LiteralPath $MesaDir -Filter "*.7z")[-1]
7z x "$($mesaArchive.FullName)" "-o$(Join-Path $MesaDir 'extracted')" -y | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Failed to extract Mesa3D (exit code $LASTEXITCODE)" }
Copy-Item -Path (Join-Path $MesaDir "extracted/x64/*.dll") -Destination (Join-Path $javaDir "bin") -Force

# --- mod loader installers ---
$installers = @($InstallersJson | ConvertFrom-Json)
$clients = New-Object System.Collections.Generic.List[string]   # generated launch scripts
$clientLoaders = @{}                                            # version id -> loader name

$installerDownloads = @($installers | ForEach-Object { New-Dl ([string]$_.url) (Join-Path $RunDir "$($_.name)-$($_.version)-installer.jar") })
Write-Step "Downloading $($installerDownloads.Count) installer(s)"
Invoke-Downloads -Downloads $installerDownloads

Write-Step "Installing mod loaders with $InstallerJava"
# The Forge 1.14.3 installer performs the DEOBF_REALMS post-processing step (net.minecraftforge.installertools.DeobfRealms), and when it downloads
# libraries/com/mojang/realms/1.14.17/realms-1.14.17.jar it does not create the parent directory, so installing in an empty directory throws NoSuchFileException,
# causing "Failed to download realms jar". Only the 1.14.3 Forge installer has this processor in the current matrix, so the directory is created in advance here.
New-Item -ItemType Directory -Force -Path (Join-Path $LibrariesDir "com/mojang/realms/1.14.17") | Out-Null

foreach ($installer in $installers) {
    $loader = [string]$installer.name
    $installerPath = Join-Path $RunDir "$loader-$($installer.version)-installer.jar"
    $installerLog = Join-Path $InstallerLogsDir "$loader-$MinecraftVersion.log"
    "[$(Get-Date -Format s)] $loader $MinecraftVersion" | Out-File -FilePath $installerLog -Encoding utf8
    Write-Step "[$loader] installing for $MinecraftVersion"
    $versionsBefore = @(Get-ChildItem -LiteralPath $VersionsDir -Directory | Select-Object -ExpandProperty Name)
    # Every loader has its own CLI flavour; forge additionally goes through the test jar.
    $arguments = switch ($loader) {
        "fabric" { @("-jar", $installerPath, "client", "-dir", $ClientDir, "-mcversion", $MinecraftVersion) }
        "quilt" { @("-jar", $installerPath, "install", "client", $MinecraftVersion, "--install-dir=$ClientDir") }
        "forge" { @("-cp", "$installerPath;$TestJarPath", "customskinloader.test.installer.Main", "--installClient", $ClientDir) }
        "neoforge" { @("-jar", $installerPath, "--installClient", $ClientDir) }
    }
    & $InstallerJava @arguments 2>&1 | Out-File -FilePath $installerLog -Encoding utf8 -Append
    "ExitCode: $LASTEXITCODE" | Out-File -FilePath $installerLog -Encoding utf8 -Append
    # The forge test jar drops its own log into the current directory; archive it too.
    $selfLog = Join-Path $WorkingDirectory "$(Split-Path -Leaf $installerPath).log"
    if (Test-Path -LiteralPath $selfLog) { Move-Item -LiteralPath $selfLog -Destination (Join-Path $InstallerLogsDir "$loader-$MinecraftVersion-installer.log") -Force }
    if ($LASTEXITCODE -ne 0) { throw "[$loader] failed to install for $MinecraftVersion (exit code $LASTEXITCODE)" }
    # The installer has to produce exactly one new version folder, that is the client id.
    $newVersions = @(Get-ChildItem -LiteralPath $VersionsDir -Directory | Where-Object { $versionsBefore -notcontains $_.Name })
    if ($newVersions.Count -ne 1) { throw "[$loader] expected one installed version for $MinecraftVersion, got $($newVersions.Count)" }
    [void]$clients.Add("$($newVersions[0].Name).ps1")
    $clientLoaders[[string]$newVersions[0].Name] = $loader
}

Write-Step "Resolving inheritsFrom"
$versionObjects = [ordered]@{}
foreach ($directory in Get-ChildItem -LiteralPath $VersionsDir -Directory) {
    $jsonPath = Join-Path $directory.FullName "$($directory.Name).json"
    if (-not (Test-Path -LiteralPath $jsonPath)) { continue }
    $versionObject = Get-Content -LiteralPath $jsonPath -Raw | ConvertFrom-Json
    $versionObjects[[string]$versionObject.id] = $versionObject
}
$mergedObjects = [ordered]@{}
$resolving = New-Object System.Collections.Generic.HashSet[string]
foreach ($id in @($versionObjects.Keys)) { [void](Resolve-VersionObject -Id $id) }
$allVersions = @($mergedObjects.Values)

# Client jars live next to their version JSON; server jars only exist on root versions.
Write-Step "Downloading client jars"
$clientDownloads = [ordered]@{}
$serverDownloads = [ordered]@{}
foreach ($versionObject in $allVersions) {
    $id = [string]$versionObject.id
    $client = $versionObject.downloads.client
    if ($client.url) {
        $destination = Join-Path $VersionsDir $id "$id.jar"
        $clientDownloads[$destination] = New-Dl ([string]$client.url) $destination ([string]$client.sha1)
    }
    if ($versionObject.inheritsFrom) { continue }
    $server = $versionObject.downloads.server
    if ($server.url) {
        $destination = Join-Path $ServerDir "$id.jar"
        $serverDownloads[$destination] = New-Dl ([string]$server.url) $destination ([string]$server.sha1)
    }
}
Invoke-Downloads -Downloads @($clientDownloads.Values)

Write-Step "Downloading server jars"
Invoke-Downloads -Downloads @($serverDownloads.Values)

Write-Step "Downloading libraries"
$libraryDownloads = [ordered]@{}
$nativeJarsByVersion = [ordered]@{}
foreach ($versionObject in $allVersions) {
    $versionId = [string]$versionObject.id
    $nativeJars = @{}
    foreach ($library in @($versionObject.libraries)) {
        if (-not (Test-Rules $library.rules)) { continue }
        $artifact = Get-LibraryArtifact $library
        if ($artifact) {
            $destination = Join-Path $LibrariesDir $artifact.Path
            $libraryDownloads[$destination] = New-Dl $artifact.Url $destination $artifact.Sha1
        }
        if (-not $library.natives.windows) { continue }
        # The natives mapping is a classifier; "${arch}" expands to 64 for this host.
        $classifier = ([string]$library.natives.windows).Replace('${arch}', $NativeArch)
        $parts = @([string]$library.name -split ":")
        $native = New-ArtifactRecord $library $parts $library.downloads.classifiers.$classifier $classifier
        $destination = Join-Path $LibrariesDir $native.Path
        $libraryDownloads[$destination] = New-Dl $native.Url $destination $native.Sha1
        $nativeJars[$native.Path] = @($library.extract.exclude)
    }
    if ($nativeJars.Count -gt 0) { $nativeJarsByVersion[$versionId] = $nativeJars }
}
Invoke-Downloads -Downloads @($libraryDownloads.Values)

Write-Step "Extracting natives"
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($versionId in $nativeJarsByVersion.Keys) {
    $destination = Join-Path $VersionsDir $versionId "natives"
    foreach ($nativePath in $nativeJarsByVersion[$versionId].Keys) {
        $excludeList = @($nativeJarsByVersion[$versionId][$nativePath])
        $archive = [System.IO.Compression.ZipFile]::OpenRead((Join-Path $LibrariesDir $nativePath))
        try {
            foreach ($entry in $archive.Entries) {
                if ($entry.FullName.EndsWith("/")) { continue }    # directory entry
                $excluded = $false
                foreach ($prefix in $excludeList) { if ($prefix -and $entry.FullName.StartsWith([string]$prefix)) { $excluded = $true; break } }
                if ($excluded) { continue }
                # Zip entries always use '/', the local file system may not.
                $target = Join-Path $destination ($entry.FullName -replace "/", [System.IO.Path]::DirectorySeparatorChar)
                if (Test-Path -LiteralPath $target) { continue }
                $targetDirectory = Split-Path -Parent $target
                if ($targetDirectory -and -not (Test-Path -LiteralPath $targetDirectory)) { New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null }
                [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
            }
        } finally {
            $archive.Dispose()
        }
    }
}

Write-Step "Downloading logging configs"
$loggingDownloads = [ordered]@{}
foreach ($versionObject in $allVersions) {
    $loggingFile = $versionObject.logging.client.file
    if (-not $loggingFile.id -or -not $loggingFile.url) { continue }
    $destination = Join-Path $LogConfigsDir ([string]$loggingFile.id)
    if ($loggingDownloads.Contains($destination)) { continue }
    $loggingDownloads[$destination] = New-Dl ([string]$loggingFile.url) $destination ([string]$loggingFile.sha1)
}
Invoke-Downloads -Downloads @($loggingDownloads.Values)

Write-Step "Downloading asset indexes"
$assetIndexDownloads = [ordered]@{}
foreach ($versionObject in $allVersions) {
    $assetIndex = $versionObject.assetIndex
    if (-not $assetIndex.id -or -not $assetIndex.url) { continue }
    $destination = Join-Path $AssetIndexesDir "$($assetIndex.id).json"
    $assetIndexDownloads[$destination] = New-Dl ([string]$assetIndex.url) $destination ([string]$assetIndex.sha1)
}
Invoke-Downloads -Downloads @($assetIndexDownloads.Values)

Write-Step "Downloading assets"
# Union of the object hashes of every asset index; the hash is also the expected SHA-1.
$assetHashes = [ordered]@{}
foreach ($indexFile in Get-ChildItem -LiteralPath $AssetIndexesDir -Filter "*.json" -File) {
    $assetIndex = Get-Content -LiteralPath $indexFile.FullName -Raw | ConvertFrom-Json
    foreach ($property in $assetIndex.objects.PSObject.Properties) {
        $hash = [string]$property.Value.hash
        if ($hash -and -not $assetHashes.Contains($hash)) { $assetHashes[$hash] = $true }
    }
}
$assetDownloads = $assetHashes.Keys | ForEach-Object {
    $prefix = $_.Substring(0, 2)
    New-Dl "https://resources.download.minecraft.net/$prefix/$_" (Join-Path $AssetObjectsDir $prefix $_) $_
}
Invoke-Downloads -Downloads @($assetDownloads) -WarnOnFailure $true

Write-Step "Generating launch scripts"
# JVM arguments used by legacy (pre-1.13) version JSONs; ${...} are launcher placeholders.
$legacyJvmArguments = @(
    "-Dos.name=Windows 10"
    "-Dos.version=10.0"
    "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
    "-Djava.library.path=`${natives_directory}"
    "-Dminecraft.launcher.brand=`${launcher_name}"
    "-Dminecraft.launcher.version=`${launcher_version}"
    "-Dminecraft.client.jar=`${primary_jar}"
    "-cp"
    '${classpath}'
)
# Fallback user JVM arguments when the version JSON has no "default-user-jvm" block.
$defaultUserJvmArguments = @(
    "-Xmx2G"
    "-XX:+UnlockExperimentalVMOptions"
    "-XX:+UseG1GC"
    "-XX:G1NewSizePercent=20"
    "-XX:G1ReservePercent=20"
    "-XX:MaxGCPauseMillis=50"
    "-XX:G1HeapRegionSize=32M"
)
foreach ($versionObject in $allVersions) {
    $versionId = [string]$versionObject.id
    $extraJvmArguments = @()
    $extraGameArguments = @()
    # Extra arguments contributed by ExternalArgs.psd1 for this loader + game version.
    if ($clientLoaders.ContainsKey($versionId)) {
        $loader = [string]$clientLoaders[$versionId]
        $gameVersion = [version]$MinecraftVersion
        foreach ($entry in @($ExternalArgs.Args)) {
            $matched = $false
            foreach ($rule in @($entry.Matrix)) {
                if (@($rule.Loaders) -notcontains $loader) { continue }
                $range = @($rule.VersionRange)
                # VersionRange is a flat list of inclusive [min, max] pairs.
                for ($index = 0; $index + 1 -lt $range.Count; $index += 2) {
                    if ($gameVersion -ge [version][string]$range[$index] -and $gameVersion -le [version][string]$range[$index + 1]) { $matched = $true; break }
                }
                if ($matched) { break }
            }
            if (-not $matched) { continue }
            if ($entry.JvmArgs) { $extraJvmArguments += @([string]$entry.JvmArgs -split "\s+" | Where-Object { $_ }) }
            if ($entry.AppArgs) { $extraGameArguments += @([string]$entry.AppArgs -split "\s+" | Where-Object { $_ }) }
        }
    }
    if ($versionObject.arguments) {
        $jvmArguments = @(Expand-ArgumentList $versionObject.arguments.jvm)
        $jvmArguments += if ($versionObject.arguments.'default-user-jvm') { @(Expand-ArgumentList $versionObject.arguments.'default-user-jvm') } else { $defaultUserJvmArguments }
        $gameArguments = @(Expand-ArgumentList $versionObject.arguments.game)
    } else {
        $jvmArguments = @($legacyJvmArguments) + $defaultUserJvmArguments
        $gameArguments = @([string]$versionObject.minecraftArguments -split "\s+" | Where-Object { $_ })
    }
    # Quick play versions connect through their own argument, all others get --server/--port.
    if ($gameArguments -notcontains "--quickPlayMultiplayer") { $gameArguments += @("--server", $ServerAddress, "--port", "$ServerPort") }

    $loggingConfigPath = ""
    $logging = $versionObject.logging
    if ($logging.client.file.id -and $logging.client.argument) {
        $loggingConfigPath = "assets/log_configs/$($logging.client.file.id)"
        $jvmArguments += ([string]$logging.client.argument).Replace('${path}', '${logging_config_path}')
    }

    $jvmArguments += $extraJvmArguments
    $gameArguments += $extraGameArguments

    # Classpath of this version, de-duplicated by group:artifact[:classifier] so that only
    # the newest variant of an artifact ends up on the command line.
    $classpathPaths = @()
    $seenLibraries = @{}
    foreach ($library in @($versionObject.libraries)) {
        if (-not (Test-Rules $library.rules)) { continue }
        $libraryParts = @([string]$library.name -split ":")
        if ($libraryParts.Count -lt 3) { $key = [string]$library.name }
        elseif ($libraryParts.Count -ge 4 -and $libraryParts[3]) { $key = "$($libraryParts[0]):$($libraryParts[1]):$($libraryParts[3])" }
        else { $key = "$($libraryParts[0]):$($libraryParts[1])" }
        if ($seenLibraries.ContainsKey($key)) { continue }
        $seenLibraries[$key] = $true
        $classpathArtifact = Get-LibraryArtifact $library
        if ($classpathArtifact) { $classpathPaths += $classpathArtifact.Path }
    }

    $assetIndexId = if ($versionObject.assetIndex.id) { [string]$versionObject.assetIndex.id } else { "" }

    # Render the template. Every value becomes a PowerShell literal so that entries such as
    # '${classpath}' or '-Dos.name=Windows 10' survive the round trip into the script.
    $tokens = [ordered]@{
        VERSION_NAME        = (ConvertTo-PowerShellLiteral $versionId)
        ASSETS_INDEX        = (ConvertTo-PowerShellLiteral $assetIndexId)
        QUICK_PLAY          = (ConvertTo-PowerShellLiteral "${ServerAddress}:$ServerPort")
        LAUNCHER_VERSION    = (ConvertTo-PowerShellLiteral $modVersion)
        PRIMARY_JAR         = (ConvertTo-PowerShellLiteral "versions/$versionId/$versionId.jar")
        NATIVES_DIR         = (ConvertTo-PowerShellLiteral "versions/$versionId/natives")
        LOGGING_CONFIG_LINE = $(if ($loggingConfigPath) { '$logging_config_path = Join-Path $PSScriptRoot ' + (ConvertTo-PowerShellLiteral $loggingConfigPath) } else { "" })
        CLASSPATH_ENTRIES   = (($classpathPaths | ForEach-Object { '    (Join-Path $library_directory ' + (ConvertTo-PowerShellLiteral $_) + ')' }) -join "`n")
        MAIN_CLASS          = (ConvertTo-PowerShellLiteral ([string]$versionObject.mainClass))
        JVM_ARGUMENTS       = (($jvmArguments | ForEach-Object { '    ' + (ConvertTo-PowerShellLiteral ([string]$_)) }) -join "`n")
        GAME_ARGUMENTS      = (($gameArguments | ForEach-Object { '    ' + (ConvertTo-PowerShellLiteral ([string]$_)) }) -join "`n")
    }
    $content = $LaunchTemplate
    foreach ($token in $tokens.Keys) { $content = $content.Replace("@@$token@@", $tokens[$token]) }
    # Set-Content terminates every value with the platform newline; normalise first so the
    # generated file keeps the exact line endings the old line-by-line builder produced.
    $content = $content -replace '\r?\n', [Environment]::NewLine
    Set-Content -LiteralPath (Join-Path $ClientDir "$versionId.ps1") -Value $content -Encoding utf8
}

"clients=$($clients -join ',')" | Out-File -FilePath $env:GITHUB_OUTPUT -Append
Write-Step "Done. Java $javaComponent, clients: $($clients -join ', ')"
