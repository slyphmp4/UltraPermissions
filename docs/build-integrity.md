# Build integrity and dependency policy (UltraPermissions)

This project retains the original TechsCode behavior and GUI. Build hardening must not silently replace the public Bukkit/Bungee API, the plugin's bundled dependency classes, or its supported Java 17 bytecode format.

## Toolchain

- Wrapper: **Gradle 8.14.5**, a stable maintenance release; its wrapper JAR SHA-256 is `7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172`.
- Distribution: `gradle-8.14.5-bin.zip`, SHA-256 `6f74b601422d6d6fc4e1f9a1ab6522f642c2fdcbc15ae33ebd30ba3d7198e854`.
- Both values are listed by [Gradle's official release checksums](https://gradle.org/release-checksums/), recorded in `gradle/wrapper/provenance.json`, and checked in CI before executing the wrapper.
- CI pins **Eclipse Temurin 21.0.13+9** on both Linux and Windows to avoid compiler patch drift. Gradle uses Java 21 toolchains; compilation targets **Java 17** (`--release 17`). The Paper 26.2 compatibility probe runs Java 25 inside a pinned container.
- Keeping the verified Gradle 8.14.5 wrapper avoids an untested major-version migration. Revisit Gradle 9 only with its upgrade guide, a regenerated/authenticated wrapper JAR, and the full CI matrix.

## Offline dependency model

No dynamic/SNAPSHOT version resolution or public Maven repositories are used by the build:

- `compileOnly`: local Spigot/Bungee, Vault, PlaceholderAPI and other integration API JARs under `libs/compile-only/`. They must **not** be packaged into the plugin.
- `runtimeOnly`: `me.techscode.ultrapermissions:runtime-libraries:5.6.8` from the checked-in local Maven repository. Its contents are included in the production JAR, mostly relocated under `me/TechsCode/UltraPermissions/dependencies/`.
- `config/dependencies.sha256` pins **all 20 checked-in JARs** (18 API and 2 runtime/compile API artifacts). `verifyBuildInputs` verifies every SHA-256 and rejects missing, extra or duplicate JARs **before `compileJava`**. A negative GitHub Actions test changes one checksum and requires the precompile guard to reject it for exactly the expected reason.
- `ComponentChecks` independently validates the finished JAR's embedded library bytecode, provided API exclusions, Java target, plugin metadata, API contracts and resources.

An unused, outdated `snakeyaml-1.18.jar` was removed from the compile-only inventory after confirming that both Linux and Windows builds succeed without it. It was never a runtime library. The server's own YAML implementation and the remaining compatibility-only APIs still need their own version-specific integration/security checks.

### Bundled HTTP client migration debt

The runtime bundle also contains unrelocated `org/apache/http/**` classes (HttpClient **4.5.14**, HttpCore **4.4.16**) used by the legacy update client. These are old APIs, not a long-term preferred dependency. Replacing them with the Java 17+ HTTP client is part of the stage-9 updater/network review; it must be accompanied by updater network-failure and runtime compatibility tests. Do not simply delete or repack those classes: that would break current updater class references. Other bundled dependency versions are listed in `src/main/resources/META-INF/maven/**/pom.properties`. License/NOTICE and Maven metadata resources are preserved in the package.

## Reproducibility and provenance

- JAR and sources JAR have deterministic entry ordering and no preserved source timestamps. Main Java sources are supplied to `javac` in sorted order: otherwise a generic bridge method in `GroupDeleteView$1.class` was emitted on Windows but not Linux.
- The Linux and Windows jobs independently run `clean build --offline --no-build-cache`. A separate job compares the two production JARs **and** the sources JARs byte-for-byte; an unexplained mismatch fails the gate.
- The Paper smoke test pins both the official Paper download SHA-256 and the Java 25 container digest. It tests the built Linux artifact in a disposable, network-disabled plugin runtime after the Paper bootstrap.
- Releases require both the runtime and reproducibility jobs, publish `SHA256SUMS.txt`, the source archive and the runtime verification report. These supply checksums and commit/tag traceability, **not** a cryptographically signed SLSA provenance attestation.
- `libs/compile-only/provenance.json` describes known origins and hashes without disclosing developers' local filesystem paths.
- `.github/dependabot.yml` requests weekly, separately reviewed updates of the pinned GitHub Actions versions. It does not automatically upgrade the vendored JARs or merge anything.

## Local verification

With Java 21 installed and the checked-in binaries available:

```bash
bash ./gradlew --no-daemon clean build --offline --no-build-cache --console=plain
python3 tools/check_localizations.py --jar build/libs/UltraPermissions-5.6.11.jar
```

Expected: `verifyBuildInputs`, component checks, security tests, resources and locale checks pass. For cross-platform byte identity and Paper Java 25 smoke, use the GitHub Actions workflow: local builds alone are not substitutes.

**Not implied by this document:** production-server load tests, older Spigot/Bungee combinations, real Vault integrations, a vulnerability-free third-party dependency graph, or automatic release authorization. Merges/releases remain manual decisions.
