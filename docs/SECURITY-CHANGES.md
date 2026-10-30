[English](SECURITY-CHANGES.md) · [Українська](SECURITY-CHANGES.uk.md) · [Русский](SECURITY-CHANGES.ru.md)

# Security changes

## Network boundaries

1. **H-01 — unsigned control messages.** The old format is rejected. A dedicated channel uses HMAC-SHA256, realm, sender/recipient, direction, timestamp and a nonce. Replayed/expired packets are rejected, with size and rate limits. `restart`/`update` are forbidden even in a signed message.
2. **H-02 — credentials in player transport.** MySQL passwords are not sent through plugin messaging. Each node is configured locally. An incoming packet cannot replace MySQL state.
3. **H-03 — synchronization SQL injection.** SELECT/INSERT/UPDATE/DELETE values are bound as parameters; models/keys are validated and table identifiers are restricted. The legacy helper cannot accept executable SQL text.

## Reliability and privileges

4. **M-01 — unverified executable downloads.** Network-based editor installation and update JAR downloads are disabled. Install a verified JAR manually; active installer/update paths do not perform a network-driven loadPlugin.
5. **M-02 — pool/thread exhaustion.** Busy-wait loops and per-iteration thread creation were removed. Pool/SQL operations have timeouts; synchronization is limited to 64 jobs and repeated notifications are coalesced.
6. **M-03 — stale GUI authorization.** The current role is checked before an action, and the inventory is bound to the session. Revoking superadmin stops actions. Chat input is moved to the server scheduler and checked again.
7. **M-04 — Java item deserialization.** New data uses bounded JSON with the UPI1 prefix. The legacy format permits only approved containers/Bukkit aliases, with limits on size, depth, references and classes. Arbitrary classes and unapproved registered aliases are rejected.
8. **M-05 — MySQL transport.** TLS is enabled by default and remote plaintext connections are forbidden. VERIFY_IDENTITY is required; a driver without support is rejected before credentials are sent. Explicit ssl=false is permitted only for the exact hosts localhost/127.0.0.1.

## Additional pre-release changes

Administrative `/upc` branches now share a superadmin requirement; exceptions are limited AddGroup access and permission-list viewing. Suffix-editing branches lacking that requirement were closed, and RegisterUser was restricted. Unknown users are denied access without an Optional exception. String argument labels, true/false parsing and console replies through CommandSender instead of a nullable Player were corrected. The distribution contains no personal purchaser ID or shared embedded update API token. The license check is retained.

## What CI verifies and what remains outside it

CI runs negative checks against Bukkit/JDBC test doubles and structurally verifies the final JAR. Specific servers, database certificates/drivers, firewall/forwarding, all CraftMeta variants and the in-game runtime require a separate test installation. A standard legacy icon passes the migration check; unsupported legacy components fail explicitly instead of executing.

The LOW finding about synchronous external-IP lookup over HTTP and informational environment findings were not part of these eight fixes. Bundled libraries are listed separately; the absence of an advisory for a version does not prove that it has no unknown flaws. This is security maintenance, without a promise that “all exploits are removed” or a claim that viruses were found/removed.
