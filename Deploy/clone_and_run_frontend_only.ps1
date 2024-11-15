$frontend_repo = "https://github.com/algorithmity/job-matcher.git"

$currentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend_project_dir = Join-Path -Path $currentDir -ChildPath "job-matcher"

function Write-Custom-Warning {
    param (
        [string]$message
    )

    Write-Host -ForegroundColor Red "WARNING:" -NoNewline
    Write-Host -ForegroundColor Yellow " $message"
}

if (Test-Path $frontend_project_dir) {
    Write-Custom-Warning " ${frontend_project_dir} - already exists. Do you want to delete the folder now? (y/N)"
    $response = Read-Host
        if ($response -eq 'y' -or $response -eq 'Y') {
        Remove-Item -Recurse -Force $frontend_project_dir
        Write-Host "Folder deleted."
        Write-Host "Getting the project from GitHub:"
        git clone $frontend_repo
    } else {
        Write-Host "Keeping existing folder."
    }
    Write-Host
}

docker compose -f docker-compose-frontend-only.yml up -d --build

Write-Host "Script completed. Press Enter to exit."
Read-Host

