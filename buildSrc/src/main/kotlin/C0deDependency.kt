import org.gradle.api.artifacts.dsl.RepositoryHandler

/**
 * The jars of c0de come from its GitHub releases (https://github.com/joernott/minecraft-c0de/releases), e.g.
 * `v0.3.0/c0de-fabric-0.3.0+1.21.1.jar`, until the Modrinth project of c0de is available on the Modrinth Maven
 * repository.
 */
fun RepositoryHandler.c0deReleases() {
    ivy {
        name = "c0de releases"
        setUrl("https://github.com/joernott/minecraft-c0de/releases/download")
        patternLayout { artifact("v[revision]/[module]-[revision]+[classifier].[ext]") }
        metadataSources { artifact() }
        content { includeGroup("de.joernott.c0de") }
    }
}

/** The c0de jar of a loader and Minecraft version, e.g. `c0de("fabric", "1.21.1", "0.3.0")`. */
fun c0de(loader: String, minecraft: String, version: String): String = "de.joernott.c0de:c0de-$loader:$version:$minecraft"
