import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    exclude(group = "com.google.gson")
    exclude(group = "org.sl4j")
    exclude(group = "org.intellij")
    exclude(group = "org.jetbrains.annotations")
    exclude(group = "kotlinx")
    exclude(group = "kotlin")
    exclude(group = "javax")
}


base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}



repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
    maven {
        name = "Sleeping Town"
        url = uri("https://repo.sleeping.town")
    }
    maven {
        name = "Nucleoid"
        url = uri("https://maven.nucleoid.xyz/")
    }
    maven {
        name = "Geyser"
        url = uri("https://repo.opencollab.dev/main/")
    }
}

loom {
    accessWidenerPath = file("src/main/resources/bv-auth.classtweaker")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    implementation("folk.sisby:kaleido-config:${project.property("kaleido_version")}")
    include("folk.sisby:kaleido-config:${project.property("kaleido_version")}")

    compileOnly("eu.pb4:placeholder-api:${project.property("placeholderapi_version")}")
    compileOnly("org.geysermc.geyser:api:${project.property("geyserapi_version")}")

    /*
    * KTOR. I really wished kotlin had sections so I could tuck away this.
    */

    implementation("io.ktor:ktor-client-core:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-client-core:${project.property("ktor_version")}")

    implementation("io.ktor:ktor-client-cio:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-client-cio:${project.property("ktor_version")}")

    implementation("io.ktor:ktor-client-content-negotiation:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-client-content-negotiation:${project.property("ktor_version")}")

    implementation("io.ktor:ktor-client-auth:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-client-auth:${project.property("ktor_version")}")

    implementation("io.ktor:ktor-client-websockets:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-client-websockets:${project.property("ktor_version")}")

    implementation("io.ktor:ktor-serialization-kotlinx-json:${project.property("ktor_version")}")
    transitiveInclude("io.ktor:ktor-serialization-kotlinx-json:${project.property("ktor_version")}")

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "mc_semver" to project.property("mc_semver") as String,
            "loader_version" to project.property("loader_version")  as String,
            "kotlin_loader_version" to project.property("kotlin_loader_version")  as String
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
