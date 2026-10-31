pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{url=uri("https://jitpack.io")}
    }
}

rootProject.name = "RoshAll"

include(
  ":all", ":lib", ":file_explorer",
  ":code", ":terminal",":gambar",
  ":video",
  ":browser"
)
