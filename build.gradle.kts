// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.android.library") version "8.0.0" apply false
    kotlin("android.extensions") version "1.8.0" apply false
}


buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
    }
}