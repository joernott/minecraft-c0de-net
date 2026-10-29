"""The logo of c0de net: the two green slashes of the c0de logo in front of a wire globe, the web, on black.

Writes the picture as PNG (256 x 256, drawn with 4 x 4 samples per pixel) to docs/images/logo.png (for Modrinth)
and src/main/resources/assets/c0de_net/icon.png (the icon in the mod lists).
Only the standard library is used.

Usage: python scripts/generate_logo.py
"""

import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import png_files  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
SIZE = 256
SAMPLES = 4

BACKGROUND = (0x00, 0x00, 0x00)
GLOBE = (0x00, 0x6E, 0x00)
SLASH = (0x00, 0xC8, 0x00)
SLASH_EDGE = (0x00, 0x5E, 0x00)

CENTER = 128.0
RADIUS = 104.0
GLOBE_WIDTH = 7.0
# Meridians: ellipses with these horizontal radii (the circle itself is the outer one); parallels at these heights
MERIDIANS = [0.0, RADIUS * math.sin(math.radians(35)), RADIUS * math.sin(math.radians(70))]
PARALLELS = [0.0, RADIUS * math.sin(math.radians(35)), -RADIUS * math.sin(math.radians(35))]
# The slashes: parallelograms from the lower left to the upper right, with a black gap around them
SLASHES = [
    [(59, 196), (89, 196), (141, 60), (111, 60)],
    [(115, 196), (145, 196), (197, 60), (167, 60)],
]
EDGE = 5.0
GAP = 9.0


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


def on_globe(x: float, y: float) -> bool:
    dx, dy = x - CENTER, y - CENTER
    distance = math.hypot(dx, dy)
    if abs(distance - RADIUS) < GLOBE_WIDTH / 2:
        return True
    if distance > RADIUS:
        return False
    for height in PARALLELS:
        if abs(dy - height) < GLOBE_WIDTH / 2:
            return True
    for rx in MERIDIANS:
        if rx == 0:
            if abs(dx) < GLOBE_WIDTH / 2:
                return True
            continue
        # The distance to the ellipse, from its implicit function and its gradient
        f = math.sqrt((dx / rx) ** 2 + (dy / RADIUS) ** 2)
        gradient = math.hypot(dx / rx ** 2, dy / RADIUS ** 2) / max(f, 1e-9)
        if abs(f - 1) / max(gradient, 1e-9) < GLOBE_WIDTH / 2:
            return True
    return False


def color(x: float, y: float) -> tuple[int, int, int]:
    for slash in SLASHES:
        if inside(slash, x, y):
            return SLASH if edge_distance(slash, x, y) > EDGE else SLASH_EDGE
    for slash in SLASHES:
        if edge_distance(slash, x, y) < GAP:
            return BACKGROUND
    return GLOBE if on_globe(x, y) else BACKGROUND


def raster():
    image = []
    for py in range(SIZE):
        row = []
        for px in range(SIZE):
            total = [0, 0, 0]
            for sy in range(SAMPLES):
                for sx in range(SAMPLES):
                    c = color(px + (sx + 0.5) / SAMPLES, py + (sy + 0.5) / SAMPLES)
                    for i in range(3):
                        total[i] += c[i]
            row.append(tuple(v // (SAMPLES * SAMPLES) for v in total) + (255,))
        image.append(row)
    return image


def main() -> None:
    image = raster()
    png_files.write_png(ROOT / "docs" / "images" / "logo.png", image)
    png_files.write_png(ROOT / "src" / "main" / "resources" / "assets" / "c0de_net" / "icon.png", image)


if __name__ == "__main__":
    main()
