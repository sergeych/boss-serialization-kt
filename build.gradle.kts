import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val  kotlinVersion = "1.5.31"
    kotlin("jvm") version kotlinVersion
    id("java-library")
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version "1.4.30"
}

group = "net.sergeych"
version = "1.0-beta1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven( "https://maven.universablockchain.com")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.0")
    implementation("com.icodici:common_tools:3.14.3+")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}

java {
    withSourcesJar()
}

tasks.register<Copy>("localRelease") {
    dependsOn("jar")
    from("$rootDir/build/libs/boss-serialization-$version.jar")
    into("$rootDir/../jarlib")
    rename { "boss-serialization1.jar" }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

