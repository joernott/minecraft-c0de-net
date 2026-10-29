"""Generates the Patchouli book "c0de net Handbook" from the handbook docs/reference/net.md, like the handbooks of c0de.

Every "## " section of the handbook becomes a category, every "### " element an entry with the same parts: a
description, the recipe (a crafting page; for functions, the parameters and results) and an example. The
paragraphs and list items are distributed over text pages; programs get pages of their own. The book, its recipe
(only loaded if Patchouli is installed; in the formats of Minecraft 1.21.1 and of 1.21.2 and later) and the pages
are generated files: the CI runs this script and fails if they differ from the committed files.

Usage: python scripts/generate_book.py
"""

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HANDBOOK = ROOT / "docs" / "reference" / "net.md"
RESOURCES = ROOT / "src" / "main" / "resources"
VERSIONED = ROOT / "src" / "main" / "versioned" / "recipes"
BOOK = "handbook"
CONTENT = RESOURCES / "assets" / "c0de_net" / "patchouli_books" / BOOK / "en_us"
IMAGE = re.compile(r"!\[[^\]]*\]\(images/([a-z0-9_]+)\.png\)")

# The number of characters that fit on a text page; the first page of an entry also shows its title
PAGE_CHARACTERS = 320
FIRST_PAGE_CHARACTERS = 260
# The number of lines of a program on one page; longer programs continue on the next page
CODE_LINES = 12

# The icons of the categories and entries: by the slug of the entry, then of the category
ICONS = {
    "getting_started": "c0de:include_block",
    "how_requests_are_sent": "minecraft:string",
    "the_handbook": "c0de:include_block",
    "settings": "minecraft:writable_book",
    "c0de_net_http": "c0de:call_block",
    "http_connection": "c0de:type_struct_block",
    "errors": "c0de:type_error_block",
    "examples": "c0de:program_block",
}

# The recipe of the book: an INCLUDE block surrounded by eight strings (user decision)
RECIPE = {
    "pattern": ["SSS", "SIS", "SSS"],
    "key": {"S": "minecraft:string", "I": "c0de:include_block"},
}


def write(path: Path, content: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def inline(text: str) -> str:
    """Markdown bold and code in the formatting codes of Patchouli."""
    text = re.sub(r"\*\*(.+?)\*\*", r"$(l)\1$()", text)
    return re.sub(r"`(.+?)`", r"$(1)\1$()", text)


def blocks(lines: list[str]) -> list[tuple[str, str]]:
    """The blocks of a section: ("text", paragraph), ("item", list item) or ("code", program)."""
    result = []
    paragraph: list[str] = []
    code: list[str] | None = None

    def flush() -> None:
        if paragraph:
            if all(line.startswith("- ") for line in paragraph):
                result.extend(("item", "$(li)" + inline(line[2:].strip())) for line in paragraph)
            else:
                result.append(("text", inline(" ".join(line.strip() for line in paragraph))))
            paragraph.clear()

    for line in lines:
        if code is not None:
            if line.startswith("```"):
                result.append(("code", "\n".join(code)))
                code = None
            else:
                code.append(line.rstrip())
        elif line.startswith("```"):
            flush()
            code = []
        elif not line.strip():
            flush()
        else:
            paragraph.append(line)
    flush()
    return result


def code_pages(program: str) -> list[dict]:
    """Program lines, the indentation kept with spaces (two per block, to save width)."""
    lines = []
    for line in program.split("\n"):
        stripped = line.lstrip(" ")
        depth = (len(line) - len(stripped)) // 4
        lines.append("  " * depth + stripped)
    return [{"type": "patchouli:text", "text": "$(1)" + "$(br)".join(lines[start:start + CODE_LINES]) + "$()"}
            for start in range(0, len(lines), CODE_LINES)]


def pages(section: list[str], first: bool = True) -> list[dict]:
    result = []
    text = ""
    previous = None
    limit = FIRST_PAGE_CHARACTERS if first else PAGE_CHARACTERS
    for kind, content in blocks(section):
        if kind == "code":
            if text:
                result.append({"type": "patchouli:text", "text": text})
                text = ""
            result.extend(code_pages(content))
            limit = PAGE_CHARACTERS
            previous = kind
            continue
        # List items follow each other directly, everything else is separated by an empty line
        separator = "" if kind == "item" and previous == "item" else "$(br2)"
        if text and len(text) + len(content) + len(separator) > limit:
            result.append({"type": "patchouli:text", "text": text})
            text = ""
            limit = PAGE_CHARACTERS
        text = content if not text else text + separator + content
        previous = kind
    if text:
        result.append({"type": "patchouli:text", "text": text})
    return result


def parts(lines: list[str]) -> dict[str, list[str]]:
    """The parts of an element: description, recipe, parameters, example."""
    result: dict[str, list[str]] = {"description": []}
    current = "description"
    markers = {"**Recipe**": "recipe", "**Parameters and results**": "parameters", "**Example**": "example"}
    for line in lines:
        if line.strip() in markers:
            current = markers[line.strip()]
            result.setdefault(current, [])
        else:
            result.setdefault(current, []).append(line)
    return result


def element_pages(lines: list[str]) -> list[dict]:
    element = parts(lines)
    result = pages(element["description"])
    if "recipe" in element:
        recipe = element["recipe"]
        images = [m.group(1) for line in recipe for m in IMAGE.finditer(line)]
        note = " ".join(line.strip() for line in recipe if line.strip() and not IMAGE.search(line))
        for index, image in enumerate(images):
            page = {"type": "patchouli:crafting", "recipe": "c0de_net:" + image}
            # A short sentence fits below the crafting grid, a longer one gets a page of its own
            if index == 0 and note and len(note) <= 110:
                page["text"] = inline(note)
            result.append(page)
        if note and (not images or len(note) > 110):
            result.append({"type": "patchouli:text", "text": inline(note)})
    for part in ("parameters", "example"):
        if part in element:
            result.extend(pages(element[part], first=False))
    return result


def recipe(modern: bool) -> dict:
    """The recipe of the book; until 1.21.1 an ingredient is an object, since 1.21.2 an item ID."""
    return {
        "fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["patchouli"]}],
        "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "patchouli"}],
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": RECIPE["pattern"],
        "key": {key: item if modern else {"item": item} for key, item in RECIPE["key"].items()},
        "result": {"id": "patchouli:guide_book", "count": 1, "components": {"patchouli:book": "c0de_net:" + BOOK}},
    }


def main() -> None:
    # The pages are generated anew, so that pages of removed sections don't stay
    if CONTENT.exists():
        shutil.rmtree(CONTENT)
    lines = HANDBOOK.read_text(encoding="utf-8").splitlines()
    title = next(line[2:].strip() for line in lines if line.startswith("# "))
    intro: list[str] = []
    categories: list[tuple[str, list[str], list[tuple[str, list[str]]]]] = []
    for line in lines:
        if line.startswith("# "):
            continue
        if line.startswith("## "):
            categories.append((line[3:].strip(), [], []))
        elif line.startswith("### "):
            categories[-1][2].append((line[4:].strip(), []))
        elif categories and categories[-1][2]:
            categories[-1][2][-1][1].append(line)
        elif categories:
            categories[-1][1].append(line)
        else:
            intro.append(line)

    write(RESOURCES / "data" / "c0de_net" / "patchouli_books" / BOOK / "book.json", {
        "name": title,
        "landing_text": " ".join(text for kind, text in blocks(intro) if kind == "text"),
        "version": 1,
        "use_resource_pack": True,
        "model": "patchouli:book_green",
        "book_texture": "patchouli:textures/gui/book_green.png",
        "creative_tab": "c0de:c0de",
        "show_progress": False,
    })
    write(VERSIONED / "1.21.1" / "data" / "c0de_net" / "recipe" / (BOOK + ".json"), recipe(False))
    write(VERSIONED / "1.21.2" / "data" / "c0de_net" / "recipe" / (BOOK + ".json"), recipe(True))

    for number, (category_title, description, elements) in enumerate(categories):
        category = slug(category_title)
        write(CONTENT / "categories" / (category + ".json"), {
            "name": category_title,
            "description": " ".join(text for kind, text in blocks(description) if kind == "text"),
            "icon": ICONS[category],
            "sortnum": number,
        })
        for position, (name, element) in enumerate(elements):
            write(CONTENT / "entries" / category / (slug(name) + ".json"), {
                "name": name,
                "icon": ICONS.get(slug(name), ICONS[category]),
                "category": "c0de_net:" + category,
                "sortnum": position,
                "pages": element_pages(element),
            })


if __name__ == "__main__":
    main()
