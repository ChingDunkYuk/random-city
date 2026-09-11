# 批量下载 103 个城市代表图 -> assets/city_images/{cityId}.jpg (800x450, JPEG q78)
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName System.Web

$UA = 'RandomCityApp/0.9 (https://localhost; random-city)'
$OUT_DIR = 'app\src\main\assets\city_images'
$TMP_DIR = '.trae\imgtmp'
New-Item -ItemType Directory -Force -Path $OUT_DIR | Out-Null
New-Item -ItemType Directory -Force -Path $TMP_DIR | Out-Null

$cities = (Get-Content 'app\src\main\assets\cities.json' -Raw -Encoding UTF8 | ConvertFrom-Json).cities

function Get-WikiUrl($name) {
    try {
        $q = [System.Web.HttpUtility]::UrlEncode($name)
        $api = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=$q&gsrlimit=3&gsrnamespace=0&prop=pageimages&pithumbsize=1200&format=json"
        $r = Invoke-RestMethod -Uri $api -Headers @{ 'User-Agent' = $UA } -TimeoutSec 12
        if ($r.query.pages) {
            $pages = $r.query.pages.PSObject.Properties.Value | Sort-Object index
            foreach ($p in $pages) { if ($p.thumbnail.source) { return $p.thumbnail.source } }
        }
    } catch { }
    return $null
}

function Get-OpenverseUrl($name) {
    try {
        $q = [System.Web.HttpUtility]::UrlEncode($name)
        $api = "https://api.openverse.org/v1/images/?q=$q&aspect_ratio=wide&per_page=5&filter_dead=true"
        $r = Invoke-RestMethod -Uri $api -Headers @{ 'User-Agent' = $UA } -TimeoutSec 12
        foreach ($it in $r.results) {
            # 原图优先(>=800宽),否则 thumb 代理
            if ($it.url -and $it.width -ge 800) { return $it.url }
        }
        foreach ($it in $r.results) { if ($it.thumbnail) { return $it.thumbnail } }
    } catch { }
    return $null
}

function Save-CroppedJpeg($srcPath, $dstPath) {
    # WPF 图像管线:System.Drawing 在本环境静默输出纯白图,改用稳定托管实现
    $bmp = [System.Windows.Media.Imaging.BitmapImage]::new()
    $bmp.BeginInit()
    $bmp.UriSource = [Uri]((Resolve-Path $srcPath).Path)
    $bmp.CacheOption = [System.Windows.Media.Imaging.BitmapCacheOption]::OnLoad
    $bmp.EndInit()
    [int]$sw = $bmp.PixelWidth; [int]$sh = $bmp.PixelHeight
    $targetRatio = 16.0 / 9.0
    if ($sw / $sh -gt $targetRatio) {
        [int]$cw = [int]($sh * $targetRatio); [int]$ch = $sh; [int]$cx = [int](($sw - $cw) / 2); [int]$cy = 0
    } else {
        [int]$cw = $sw; [int]$ch = [int]($sw / $targetRatio); [int]$cx = 0; [int]$cy = [int](($sh - $ch) / 2)
    }
    $cropRect = [System.Windows.Int32Rect]::new($cx, $cy, $cw, $ch)
    $cropped = [System.Windows.Media.Imaging.CroppedBitmap]::new($bmp, $cropRect)
    $sx = [double]800.0 / $cw; $sy = [double]450.0 / $ch
    $scale = [System.Windows.Media.ScaleTransform]::new($sx, $sy)
    $final = [System.Windows.Media.Imaging.TransformedBitmap]::new($cropped, $scale)
    $enc = [System.Windows.Media.Imaging.JpegBitmapEncoder]::new()
    $enc.QualityLevel = 78
    $enc.Frames.Add([System.Windows.Media.Imaging.BitmapFrame]::Create($final))
    $fs = [System.IO.File]::Create($dstPath)
    try { $enc.Save($fs) } finally { $fs.Close() }
}

$ok = 0; $wikiCnt = 0; $ovCnt = 0; $failed = @(); $report = @()
foreach ($c in $cities) {
    $id = $c.id; $name = $c.name; $dst = "$OUT_DIR\$id.jpg"
    # 断点续跑:已存在且 >10KB 才算有效(上一轮的纯白图仅 6.4KB,会被重下)
    if ((Test-Path $dst) -and (Get-Item $dst).Length -gt 10KB) { $ok++; continue }
    $url = Get-WikiUrl $name; $src = 'wiki'
    if (-not $url) { $url = Get-OpenverseUrl $name; $src = 'openverse' }
    if (-not $url) { $failed += "$id($name): no url"; continue }
    $tmp = "$TMP_DIR\$id.raw"
    try {
        Invoke-WebRequest -Uri $url -Headers @{ 'User-Agent' = $UA } -TimeoutSec 20 -OutFile $tmp -UseBasicParsing
        Save-CroppedJpeg $tmp $dst
        $ok++; if ($src -eq 'wiki') { $wikiCnt++ } else { $ovCnt++ }
        $report += "$id <- $src"
    } catch {
        $failed += "$id($name): $($_.Exception.Message)"
    } finally {
        if (Test-Path $tmp) { Remove-Item $tmp -Force }
    }
    Start-Sleep -Milliseconds 300
}

$totalKB = [math]::Round(((Get-ChildItem "$OUT_DIR\*.jpg" | Measure-Object Length -Sum).Sum / 1KB))
Write-Output "DONE. OK=$ok (wiki=$wikiCnt openverse=$ovCnt) FAILED=$($failed.Count) TOTAL=${totalKB}KB"
Write-Output "FAILED LIST:"; $failed | ForEach-Object { Write-Output "  $_" }
