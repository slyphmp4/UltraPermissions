[English](COMPATIBILITY.md) · [Українська](COMPATIBILITY.uk.md) · [Русский](COMPATIBILITY.ru.md)

# Minecraft compatibility through 26.2

The 5.6.10 stable release supports **Paper for Minecraft Java Edition 26.2 on Java 25** within the runtime checks below. Earlier Bukkit/Spigot/Paper versions remain compatibility targets; they have not all been tested. The `api-version: 1.13` descriptor specifies the API baseline, not proof that every intervening server works.

| Server | Java | Verification in this release |
| --- | --- | --- |
| Paper 26.2, build 135 | 25 | Isolated server startup and 12 runtime assertions passed. |
| Paper 26.1.x | 25 | Version-format support; no separate runtime test. |
| Bukkit/Spigot/Paper 1.13–1.21.11 | At least 17; also satisfy the server's Java requirements | Legacy compatibility target; compilation uses Spigot API 1.21.11. No exhaustive runtime matrix. |
| BungeeCord | At least 17 | Compiled against API 1.21-R0.4; no complete proxy/network integration test. |

Paper's Java requirements are documented [upstream](https://docs.papermc.io/paper/getting-started/#requirements). This matrix concerns Java Edition server plugins. Other server implementations need their own verification.

## What the runtime gate checks

GitHub Actions starts the exact release JAR on **Paper 26.2 build 135 / Java 25** in a disposable Docker container. It verifies plugin initialization, material/version detection, real Bukkit item metadata and GUI-item serialization, inventory creation, group membership and inherited permission storage, console command dispatch, granted/denied permission lookups with real storage, and the permission-field injection on the actual CraftPlayer class.

Permission lookups use a test Player proxy; field injection uses an uninitialized CraftPlayer fixture. These checks do not simulate a connected Minecraft client, every GUI interaction, external integrations, a real proxy network, or MySQL TLS negotiation. `RuntimeProbe.jar` is a test companion and is excluded from the installed plugin and release downloads.

The Paper JAR and Java container image are pinned by SHA-256. Only Paper's dependency-preparation stage accesses the network; the plugin runs with `--network none`, no published ports and a fresh data directory. The release waits for this gate and both OS builds to pass.

To reproduce after `gradlew build`, with Python 3 and Docker available:

```sh
python tools/verify_paper.py --plugin build/libs/UltraPermissions-5.6.10.jar --probe build/runtime-test/RuntimeProbe.jar --work-dir build/paper-runtime --report-dir build/reports/runtime --accept-eula
```

Use a new work directory each time. `--accept-eula` explicitly accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for the disposable test server. Reports contain the tested plugin hash and runtime versions. Back up existing server data before upgrading.
