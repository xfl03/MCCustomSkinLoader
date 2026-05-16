param(
    [string] $GradleCommand = "./gradlew.bat"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $ScriptRoot "../..")).Path
Set-Location -LiteralPath $RepositoryRoot

& $GradleCommand clean build --stacktrace
if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed with exit code $LASTEXITCODE."
}
