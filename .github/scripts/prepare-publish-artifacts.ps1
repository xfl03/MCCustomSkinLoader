param(
    [string] $Channel = $env:PUBLISH_CHANNEL,
    [string] $LatestJsonName = $env:LATEST_JSON_NAME,
    [string] $DetailJsonName = $env:DETAIL_JSON_NAME
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($Channel)) {
    $Channel = "Beta"
}

$publishScript = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "publish-artifacts.ps1"
$arguments = @("-Channel", $Channel)

if (-not [string]::IsNullOrWhiteSpace($LatestJsonName)) {
    $arguments += @("-LatestJsonName", $LatestJsonName)
}

if (-not [string]::IsNullOrWhiteSpace($DetailJsonName)) {
    $arguments += @("-DetailJsonName", $DetailJsonName)
}

& $publishScript @arguments
