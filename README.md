# Papyrus
Papyrus provides various named mappings to the Fabric loader MappingResolver. This allows for more developer friendly
access to mappings at runtime for example for using named classes in reflection instead of intermediary.
Papyrus downloads mappings on the first run on any given version and stores cached mappings in tiny v2 format at
${game_dir}/cache/papyrus/${mapping}_${version}.tiny

## Provided mappings

|    Mappings     | Namespace used in mapping resolver |
|:---------------:|:----------------------------------:|
| Mojang Mappings |               mojmap               |
|      Yarn       |                yarn                |

## Note on versioning
Since Papyrus is almost entirely independent of the version of the game itself. The mod version indicates the java
version said build was built against. For example Papyrus x.y.z+java.17 is built against java 17 and as such supports
any game version that's also built against java 17 (that being any version from 1.18 to 1.20.4 inclusively).
