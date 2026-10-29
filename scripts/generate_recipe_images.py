"""Draws the recipe of the handbook as an image for docs/reference/net.md (docs/reference/images/handbook.png), like
generate_recipe_images.py of c0de: the crafting grid, an arrow and the green book of the handbook.

The INCLUDE block is read from the jar of c0de, the string and the book from the Minecraft client jar, both in the
Gradle caches. So this script runs on a machine that has built the node 1.21.1-fabric, not in the CI.

Usage: python scripts/generate_recipe_images.py
"""

import json
import os
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import png_files  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
RECIPES = ROOT / "src" / "main" / "versioned" / "recipes" / "1.21.1" / "data" / "c0de_net" / "recipe"
OUTPUT = ROOT / "docs" / "reference" / "images"
GRADLE = Path(os.path.expanduser("~/.gradle/caches"))
CLIENT_JAR = GRADLE / "fabric-loom" / "1.21.1" / "minecraft-client.jar"

SCALE = 2
SLOT = 18
PANEL = (0xC6, 0xC6, 0xC6, 255)
SLOT_INSIDE = (0x8B, 0x8B, 0x8B, 255)
SLOT_DARK = (0x37, 0x37, 0x37, 255)
SLOT_LIGHT = (0xFF, 0xFF, 0xFF, 255)
TEXT = (0x3F, 0x3F, 0x3F, 255)


def c0de_jar() -> Path:
    """The jar of c0de for 1.21.1 on Fabric, downloaded by Gradle from the GitHub releases of c0de."""
    with open(ROOT / "stonecutter.properties.toml", encoding="utf-8") as file:
        version = next(line.split('"')[1] for line in file if line.strip().startswith("deps.c0de"))
    jars = sorted(GRADLE.glob("modules-2/files-2.1/de.joernott.c0de/c0de-fabric/" + version
                              + "/*/c0de-fabric-" + version + "-1.21.1.jar"))
    if not jars:
        sys.exit("The jar of c0de " + version + " is missing: build the node 1.21.1-fabric first")
    return jars[0]


def texture(jar: Path, name: str):
    with zipfile.ZipFile(jar) as archive:
        data = archive.read(name)
    temp = OUTPUT / ".texture.png"
    temp.write_bytes(data)
    image = png_files.read_png(temp)
    temp.unlink()
    return image[:16]


def icon(item: str):
    namespace, name = item.split(":")
    if namespace == "c0de":
        # The icons of the language blocks are their top textures, as in c0de
        return texture(c0de_jar(), "assets/c0de/textures/block/" + name + ".png")
    if namespace == "patchouli":
        # The handbook is a green book
        book = texture(CLIENT_JAR, "assets/minecraft/textures/item/book.png")
        return [[(r // 2, min(255, g + 40), b // 2, a) for r, g, b, a in row] for row in book]
    return texture(CLIENT_JAR, "assets/minecraft/textures/item/" + name + ".png")


def fill(canvas, x, y, width, height, color):
    for row in range(y, y + height):
        for column in range(x, x + width):
            canvas[row][column] = color


def slot(canvas, x, y, item):
    fill(canvas, x, y, SLOT, SLOT, SLOT_INSIDE)
    fill(canvas, x, y, SLOT - 1, 1, SLOT_DARK)
    fill(canvas, x, y, 1, SLOT - 1, SLOT_DARK)
    fill(canvas, x + 1, y + SLOT - 1, SLOT - 1, 1, SLOT_LIGHT)
    fill(canvas, x + SLOT - 1, y + 1, 1, SLOT - 1, SLOT_LIGHT)
    if not item:
        return
    image = icon(item)
    for row in range(16):
        for column in range(16):
            if image[row][column][3]:
                canvas[y + 1 + row][x + 1 + column] = image[row][column]


def grid(recipe: dict) -> list:
    """The 9 items of the crafting grid, row by row; None for empty slots."""
    cells = [None] * 9
    for row, line in enumerate(recipe["pattern"]):
        for column, key in enumerate(line):
            if key != " ":
                cells[row * 3 + column] = recipe["key"][key]["item"]
    return cells


def draw(recipe: dict):
    width, height = 3 * SLOT + 40 + SLOT + 8, 3 * SLOT + 8
    canvas = [[PANEL] * width for _ in range(height)]
    for i, item in enumerate(grid(recipe)):
        slot(canvas, 4 + (i % 3) * SLOT, 4 + (i // 3) * SLOT, item)
    arrow_x, middle = 4 + 3 * SLOT + 8, 4 + SLOT + SLOT // 2
    fill(canvas, arrow_x, middle - 1, 16, 3, TEXT)
    for i in range(6):
        fill(canvas, arrow_x + 16 + i, middle - 5 + i, 1, 11 - 2 * i, TEXT)
    slot(canvas, arrow_x + 32, 4 + SLOT, recipe["result"]["id"])
    return [[pixel for pixel in row for _ in range(SCALE)] for row in canvas for _ in range(SCALE)]


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for file in sorted(RECIPES.glob("*.json")):
        png_files.write_png(OUTPUT / (file.stem + ".png"), draw(json.loads(file.read_text(encoding="utf-8"))))


if __name__ == "__main__":
    main()
