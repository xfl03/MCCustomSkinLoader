param(
    [string] $BranchName = $env:GITHUB_REF_NAME,
    [string] $BuildType = $env:BUILD_TYPE,
    [string] $OutputPath = "build/publish/release-notes.md",
    [string] $TimeZoneId = "China Standard Time"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $ScriptRoot "../..")).Path
Set-Location -LiteralPath $RepositoryRoot

if ([string]::IsNullOrWhiteSpace($BranchName)) {
    throw "BranchName is required."
}

if ([string]::IsNullOrWhiteSpace($BuildType)) {
    throw "BuildType is required."
}

$absoluteOutputPath = if ([System.IO.Path]::IsPathRooted($OutputPath)) {
    [System.IO.Path]::GetFullPath($OutputPath)
} else {
    [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($RepositoryRoot, $OutputPath))
}

[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($absoluteOutputPath)) | Out-Null

$buildTime = [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, $TimeZoneId).ToString("yyyy.MM.dd HH:mm")
@"
Build Time / 构建时间: $buildTime

Synchronize $BranchName branch code updates, keeping only the latest $BuildType build.
自动同步 $BranchName 分支构建产物，仅保留最新 $BuildType 版本。
"@ | Set-Content -LiteralPath $absoluteOutputPath -Encoding UTF8
