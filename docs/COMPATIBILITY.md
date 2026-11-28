[English](COMPATIBILITY.md) · [Українська](COMPATIBILITY.uk.md) · [Русский](COMPATIBILITY.ru.md)

# Minecraft compatibility through 26.3

One **UltraPermissions.jar** is checked on each pinned server below. Every target runs two healthy boots plus a configured-storage failure boot: permission grants and denials, inheritance, rank paths, head profiles, localized update-GUI metadata, persistence and the native update folder.

| Server | Build | Java |
|---|---|---|
| Paper 1.20.1 | 196 | 17 |
| Paper 1.20.6 | 151 | 21 |
| Paper 1.21.11 | 132 | 21 |
| Paper 26.1.2 | 74 | 25 |
| Paper 26.2 | 135 | 25 |
| Paper 26.3 | 253 | 25 |
| Folia 26.3 | 28 | 25 |

The server downloads, SHA-256 checksums and Java container digests are pinned in [paper-targets.json](../config/paper-targets.json) and [folia-targets.json](../config/folia-targets.json). CI verifies that all seven reports describe the exact same plugin JAR; a missing or failed target blocks release. Paper 26.2 and 26.3 also run the synthetic load gate with 512 profiles and at least 400,000 permission checks.

| Other platform | Compatibility scope |
|---|---|
| Earlier Bukkit/Spigot/Paper 1.13–1.21.11 | Existing compatibility targets retained; not every intermediate release is runtime tested. Java 17 or the server's higher requirement applies. |
| BungeeCord | Compiles against 1.21-R0.4; no complete real-proxy test. Java 17 minimum. |
| Velocity / Cardboard / Fabric | Velocity proxy plugin is unsupported; other implementations require separate verification. Fabric requires a Bukkit compatibility layer such as Cardboard. |

The probe uses synthetic Player/CraftPlayer fixtures. It does not simulate a connected client, every menu interaction, real MySQL TLS negotiation or every optional integration. Vault, PlaceholderAPI, MVdWPlaceholderAPI and NametagEdit remain conditional integrations. Client rendering and the actual Cardboard server require separate manual checks.

Current source version: 5.6.20. Build: Gradle 8.14.6 / JDK 21; application bytecode: Java 17. The compile API remains Spigot 1.21.11, and `api-version: 1.13` remains the API baseline; it is not proof of runtime support on every release.

Configured MySQL failure stops the server/proxy instead of loading old local rights. Each failure boot verifies that existing local permission files are unchanged.

```sh
python tools/verify_paper.py --paper-version 26.3 --plugin build/libs/UltraPermissions.jar --probe build/runtime-test/RuntimeProbe.jar --work-dir build/paper-runtime-26.3 --report-dir build/reports/runtime --accept-eula
```

Use `--platform Folia --server-version 26.3` to select Folia and use a fresh working directory. The EULA flag applies to the disposable server. The runtime container has no external network or published ports. Only the verified bootstrap can use the network to prepare dependencies.

The Folia test uses two loopback players in different regions. It verifies that both can open and navigate the original GUI, that chat input returns to the player's region, and that concurrent grants have distinct IDs and survive a restart.

Releases include `runtime-verification.json` for 26.3 and `compatibility-verification.json` for the complete matrix.

[Paper requirements](https://docs.papermc.io/paper/getting-started/#requirements) · [Minecraft EULA](https://aka.ms/MinecraftEULA)
