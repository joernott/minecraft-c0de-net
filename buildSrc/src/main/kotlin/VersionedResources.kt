import java.io.File

/**
 * The resource directories of data whose format changed between the Minecraft versions (task 12.1): the directory
 * `src/main/versioned` has a directory per topic (e.g. `recipes`), which has a directory per format named after
 * the first Minecraft version of the format (e.g. `1.21.1`, `1.21.2`). Of every topic, the newest format that
 * [applies] (the version of the node is at least the name of the directory) is used.
 */
fun versionedResourceDirs(root: File, applies: (String) -> Boolean): List<File> =
    (root.listFiles() ?: emptyArray()).filter { it.isDirectory }.sortedBy { it.name }.mapNotNull { topic ->
        (topic.listFiles() ?: emptyArray())
            .filter { it.isDirectory && applies(it.name) }
            .maxWithOrNull(compareBy<File, List<Int>>(versionComparator) { versionParts(it.name) })
    }

private fun versionParts(version: String): List<Int> = version.split('.').map { it.toIntOrNull() ?: 0 }

private val versionComparator = Comparator<List<Int>> { a, b ->
    (0 until maxOf(a.size, b.size))
        .map { (a.getOrNull(it) ?: 0).compareTo(b.getOrNull(it) ?: 0) }
        .firstOrNull { it != 0 } ?: 0
}
