[English](COMPATIBILITY.md) · [Українська](COMPATIBILITY.uk.md) · [Русский](COMPATIBILITY.ru.md)

# Minecraft compatibility through 26.2

This release targets Paper 26.2 / Java 25. Its runtime gate performs two healthy boots plus a configured-storage failure boot, including head-profile and localized update-GUI metadata checks, actual storage persistence and the native update folder.

Earlier Bukkit/Spigot/Paper versions remain compatibility targets, not an exhaustively tested matrix. api-version: 1.13 is an API baseline, not proof of runtime support on every intermediate release.

| Server | Java | Verification |
|---|---|---|
| Paper 26.2 build 135 | 25 | Runtime gate: persistence, ranks, metadata, locales, native updates and MySQL failure stop. |
| Bukkit/Spigot/Paper 1.13–1.21.11 | At least 17 and the server requirement | Compiles against Spigot 1.21.11; no exhaustive runtime matrix. |
| BungeeCord | At least 17 | Compiles against 1.21-R0.4; no complete real-proxy test. |
| Velocity / Folia / Cardboard / Fabric | Platform-specific | Velocity proxy plugin is unsupported; other implementations require separate verification. |

The probe uses synthetic Player/CraftPlayer fixtures. It does not simulate a connected client, real MySQL TLS negotiation or every optional integration. Vault, PlaceholderAPI, MVdWPlaceholderAPI and NametagEdit remain conditional integrations.

Current source version: 5.6.18. Build: Gradle 8.14.6 / JDK 21; application bytecode: Java 17. The separate load gate uses 512 synthetic profiles and at least 400,000 permission checks; it is not a connected-player benchmark.

Configured MySQL failure stops the server/proxy instead of loading old local rights. The failure boot verifies that existing local permission files are unchanged.

```sh
python tools/verify_paper.py --plugin build/libs/UltraPermissions.jar --probe build/runtime-test/RuntimeProbe.jar --work-dir build/paper-runtime-5616 --report-dir build/reports/runtime --accept-eula
```

Use a fresh work directory for each run. The flag accepts the Minecraft EULA for the disposable server. The container has no published ports; plugins run with networking disabled. Only the pinned Paper bootstrap prepares dependencies with network access.

[Paper requirements](https://docs.papermc.io/paper/getting-started/#requirements) · [Minecraft EULA](https://aka.ms/MinecraftEULA)
