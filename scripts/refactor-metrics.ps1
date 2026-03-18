param(
    [switch]$Json
)

$ErrorActionPreference = "Stop"

$scopeFiles = @(
    "src/main/java/org/example/trusttrade/item/controller/ItemController.java",
    "src/main/java/org/example/trusttrade/item/controller/ProductController.java",
    "src/main/java/org/example/trusttrade/item/controller/CategoryController.java",
    "src/main/java/org/example/trusttrade/item/controller/MapController.java",
    "src/main/java/org/example/trusttrade/item/service/ProductService.java",
    "src/main/java/org/example/trusttrade/item/service/ItemService.java",
    "src/main/java/org/example/trusttrade/item/service/CategoryService.java",
    "src/main/java/org/example/trusttrade/item/service/ItemRegistrationService.java",
    "src/main/java/org/example/trusttrade/global/service/MapService.java",
    "src/main/java/org/example/trusttrade/auction/controller/AuctionController.java",
    "src/main/java/org/example/trusttrade/auction/service/AuctionService.java"
)

$controllerFiles = @(
    "src/main/java/org/example/trusttrade/item/controller/ItemController.java",
    "src/main/java/org/example/trusttrade/item/controller/ProductController.java",
    "src/main/java/org/example/trusttrade/item/controller/CategoryController.java",
    "src/main/java/org/example/trusttrade/item/controller/MapController.java",
    "src/main/java/org/example/trusttrade/auction/controller/AuctionController.java"
)

$serviceFiles = @(
    "src/main/java/org/example/trusttrade/item/service/ProductService.java",
    "src/main/java/org/example/trusttrade/item/service/ItemService.java",
    "src/main/java/org/example/trusttrade/item/service/CategoryService.java",
    "src/main/java/org/example/trusttrade/item/service/ItemRegistrationService.java",
    "src/main/java/org/example/trusttrade/global/service/MapService.java",
    "src/main/java/org/example/trusttrade/auction/service/AuctionService.java"
)

function Get-LineCount([string]$path) {
    return (Get-Content $path | Measure-Object -Line).Lines
}

function Get-PatternCount([string]$path, [string]$pattern) {
    return (Select-String -Path $path -Pattern $pattern | Measure-Object).Count
}

$fileMetrics = foreach ($file in $scopeFiles) {
    [PSCustomObject]@{
        file = $file
        lines = Get-LineCount $file
    }
}

$endpointPattern = "@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@PatchMapping"
$branchPattern = "\bif\s*\(|\belse\s+if\b|\bcatch\s*\("

$endpointCount = ($controllerFiles | ForEach-Object { Get-PatternCount $_ $endpointPattern } | Measure-Object -Sum).Sum
$branchCount = ($serviceFiles | ForEach-Object { Get-PatternCount $_ $branchPattern } | Measure-Object -Sum).Sum

$testFiles = if (Test-Path "src/test/java") {
    Get-ChildItem -Path "src/test/java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
} else {
    @()
}

$metrics = [PSCustomObject]@{
    measuredAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    scopeFileCount = $scopeFiles.Count
    totalLines = ($fileMetrics | Measure-Object -Property lines -Sum).Sum
    endpointCount = $endpointCount
    serviceBranchCount = $branchCount
    testFileCount = $testFiles.Count
    files = $fileMetrics
}

if ($Json) {
    $metrics | ConvertTo-Json -Depth 4
    exit 0
}

Write-Output "Measured At: $($metrics.measuredAt)"
Write-Output "Scope Files: $($metrics.scopeFileCount)"
Write-Output "Total Lines: $($metrics.totalLines)"
Write-Output "Endpoints: $($metrics.endpointCount)"
Write-Output "Service Branches: $($metrics.serviceBranchCount)"
Write-Output "Test Files: $($metrics.testFileCount)"
Write-Output ""
Write-Output "Files:"
$fileMetrics |
    Sort-Object lines -Descending |
    Format-Table -AutoSize
