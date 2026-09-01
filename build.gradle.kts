import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "co.anomaly"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.0.0")
    implementation("com.squareup.okio:okio:3.9.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    intellijPlatform {
        pycharm("2026.2")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    instrumentCode = false
}

tasks {
    test {
        useJUnit()
    }

    patchPluginXml {
        sinceBuild.set("241.15989")
        untilBuild.set("")
    }

    buildPlugin {
        archiveBaseName.set("gitlab-milestones-and-issues")
    }
}
