"""Generates the handbook "c0de net" of the mod from docs/reference/net.md, as a book of the handbooks of c0de
(c0de docs/concept/21-handbooks.md, handbooks of other mods since c0de 0.6.1).

The book is `c0de_net:net`: `assets/c0de_net/handbook/books/net.json` with its name, landing text and the colour of
the label of its binder, every "## " section of the handbook a category and every "### " element an entry with the
same parts as the handbooks of c0de: a description, the recipe (a crafting grid; for functions, the parameters and
results) and an example. The pages are Markdown for lavender-md; the book screen of c0de distributes them over
pages. The crafting grids are in `assets/c0de_net/handbook/recipes.json`, and the binder is the generic binder of
c0de (`c0de:handbook`), crafted with the recipe `c0de_net:handbook` in the formats of Minecraft 1.21.1 and of 1.21.2
and later. All of them are generated files: the CI runs this script and fails if they differ from the committed
files.

The German translation docs/de/reference/net.md (task 6.4) becomes the de_de pages of the book; c0de shows the pages
in the language of the game and falls back to English. The translation has the same headings in the same order,
and its pages get the IDs of the English headings at the same position. The name and the landing text of the book
are translation keys, written into the language files of the mod.

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
NAMESPACE = "c0de_net"
BOOK = "net"
BOOK_ID = NAMESPACE + ":" + BOOK
CONTENT = RESOURCES / "assets" / NAMESPACE / "handbook"
LANG = RESOURCES / "assets" / NAMESPACE / "lang"
ENGLISH = "en_us"
# The handbook in every language of the book
LANGUAGES = {ENGLISH: HANDBOOK, "de_de": ROOT / "docs" / "de" / "reference" / "net.md"}
# Images of recipes; the translations use the images of the English folder
IMAGE = re.compile(r"!\[[^\]]*\]\((?:\.\./\.\./reference/)?images/([a-z0-9_]+)\.png\)")
# The colour of the label of the binder (teal, next to the colours of the binders of c0de)
COLOR = "#3FB8C8"

# The icons of the categories and entries: by the slug of the entry, then of the category
ICONS = {
    "getting_started": "c0de:include_block",
    "how_requests_are_sent": "minecraft:string",
    "the_handbook": "c0de:handbook",
    "settings": "minecraft:writable_book",
    "c0de_net_http": "c0de:call_block",
    "http_connection": "c0de:type_struct_block",
    "errors": "c0de:type_error_block",
    "examples": "c0de:program_block",
}

# The recipe of the binder: an INCLUDE block surrounded by eight strings (user decision)
RECIPE = {
    "pattern": ["SSS", "SIS", "SSS"],
    "key": {"S": "minecraft:string", "I": "c0de:include_block"},
}

# The characters lavender-md and c0de's features read as Markdown; plain text escapes them with a backslash
SPECIAL = set("\\*_~{[]!<>%`;")
# Code, bold text and links in the documentation, which stay Markdown
INLINE = re.compile(r"`[^`]+`|\*\*(.+?)\*\*|\[([^\]]+)\]\(([^)]+)\)")
# The targets of links within the handbook: anchor -> target in the book, by language
LINKS: dict[str, dict[str, str]] = {}
CURRENT = {"language": ENGLISH}


def write(path: Path, content: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


def write_page(path: Path, meta: dict, markdown: str) -> None:
    """A category or entry: its metadata as a JSON block, then its Markdown."""
    path.parent.mkdir(parents=True, exist_ok=True)
    text = "```json\n" + json.dumps(meta, indent=2, ensure_ascii=False) + "\n```\n"
    if markdown:
        text += "\n" + markdown + "\n"
    path.write_text(text, encoding="utf-8", newline="\n")


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def anchor(heading: str) -> str:
    """The anchor GitHub gives a heading: lower case, without punctuation (letters like ä stay), spaces to -."""
    return re.sub(r"[^\w -]", "", heading.lower()).replace(" ", "-")


def escape(text: str) -> str:
    return "".join("\\" + c if c in SPECIAL else c for c in text)


def inline(text: str) -> str:
    """A line of the handbook as Markdown of the book: code and bold stay, links within the handbook become links
    of the book, web links stay links, other links become their text, everything else is escaped."""
    result = []
    position = 0
    for match in INLINE.finditer(text):
        result.append(escape(text[position:match.start()]))
        if match.group(0).startswith("`"):
            result.append(match.group(0))
        elif match.group(1) is not None:
            result.append("**" + inline(match.group(1)) + "**")
        elif match.group(3).startswith(("http://", "https://")):
            result.append("[" + inline(match.group(2)) + "](" + match.group(3) + ")")
        elif match.group(3).startswith("#") and match.group(3)[1:] in LINKS.get(CURRENT["language"], {}):
            result.append("[" + inline(match.group(2)) + "](^" + LINKS[CURRENT["language"]][match.group(3)[1:]] + ")")
        else:
            result.append(inline(match.group(2)))
        position = match.end()
    result.append(escape(text[position:]))
    return "".join(result)


def program(lines: list[str]) -> str:
    """A program example; the indentation is kept, two spaces per block, to save width."""
    indented = []
    for line in lines:
        stripped = line.lstrip(" ")
        depth = (len(line) - len(stripped)) // 4
        indented.append("  " * depth + stripped)
    return "```c0de\n" + "\n".join(indented) + "\n```"


def markdown(lines: list[str]) -> str:
    """The Markdown of a part of the handbook: paragraphs, lists, program examples and crafting grids."""
    result = []
    paragraph: list[str] = []
    code: list[str] | None = None

    def flush() -> None:
        if paragraph:
            if all(line.startswith("- ") for line in paragraph):
                result.append("\n".join("- " + inline(line[2:].strip()) for line in paragraph))
            else:
                result.append(inline(" ".join(line.strip() for line in paragraph)))
            paragraph.clear()

    for line in lines:
        image = IMAGE.fullmatch(line.strip()) if code is None else None
        if image:
            flush()
            result.append("<recipe;" + NAMESPACE + ":" + image.group(1) + ">")
            continue
        if code is not None:
            if line.startswith("```"):
                result.append(program(code))
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
    return "\n\n".join(result)


def text_only(lines: list[str]) -> str:
    """The paragraphs of a part, without lists and programs (descriptions of categories, the landing text)."""
    paragraphs = [block for block in markdown(lines).split("\n\n")
                  if not block.startswith(("- ", "```", "<recipe;"))]
    return "\n\n".join(paragraphs)


def structure(path: Path) -> tuple[str, list[str], list[tuple[str, list[str], list[tuple[str, list[str]]]]]]:
    """The title, the introduction and the categories ("## ") with their elements ("### ") of a handbook."""
    lines = path.read_text(encoding="utf-8").splitlines()
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
    return title, intro, categories


def recipe(modern: bool) -> dict:
    """The recipe of the binder; until 1.21.1 an ingredient is an object, since 1.21.2 an item ID, and since then the
    item model reads the colour of the label from the custom model data."""
    color = int(COLOR[1:], 16)
    components = {"minecraft:custom_data": {"book": BOOK_ID, "color": color}}
    if modern:
        components["minecraft:custom_model_data"] = {"colors": [color]}
    return {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": RECIPE["pattern"],
        "key": {key: item if modern else {"item": item} for key, item in RECIPE["key"].items()},
        "result": {"id": "c0de:handbook", "count": 1, "components": components},
    }


def grid() -> dict:
    """The crafting grid of the recipe of the binder for the pages of the book."""
    slots = [[RECIPE["key"][key]] if key != " " else [] for row in RECIPE["pattern"] for key in row]
    return {"slots": slots, "result": "c0de:handbook", "count": 1, "book": BOOK_ID, "color": COLOR}


def main() -> None:
    write(CONTENT / "books" / (BOOK + ".json"), {
        "name": "book." + NAMESPACE + "." + BOOK + ".name",
        "landing_text": "book." + NAMESPACE + "." + BOOK + ".landing_text",
        "texture": "c0de:textures/gui/handbook.png",
        "color": COLOR,
    })
    write(CONTENT / "recipes.json", {NAMESPACE + ":handbook": grid()})
    write(VERSIONED / "1.21.1" / "data" / NAMESPACE / "recipe" / "handbook.json", recipe(False))
    write(VERSIONED / "1.21.2" / "data" / NAMESPACE / "recipe" / "handbook.json", recipe(True))

    # The pages are generated anew, so that pages of removed sections don't stay
    for folder in (CONTENT / "categories", CONTENT / "entries"):
        if folder.exists():
            shutil.rmtree(folder)
    _, _, english = structure(HANDBOOK)
    texts = {}
    for language, handbook in LANGUAGES.items():
        if not handbook.exists():
            continue
        title, intro, categories = structure(handbook)
        if len(categories) != len(english):
            raise ValueError(f"{handbook}: {len(categories)} sections, the English handbook has {len(english)}")
        # The links within the handbook: the anchors of its headings in this language
        LINKS[language] = {}
        for number, (category_title, _, elements) in enumerate(categories):
            english_title, _, english_elements = english[number]
            LINKS[language][anchor(category_title)] = slug(english_title)
            for position, (name, _) in enumerate(elements):
                LINKS[language][anchor(name)] = slug(english_title) + "/" + slug(english_elements[position][0])
        CURRENT["language"] = language
        folder = "" if language == ENGLISH else language
        texts[language] = {
            "book." + NAMESPACE + "." + BOOK + ".name": title,
            "book." + NAMESPACE + "." + BOOK + ".landing_text": text_only(intro),
        }
        for number, (category_title, description, elements) in enumerate(categories):
            english_title, _, english_elements = english[number]
            if len(elements) != len(english_elements):
                raise ValueError(f"{handbook}, {category_title}: {len(elements)} elements, the English handbook has "
                                 f"{len(english_elements)}")
            category = slug(english_title)
            write_page(CONTENT / "categories" / BOOK / folder / (category + ".md"),
                       {"title": category_title, "icon": ICONS[category], "ordinal": number},
                       text_only(description))
            for position, (name, element) in enumerate(elements):
                entry = slug(english_elements[position][0])
                write_page(CONTENT / "entries" / BOOK / folder / category / (entry + ".md"),
                           {"title": name, "icon": ICONS.get(entry, ICONS[category]), "category": category,
                            "ordinal": position},
                           markdown(element))
    # The language files; a language without a translation of the handbook gets the English texts
    for language in LANGUAGES:
        write(LANG / (language + ".json"), texts.get(language, texts[ENGLISH]))


if __name__ == "__main__":
    main()
