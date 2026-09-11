#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Random City 城市图片管线(v1.1.0,替代旧 ps1 脚本)。

子命令:
  convert-webp   全量 jpg -> webp(q80),逐张校验,全部通过后才删除 jpg
  download       下载城市代表图(Wikipedia -> Openverse),裁剪 800x450 存 webp
                 --city <id> 只下指定城市;--only-missing 只补缺失
  validate       对照城市数据校验图片齐全/无游离/格式/尺寸/体积

用法(仓库根目录):
  python tools/city_images.py convert-webp
  python tools/city_images.py download --only-missing
  python tools/city_images.py download --city tokyo
  python tools/city_images.py validate
"""

import argparse
import io
import json
import sys
import urllib.parse
import urllib.request
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
IMAGE_DIR = ROOT / "app" / "src" / "main" / "assets" / "city_images"
# v1.1.0 起城市数据拆分到 assets/data/;兼容旧单文件路径
CANDIDATE_DATA = [
    ROOT / "app" / "src" / "main" / "assets" / "data" / "cities.json",
    ROOT / "app" / "src" / "main" / "assets" / "cities.json",
]

WIDTH, HEIGHT = 800, 450
QUALITY = 80
MAX_BYTES = 120 * 1024
MIN_BYTES = 10 * 1024
UA = {"User-Agent": "RandomCityApp/1.1 (https://localhost; random-city)"}


def load_cities() -> list[dict]:
    for path in CANDIDATE_DATA:
        if path.exists():
            data = json.loads(path.read_text(encoding="utf-8"))
            return data["cities"]
    sys.exit("未找到城市数据文件: " + " / ".join(str(p) for p in CANDIDATE_DATA))


def fetch_json(url: str) -> dict | None:
    try:
        req = urllib.request.Request(url, headers=UA)
        with urllib.request.urlopen(req, timeout=12) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception:
        return None


def fetch_bytes(url: str) -> bytes | None:
    try:
        req = urllib.request.Request(url, headers=UA)
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.read()
    except Exception:
        return None


def wikipedia_image_url(query: str) -> str | None:
    q = urllib.parse.quote(query)
    api = (
        "https://en.wikipedia.org/w/api.php?action=query"
        f"&generator=search&gsrsearch={q}&gsrlimit=3&gsrnamespace=0"
        "&prop=pageimages&pithumbsize=1200&format=json"
    )
    data = fetch_json(api)
    pages = (data or {}).get("query", {}).get("pages", {})
    for page in sorted(pages.values(), key=lambda p: p.get("index", 99)):
        thumb = page.get("thumbnail")
        if thumb and thumb.get("source"):
            return thumb["source"]
    return None


def openverse_image_url(query: str) -> str | None:
    q = urllib.parse.quote(query)
    api = f"https://api.openverse.org/v1/images/?q={q}&aspect_ratio=wide&per_page=5&filter_dead=true"
    data = fetch_json(api)
    results = (data or {}).get("results", [])
    for item in results:
        if item.get("url") and (item.get("width") or 0) >= 800:
            return item["url"]
    for item in results:
        if item.get("thumbnail"):
            return item["thumbnail"]
    return None


def encode_webp(img: Image.Image, quality: int) -> bytes:
    buf = io.BytesIO()
    img.save(buf, "WEBP", quality=quality, method=6)
    return buf.getvalue()


def to_webp(raw: bytes) -> bytes:
    """中心裁剪 16:9 后缩放到 800x450,编码为 WebP。

    质量从 q80 起步,复杂场景超限(≤120KB 标准)时自动降档,
    取满足体积上限的最高质量;降至 q60 仍超限则原样返回由校验报错。
    """
    img = Image.open(io.BytesIO(raw)).convert("RGB")
    sw, sh = img.size
    target = 16 / 9
    if sw / sh > target:
        cw, ch = int(sh * target), sh
        left, top = (sw - cw) // 2, 0
    else:
        cw, ch = sw, int(sw / target)
        left, top = 0, (sh - ch) // 2
    img = img.crop((left, top, left + cw, top + ch)).resize((WIDTH, HEIGHT), Image.LANCZOS)
    best = encode_webp(img, QUALITY)
    for q in (75, 70, 65, 60):
        if len(best) <= MAX_BYTES:
            break
        best = encode_webp(img, q)
    return best


def check_webp(path: Path) -> str | None:
    """返回 None 表示合规,否则为问题描述。"""
    size = path.stat().st_size
    if size <= MIN_BYTES:
        return f"体积异常({size}B ≤ {MIN_BYTES}B)"
    if size > MAX_BYTES:
        return f"超限({size}B > {MAX_BYTES}B)"
    try:
        with Image.open(path) as img:
            img.load()
            if img.format != "WEBP":
                return f"格式非 WebP({img.format})"
            if img.size != (WIDTH, HEIGHT):
                return f"尺寸 {img.size[0]}x{img.size[1]} ≠ {WIDTH}x{HEIGHT}"
    except Exception as exc:
        return f"不可解码: {exc}"
    return None


def cmd_convert_webp(_args: argparse.Namespace) -> int:
    jpgs = sorted(IMAGE_DIR.glob("*.jpg"))
    if not jpgs:
        print("没有待转换的 jpg。")
        return 0
    print(f"共 {len(jpgs)} 张 jpg 待转换…")
    failures: list[str] = []
    for jpg in jpgs:
        webp = jpg.with_suffix(".webp")
        try:
            webp.write_bytes(to_webp(jpg.read_bytes()))
        except Exception as exc:
            failures.append(f"{jpg.name}: 转换失败 {exc}")
            continue
        problem = check_webp(webp)
        if problem:
            failures.append(f"{webp.name}: {problem}")
    if failures:
        print("\n".join(failures))
        print(f"\n{len(failures)} 张失败,已保留全部 jpg,请修复后重跑。")
        return 1
    for jpg in jpgs:
        jpg.unlink()
    total = sum(f.stat().st_size for f in IMAGE_DIR.glob("*.webp"))
    print(f"转换完成:{len(jpgs)} 张 webp,共 {total / 1024 / 1024:.1f}MB,jpg 已删除。")
    return 0


def cmd_download(args: argparse.Namespace) -> int:
    cities = load_cities()
    if args.city:
        cities = [c for c in cities if c["id"] == args.city]
        if not cities:
            print(f"城市不存在: {args.city}")
            return 1
    elif args.only_missing:
        cities = [c for c in cities if not (IMAGE_DIR / f"{c['id']}.webp").exists()]
    if not cities:
        print("没有需要下载的城市。")
        return 0
    print(f"待下载 {len(cities)} 城…")
    failures = []
    for city in cities:
        cid, name = city["id"], city["name"]
        keywords = city.get("imageKeywords") or []
        url = wikipedia_image_url(name) or openverse_image_url(name)
        if not url and keywords:
            url = openverse_image_url(keywords[0])
        if not url:
            failures.append(f"{cid} ({name}): 未找到图片")
            continue
        raw = fetch_bytes(url)
        if not raw:
            failures.append(f"{cid} ({name}): 下载失败 {url}")
            continue
        out = IMAGE_DIR / f"{cid}.webp"
        try:
            out.write_bytes(to_webp(raw))
        except Exception as exc:
            failures.append(f"{cid} ({name}): 处理失败 {exc}")
            continue
        problem = check_webp(out)
        if problem:
            out.unlink()
            failures.append(f"{cid} ({name}): {problem}")
            continue
        print(f"  OK {cid} <- {url}")
    if failures:
        print("\n失败清单(需人工处理):")
        print("\n".join("  " + f for f in failures))
        return 1
    print("全部完成,请人工逐张质检(主体/横图/无水印)后再入库。")
    return 0


def cmd_validate(_args: argparse.Namespace) -> int:
    cities = load_cities()
    ids = {c["id"] for c in cities}
    problems: list[str] = []
    files = {f.name: f for f in IMAGE_DIR.glob("*.webp")}
    for cid in sorted(ids):
        f = IMAGE_DIR / f"{cid}.webp"
        if not f.exists():
            problems.append(f"缺图: {cid}")
        elif (p := check_webp(f)):
            problems.append(f"{cid}.webp: {p}")
    for name in sorted(set(files) - {f"{i}.webp" for i in ids}):
        problems.append(f"游离图片: {name}")
    stray_jpg = list(IMAGE_DIR.glob("*.jpg"))
    for f in stray_jpg:
        problems.append(f"残留旧格式: {f.name}")
    if problems:
        print("\n".join(problems))
        print(f"\n校验失败:{len(problems)} 项问题。")
        return 1
    print(f"校验通过:{len(ids)} 城,图片齐全、格式尺寸体积合规。")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("convert-webp", help="全量 jpg -> webp").set_defaults(func=cmd_convert_webp)
    dl = sub.add_parser("download", help="下载城市代表图")
    dl.add_argument("--city", help="只下载指定城市 id")
    dl.add_argument("--only-missing", action="store_true", help="只补缺失的城市图")
    dl.set_defaults(func=cmd_download)
    sub.add_parser("validate", help="校验图片完整性").set_defaults(func=cmd_validate)
    args = parser.parse_args()
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
