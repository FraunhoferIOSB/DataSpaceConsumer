$SOURCE_FOLDER = "../extensions"
$DESTINATION_FOLDER = "../framework/extensions"

if (-not (Test-Path $SOURCE_FOLDER -PathType Container)) {
    Write-Host "[ERROR] Source folder does not exist: $SOURCE_FOLDER"
    exit 1
}

if (-not (Test-Path $DESTINATION_FOLDER -PathType Container)) {
    New-Item -ItemType Directory -Path $DESTINATION_FOLDER -Force | Out-Null
}

$exclude = Join-Path (Resolve-Path $SOURCE_FOLDER) "target"

Write-Host "Source: $SOURCE_FOLDER"
Write-Host "Destination: $DESTINATION_FOLDER"
Write-Host "Exclude root: $exclude"
Write-Host ""

$copied = 0
$skipped = 0

$exclude = (Resolve-Path (Join-Path $SOURCE_FOLDER "target") -ErrorAction SilentlyContinue)

Get-ChildItem -Path $SOURCE_FOLDER -Recurse -Filter "*-all.jar" -File | ForEach-Object {

    if ($exclude -and $_.FullName.StartsWith($exclude.Path, [System.StringComparison]::OrdinalIgnoreCase)) {
        Write-Host "  -> skipped (inside extensions/target)"
        $skipped++
        return
    }

    Copy-Item $_.FullName -Destination $DESTINATION_FOLDER -Force
    Write-Host "  -> copied: $_.Name" 
    $copied++
}

Write-Host ""
Write-Host "Summary:"
Write-Host "  Copied:  $copied"
Write-Host "  Skipped: $skipped"
