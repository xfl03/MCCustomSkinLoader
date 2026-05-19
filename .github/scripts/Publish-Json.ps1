param(
    [string]$ArtifactDir,
    [string]$Type  # "beta" or "release"
)

$ErrorActionPreference = "Stop"

# ---- Validate required env vars ----
foreach ($var in @('R2_SECRET_ID', 'R2_SECRET_KEY', 'R2_BUCKET', 'R2_BASE_URL')) {
    if (-not $env:$var) { throw "Missing required env: $var" }
}

$suffix = if ($Type -eq "beta") { "-beta" } else { "" }

# ---- Find the built jar ----
$jar = Get-ChildItem -Path $ArtifactDir -Filter "*.jar" | Where-Object { $_.Name -notlike "*-sources.jar" } | Select-Object -First 1
if (-not $jar) { throw "No jar found in $ArtifactDir" }

$jarFilename = $jar.Name
$jarKey = "mods/$jarFilename"

# ---- Derive short version from gradle.properties + build number ----
# mod_version=15.0, IS_SNAPSHOT=true, GITHUB_RUN_NUMBER=33 -> 15.0-s33
# mod_version=15.0, IS_SNAPSHOT=false                -> 15.0
$version = ((Get-Content "gradle.properties" | Select-String "^mod_version=") -replace "^mod_version=", "").Trim()
$isSnapshot = if ($env:IS_SNAPSHOT) { $env:IS_SNAPSHOT -ne "false" } else { $true }
if ($isSnapshot) {
    $shortVersion = "$version-s$env:GITHUB_RUN_NUMBER"
} else {
    $shortVersion = "$version"
}

# ---- Read MC major versions from gradle.properties ----
$mcVersions = ((Get-Content "gradle.properties" | Select-String "^minecraft_major_versions=") -replace "^.*=", "").Trim()
$mcVersionList = $mcVersions -split "," | ForEach-Object { $_.Trim() }

$baseUrl = $env:R2_BASE_URL.TrimEnd('/')

# ---- Generate latest(-beta).json ----
$latestJson = @{
    version     = $shortVersion
    downloads   = @{ Universal = "$baseUrl/$jarKey" }
    launchermeta = @{}
} | ConvertTo-Json -Depth 10
Set-Content -Path "$ArtifactDir/latest$suffix.json" -Value $latestJson -NoNewline

# ---- Generate detail(-beta).json ----
$details = [ordered]@{}
foreach ($mcVer in $mcVersionList) {
    $details[$mcVer] = @{ Universal = "$baseUrl/$jarKey" }
}
$detailJson = @{
    version   = $shortVersion
    timestamp = [System.DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    details   = $details
} | ConvertTo-Json -Depth 10
Set-Content -Path "$ArtifactDir/detail$suffix.json" -Value $detailJson -NoNewline

# ---- Upload JSON files to R2 ----
$env:AWS_ACCESS_KEY_ID = $env:R2_SECRET_ID
$env:AWS_SECRET_ACCESS_KEY = $env:R2_SECRET_KEY
$env:AWS_DEFAULT_REGION = "auto"

aws s3 cp "$ArtifactDir/latest$suffix.json" "s3://$env:R2_BUCKET/latest$suffix.json" --endpoint-url $($env:R2_BASE_URL.TrimEnd('/'))
aws s3 cp "$ArtifactDir/detail$suffix.json" "s3://$env:R2_BUCKET/detail$suffix.json" --endpoint-url $($env:R2_BASE_URL.TrimEnd('/'))

# ---- Upload JSON files to COS ----
try {
    pip install coscmd -q --disable-pip-version-check
    coscmd config -a $env:COS_SECRET_ID -s $env:COS_SECRET_KEY -b $env:COS_BUCKET -r ap-shanghai
    coscmd upload "$ArtifactDir/latest$suffix.json" "latest$suffix.json"
    coscmd upload "$ArtifactDir/detail$suffix.json" "detail$suffix.json"
} catch {
    Write-Warning "COS JSON upload failed: $_"
}

# ---- Output CDN URLs for subsequent refresh step ----
$cdnBase = "https://csl.littleservice.cn"
"cdn-urls<<EOF" | Out-File $env:GITHUB_OUTPUT -Append
"${cdnBase}/latest${suffix}.json" | Out-File $env:GITHUB_OUTPUT -Append
"${cdnBase}/detail${suffix}.json" | Out-File $env:GITHUB_OUTPUT -Append
"EOF" | Out-File $env:GITHUB_OUTPUT -Append
