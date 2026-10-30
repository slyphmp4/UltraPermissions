[English](CONFIGURATION.md) · [Українська](CONFIGURATION.uk.md) · [Русский](CONFIGURATION.ru.md)

# Configure UltraPermissions 5.6.8-security.1

## Single server

For a standalone server using local JSON storage, no additional setup is required: the inter-server channel is disabled, and regular commands and the GUI use the existing superadmin model.

## Proxy and backends

Update the proxy and every backend to the same security version. The old BungeeCord/UltraPermissions// format is rejected; a mixed network with old JARs will not synchronize.

The first startup creates `plugins/UltraPermissions/security.properties` with a new random 32-byte key and `messaging-enabled=false`. For a cluster, copy **only the same network-key and realm** from one trusted node to the others. Assign distinct node-id values, and enable messaging-enabled after configuring all nodes. Never send the key through game chat or plugin messaging. Restrict file access to the server owner; configure the appropriate ACL on Windows.

Proxy:

```properties
messaging-enabled=true
realm=ultrapermissions
node-id=proxy
proxy-id=proxy
network-key=<BASE64_KEY_32_BYTES>
```

Backend named survival in the BungeeCord configuration:

```properties
messaging-enabled=true
realm=ultrapermissions
node-id=survival
proxy-id=proxy
network-key=<SAME_KEY>
```

Replace `<BASE64_KEY_32_BYTES>` and `<SAME_KEY>` with the same private key, encoded as Base64.

On the proxy, node-id must equal proxy-id. On a backend, node-id must exactly match its internal ServerInfo name in BungeeCord. Identifiers/realm must contain 1–64 characters from A–Z, a–z, 0–9, `_`, `.`, `-`. A shared key establishes a shared trust boundary for the whole cluster. Configuration changes take effect after a restart.

The new `ultrapermissions:secure` channel uses HMAC-SHA256, direction/sender/recipient/realm, a 30-second time window and a nonce. Node clocks must be synchronized. The proxy consumes this channel, including client-originated and invalid packets. A backend does not trust a message merely because a Player is present: signature verification must succeed. Packet size and rate are bounded: 100 verification attempts per second globally and 40 accepted messages per second per sender; the backend queue is limited to 256. Messages may be dropped under overload. Synchronization is limited to 64 concurrent jobs and coalesces repeated notifications.

Do not include the key in a distributed project or JAR. If it is compromised, replace it on every node. A firewall, a backend closed to direct access and protected forwarding remain part of a correct network setup.

## Local MySQL credentials

The password is no longer sent from a backend to the proxy. Configure every node independently: the existing local Registry remains supported, or create `plugins/UltraPermissions/mysql.properties` on the relevant node:

```properties
hostname=db.example.net
port=3306
database=permissions
username=ultrapermissions_node
password=<LOCAL_PASSWORD>
ssl=true
```

Replace `<LOCAL_PASSWORD>` with that node's local database password. The hostname must match the name/IP in the certificate. Use a DNS name for IPv6. The UTF-8 file is read by the standard Java Properties parser; escape backslashes in values. This is a local secret: do not copy the file into a public project or grant access to players or unrelated backup readers.

When mysql.properties exists, it overrides the Registry's startup credentials/TLS settings. GUI changes do not update this file; reconcile it manually before the next startup. An incomplete/invalid file stops MySQL startup instead of enabling an insecure fallback. To return to the local Registry, remove mysql.properties from the configuration directory yourself.

For a remote host, `ssl=false` is rejected before connecting. VERIFY_IDENTITY checks the CA and server name; downgrade, unverified RSA-key retrieval, local infile and multi-queries are disabled. A driver that does not advertise VERIFY_IDENTITY support is rejected before credentials are sent. The server supplies the actual JDBC driver; it is not included in this JAR. For a private CA, configure the JVM trust store using the actual Connector/J settings, then verify that an invalid certificate/name is rejected.

Explicit ssl=false is allowed only for the exact hosts `localhost` and `127.0.0.1`. TLS is enabled by default. Restrict database access and use separate least-privilege node accounts. The code needs table CREATE and CRUD within its own database, but no global FILE/GRANT/administrative privileges. Existing tables and data are not deleted.

[Official TLS settings](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html).

## Updates, GUI and legacy icons

Install the editor/updates manually using a verified JAR. There is no automatic loadPlugin from a network response or restart/update through proxy messages, even with a valid signature. Local administrative GUI actions require a current superadmin role in every handler.

Back up the plugin's runtime data/database before deployment. New icons use the UPI1 prefix and JSON format. Ordinary legacy Bukkit/Guava streams are read with limits on classes, aliases, depth, object references and arrays. An unknown format/component produces an explicit error rather than arbitrary class execution. Data is neither converted by an external tool nor erased in bulk. Test nonstandard legacy components on a data copy on a test server.

CI does not start Minecraft/a proxy or verify a real TLS session. See [security changes](SECURITY-CHANGES.md) for the verification boundaries. Configure the bundled bStats telemetry in `plugins/bStats/config.yml`; local configuration files/keys must not be published to Git.
