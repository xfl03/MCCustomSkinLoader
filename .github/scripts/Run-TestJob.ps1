# Launches a Minecraft server plus one or more clients, waits until each client
# reports that its skin has loaded, then captures an in-game screenshot.

param(
    [Parameter(Mandatory)][string]$MinecraftVersion,   # e.g. "1.20.1"; locates "<version>.jar" in the server dir
    [Parameter(Mandatory)][string]$Clients             # comma-separated client launch script names in Test/run/client
)

# Abort as soon as any unhandled error occurs.
$ErrorActionPreference = "Stop"

# --- Behaviour tuning --------------------------------------------------------
$StallSeconds = 30          # treat a process as stalled if it produces no log output for this long
$MaxAttempts = 3            # retry each launch (server and client) up to this many times
$DeadlineMinutes = 3        # timeout for a single launch attempt
$SkinLoadedMarkers = @("'s profile loaded. (", "Cached profile will be used.")  # log lines proving the skin loaded
$ServerReadyPattern = "Done \("  # server log line emitted once the world is ready

# Resolve RunDir to an absolute path so child processes get a stable location.
$RunDir = Join-Path (Get-Location).Path "Test/run"
$ServerDir = Join-Path $RunDir "server"
$ClientDir = Join-Path $RunDir "client"
$ServerLogDir = Join-Path $ServerDir "logs"
$ClientLogDir = Join-Path $ClientDir "logs"
$ScreenshotsDir = Join-Path $ClientDir "screenshots"
$CustomSkinLoaderLog = Join-Path $ClientDir "CustomSkinLoader/CustomSkinLoader.log"

# Load the Win32 helpers used to post synthetic key messages to the game window.
Add-Type -TypeDefinition @"
using System; using System.Runtime.InteropServices;
public static class TestJobNativeMethods {
    [DllImport("user32.dll")] public static extern IntPtr SendMessage(IntPtr hWnd, uint msg, IntPtr wParam, IntPtr lParam);
    [DllImport("user32.dll")] public static extern uint MapVirtualKey(uint uCode, uint uMapType);
}
"@

# Read every line of a log file, or return an empty array when it does not exist yet.
function Get-LogLines { param([string]$Path) @(if (Test-Path -LiteralPath $Path) { Get-Content -LiteralPath $Path -ErrorAction SilentlyContinue }) }

# Kill a process together with all of its children (taskkill /T).
function Stop-ProcessTree {
    param([System.Diagnostics.Process]$Process)
    if ($Process -and -not $Process.HasExited) {
        taskkill /PID $Process.Id /T /F 2>&1 | Out-Null
        $Process.WaitForExit(30000) | Out-Null
    }
}

# Post a single key-down/key-up message to a window, building a correct WM_KEYDOWN/WM_KEYUP lParam.
function Send-GameKey {
    param([IntPtr]$Window, [int]$Key, [bool]$Down)
    $scan = [long][TestJobNativeMethods]::MapVirtualKey([uint32]$Key, 0)
    $msg = [uint32]($Down ? 0x0100 : 0x0101)
    $lp = [IntPtr](($Down ? 1 : 0xC0000001) -bor ($scan -shl 16))
    [TestJobNativeMethods]::SendMessage($Window, $msg, [IntPtr]$Key, $lp) | Out-Null
}

# Poll logs: return $null once $Ready is true, otherwise return the failure reason (exited / stall / deadline).
function Wait-LoggedProcess {
    param([System.Diagnostics.Process]$Process, [hashtable[]]$Logs, [scriptblock]$Ready, [string]$TimeoutMessage)
    $lastOutput = Get-Date
    $deadline = (Get-Date).AddMinutes($DeadlineMinutes)
    while ((Get-Date) -lt $deadline) {
        foreach ($log in $Logs) {
            $info = Get-Item -LiteralPath $log.Path -ErrorAction SilentlyContinue
            # Skip files that have not been touched since the launch started (stale content from a previous attempt).
            if ($log.MinTime -and $info -and $info.LastWriteTimeUtc -le $log.MinTime) { continue }
            $lines = @(Get-LogLines $log.Path)
            if ($lines.Count -lt $log.Pos) { $log.Pos = 0 }   # log was truncated / recreated
            if ($lines.Count -le $log.Pos) { continue }       # nothing new to read
            foreach ($line in $lines[$log.Pos..($lines.Count - 1)]) {
                Write-Host "$($log.Prefix)$line"
                # Remember whether this log reported a loaded skin.
                if ($log.Markers -and ($SkinLoadedMarkers | Where-Object { $line.Contains($_) })) { $state.SkinLoaded = $true }
            }
            $log.Pos = $lines.Count
            $lastOutput = Get-Date
        }
        if (& $Ready) { return $null }
        if ($Process.HasExited) { return "exited" }
        if (((Get-Date) - $lastOutput).TotalSeconds -ge $StallSeconds) { return "stall, no output for $($StallSeconds)s" }
        Start-Sleep -Milliseconds 500
    }
    return $TimeoutMessage
}

# Make sure the log and screenshot directories exist before launching anything.
New-Item -ItemType Directory -Force -Path $ServerLogDir, $ClientLogDir, $ScreenshotsDir | Out-Null

$serverJar = Join-Path $ServerDir "$MinecraftVersion.jar"

Write-Host "Starting server for $MinecraftVersion"
$server = $null
$reason = ""
$serverOut = Join-Path $ServerLogDir "test-server.log"
$serverReady = { (Get-Content -LiteralPath $serverOut -Raw -ErrorAction SilentlyContinue) -match $ServerReadyPattern }
$serverLogs = @(@{ Path = $serverOut; Prefix = "[server] "; Pos = 0 })
# Start the server, retrying a few times until it prints the "ready" marker.
for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
    if ($server) { Stop-ProcessTree $server; Start-Sleep -Seconds 3 }
    Write-Host "[server] attempt $attempt/$MaxAttempts starting$(if ($attempt -gt 1) { " (previous attempt: $reason)" })"
    $command = "& java -Xmx2G -jar '$serverJar' nogui 2>&1 | Tee-Object -FilePath '$serverOut'"
    $server = Start-Process -FilePath "pwsh" -ArgumentList @("-NoProfile", "-Command", $command) -WorkingDirectory $ServerDir -PassThru -NoNewWindow
    $reason = Wait-LoggedProcess $server $serverLogs $serverReady "deadline, no ready marker after $($DeadlineMinutes) minutes"
    if (-not $reason) { break }
    Write-Host "[server] attempt $attempt/$MaxAttempts failed: $reason"
}

$failed = [bool]$reason
if ($failed) { Write-Host "[server] failed to start" } else { Write-Host "[server] ready" }

if (-not $failed) {
    # Run each requested client in turn (comma-separated list).
    foreach ($clientName in @(($Clients -split ",") | ForEach-Object { $_.Trim() } | Where-Object { $_ })) {
        $clientScript = Join-Path $ClientDir $clientName
        $reason = ""
        for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
            # Only the first attempt uses the plain log name; later attempts get a suffix.
            $suffix = $attempt -eq 1 ? "" : ".attempt$attempt"
            $clientOut = Join-Path $ClientLogDir "$clientName$suffix.log"
            Remove-Item -LiteralPath $clientOut -Force -ErrorAction SilentlyContinue
            $shotsBefore = @(Get-ChildItem -LiteralPath $ScreenshotsDir -File -ErrorAction SilentlyContinue).Count
            $state = @{ SkinLoaded = $false }
            $clientLog = @{ Path = $clientOut; Prefix = "[$clientName] "; Pos = 0; Markers = $true }
            # Ignore CustomSkinLoader content written before this attempt started.
            $cslLog = @{ Path = $CustomSkinLoaderLog; Prefix = "[$clientName:csl] "; Pos = 0; Markers = $true; MinTime = (Get-Date).ToUniversalTime() }

            Write-Host "[$clientName] attempt $attempt/$MaxAttempts launching$(if ($attempt -gt 1) { " (previous attempt: $reason)" })"
            $command = "& '$clientScript' 2>&1 | Tee-Object -FilePath '$clientOut'"
            $client = Start-Process -FilePath "pwsh" -ArgumentList @("-NoProfile", "-Command", $command) -WorkingDirectory $ClientDir -PassThru -NoNewWindow

            $reason = Wait-LoggedProcess $client @($clientLog, $cslLog) { $state.SkinLoaded } "deadline, no skin marker after $($DeadlineMinutes) minutes"

            if (-not $reason) {
                # Wait for the "Chat message can't be verified" popup to close; otherwise it blocks the Tab player list.
                Write-Host "[$clientName] skin loaded, waiting 15 seconds"
                Start-Sleep -Seconds 15
                $game = Get-Process -Name java -ErrorAction SilentlyContinue |
                        Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like "Minecraft*" } | Select-Object -First 1
                if (-not $game) { Write-Host "[keys] Minecraft window not found"; $reason = "window not found" }
                else {
                    Write-Host "[keys] window: $($game.MainWindowTitle)"
                    $w = $game.MainWindowHandle
                    Send-GameKey $w 0x74 $true; Send-GameKey $w 0x74 $false; Start-Sleep -Milliseconds 500  # F5: switch to third-person view
                    Send-GameKey $w 0x09 $true; Start-Sleep -Milliseconds 500                               # press and hold Tab (player list)
                    Send-GameKey $w 0x71 $true; Send-GameKey $w 0x71 $false; Start-Sleep -Milliseconds 500  # F2: take screenshot
                    Send-GameKey $w 0x09 $false                                                             # release Tab
                    $screenshotDeadline = (Get-Date).AddSeconds(30)
                    $captured = $false
                    while ((Get-Date) -lt $screenshotDeadline) {
                        if (@(Get-ChildItem -LiteralPath $ScreenshotsDir -File -ErrorAction SilentlyContinue).Count -gt $shotsBefore) { $captured = $true; break }
                        Start-Sleep -Milliseconds 500
                    }
                    if ($captured) { Write-Host "[$clientName] screenshot captured" }
                    else { Write-Host "[$clientName] screenshot not found"; $reason = "screenshot not found" }
                }
            }

            # Print remaining logs + archive the CustomSkinLoader log.
            $rest = @(Get-LogLines $clientOut)
            if ($rest.Count -gt $clientLog.Pos) { $rest[$clientLog.Pos..($rest.Count - 1)] | ForEach-Object { Write-Host "[$clientName] $_" } }
            if (Test-Path -LiteralPath $CustomSkinLoaderLog) { Copy-Item -LiteralPath $CustomSkinLoaderLog -Destination (Join-Path $ClientLogDir "$clientName-CustomSkinLoader$suffix.log") -Force }

            Stop-ProcessTree $client
            if (-not $reason) { Start-Sleep -Seconds 2; break }
            Write-Host "[$clientName] attempt $attempt/$MaxAttempts failed: $reason"

            # Wait until the server confirms the previous client left the game before retrying.
            if ($attempt -lt $MaxAttempts) {
                $from = @(Get-Content -LiteralPath $ServerOut -ErrorAction SilentlyContinue).Count
                $releaseDeadline = (Get-Date).AddSeconds(10)
                while ((Get-Date) -lt $releaseDeadline) {
                    $serverLines = @(Get-Content -LiteralPath $ServerOut -ErrorAction SilentlyContinue)
                    if ($serverLines.Count -gt $from -and ($serverLines[$from..($serverLines.Count - 1)] -join "`n").Contains(" left the game")) { break }
                    Start-Sleep -Milliseconds 500
                }
            }
        }

        if ($reason) { Write-Host "[$clientName] skin not loaded"; $failed = $true }
    }
}

# Always tear down the server before reporting the result.
Stop-ProcessTree $server
Write-Host "Test finished for $MinecraftVersion (failed: $failed)"
if ($failed) { exit 1 }
exit 0
