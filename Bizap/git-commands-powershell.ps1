#!/usr/bin/env pwsh
# git-commands-powershell.ps1 - Common Git Commands for PowerShell (Windows)

Write-Host "📚 BIZAP GIT COMMANDS - PowerShell Edition" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "IMPORTANT: PowerShell uses DIFFERENT syntax than Bash!" -ForegroundColor Yellow
Write-Host ""

Write-Host "❌ WRONG (Bash syntax):" -ForegroundColor Red
Write-Host "  command1 && command2       # Won't work in PowerShell" -ForegroundColor Red
Write-Host "  command1 | head -20        # 'head' doesn't exist" -ForegroundColor Red
Write-Host "  command1 | tail -5         # 'tail' doesn't exist" -ForegroundColor Red
Write-Host ""

Write-Host "✅ CORRECT (PowerShell syntax):" -ForegroundColor Green
Write-Host "  command1; command2         # Use semicolon instead of &&" -ForegroundColor Green
Write-Host "  command1 | Select -First 20    # Use Select-Object" -ForegroundColor Green
Write-Host "  command1 | Select -Last 5      # Use Select-Object" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "COMMON GIT COMMANDS (PowerShell)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Show current status
Write-Host "1. CHECK GIT STATUS:" -ForegroundColor Yellow
Write-Host '   git status' -ForegroundColor Green
Write-Host ""

# Show recent commits
Write-Host "2. SHOW RECENT COMMITS (10 commits):" -ForegroundColor Yellow
Write-Host '   git log --oneline -10' -ForegroundColor Green
Write-Host ""

# Show all commits with graph
Write-Host "3. SHOW ALL COMMITS WITH GRAPH (20 commits):" -ForegroundColor Yellow
Write-Host '   git log --oneline --graph --decorate -20' -ForegroundColor Green
Write-Host ""

# Show latest 5 tags
Write-Host "4. SHOW RECENT TAGS:" -ForegroundColor Yellow
Write-Host '   git tag -l -n1 | Select -Last 5' -ForegroundColor Green
Write-Host ""

# Switch branches
Write-Host "5. SWITCH TO MAIN BRANCH:" -ForegroundColor Yellow
Write-Host '   git checkout main; git pull origin main' -ForegroundColor Green
Write-Host ""

# Stash changes
Write-Host "6. SAVE UNCOMMITTED CHANGES (stash):" -ForegroundColor Yellow
Write-Host '   git stash' -ForegroundColor Green
Write-Host ""

# Restore stashed changes
Write-Host "7. RESTORE STASHED CHANGES:" -ForegroundColor Yellow
Write-Host '   git stash pop' -ForegroundColor Green
Write-Host ""

# View current branch
Write-Host "8. SHOW CURRENT BRANCH:" -ForegroundColor Yellow
Write-Host '   git branch --show-current' -ForegroundColor Green
Write-Host ""

# Create new branch
Write-Host "9. CREATE NEW BRANCH:" -ForegroundColor Yellow
Write-Host '   git checkout -b branch-name' -ForegroundColor Green
Write-Host ""

# View remote info
Write-Host "10. SHOW REMOTE REPOSITORY:" -ForegroundColor Yellow
Write-Host '    git remote -v' -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "POWERSHELL-SPECIFIC TIPS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📌 Pagination Helper (like 'less' in Bash):" -ForegroundColor Cyan
Write-Host '   git log --oneline | Out-Host -Paging' -ForegroundColor Green
Write-Host ""

Write-Host "📌 Count items (like 'wc -l'):" -ForegroundColor Cyan
Write-Host '   (git log --oneline | Measure-Object).Count' -ForegroundColor Green
Write-Host ""

Write-Host "📌 Filter output (like 'grep'):" -ForegroundColor Cyan
Write-Host '   git log --oneline | Select-String "pattern"' -ForegroundColor Green
Write-Host ""

Write-Host "📌 First N items (like 'head -N'):" -ForegroundColor Cyan
Write-Host '   git log --oneline | Select-Object -First 10' -ForegroundColor Green
Write-Host ""

Write-Host "📌 Last N items (like 'tail -N'):" -ForegroundColor Cyan
Write-Host '   git log --oneline | Select-Object -Last 5' -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "NOW RUN THIS TO VERIFY YOUR BUILD:" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host ".\verify-build-status.ps1" -ForegroundColor Yellow
Write-Host ""

