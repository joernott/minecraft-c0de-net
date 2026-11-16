// Requests and answers of c0de net: plain Java without any dependency on Minecraft, a mod loader or c0de.
// The mod nodes compile these sources directly (see build.fabric.gradle.kts and build.neoforge.gradle.kts),
// this project exists to test them in isolation.
//
// Formatting of the other files (mod sources with Stonecutter comments, build scripts) is checked by
// scripts/check-whitespace.sh, because Spotless only accepts files within its project directory.
plugins {
    `java-library`
    id("com.diffplug.spotless") version "8.11.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // The lowest Java version of all nodes (Minecraft 1.21.x)
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // c0de_net/mail (docs/concept.md#the-library-c0de_netmail): Jakarta Mail with its implementation Angus Mail
    implementation("org.eclipse.angus:angus-mail:2.0.5")
    // A mail server (SMTP and IMAP) in the tests
    testImplementation("com.icegreen:greenmail-junit5:2.1.15")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Formatting: `./gradlew spotlessCheck` (part of `check`) and `./gradlew spotlessApply`
spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
