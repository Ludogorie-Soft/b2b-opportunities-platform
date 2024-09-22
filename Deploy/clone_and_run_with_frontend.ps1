$frontend_repo = "https://github.com/algorithmity/job-matcher.git"

$currentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend_project_dir = Join-Path -Path $currentDir -ChildPath "job-matcher"
$db_dir = Join-Path -Path $currentDir -ChildPath "db"

Write-Host

$foldersExist = $false


function Write-Custom-Warning {
    param (
        [string]$message
    )
	
    Write-Host -ForegroundColor Red "WARNING:" -NoNewline
    Write-Host -ForegroundColor Yellow " $message"
}


if (Test-Path $frontend_project_dir) {
	$foldersExist = $true
	Write-Custom-Warning "${frontend_project_dir} - already exists and won't be updated!"
	Write-Host " - Delete the folder and re-run the script if you want to get the latest version from git. `n"
}

if (Test-Path $db_dir) {
	$foldersExist = $true
	Write-Custom-Warning "${db_dir} already exists!"
	Write-Host " - This is totally fine if your DB is not messed up."
	Write-Host " - Delete the folder and re-run the script to start with a fresh DB. `n"
}

if ($foldersExist) {
	Write-Host -ForegroundColor Red ("-" * 100)
	Write-Custom-Warning "Press Enter to continue or Ctrl+C to cancel."
    Read-Host
}

if (-not (Test-Path $frontend_project_dir)) {
	git clone $frontend_repo
}

docker compose -f docker-compose-with-frontend.yml up -d --build