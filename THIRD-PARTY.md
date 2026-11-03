[English](THIRD-PARTY.md) · [Українська](THIRD-PARTY.uk.md) · [Русский](THIRD-PARTY.ru.md)

# Third-party components

UltraPermissions: TechsCode. This security build is prepared by slyphmp4. The repository retains LICENSE; it does not replace the licenses of third-party libraries/APIs.

## Bundled runtime components

`runtime-libraries` is a local aggregate of the components below. `compile-api` supplies declarations for the Java compiler; the installable JAR contains the runtime variant. Application classes are compiled from src/main/java. Binary dependency hashes are pinned in config/dependencies.sha256.

| Group:artifact | Version | License metadata |
|---|---|---|
| com.google.code.gson:gson | 2.15.0 | Apache-2.0 |
| com.google.errorprone:error_prone_annotations | 2.52.0 | Apache 2.0 |
| com.zaxxer:HikariCP | 7.1.0 | The Apache Software License, Version 2.0 |
| commons-codec:commons-codec | 1.23.0 | See META-INF/component distribution |
| commons-io:commons-io | 2.22.0 | See META-INF/component distribution |
| commons-logging:commons-logging | 1.4.0 | See META-INF/component distribution |
| de.tr7zw:item-nbt-api | 2.16.1 | See META-INF/component distribution |
| org.apache.commons:commons-lang3 | 3.21.0 | See META-INF/component distribution |
| org.apache.httpcomponents:httpclient | 4.5.14 | See META-INF/component distribution |
| org.apache.httpcomponents:httpcore | 4.4.16 | See META-INF/component distribution |
| org.slf4j:slf4j-api | 2.0.20 | See META-INF/component distribution |
| org.slf4j:slf4j-nop | 2.0.20 | See META-INF/component distribution |
| org.bstats:bstats-base | 3.2.1 | See META-INF/component distribution |
| org.bstats:bstats-bukkit | 3.2.1 | See META-INF/component distribution |
| org.bstats:bstats-bungeecord | 3.2.1 | See META-INF/component distribution |
| org.jetbrains:annotations | UNKNOWN | See META-INF/component distribution |
| net.kyori:adventure-internal-subset | UNKNOWN | See META-INF/component distribution |

The available metadata does not establish the exact versions of the embedded JetBrains annotations and Kyori internal subset. They are marked UNKNOWN; do not assign arbitrary versions to them. bStats 3.2.1 is also present as a relocated copy within NBT API.

## APIs provided by the server/integrations

The files in libs/compile-only are used only for compilation and tests and are not included in the plugin JAR. They include Spigot/BungeeCord, Vault, PlaceholderAPI, MVdWPlaceholderAPI, LuckPerms, NametagEdit, PermissionsEx, Guava, SnakeYAML, Authlib and annotations. Each file's URL and SHA are recorded in libs/compile-only/provenance.json. A version in the compile classpath does not establish the version running on a live server.

Licenses/NOTICE files and Maven metadata included in the META-INF resources are retained during building. Check each component's terms before further distribution. CI hash checks establish integrity, not the absence of CVEs; inspect the actual server/proxy/JDBC dependency inventory separately.

The refreshed runtime and its compiler declarations are identical and pinned in config/runtime-components.json. tools/refresh_runtime_bundle.py is an explicit maintenance tool; normal builds stay offline. Apache HTTP 4.x and unknown Kyori/JetBrains subsets remain byte-identical to the pinned historical baseline.
