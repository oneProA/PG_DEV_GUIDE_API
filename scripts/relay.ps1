$ErrorActionPreference = 'Stop'

$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Read-Utf8File {
    param([string]$Path)
    return [System.IO.File]::ReadAllText($Path, $Utf8NoBom)
}

function Write-Utf8File {
    param(
        [string]$Path,
        [string]$Content
    )
    [System.IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Append-Utf8Line {
    param(
        [string]$Path,
        [string]$Line
    )
    $payload = $Line + [Environment]::NewLine
    [System.IO.File]::AppendAllText($Path, $payload, $Utf8NoBom)
}

function Resolve-CodexCommand {
    param([string]$PreferredCommand)

    if ($PreferredCommand) {
        $cmd = Get-Command $PreferredCommand -ErrorAction SilentlyContinue
        if ($cmd) { return $cmd.Source }
    }

    foreach ($name in @('codex.exe', 'codex.cmd', 'codex')) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($cmd) { return $cmd.Source }
    }

    $fallback = Join-Path $env:USERPROFILE '.vscode\extensions\openai.chatgpt-26.323.20928-win32-x64\bin\windows-x86_64\codex.exe'
    if (Test-Path $fallback) { return $fallback }

    throw 'Unable to resolve a Codex CLI executable. Set CODEX_CLI_COMMAND if needed.'
}

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $line = "[$timestamp] $Message"
    Append-Utf8Line -Path $script:ConversationLogPath -Line $line
    Write-Output $line
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectRoot = Split-Path -Parent $ScriptDir

$MissionPath = Join-Path $ProjectRoot 'MISSION.md'
$ReportPath = Join-Path $ProjectRoot 'MISSION_REPORT.md'
$ConversationLogPath = Join-Path $ProjectRoot 'conversation_log.md'
$RuntimeRoot = Join-Path $ProjectRoot '.codex-runtime'
$CodexHome = Join-Path $RuntimeRoot 'home'
$CodexTmp = Join-Path $RuntimeRoot 'tmp'

Write-Log '--- AI relay script started ---'

if (-not (Test-Path $MissionPath)) {
    Write-Log "Error: Mission file 'MISSION.md' was not found in '$ProjectRoot'."
    exit 1
}

$missionContent = Read-Utf8File -Path $MissionPath
if ([string]::IsNullOrWhiteSpace($missionContent)) {
    Write-Log "Mission file 'MISSION.md' is empty. Nothing to do."
    exit 0
}

$resolvedCodex = Resolve-CodexCommand -PreferredCommand $env:CODEX_CLI_COMMAND
Write-Log "Resolved Codex CLI: $resolvedCodex"

New-Item -ItemType Directory -Force -Path $CodexHome, $CodexTmp | Out-Null

$oldEnv = @{
    CODEX_HOME = $env:CODEX_HOME
    TMPDIR = $env:TMPDIR
    TMP = $env:TMP
    TEMP = $env:TEMP
}

try {
    $env:CODEX_HOME = $CodexHome
    $env:TMPDIR = $CodexTmp
    $env:TMP = $CodexTmp
    $env:TEMP = $CodexTmp

    Write-Log "Executing Codex task. Mission file: $MissionPath"

    $missionBytes = $Utf8NoBom.GetBytes($missionContent)
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $resolvedCodex
    $psi.Arguments = 'exec --sandbox workspace-write --skip-git-repo-check'
    $psi.WorkingDirectory = $ProjectRoot
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = $Utf8NoBom
    $psi.StandardErrorEncoding = $Utf8NoBom

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    $null = $process.Start()

    $process.StandardInput.BaseStream.Write($missionBytes, 0, $missionBytes.Length)
    $process.StandardInput.Close()

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    $reportContent = $stdout
    if ($stderr) {
        if ($reportContent) {
            $reportContent += [Environment]::NewLine
        }
        $reportContent += $stderr
    }
    Write-Utf8File -Path $ReportPath -Content $reportContent

    if ($process.ExitCode -eq 0) {
        $updatedMission = "--- Task Complete (Codex) ---`r`n$reportContent"
        Write-Utf8File -Path $MissionPath -Content $updatedMission
        Write-Log "Codex task completed successfully. Report: $ReportPath"
        Write-Log 'MISSION.md was updated after a successful run.'
    } else {
        $updatedMission = "--- Task Failed (Codex) ---`r`n$reportContent"
        Write-Utf8File -Path $MissionPath -Content $updatedMission
        Write-Log "Codex task failed with exit code $($process.ExitCode). Report: $ReportPath"
        Write-Log 'MISSION.md was updated after a failed run.'
        exit $process.ExitCode
    }
}
finally {
    $env:CODEX_HOME = $oldEnv.CODEX_HOME
    $env:TMPDIR = $oldEnv.TMPDIR
    $env:TMP = $oldEnv.TMP
    $env:TEMP = $oldEnv.TEMP
    Write-Log '--- AI relay script finished ---'
}
