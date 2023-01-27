(gradle as ExtensionAware).extra["kotlinx-coroutines-core"] = "1.6.4"
(gradle as ExtensionAware).extra["kotlinx-serialization-json"] = "1.4.1"
(gradle as ExtensionAware).extra["kotlinx-datetime"] = "0.4.0"
(gradle as ExtensionAware).extra["exposedVersion"] = "0.41.1"

val core: String = (gradle as ExtensionAware).extra["kotlinx-coroutines-core"] as String
val json: String = (gradle as ExtensionAware).extra["kotlinx-serialization-json"] as String
val datetime: String = (gradle as ExtensionAware).extra["kotlinx-datetime"] as String
val exposedVersion: String = (gradle as ExtensionAware).extra["exposedVersion"] as String

plugins {
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    application
}

group = "me.simone"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$json")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("me.simone.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}