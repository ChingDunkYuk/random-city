# 重下 3 张不合格图:用更精准的查询词
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName System.Web

$UA = 'RandomCityApp/0.9 (https://localhost; random-city)'
$OUT_DIR = 'app\src\main\assets\city_images'
$TMP_DIR = '.trae\imgtmp'

# 每个城市按顺序尝试的查询词(Wikipedia 搜索)
$targets = @(
    @{ id = 'hong_kong';  queries = @('Hong Kong Island skyline', 'Victoria Peak', 'International Commerce Centre') }
)

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

function Save-CroppedJpeg($srcPath, $dstPath) {
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

foreach ($t in $targets) {
    $done = $false
    foreach ($q in $t.queries) {
        $url = Get-WikiUrl $q
        if (-not $url) { continue }
        $tmp = "$TMP_DIR\$($t.id).raw"
        try {
            Invoke-WebRequest -Uri $url -Headers @{ 'User-Agent' = $UA } -TimeoutSec 20 -OutFile $tmp -UseBasicParsing
            Save-CroppedJpeg $tmp "$OUT_DIR\$($t.id).jpg"
            Write-Output "$($t.id) <- '$q' OK ($([math]::Round((Get-Item "$OUT_DIR\$($t.id).jpg").Length/1KB))KB)"
            $done = $true
            break
        } catch {
            Write-Output "$($t.id) <- '$q' FAILED: $($_.Exception.Message)"
        } finally {
            if (Test-Path $tmp) { Remove-Item $tmp -Force }
        }
    }
    if (-not $done) { Write-Output "$($t.id): ALL QUERIES FAILED" }
    Start-Sleep -Milliseconds 400
}
