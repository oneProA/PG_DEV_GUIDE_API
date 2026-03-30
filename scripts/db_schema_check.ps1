$ErrorActionPreference = 'Stop'

param(
    [string]$Schema = '',
    [string[]]$Tables = @('users', 'payments', 'cancels', 'inquiries'),
    [string]$PsqlPath = ''
)

function Resolve-RepoRoot {
    $dir = Resolve-Path (Join-Path $PSScriptRoot '..')
    return $dir.Path
}

function Get-ApplicationYmlPath {
    $root = Resolve-RepoRoot
    $path = Join-Path $root 'src\main\resources\application.yml'
    if (-not (Test-Path $path)) {
        throw "application.yml not found: $path"
    }
    return $path
}

function Read-ApplicationSetting {
    param(
        [string[]]$Lines,
        [string]$KeyRegex
    )
    $m = $Lines | Select-String -Pattern $KeyRegex | Select-Object -First 1
    if (-not $m) { return '' }
    return $m.Matches[0].Groups[1].Value.Trim()
}

function Parse-JdbcUrl {
    param([string]$JdbcUrl)

    if ($JdbcUrl -notmatch '^jdbc:postgresql://([^:/]+):(\d+)/([^?]+)\?(.*)$') {
        throw "Unexpected JDBC url format: $JdbcUrl"
    }

    return @{
        Host = $Matches[1]
        Port = $Matches[2]
        Db   = $Matches[3]
        Qs   = $Matches[4]
    }
}

function Resolve-Psql {
    param([string]$Provided)

    if ($Provided) {
        $resolved = Resolve-Path $Provided -ErrorAction SilentlyContinue
        if ($resolved) { return $resolved.Path }
        throw "PsqlPath not found: $Provided"
    }

    $cmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $repoRoot = Split-Path (Resolve-RepoRoot) -Parent
    $shim = Join-Path $repoRoot 'bin\psql.cmd'
    if (Test-Path $shim) { return $shim }

    throw 'psql not found in PATH and bin\psql.cmd not found. Install psql or provide -PsqlPath.'
}

function Invoke-Psql {
    param(
        [string]$PsqlExe,
        [string]$Conn,
        [string]$Sql
    )

    & $PsqlExe $Conn -v ON_ERROR_STOP=1 -P pager=off -c $Sql
    if ($LASTEXITCODE -ne 0) {
        throw "psql failed with exit code $LASTEXITCODE"
    }
}

$appYml = Get-ApplicationYmlPath
$lines = Get-Content $appYml
$jdbc = Read-ApplicationSetting -Lines $lines -KeyRegex '^\s*url:\s*(.+)$'
$pgUser = Read-ApplicationSetting -Lines $lines -KeyRegex '^\s*username:\s*(.+)$'
$pgPass = Read-ApplicationSetting -Lines $lines -KeyRegex '^\s*password:\s*(.+)$'

if (-not $jdbc) { throw "Missing datasource url in $appYml" }
if (-not $pgUser) { throw "Missing datasource username in $appYml" }
if (-not $pgPass) { throw "Missing datasource password in $appYml" }

$jdbcParts = Parse-JdbcUrl -JdbcUrl $jdbc
$pgHost = $jdbcParts.Host
$pgPort = $jdbcParts.Port
$pgDb = $jdbcParts.Db
$qs = $jdbcParts.Qs

$detectedSchema = ''
if ($qs -match '(?:^|&)currentSchema=([^&]+)') { $detectedSchema = $Matches[1] }
if (-not $Schema) { $Schema = $detectedSchema }
if (-not $Schema) { $Schema = 'public' }

$psql = Resolve-Psql -Provided $PsqlPath

Write-Output "Target: host=$pgHost port=$pgPort db=$pgDb schema=$Schema user=$pgUser"

$env:PGPASSWORD = $pgPass
try {
    $conn = "host=$pgHost port=$pgPort dbname=$pgDb user=$pgUser sslmode=require options=--search_path=$Schema"

    Write-Output ''
    Write-Output '== Session =='
    Invoke-Psql -PsqlExe $psql -Conn $conn -Sql "select current_database() as db, current_user as usr, current_schema() as schema;"

    Write-Output ''
    Write-Output "== Tables (schema: $Schema) =="
    Invoke-Psql -PsqlExe $psql -Conn $conn -Sql "select table_name from information_schema.tables where table_schema = '$Schema' and table_type='BASE TABLE' order by table_name;"

    foreach ($t in $Tables) {
        Write-Output ''
        Write-Output "== Columns: $Schema.$t =="
        Invoke-Psql -PsqlExe $psql -Conn $conn -Sql "select ordinal_position, column_name, data_type, is_nullable, column_default from information_schema.columns where table_schema='$Schema' and table_name='$t' order by ordinal_position;"

        Write-Output ''
        Write-Output "== Primary key: $Schema.$t =="
        Invoke-Psql -PsqlExe $psql -Conn $conn -Sql @"
select kcu.column_name
from information_schema.table_constraints tc
join information_schema.key_column_usage kcu
  on tc.constraint_name = kcu.constraint_name
 and tc.table_schema = kcu.table_schema
where tc.table_schema = '$Schema'
  and tc.table_name = '$t'
  and tc.constraint_type = 'PRIMARY KEY'
order by kcu.ordinal_position;
"@

        Write-Output ''
        Write-Output "== Foreign keys: $Schema.$t =="
        Invoke-Psql -PsqlExe $psql -Conn $conn -Sql @"
select
  tc.constraint_name,
  kcu.column_name,
  ccu.table_name as foreign_table,
  ccu.column_name as foreign_column
from information_schema.table_constraints tc
join information_schema.key_column_usage kcu
  on tc.constraint_name = kcu.constraint_name
 and tc.table_schema = kcu.table_schema
join information_schema.constraint_column_usage ccu
  on ccu.constraint_name = tc.constraint_name
 and ccu.table_schema = tc.table_schema
where tc.table_schema = '$Schema'
  and tc.table_name = '$t'
  and tc.constraint_type = 'FOREIGN KEY'
order by tc.constraint_name, kcu.ordinal_position;
"@

        Write-Output ''
        Write-Output "== Indexes: $Schema.$t =="
        Invoke-Psql -PsqlExe $psql -Conn $conn -Sql "select indexname, indexdef from pg_indexes where schemaname='$Schema' and tablename='$t' order by indexname;"
    }
}
finally {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

