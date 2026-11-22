"""The logo of c0de paint: the two green slashes of the c0de logo on black, and in front of them a paintbrush, placed
diagonally, with a head of bristles in the colours of the rainbow in the upper left corner and its handle pointing
to the lower right corner.

Writes the logo in two sizes, drawn with 4 x 4 samples per pixel:
- docs/images/logo.png (256 x 256), also the icon of the mod in the mod lists
  (src/main/resources/assets/c0de_paint/icon.png);
- docs/images/logo_curseforge.png (400 x 400) for CurseForge.

The drawing is defined on a grid of 256 x 256 units and scaled to the size of the picture. Only the standard library
is used.

Usage: python scripts/generate_logo.py
"""

import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import png_files  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
UNITS = 256.0
SAMPLES = 4

BACKGROUND = (0x00, 0x00, 0x00)
SLASH = (0x00, 0xC8, 0x00)
SLASH_EDGE = (0x00, 0x5E, 0x00)
OUTLINE = (0x10, 0x10, 0x10)
# The slashes of the c0de logo: parallelograms from the lower left to the upper right
SLASHES = [
    [(59, 196), (89, 196), (141, 60), (111, 60)],
    [(115, 196), (145, 196), (197, 60), (167, 60)],
]
EDGE = 5.0

# The brush along its axis from the tip of the bristles (upper left) to the end of the handle (lower right)
TIP = (34.0, 34.0)
AXIS = (1 / math.sqrt(2), 1 / math.sqrt(2))
ACROSS = (1 / math.sqrt(2), -1 / math.sqrt(2))
BRISTLES_END = 92.0
FERRULE_END = 124.0
HANDLE_END = 262.0
BORDER = 4.0
# The bristles in the colours of the rainbow, across the brush
RAINBOW = [
    (0xE5, 0x32, 0x32),
    (0xF2, 0x8C, 0x1E),
    (0xF5, 0xD5, 0x26),
    (0x4C, 0xC2, 0x3A),
    (0x2E, 0x8C, 0xE6),
    (0x8E, 0x44, 0xD8),
]
FERRULE = (0xC8, 0xC8, 0xD0)
FERRULE_DARK = (0x80, 0x80, 0x8C)
HANDLE = (0xA0, 0x62, 0x2D)
HANDLE_LIGHT = (0xC8, 0x86, 0x48)


def inside(polygon, x, y) -> bool:
    result = False
    for (x0, y0), (x1, y1) in zip(polygon, polygon[1:] + polygon[:1]):
        if (y0 > y) != (y1 > y) and x < x0 + (y - y0) * (x1 - x0) / (y1 - y0):
            result = not result
    return result


def edge_distance(polygon, x, y) -> float:
    best = math.inf
    for (x0, y0), (x1, y1) in zip(polygon, polygon[1:] + polygon[:1]):
        dx, dy = x1 - x0, y1 - y0
        t = max(0.0, min(1.0, ((x - x0) * dx + (y - y0) * dy) / (dx * dx + dy * dy)))
        best = min(best, math.hypot(x - x0 - t * dx, y - y0 - t * dy))
    return best


def half_width(s: float) -> float:
    """Half the width of the brush at the distance s from the tip, or -1 beyond its ends."""
    if s < 0 or s > HANDLE_END:
        return -1
    if s < BRISTLES_END:
        # A rounded, pointed tip that widens towards the ferrule
        return 25 * math.sqrt(min(1.0, s / 40)) - 3 * max(0.0, (s - 40) / (BRISTLES_END - 40))
    if s < FERRULE_END:
        return 22
    # The handle narrows to its end and is rounded there
    width = 16 - 6 * (s - FERRULE_END) / (HANDLE_END - FERRULE_END)
    rest = HANDLE_END - s
    return min(width, math.sqrt(max(0.0, rest * (2 * width - rest)))) if rest < width else width


def brush(x: float, y: float):
    """The colour of the brush at a point, or None outside of it."""
    dx, dy = x - TIP[0], y - TIP[1]
    s = dx * AXIS[0] + dy * AXIS[1]
    t = dx * ACROSS[0] + dy * ACROSS[1]
    width = half_width(s)
    outer = max(half_width(s - BORDER), half_width(s + BORDER), width)
    if width < 0 and outer < 0:
        return None
    if abs(t) > width:
        return OUTLINE if abs(t) <= outer + BORDER else None
    if abs(t) > width - BORDER / 2 or (width - abs(t) < BORDER and s < 6):
        return OUTLINE
    if s < BRISTLES_END:
        stripe = int((t + width) / (2 * width) * len(RAINBOW))
        color = RAINBOW[min(stripe, len(RAINBOW) - 1)]
        # Darker towards the tip, as if dipped in paint
        shade = 0.75 + 0.25 * min(1.0, s / 30)
        return tuple(int(c * shade) for c in color)
    if s < FERRULE_END:
        # Two crimps across the ferrule
        if abs(s - (BRISTLES_END + 10)) < 2 or abs(s - (FERRULE_END - 8)) < 2:
            return FERRULE_DARK
        return FERRULE if t < width * 0.3 else FERRULE_DARK
    return HANDLE_LIGHT if -width * 0.6 < t < -width * 0.2 else HANDLE


def color(x: float, y: float) -> tuple[int, int, int]:
    front = brush(x, y)
    if front:
        return front
    for slash in SLASHES:
        if inside(slash, x, y):
            return SLASH if edge_distance(slash, x, y) > EDGE else SLASH_EDGE
    return BACKGROUND


def raster(size: int):
    scale = UNITS / size
    image = []
    for py in range(size):
        row = []
        for px in range(size):
            total = [0, 0, 0]
            for sy in range(SAMPLES):
                for sx in range(SAMPLES):
                    c = color((px + (sx + 0.5) / SAMPLES) * scale, (py + (sy + 0.5) / SAMPLES) * scale)
                    for i in range(3):
                        total[i] += c[i]
            row.append(tuple(v // (SAMPLES * SAMPLES) for v in total) + (255,))
        image.append(row)
    return image


def main() -> None:
    logo = raster(256)
    (ROOT / "docs" / "images").mkdir(parents=True, exist_ok=True)
    png_files.write_png(ROOT / "docs" / "images" / "logo.png", logo)
    png_files.write_png(ROOT / "src" / "main" / "resources" / "assets" / "c0de_paint" / "icon.png", logo)
    png_files.write_png(ROOT / "docs" / "images" / "logo_curseforge.png", raster(400))


if __name__ == "__main__":
    main()
