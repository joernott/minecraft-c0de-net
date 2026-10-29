"""The logo of c0de net: the two green slashes of the c0de logo in front of a wire globe, the web, on black.

Writes the picture as PNG (256 x 256, drawn with 4 x 4 samples per pixel) to docs/images/logo.png (for Modrinth)
and src/main/resources/assets/c0de_net/icon.png (the icon in the mod lists). The alternative docs/images/
logo_cobweb.png has a cobweb in an item frame behind the slashes, as Minecraft shows it: the frame of birch planks,
its leather back and the cobweb at half size. Its textures come from the Minecraft client jar in the Gradle cache
of Loom, so the script runs on a machine that has built the node 1.21.1-fabric. Only the standard library is used.

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
# The item frame: 12 of its pixels across the picture (20 picture pixels each), one of them its border
FRAME_PIXEL = 20
FRAME_START = (SIZE - 12 * FRAME_PIXEL) // 2
# The item in a frame is drawn at half size: 8 frame pixels for its 16 pixels
COBWEB_PIXEL = FRAME_PIXEL / 2
COBWEB_START = (SIZE - 16 * COBWEB_PIXEL) / 2


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


def framed_cobweb():
    """The background of the alternative logo: a cobweb in an item frame."""
    cobweb, back, planks = texture("cobweb"), texture("item_frame"), texture("birch_planks")

    def color(x: float, y: float) -> tuple[int, int, int]:
        cx, cy = int((x - COBWEB_START) // COBWEB_PIXEL), int((y - COBWEB_START) // COBWEB_PIXEL)
        if 0 <= cx < 16 and 0 <= cy < 16 and cobweb[cy][cx][3]:
            return cobweb[cy][cx][:3]
        fx, fy = int((x - FRAME_START) // FRAME_PIXEL), int((y - FRAME_START) // FRAME_PIXEL)
        if not (0 <= fx < 12 and 0 <= fy < 12):
            return BACKGROUND
        # The frame pixels are those of the textures at the same place of the block (2 to 13)
        if fx in (0, 11) or fy in (0, 11):
            return planks[fy + 2][fx + 2][:3]
        return back[fy + 2][fx + 2][:3]

    return color


def raster(background):
    def color(x: float, y: float) -> tuple[int, int, int]:
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
    png_files.write_png(ROOT / "docs" / "images" / "logo_cobweb.png", raster(framed_cobweb()))


if __name__ == "__main__":
    main()
