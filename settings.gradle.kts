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

rootProject.name = "Rosh"

include(
  ":rosh", ":lib", ":terminal",
  ":code", ":berkas",":browser"
)
