Papyrus
=======

Papyrus provides various named mappings to the Fabric loader MappingResolver. This allows for more developer friendly
access to mappings at runtime for example for using named classes in reflection instead of intermediary.
Papyrus downloads mappings on the first run on any given version and stores cached mappings in tiny v2 format at
${game_dir}/cache/papyrus/${mapping}_${version}.tiny

# Using Papyrus
To use Papyrus just add the maven repository which hosts Papyrus to your gradle build script:
``` kotlin
# Kotlin DSL
repositories {
    maven("https://maven.nelind.dk/releases") {
        name = "Nelind Maven"
    }
}

# Groovy DSL
repositories {
    maven {
        name 'Nelind Maven'
        url 'https://maven.nelind.dk/releases'
    }
}
```
and then include Papyrus JIJ with the standard loom "include" gradle configuration:
``` kotlin
# Kotlin DSL
include("dk.nelind:papyrus:(version)")

# Groovy DSL
include "dk.nelind:papyrus:(version)"
```

Then get and use the `MappingResolver` with any of the added mapping namespaces:
```java
MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
String runtimeClassNameFromYarnName = mappingResolver.mapClassName("yarn", yarnClassName);
String runtimeClassNameFromMojmapName = mappingResolver.mapClassname("mojmap", mojmapClassName);
```
Papyrus injects the added mappings at `preLaunch` so you should only use Papyrus at some point after that.
If the here and there talks about possible future fabric loader plugins go somewhere where it makes sense for
Papyrus to become a loader plugin it will. Hopefully making extra mappings available at `preLaunch`

You can also add Papyrus with the `modApi` configuration and then add Papyrus as a dependency in your `fabric.mod.json`.
However, this is highly discouraged as it pollutes your classpath with Papyrus classes and makes what should be
considered a technical detail more cumbersome for the end user than it needs to be.

## Provided mappings

|    Mappings     | Namespace used in mapping resolver |
|:---------------:|:----------------------------------:|
| Mojang Mappings |               mojmap               |
|      Yarn       |                yarn                |

## Note on versioning
Since Papyrus is almost entirely independent of the version of the game itself. The mod version indicates the java
version said build was built against. For example Papyrus x.y.z+java.17 is built against java 17 and as such supports
any game version that's also built against java 17 (that being any version from 1.18 to 1.20.4 inclusively).
