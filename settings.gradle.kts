pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.spongepowered.org/maven")
    }
    resolutionStrategy {
        eachPlugin {
            requested.version?.let {
                if (it.startsWith("useModule@")) {
                    useVersion("")
                    useModule(it.removePrefix("useModule@"))
                }
            }
        }
    }
}