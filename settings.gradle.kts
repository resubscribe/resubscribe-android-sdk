pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven {
//            name = "myrepo"
//            url = uri("/Users/jack/resubscribe/resubscribe-android-sdk/build/repo")
//        }
    }
}

rootProject.name = "Resubscribe Android SDK"
include(":app")
 