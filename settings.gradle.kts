pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // https://stonecutter.kikugie.dev/
    id("dev.kikugie.stonecutter") version "0.9.8"

    // Applies the right Loom variant for obfuscated (<26.1) and unobfuscated (>=26.1) versions
    // https://codeberg.org/KikuGie/loom-back-compat
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // Downloads the JDKs required by the toolchains (Java 21 for 1.21.x, Java 25 for 26.x)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "c0de_net"

// Requests and answers: plain Java, no dependency on Minecraft, a mod loader or c0de
include("core")

stonecutter {
    create(rootProject) {
        // Creates the nodes `versions/{version}-{loader}`, built with `build.{loader}.gradle.kts`.
        // The same nodes as c0de (https://github.com/joernott/minecraft-c0de/blob/main/settings.gradle.kts)
        fun match(project: String, vararg loaders: String, version: String = project) {
            for (loader in loaders) version("$project-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        match("1.21.1", "fabric", "neoforge")
        match("1.21.11", "fabric", "neoforge")
        match("26.1.2", "fabric", "neoforge")
        match("26.2", "fabric", "neoforge")
        match("26.3", "fabric", "neoforge")

        // The committed sources are in the state of this node
        vcsVersion = "1.21.1-fabric"
    }
}
