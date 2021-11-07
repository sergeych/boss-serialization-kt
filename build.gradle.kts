import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val  kotlinVersion = "1.5.31"
    kotlin("jvm") version "$kotlinVersion"
    id("java-library")
    id("org.jetbrains.kotlin.plugin.serialization") version "${kotlinVersion}"
}

group = "net.sergeych"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven( "https://maven.universablockchain.com")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.0")
    implementation("com.icodici:universa_core:3.14.3+")

    testImplementation(kotlin("test"))
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
    from("$rootDir/build/libs/kotyara-$version.jar")
    into("$rootDir/../jarlib")
    rename { "kotyara.jar" }
}

