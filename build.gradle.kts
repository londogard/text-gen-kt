import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlin_version = "1.3.72"
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
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jetbrains.kotlin.plugin.serialization") version ("1.3.72")
    kotlin("jvm") version "1.3.72"
}


group = "com.londogard"
version = "1.0.2-beta"
val smileVersion = "2.5.0"
val ktestVersion = "1.3.0"
val serializationVersion = "0.20.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.haifengl:smile-nlp:$smileVersion")
    implementation("com.github.haifengl:smile-kotlin:$smileVersion")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:$serializationVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$ktestVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$ktestVersion")
    testImplementation("org.amshove.kluent:kluent:1.61")
}

tasks.withType<KotlinCompile> {
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
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}