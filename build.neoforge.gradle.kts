plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("neoforge-mutex")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val modId = property("mod.id") as String

// The development runs of 1.21.1 keep their directories (run/fabric, run/neoforge); the other versions get
// their own (e.g. run/fabric-1.21.11), because mods in the mods folders work for one version only
val runSuffix = if (sc.current.version == "1.21.1") "" else "-${sc.current.version}"

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") { name = "BlameJared" }
    c0deReleases()
}

dependencies {
    // c0de of the same Minecraft version, which runs the programs that use the library
    implementation(c0de("neoforge", sc.current.version, sc.properties["deps.c0de"]))
    // c0de_net/mail: Jakarta Mail with Angus Mail, embedded in the mod jar
    mailLibraries.forEach { library ->
        implementation(library)
        val (group, name, version) = library.split(":")
        jarJar("$group:$name:[$version,${version.substringBefore('.').toInt() + 1}.0)") {
            version {
                prefer(version)
            }
        }
    }
}

// The requests and answers are compiled into every node
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
    // Data whose format changed between the Minecraft versions (buildSrc/VersionedResources.kt)
    versionedResourceDirs(rootProject.file("src/main/versioned")) { sc.current.parsed >= it }
        .forEach { resources.srcDir(it) }
}

neoForge {
    version = property("deps.neo_loader") as String

    sc.properties.getOrNull("deps.parchment")?.let {
        parchment {
            minecraftVersion = sc.current.version
            mappingsVersion = it
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            jvmArguments.addAll("-Xms2G", "-Xmx6G")
        }

        register("client") {
            gameDirectory = rootProject.file("run/neoforge$runSuffix")
            client()
            // Fixed user name for multiplayer tests, as in c0de
            programArguments.addAll("--username", "Tester1")
        }

        // A second client for multiplayer tests: its own game directory and user name
        register("client2") {
            gameDirectory = rootProject.file("run/neoforge-client2$runSuffix")
            client()
            programArguments.addAll("--username", "Tester2")
        }

        register("server") {
            gameDirectory = rootProject.file("run/neoforge$runSuffix")
            server()
        }

        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("build/gametest")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            // The GameTests check the programs of the handbook
            systemProperty("c0denet.handbook", rootProject.file("docs/reference/net.md").absolutePath)
        }
    }
}

// Created by ModDevGradle once the NeoForge version is set: libraries for development runs, which are not mods
// (the jar-in-jar libraries are only unpacked in production). From 1.21.11 on, the normal runtime classpath is used.
if (sc.current.parsed < "1.21.11") {
    dependencies {
        mailLibraries.forEach { "additionalRuntimeClasspath"(it) }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("description", sc.properties["mod.description"])
            register("authors", sc.properties["mod.authors"])
            register("license", sc.properties["mod.license"])
            register("sources", sc.properties["mod.sources_url"])
            register("issues", sc.properties["mod.issues_url"])
            register("minecraft", sc.properties["mod.mc_compat"])
            register("c0de", sc.properties["deps.c0de"])
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        exclude("fabric.mod.json")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val id = project.property("mod.id") as String
        inputs.property("mod_id", id)
        from(rootProject.file("LICENSE")) { rename { "${it}_$id" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Publishing to Modrinth and CurseForge (task 7.4): `publishMods` is a dry run unless MODRINTH_TOKEN, CURSEFORGE_TOKEN
// and both project IDs are set
publishMods {
    val modVersion = property("mod.version") as String
    val projectId = sc.properties.get("publish.modrinth_id")
    val token = providers.environmentVariable("MODRINTH_TOKEN")
    val curseforgeId = sc.properties.get("publish.curseforge_id")
    val curseforgeToken = providers.environmentVariable("CURSEFORGE_TOKEN")
    // A version that is already released on GitHub is published from the jars of its release
    // (-Ppublish.released_jars=<folder>, .github/workflows/curseforge.yml, task 7.4)
    // The jar task stays the input of the publishing tasks, so the release builds the jars it publishes
    val releasedJars = providers.gradleProperty("publish.released_jars").orNull
    val root = rootProject.layout.projectDirectory
    val releasedJar = "${base.archivesName.get()}-${project.version}.jar"
    if (releasedJars == null) {
        file = tasks.jar.flatMap { it.archiveFile }
    } else {
        file = root.file("$releasedJars/$releasedJar")
    }
    version = "${modVersion}+${sc.current.version}-neoforge"
    displayName = "c0de net $modVersion for NeoForge ${sc.current.version}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
        .map { changelogSection(it, modVersion) }
    type = if (modVersion.startsWith("0.")) ALPHA else STABLE
    modLoaders.add("neoforge")
    dryRun = token.zip(curseforgeToken) { modrinth, curseforge -> modrinth.isBlank() || curseforge.isBlank() }
        .map { it || projectId.isBlank() || curseforgeId.isBlank() }.orElse(true)
    modrinth {
        this.projectId = projectId
        accessToken = token
        // The server runs the library, the client sends the requests
        environment = CLIENT_AND_SERVER
        minecraftVersions.addAll(sc.properties.get("mod.mc_releases").split(",").map { it.trim() })
        requires("c0de")
    }
    curseforge {
        this.projectId = curseforgeId
        accessToken = curseforgeToken
        clientRequired = true
        serverRequired = true
        // -Ppublish.curseforge_without_c0de leaves c0de out: CurseForge refuses it as a dependency until its project is
        // approved (task 7.4); add it to the files on curseforge.com afterwards
        val c0de = !providers.gradleProperty("publish.curseforge_without_c0de").isPresent
        minecraftVersions.addAll(sc.properties.get("mod.mc_releases").split(",").map { it.trim() })
        if (c0de) requires("c0de")
    }
}

// All compile errors at once, which helps when porting to a new Minecraft version
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}
