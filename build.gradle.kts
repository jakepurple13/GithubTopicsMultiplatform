import org.jetbrains.kotlin.gradle.plugin.extraProperties

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven(url = "https://jitpack.io")
    }
}

buildscript {
    dependencies {
        classpath("io.realm.kotlin:gradle-plugin:1.10.0")
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
}