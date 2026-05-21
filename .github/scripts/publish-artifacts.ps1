param(
    [ValidateSet("Beta", "Release")]
    [string] $Channel = "Beta",

    [string] $LatestJsonName,

    [string] $DetailJsonName,

    [string] $ArtifactsRoot = "build/publish",

    [switch] $SkipObjectStorage
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $ScriptRoot "..\..")).Path
Set-Location -LiteralPath $RepositoryRoot

function Resolve-RepoPath {
    param([Parameter(Mandatory = $true)][string] $Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }

    return [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($RepositoryRoot, $Path))
}

function ConvertTo-ShortVersion {
    param([Parameter(Mandatory = $true)][string] $Version)

    return $Version.Replace("SNAPSHOT-", "s")
}

function Read-BuildProperties {
    $properties = [ordered] @{}
    foreach ($line in Get-Content -LiteralPath (Resolve-RepoPath "build.properties")) {
        $trimmedLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmedLine) -or $trimmedLine.StartsWith("#")) {
            continue
        }

        $separatorIndex = $trimmedLine.IndexOf("=")
        if ($separatorIndex -lt 1) {
            continue
        }

        $name = $trimmedLine.Substring(0, $separatorIndex).Trim()
        $value = $trimmedLine.Substring($separatorIndex + 1).Trim()
        $properties[$name] = $value
    }

    return $properties
}

function Get-RequiredProperty {
    param(
        [Parameter(Mandatory = $true)][System.Collections.IDictionary] $Properties,
        [Parameter(Mandatory = $true)][string] $Name
    )

    $value = $Properties[$Name]
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Missing $Name in build.properties"
    }

    return $value
}

function Get-PropertyList {
    param(
        [Parameter(Mandatory = $true)][System.Collections.IDictionary] $Properties,
        [Parameter(Mandatory = $true)][string] $Name
    )

    return (Get-RequiredProperty $Properties $Name) -split "," |
        ForEach-Object { $_.Trim() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
}

function Get-PropertyListByNames {
    param(
        [Parameter(Mandatory = $true)][System.Collections.IDictionary] $Properties,
        [Parameter(Mandatory = $true)][string[]] $Names
    )

    foreach ($name in $Names) {
        if (-not [string]::IsNullOrWhiteSpace($Properties[$name])) {
            return Get-PropertyList $Properties $name
        }
    }

    throw "Missing one of these properties in build.properties: $($Names -join ', ')"
}

function Test-VersionAtLeast {
    param(
        [Parameter(Mandatory = $true)][string] $Version,
        [Parameter(Mandatory = $true)][string] $MinimumVersion
    )

    return [version] $Version -ge [version] $MinimumVersion
}

function Get-VersionFromJarName {
    param([Parameter(Mandatory = $true)][string] $FileName)

    $prefix = "CustomSkinLoader_Universal-"
    $suffix = ".jar"
    if (-not $FileName.StartsWith($prefix) -or -not $FileName.EndsWith($suffix)) {
        throw "Unexpected Universal jar name: $FileName"
    }

    return $FileName.Substring($prefix.Length, $FileName.Length - $prefix.Length - $suffix.Length)
}

function New-Directory {
    param([Parameter(Mandatory = $true)][string] $Path)

    [System.IO.Directory]::CreateDirectory((Resolve-RepoPath $Path)) | Out-Null
}

function Copy-PublishArtifacts {
    param([Parameter(Mandatory = $true)][string] $Destination)

    New-Directory $Destination

    $destinationPath = Resolve-RepoPath $Destination
    Get-ChildItem -LiteralPath $destinationPath -File -ErrorAction SilentlyContinue | Remove-Item -Force

    $jar = Get-ChildItem -LiteralPath (Resolve-RepoPath "Bootstrap/build/libs") -Filter "CustomSkinLoader_Universal-*.jar" -File -ErrorAction Stop |
        Where-Object { $_.Name -notlike "*-sources.jar" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $jar) {
        throw "No Universal jar found under Bootstrap/build/libs."
    }

    Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $destinationPath $jar.Name) -Force
    return Get-Item -LiteralPath (Join-Path $destinationPath $jar.Name)
}

function ConvertTo-JsonFile {
    param(
        [Parameter(Mandatory = $true)] $InputObject,
        [Parameter(Mandatory = $true)][string] $Path
    )

    $json = $InputObject | ConvertTo-Json -Depth 10
    $absolutePath = Resolve-RepoPath $Path
    [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($absolutePath)) | Out-Null
    [System.IO.File]::WriteAllText($absolutePath, $json + [System.Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))
}

function New-Metadata {
    param(
        [Parameter(Mandatory = $true)][string] $Version,
        [Parameter(Mandatory = $true)][string] $OutputDirectory,
        [Parameter(Mandatory = $true)][string] $LatestName,
        [Parameter(Mandatory = $true)][string] $DetailName,
        [Parameter(Mandatory = $true)][System.Collections.IDictionary] $BuildProperties
    )

    $publicBaseUrl = if ([string]::IsNullOrWhiteSpace($env:CLOUDFLARE_CDN_ROOT)) { "https://csl.3-3.dev/" } else { $env:CLOUDFLARE_CDN_ROOT.Trim() }
    if (-not $publicBaseUrl.EndsWith("/")) {
        $publicBaseUrl += "/"
    }

    $outputPath = Resolve-RepoPath $OutputDirectory
    $jars = Get-ChildItem -LiteralPath $outputPath -Filter "CustomSkinLoader_Universal-*.jar" -File |
        Sort-Object Name

    if (-not $jars) {
        throw "No publish jars found in $OutputDirectory."
    }

    $latest = [ordered] @{
        version = ConvertTo-ShortVersion $Version
        downloads = [ordered] @{}
        launchermeta = [ordered] @{}
    }

    foreach ($jar in $jars) {
        $latest.downloads["Universal"] = "$publicBaseUrl`mods/$($jar.Name)"
    }

    $minecraftMajorVersions = Get-PropertyList $BuildProperties "minecraft_major_versions"
    $detailMap = [ordered] @{}
    foreach ($minecraftVersion in $minecraftMajorVersions) {
        $loaderMap = [ordered] @{}
        $loaderMap["Forge"] = $latest.downloads["Universal"]

        if (Test-VersionAtLeast $minecraftVersion "1.14") {
            $loaderMap["Fabric"] = $latest.downloads["Universal"]
            $loaderMap["Quilt"] = $latest.downloads["Universal"]
        }

        if (Test-VersionAtLeast $minecraftVersion "1.20") {
            $loaderMap["NeoForge"] = $latest.downloads["Universal"]
        }

        $detailMap[$minecraftVersion] = $loaderMap
    }

    $detail = [ordered] @{
        version = $latest.version
        timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
        details = $detailMap
    }

    ConvertTo-JsonFile $latest (Join-Path $outputPath $LatestName)
    ConvertTo-JsonFile $detail (Join-Path $outputPath $DetailName)
}

function Write-GitHubOutputValue {
    param(
        [Parameter(Mandatory = $true)][string] $Name,
        [Parameter(Mandatory = $true)][string] $Value
    )

    if ([string]::IsNullOrWhiteSpace($env:GITHUB_OUTPUT)) {
        return
    }

    $delimiter = "EOF_$([guid]::NewGuid().ToString('N'))"
    $content = "$Name<<$delimiter" + [System.Environment]::NewLine +
        $Value + [System.Environment]::NewLine +
        $delimiter + [System.Environment]::NewLine
    [System.IO.File]::AppendAllText($env:GITHUB_OUTPUT, $content, [System.Text.UTF8Encoding]::new($false))
}

function Write-PublishOutputs {
    param(
        [Parameter(Mandatory = $true)][string] $Version,
        [Parameter(Mandatory = $true)][System.Collections.IDictionary] $BuildProperties
    )

    $shortVersion = ConvertTo-ShortVersion $Version
    $edition = Get-RequiredProperty $BuildProperties "edition"
    Write-GitHubOutputValue "edition" $edition
    Write-GitHubOutputValue "version" $shortVersion
    Write-GitHubOutputValue "mc_publish_name" "CustomSkinLoader_$edition`_$shortVersion"
    Write-GitHubOutputValue "mc_publish_version" "$shortVersion-$edition"
    Write-GitHubOutputValue "loaders" ((Get-PropertyListByNames $BuildProperties @("loaders", "mod_loaders")) -join [System.Environment]::NewLine)
    Write-GitHubOutputValue "game_versions" ((Get-PropertyListByNames $BuildProperties @("game-versions", "minecraft_major_versions")) -join [System.Environment]::NewLine)
    Write-GitHubOutputValue "java_versions" ((Get-PropertyListByNames $BuildProperties @("java", "java_full_versions")) -join [System.Environment]::NewLine)
}

function Invoke-AwsCliUpload {
    param(
        [Parameter(Mandatory = $true)][string] $EndpointUrl,
        [Parameter(Mandatory = $true)][string] $Bucket,
        [Parameter(Mandatory = $true)][string] $AccessKeyId,
        [Parameter(Mandatory = $true)][string] $SecretAccessKey,
        [Parameter(Mandatory = $true)][string] $Directory
    )

    $aws = Get-Command aws -ErrorAction SilentlyContinue
    if (-not $aws) {
        Write-Warning "AWS CLI was not found. Skip S3-compatible upload."
        return
    }

    $oldAccessKey = $env:AWS_ACCESS_KEY_ID
    $oldSecretKey = $env:AWS_SECRET_ACCESS_KEY
    $oldRegion = $env:AWS_DEFAULT_REGION

    try {
        $env:AWS_ACCESS_KEY_ID = $AccessKeyId
        $env:AWS_SECRET_ACCESS_KEY = $SecretAccessKey
        $env:AWS_DEFAULT_REGION = "auto"

        foreach ($file in Get-ChildItem -LiteralPath $Directory -File) {
            $key = if ($file.Extension -eq ".jar") { "mods/$($file.Name)" } else { $file.Name }
            & aws --endpoint-url $EndpointUrl s3 cp $file.FullName "s3://$Bucket/$key" --only-show-errors
        }
    } finally {
        $env:AWS_ACCESS_KEY_ID = $oldAccessKey
        $env:AWS_SECRET_ACCESS_KEY = $oldSecretKey
        $env:AWS_DEFAULT_REGION = $oldRegion
    }
}

function Invoke-CosCliUpload {
    param(
        [Parameter(Mandatory = $true)][string] $Bucket,
        [Parameter(Mandatory = $true)][string] $SecretId,
        [Parameter(Mandatory = $true)][string] $SecretKey,
        [Parameter(Mandatory = $true)][string] $Directory
    )

    $coscli = Get-Command coscli -ErrorAction SilentlyContinue
    if (-not $coscli) {
        Write-Warning "coscli was not found. Skip Tencent COS upload."
        return
    }

    $configPath = Join-Path $env:RUNNER_TEMP "coscli-customskinloader.yaml"
    $cosRegion = if ([string]::IsNullOrWhiteSpace($env:COS_REGION)) { "ap-shanghai" } else { $env:COS_REGION.Trim() }
    @"
cos:
  base:
    secretid: $SecretId
    secretkey: $SecretKey
    sessiontoken: ""
    protocol: https
  buckets:
  - name: $Bucket
    alias: $Bucket
    region: $cosRegion
    endpoint: cos.$cosRegion.myqcloud.com
    ofs: false
"@ | Set-Content -LiteralPath $configPath -Encoding UTF8

    foreach ($file in Get-ChildItem -LiteralPath $Directory -File) {
        $key = if ($file.Extension -eq ".jar") { "mods/$($file.Name)" } else { $file.Name }
        & coscli -c $configPath cp $file.FullName "cos://$Bucket/$key"
    }
}

function Get-PublishObjectKeys {
    param([Parameter(Mandatory = $true)][string] $Directory)

    return Get-ChildItem -LiteralPath $Directory -File |
        ForEach-Object {
            if ($_.Extension -eq ".jar") {
                "mods/$($_.Name)"
            } else {
                $_.Name
            }
        }
}

function Invoke-TencentCdnRefresh {
    param([Parameter(Mandatory = $true)][string[]] $Keys)

    $cdnRoot = if ([string]::IsNullOrWhiteSpace($env:TENCENT_CDN_ROOT)) { "https://csl.littleservice.cn/" } else { $env:TENCENT_CDN_ROOT.Trim() }
    if (-not $cdnRoot.EndsWith("/")) {
        $cdnRoot += "/"
    }

    if ([string]::IsNullOrWhiteSpace($env:COS_SECRET_ID) -or [string]::IsNullOrWhiteSpace($env:COS_SECRET_KEY)) {
        Write-Host "Tencent CDN credentials are incomplete. Skip CDN refresh."
        return
    }

    $tccli = Get-Command tccli -ErrorAction SilentlyContinue
    if (-not $tccli) {
        Write-Warning "tccli was not found. Skip Tencent CDN refresh."
        return
    }

    $oldSecretId = $env:TENCENTCLOUD_SECRET_ID
    $oldSecretKey = $env:TENCENTCLOUD_SECRET_KEY
    try {
        $env:TENCENTCLOUD_SECRET_ID = $env:COS_SECRET_ID
        $env:TENCENTCLOUD_SECRET_KEY = $env:COS_SECRET_KEY
        $urlsJson = ConvertTo-Json -Compress -InputObject @($Keys | ForEach-Object { "$cdnRoot$_" })

        & tccli cdn PurgeUrlsCache --Urls $urlsJson
        if ($LASTEXITCODE -ne 0) {
            throw "Tencent CDN purge failed."
        }

        & tccli cdn PushUrlsCache --Urls $urlsJson
        if ($LASTEXITCODE -ne 0) {
            throw "Tencent CDN push failed."
        }
    } finally {
        $env:TENCENTCLOUD_SECRET_ID = $oldSecretId
        $env:TENCENTCLOUD_SECRET_KEY = $oldSecretKey
    }
}

if ([string]::IsNullOrWhiteSpace($LatestJsonName)) {
    $LatestJsonName = switch ($Channel) {
        "Beta" { "latest-beta.json" }
        "Canary" { "latest-canary.json" }
        "Release" { "latest.json" }
    }
}

if ([string]::IsNullOrWhiteSpace($DetailJsonName)) {
    $DetailJsonName = switch ($Channel) {
        "Beta" { "detail-beta.json" }
        "Canary" { "detail-canary.json" }
        "Release" { "detail.json" }
    }
}

New-Directory $ArtifactsRoot
$buildProperties = Read-BuildProperties
$artifacts = Copy-PublishArtifacts $ArtifactsRoot
$ArtifactsRoot = Resolve-RepoPath $ArtifactsRoot
$fullVersion = Get-VersionFromJarName $artifacts[0].Name
New-Metadata -Version $fullVersion -OutputDirectory $ArtifactsRoot -LatestName $LatestJsonName -DetailName $DetailJsonName -BuildProperties $buildProperties
Write-PublishOutputs -Version $fullVersion -BuildProperties $buildProperties

Write-Host "Prepared publish artifacts:"
Get-ChildItem -LiteralPath $ArtifactsRoot -File | ForEach-Object { Write-Host " - $($_.Name)" }

if ($SkipObjectStorage) {
    Write-Host "SkipObjectStorage was set. Metadata generated locally only."
    exit 0
}

if (-not [string]::IsNullOrWhiteSpace($env:R2_BASE_URL) -and
    -not [string]::IsNullOrWhiteSpace($env:R2_BUCKET) -and
    -not [string]::IsNullOrWhiteSpace($env:R2_SECRET_ID) -and
    -not [string]::IsNullOrWhiteSpace($env:R2_SECRET_KEY)) {
    Invoke-AwsCliUpload -EndpointUrl $env:R2_BASE_URL -Bucket $env:R2_BUCKET -AccessKeyId $env:R2_SECRET_ID -SecretAccessKey $env:R2_SECRET_KEY -Directory $ArtifactsRoot
} else {
    Write-Host "R2 secrets are incomplete. Skip R2 upload."
}

if (-not [string]::IsNullOrWhiteSpace($env:COS_BUCKET) -and
    -not [string]::IsNullOrWhiteSpace($env:COS_SECRET_ID) -and
    -not [string]::IsNullOrWhiteSpace($env:COS_SECRET_KEY)) {
    Invoke-CosCliUpload -Bucket $env:COS_BUCKET -SecretId $env:COS_SECRET_ID -SecretKey $env:COS_SECRET_KEY -Directory $ArtifactsRoot
} else {
    Write-Host "COS secrets are incomplete. Skip COS upload."
}

Invoke-TencentCdnRefresh -Keys @(Get-PublishObjectKeys $ArtifactsRoot)
