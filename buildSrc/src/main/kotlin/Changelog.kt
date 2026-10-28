/**
 * The section of a version in CHANGELOG.md: the lines after its heading `## <version>` (followed by anything,
 * e.g. the date) up to the next `## ` heading. Used as the changelog of the Modrinth versions (task 11.12).
 */
fun changelogSection(text: String, version: String): String {
    val lines = text.lines()
    val start = lines.indexOfFirst { it == "## $version" || it.startsWith("## $version ") }
    require(start >= 0) { "CHANGELOG.md has no section ## $version" }
    val end = lines.drop(start + 1).indexOfFirst { it.startsWith("## ") }
    val section = if (end < 0) lines.drop(start + 1) else lines.subList(start + 1, start + 1 + end)
    return section.joinToString("\n").trim()
}
