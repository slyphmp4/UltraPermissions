[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md)

<p align="center"><img src="assets/prv.png" alt="UltraPermissions" width="920"></p>

<p align="center">
<a href="https://github.com/slyphmp4/UltraPermissions/actions/workflows/ci.yml"><img src="https://github.com/slyphmp4/UltraPermissions/actions/workflows/ci.yml/badge.svg" alt="Build and verify"></a>
<a href="https://github.com/slyphmp4/UltraPermissions/releases"><img src="https://img.shields.io/github/v/release/slyphmp4/UltraPermissions?include_prereleases" alt="Release"></a>
<img src="https://img.shields.io/badge/build-JDK%2021-268c84" alt="JDK 21">
</p>

# UltraPermissions

Manage Minecraft permissions, groups and ranks through commands and a graphical editor. **5.6.8-security.1** is a community security build with stronger authorization, protected synchronization and GitHub Actions checks.

We prepared a standalone Gradle project, retained the core permission-management features, reworked unsafe network and SQL paths, and added negative security tests. This version addresses eight previously identified HIGH/MEDIUM findings and further strengthens authorization for administrative commands.

These are application-security fixes. The review did not establish the presence of viruses or an intentional malicious implant, so we make no claim of “virus removal” or absolute security.

## Features

- Per-user permissions and groups: grants, denials using `-permission.node`, wildcard nodes and inheritance.
- Multiple groups per player, priorities, temporary group and permission assignments, and automatic expiration.
- World and server scopes, including separate permissions for BungeeCord.
- GUIs for users, groups, permissions and rank paths, with icons and display settings.
- Prefixes, suffixes, colors, primary/secondary ranks and rank timers.
- Promotion and demotion along configurable Group Paths.
- Delegated assignment of approved groups with assignment-duration limits.
- Local JSON storage or MySQL, with signed synchronization between trusted nodes.
- Vault, PlaceholderAPI, MVdWPlaceholderAPI and NametagEdit integrations.
- Migration from LuckPerms/PermissionsEx, group copying, account transfers and File/MySQL storage transfers.
- Translations, a permission-node reference database and a permission-check logger.

## Download and install

1. Download **UltraPermissions-5.6.8-security.1.jar** from [Releases](https://github.com/slyphmp4/UltraPermissions/releases). Do not install `-sources.jar` on the server.
2. Place the JAR in `plugins`, remove any other copy of UltraPermissions from that folder, then restart the server.
3. To start administration, run **from the console** `upc AddSuperAdmin YourName` after that player has joined and has a registered user record, then open `/uperms`.
4. For a network or MySQL, follow the [configuration guide](docs/CONFIGURATION.md). Every node must use this security version.

The project targets Bukkit/Spigot/Paper and BungeeCord. Application classes target Java 17; a particular Minecraft server may require a newer Java version. Builds and CI use JDK 21. Declaring api-version 1.13 does not promise compatibility with every server version. Full in-game startup and a real TLS session are outside offline CI; the first release is marked as a pre-release.

## Commands and permissions

Main commands: `/uperms`, `/upc`, `/promote`, `/demote`.

- [Complete command list and examples](docs/COMMANDS.md)
- [Access model and permission nodes](docs/PERMISSIONS.md)
- [PlaceholderAPI and integrations](docs/PLACEHOLDERS.md)
- [Security changes and limitations](docs/SECURITY-CHANGES.md)

## Build in IntelliJ IDEA

Open the folder as a Gradle project and select **Gradle Wrapper** and **JDK 21** for the Gradle JVM.

```powershell
.\gradlew.bat clean build --offline
```

Linux/macOS:

```sh
bash ./gradlew clean build --offline
```

On its first run, the Wrapper downloads Gradle 8.14.5 and verifies its SHA-256. Plugin dependencies are pinned in `libs`; after the Wrapper distribution has been downloaded, building requires no external Maven repository. The resulting JARs are in `build/libs`.

## CI and component verification

GitHub Actions performs clean builds on **Ubuntu and Windows / JDK 21**. `verifyComponents` checks dependency hashes, JAR contents, exclusion of server APIs and test classes from the plugin, resources, version metadata, the Java target and **7085 API contracts**. `verifySecurityFixes` checks forged/replayed messages, SQL parameter binding, pool failure, GUI sessions, item codecs, TLS policy and administrative command branches. Network connections and OS process execution are blocked in the test JVM.

Pushing a `v<version>` tag triggers both builds; a release is created only after both succeed. Assets include the plugin JAR, sources JAR, complete Gradle project and SHA-256 checksums. Component checks do not replace a test installation on the exact versions of your gaming stack.

## Credits and licenses

UltraPermissions is by TechsCode; the original author attribution is retained. This community security build is prepared and maintained by **slyphmp4**. Repository license: [GPL-3.0](LICENSE). Third-party components retain their own licenses; see [THIRD-PARTY.md](THIRD-PARTY.md) and the distribution's `META-INF`.
