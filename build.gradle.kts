buildscript {
    val kotlin_version = "1.3.60"
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
    }
}


plugins {
    `maven-publish`
    id("org.jetbrains.dokka") version "0.10.0"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("org.jetbrains.kotlin.plugin.serialization") version ("1.3.60")
    kotlin("jvm") version "1.3.60"
}


group = "com.londogard"
version = "1.0-beta"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.londogard:smile-nlp-kt:1.0.1-beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/londogard/text-gen-kt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr"){
            from(components["java"])
        }
    }
}