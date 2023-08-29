import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("native.cocoapods")
    id("io.realm.kotlin")
    kotlin("kapt")
    id("kotlinx-serialization")
}

val ktorVersion = extra["ktor.version"]
val koinVersion = extra["koin.version"]

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    ios()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "common"
            isStatic = true
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.materialIconsExtended)
                api(compose.material3)
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-cio:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.ktor:ktor-client-logging:$ktorVersion")
                api("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
                api("media.kamel:kamel-image:0.7.1")
                api("io.realm.kotlin:library-base:1.10.0")
                val datastore = "1.1.0-alpha04"
                api("androidx.datastore:datastore-core:$datastore")
                api("androidx.datastore:datastore-preferences-core:$datastore")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.github.reactivecircus.cache4k:cache4k:0.11.0")
                val precompose = "1.5.0-beta01"
                api("moe.tlaster:precompose:$precompose")
                api("moe.tlaster:precompose-viewmodel:$precompose")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
                api("io.ktor:ktor-client-okhttp:$ktorVersion")

                val navigation = "2.7.1"
                val lifecycle = "2.6.1"
                api("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
                api("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle")
                api("androidx.navigation:navigation-compose:$navigation")
                api("com.fragula2:fragula-compose:2.9")

                api("io.coil-kt:coil-compose:2.4.0")
                api("io.coil-kt:coil-gif:2.4.0")

                val markwonVersion = "4.6.2"
                api("io.noties.markwon:core:$markwonVersion")
                api("io.noties.markwon:ext-strikethrough:$markwonVersion")
                api("io.noties.markwon:ext-tables:$markwonVersion")
                api("io.noties.markwon:html:$markwonVersion")
                api("io.noties.markwon:linkify:$markwonVersion")
                api("io.noties.markwon:image-coil:$markwonVersion")
                api("io.noties.markwon:syntax-highlight:$markwonVersion") {
                    exclude("org.jetbrains", "annotations-java5")
                }
                configurations["kapt"].dependencies.add(
                    DefaultExternalModuleDependency(
                        "io.noties",
                        "prism4j-bundler",
                        "2.0.0"
                    )
                )

                api("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                api(compose.desktop.components.splitPane)
                api("io.ktor:ktor-client-okhttp:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
                api("me.friwi:jcefmaven:108.4.13")
                api("com.github.Dansoftowner:jSystemThemeDetector:3.6")
            }
        }

        val desktopTest by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependencies {
                api("io.ktor:ktor-client-darwin:$ktorVersion")
            }
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
