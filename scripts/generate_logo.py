"""The logo of c0de net: the two green slashes of the c0de logo in front of a wire globe, the web, on black.

Writes the picture as PNG (256 x 256, drawn with 4 x 4 samples per pixel) to docs/images/logo.png (for Modrinth)
and src/main/resources/assets/c0de_net/icon.png (the icon in the mod lists). The alternative docs/images/
logo_cobweb.png has the cobweb of Minecraft on black instead of the globe, in front of the slashes, which its
threads partly cover. Its texture comes from the Minecraft client jar in the Gradle cache of Loom, so the script
runs on a machine that has built the node 1.21.1-fabric. Only the standard library is used.

Usage: python scripts/generate_logo.py
"""

import math
import sys
import zipfile
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

CLIENT_JAR = Path.home() / ".gradle" / "caches" / "fabric-loom" / "1.21.1" / "minecraft-client.jar"
# The cobweb hangs in the upper right corner, over the upper ends of the slashes: 10 picture pixels per pixel
COBWEB_PIXEL = 10
COBWEB_LEFT = SIZE - 16 * COBWEB_PIXEL


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


def globe(x: float, y: float) -> tuple[int, int, int]:
    return GLOBE if on_globe(x, y) else BACKGROUND


def texture(name: str):
    """A block texture of Minecraft, e.g. cobweb, written to a temporary file to read it."""
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        data = jar.read("assets/minecraft/textures/block/" + name + ".png")
    temp = ROOT / "docs" / "images" / ".texture.png"
    temp.write_bytes(data)
    image = png_files.read_png(temp)
    temp.unlink()
    return image[:16]


def cobweb():
    """The foreground of the alternative logo: the cobweb, None where it is transparent."""
    image = texture("cobweb")

    def color(x: float, y: float):
        cx, cy = int((x - COBWEB_LEFT) // COBWEB_PIXEL), int(y // COBWEB_PIXEL)
        if not (0 <= cx < 16 and 0 <= cy < 16) or not image[cy][cx][3]:
            return None
        return image[cy][cx][:3]

    return color


def raster(background, foreground=None):
    def color(x: float, y: float) -> tuple[int, int, int]:
        front = foreground(x, y) if foreground else None
        if front:
            return front
        for slash in SLASHES:
            if inside(slash, x, y):
                return SLASH if edge_distance(slash, x, y) > EDGE else SLASH_EDGE
        for slash in SLASHES:
            if edge_distance(slash, x, y) < GAP:
                return BACKGROUND
        return background(x, y)

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
    image = raster(globe)
    png_files.write_png(ROOT / "docs" / "images" / "logo.png", image)
    png_files.write_png(ROOT / "src" / "main" / "resources" / "assets" / "c0de_net" / "icon.png", image)
    png_files.write_png(ROOT / "docs" / "images" / "logo_cobweb.png", raster(lambda x, y: BACKGROUND, cobweb()))


if __name__ == "__main__":
    main()
