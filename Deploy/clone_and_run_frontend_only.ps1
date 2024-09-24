$frontend_repo = "https://github.com/algorithmity/job-matcher.git"

$currentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend_project_dir = Join-Path -Path $currentDir -ChildPath "job-matcher"

Write-Host

function Write-Custom-Warning {
    param (
        [string]$message
    )
	
    Write-Host -ForegroundColor Red "WARNING:" -NoNewline
    Write-Host -ForegroundColor Yellow " $message"
}


if (Test-Path $frontend_project_dir) {
	Write-Custom-Warning "${frontend_project_dir} - already exists and won't be updated"
	Write-Host " - Delete the folder and re-run the script if you want to get the latest version from git. `n"
	Write-Custom-Warning "Press Enter to continue or Ctrl+C to cancel."
    Read-Host
}

if (-not (Test-Path $frontend_project_dir)) {
	git clone $frontend_repo
}

docker compose -f docker-compose-frontend-only.yml up -d --build


