plugins {
	id("fabric-loom") version "1.6-SNAPSHOT"
	id("maven-publish")
}

val modVersion: String by project
val modId: String by project
val modName: String by project

val javaVersion: String by project
val minecraftVersion: String by project
val yarnVersion: String by project
val fabricLoaderVersion: String by project
val mappingIoVersion: String by project

group = "dk.nelind"
version = "$modVersion+java.$javaVersion"

base {
	archivesName.set(modId)
}

loom {
	runConfigs.configureEach {
		ideConfigGenerated(true)
	}
}

repositories {
	mavenCentral()
	maven("https://maven.fabricmc.net/") {
		name = "Fabric"
	}
}

dependencies {
	// Papyrus doesn't directly depend on any specific minecraft version or any minecraft code at all.
	// But loom needs minecraft and mappings dependencies set to work. Papyrus simply requires that the loader has access to minecraft at runtime.
	// We just use the earliest game version that is built against the same java version that any given Papyrus build is.
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("net.fabricmc:yarn:$yarnVersion:v2")

	modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
	include(api("net.fabricmc:mapping-io:$mappingIoVersion")!!)
}

tasks {
	processResources {
		val props = mapOf(
			"id" to modId,
			"name" to modName,
			"version" to project.version,
			"fabric_loader_version" to fabricLoaderVersion,
			"java_version" to javaVersion
		)
		props.forEach(inputs::property)

		filesMatching("fabric.mod.json") {
			expand(props)
		}
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${project.base.archivesName.get()}"}
		}
	}
}

java {
	withSourcesJar()

	toolchain {
		languageVersion = JavaLanguageVersion.of(javaVersion)
	}
}


publishing {
	publications {
		create<MavenPublication>("mod") {
			groupId = "dk.nelind"
			artifactId = modId

			from(components["java"])
		}
	}

	repositories {
		val username = "NELIND_MAVEN_USERNAME".let { System.getenv(it) ?: findProperty(it) }?.toString()
		val password = "NELIND_MAVEN_PASSWORD".let { System.getenv(it) ?: findProperty(it) }?.toString()
		if (username != null && password != null) {
			maven("https://maven.nelind.dk/releases") {
				name = "NelindReleases"
				credentials {
					this.username = username
					this.password = password
				}
			}
		} else {
			println("Nelind Maven credentials not present.")
		}
	}
}
