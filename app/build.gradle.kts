plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("maven-publish")
//    id("signing")
}

android {
    namespace = "ai.resubscribe.android.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui:1.6.6")
    implementation("androidx.compose.ui:ui-graphics:1.6.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.6")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation(libs.androidx.material3)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


//afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("release") {
//                from(components["release"])
//                groupId = "com.github.resubscribe"
//                artifactId = "resubscribe-android-sdk"
//                version = "1.0.6"
//            }
//        }
//    }
//}




afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "ai.resubscribe"
                artifactId = "resubscribe-android-sdk"
                version = "1.0.11"

                pom {
                    name = "Resubscribe Android SDK"
                    description = "Run in-app user conversations with AI -- right after a user doesn't convert"
                    url = "https://github.com/resubscribe/resubscribe-android-sdk/"
                    licenses {
                        license {
                            name = "MIT"
                            url = "https://opensource.org/license/mit"
                        }
                    }
                    developers {
                        developer {
                            id = "resubscribe"
                            name = "Resubscribe"
                            email = "jack@resubscribe.ai"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/resubscribe/resubscribe-android-sdk.git"
                        developerConnection = "scm:git:ssh://git@github.com/resubscribe/resubscribe-android-sdk.git"
                        url = "https://github.com/resubscribe/resubscribe-android-sdk/"
                    }
                }
            }
        }
        repositories {
            maven {
                name = "myrepo"
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
    }
//    signing {
//        useInMemoryPgpKeys(findProperty("signing.keyId").toString(),
//            findProperty("signing.secretKeyRingFile").toString(), findProperty("signing.password").toString()
//        )
//        sign("publishing.publications.release")
//    }
    tasks.register<Zip>("generateRepo") {
        val publishTask = tasks.named(
            "publishReleasePublicationToMyrepoRepository",
            PublishToMavenRepository::class.java)
        from(publishTask.map { it.repository.url })
        destinationDirectory.set(layout.buildDirectory.dir("lib"))
        archiveFileName.set("pub.zip")
    }
}


//publishing {
//    publications {
//        create<MavenPublication>("maven-android") {
//            groupId = "ai.resubscribe.android.sdk"
//            artifactId = "resubscribe-android"
//            version = "1.0.1"
//
//            pom {
//                name = "Resubscribe Android SDK"
//                description = "Run in-app user conversations with AI -- right after a user doesn't convert"
//                url = "https://github.com/resubscribe/resubscribe-android-sdk/"
//                licenses {
//                    license {
//                        name = "MIT"
//                        url = "https://opensource.org/license/mit"
//                    }
//                }
//                developers {
//                    developer {
//                        id = "resubscribe"
//                        name = "Resubscribe"
//                        email = "jack@resubscribe.ai"
//                    }
//                }
//                scm {
//                    connection = "scm:git:git://github.com/resubscribe/resubscribe-android-sdk.git"
//                    developerConnection = "scm:git:ssh://git@github.com/resubscribe/resubscribe-android-sdk.git"
//                    url = "https://github.com/resubscribe/resubscribe-android-sdk/"
//                }
//            }
//        }
//    }
//    repositories {
//        maven {
//            name = "MC"
//            url = uri("https://central.sonatype.com")
//
//            credentials {
//                username = (project.findProperty("ossrhUsername") ?: "").toString()
//                password = (project.findProperty("ossrhPassword") ?: "").toString()
//            }
//        }
//    }
//}


