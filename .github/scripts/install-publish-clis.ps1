$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$RunnerTemp = if ([string]::IsNullOrWhiteSpace($env:RUNNER_TEMP)) {
    [System.IO.Path]::GetTempPath()
} else {
    $env:RUNNER_TEMP
}

function Add-ActionPath {
    param([Parameter(Mandatory = $true)][string] $Path)

    $env:Path = "$Path;$env:Path"
    if (-not [string]::IsNullOrWhiteSpace($env:GITHUB_PATH)) {
        Add-Content -LiteralPath $env:GITHUB_PATH -Value $Path
    }
}

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    $installer = Join-Path $RunnerTemp "AWSCLIV2.msi"
    Invoke-WebRequest -Uri "https://awscli.amazonaws.com/AWSCLIV2.msi" -OutFile $installer
    $install = Start-Process msiexec.exe -Wait -PassThru -ArgumentList "/i `"$installer`" /qn"
    if ($install.ExitCode -ne 0) {
        throw "AWS CLI installer failed with exit code $($install.ExitCode)."
    }

    $awsCliDir = Join-Path $env:ProgramFiles "Amazon/AWSCLIV2"
    $machinePath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
    $userPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    $env:Path = "$awsCliDir;$machinePath;$userPath;$env:Path"
    if (-not [string]::IsNullOrWhiteSpace($env:GITHUB_PATH)) {
        Add-Content -LiteralPath $env:GITHUB_PATH -Value $awsCliDir
    }
}

if (-not (Get-Command coscli -ErrorAction SilentlyContinue)) {
    $coscliDir = Join-Path $RunnerTemp "coscli"
    New-Item -ItemType Directory -Force -Path $coscliDir | Out-Null
    Invoke-WebRequest -Uri "https://github.com/tencentyun/coscli/releases/download/v1.0.8/coscli-v1.0.8-windows-amd64.exe" -OutFile (Join-Path $coscliDir "coscli.exe")
    Add-ActionPath $coscliDir
}

if (-not (Get-Command tccli -ErrorAction SilentlyContinue)) {
    python -m pip install --user tccli
    if ($LASTEXITCODE -ne 0) {
        throw "tccli install failed with exit code $LASTEXITCODE."
    }

    $pythonUserScripts = Join-Path (python -m site --user-base) "Scripts"
    Add-ActionPath $pythonUserScripts
}
