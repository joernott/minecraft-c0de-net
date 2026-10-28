plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Version- and loader-specific properties from `stonecutter.properties.toml`
    properties {
        tags(version, loader)
    }

    // Constants for Stonecutter comments, e.g. `//? if fabric {`
    constants {
        match(loader, "fabric", "neoforge")
    }

    replacements {
        // Replacements work in both directions (when the active node changes), so they must be unambiguous
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            replace(".location()", ".identifier()")
        }
        // The GameTest framework was rewritten in 1.21.5; Fabric has an annotation of its own (FabricGameTests)
        string(current.parsed >= "1.21.5" && loader == "fabric") {
            replace("import net.minecraft.gametest.framework.GameTest;", "import net.fabricmc.fabric.api.gametest.v1.GameTest;")
            replace("@GameTest(template = ", "@GameTest(structure = ")
            replace(", timeoutTicks = ", ", maxTicks = ")
        }
        // NeoForge registers the tests of NeoForgeGameTests itself since 1.21.11 (GameTestRegistration)
        string(current.parsed >= "1.21.11" && loader == "neoforge") {
            replace("import net.minecraft.gametest.framework.GameTest;", "import de.joernott.c0denet.gametest.neoforge.GameTestRegistration.GameTest;")
            replace("import net.neoforged.neoforge.gametest.GameTestHolder;", "import de.joernott.c0denet.gametest.neoforge.GameTestRegistration.GameTestHolder;")
            replace("import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;", "import de.joernott.c0denet.gametest.neoforge.GameTestRegistration.PrefixGameTestTemplate;")
        }
    }
}
