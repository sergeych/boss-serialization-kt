import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val  kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion
    id("java-library")
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version "1.4.30"
    `maven-publish`
}

group = "net.sergeych"
version = "1.0.8-SNAPSHOT"

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
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.1")
    implementation("com.icodici:common_tools:3.14.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
//    implementation(kotlin("stdlib-jdk8"))
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
    rename { "boss-serialization.jar" }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
//            groupId = "org.gradle.sample"
//            artifactId = "library"
//            version = "1.1"

            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.universablockchain.com/")
            credentials {
                username = System.getenv("maven_user")
                password = System.getenv("maven_password")
            }
        }
    }
}

