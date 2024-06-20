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
modRuntimeOnly(include("dk.nelind:papyrus:(version)")!!)

# Groovy DSL
modRuntimeOnly include("dk.nelind:papyrus:(version)")
```
You can also choose to not include Papyrus JIJ in that case make sure to manually add Papyrus as a dependency in your
`fabric.mod.json`. If you need to access Papyrus internals you can also add Papyrus with the `modApi` configuration instead of the
`modRuntimeOnly` configuration.

Then get and use the `MappingResolver` with any of the added mapping namespaces:
```java
MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
String runtimeClassNameFromYarnName = mappingResolver.mapClassName("yarn", yarnClassName);
String runtimeClassNameFromMojmapName = mappingResolver.mapClassname("mojmap", mojmapClassName);
```
### Usage notes:
- Papyrus injects the added mappings at `preLaunch` so you should only use Papyrus at some point after that.
  If the here and there talks about possible future fabric loader plugins go somewhere where it makes sense for
  Papyrus to become a loader plugin it will. Hopefully making extra mappings available at `preLaunch`

- Mojmap handles inherited class members differently from Intermediary and Yarn!
Intermediary and Yarn only give mappings for members defined directly in a class in said classes mapping
definition whereas Mojmap gives names for all members a class has, inherited or not. In practice this means that if you
ask the mapping resolver for a Mojmap method name in a class where the class has inherited the method while the runtime
mappings are either Intermediary or Yarn you'll get `null` because the resolver *can* find that name (in Mojmap) but
there is no defined name for that method in the runtime mapping. ***What does this mean for you?*** If you're using
Mojmap names as the input to the mapping resolver *you should use the class name of the class a method or field is
declared on not the class you want to modify*. Alternatively you can have a null check and programmatically find the
class's super class and ask the mapping resolver for the member name with that class as the owning class.

## Provided mappings

|    Mappings     | Namespace used in mapping resolver |
|:---------------:|:----------------------------------:|
| Mojang Mappings |               mojmap               |
|      Yarn       |                yarn                |

## Note on versioning
Since Papyrus is almost entirely independent of the version of the game itself. The mod version indicates the java
version said build was built against. For example Papyrus x.y.z+java.17 is built against java 17 and as such supports
any game version that's also built against java 17 (that being any version from 1.18 to 1.20.4 inclusively).
