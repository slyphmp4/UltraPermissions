package me.TechsCode.UltraPermissions.base.security;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.function.LongSupplier;
import java.util.function.Consumer;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Versioned HMAC envelope. No legacy fallback. Keys only come from a local file. */
public final class SecureMessaging {
    public static final String CHANNEL = "ultrapermissions:secure";
    public static final int MAX_PAYLOAD = 24000;
    private static final int MAX_WIRE = 30000;
    private static final long WINDOW = 30000;
    private final byte[] key;
    private final String realm;
    private final LongSupplier clock;
    private final Map<String, Long> seen = new LinkedHashMap<>();
    private long rateWindow;
    private int attempts;
    private final Map<String, long[]> senderRates = new HashMap<>();
    public final String nodeId;
    public final String proxyId;

    public SecureMessaging(byte[] key, String realm, String nodeId, String proxyId, LongSupplier clock) {
        if (key.length != 32 || !identifier(realm) || !identifier(nodeId) || !identifier(proxyId)) {
            throw new IllegalArgumentException("Invalid secure messaging configuration");
        }
        this.key = key.clone(); this.realm = realm; this.nodeId = nodeId; this.proxyId = proxyId; this.clock = clock;
    }

    public static SecureMessaging load(File folder, boolean proxy, Consumer<String> log) {
        Path path = folder.toPath().resolve("security.properties");
        try {
            Files.createDirectories(folder.toPath());
            if (!Files.exists(path)) {
                byte[] random = new byte[32]; new SecureRandom().nextBytes(random);
                String template = "# Copy network-key/realm to trusted nodes. Keep this file private.\n"
                    + "messaging-enabled=false\nrealm=ultrapermissions\nnode-id=" + (proxy ? "proxy" : "backend")
                    + "\nproxy-id=proxy\nnetwork-key=" + Base64.getEncoder().encodeToString(random) + "\n";
                Files.writeString(path, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                try { Files.setPosixFilePermissions(path, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")); }
                catch (UnsupportedOperationException ignored) { /* Windows ACLs are managed by the server owner. */ }
            }
            if (Files.size(path) > 16384) throw new IOException("Configuration too large");
            Properties p = new Properties();
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { p.load(reader); }
            if (!"true".equals(p.getProperty("messaging-enabled"))) {
                log.accept("Secure proxy messaging is disabled. Configure security.properties on every trusted node to enable it.");
                return null;
            }
            SecureMessaging configured = new SecureMessaging(Base64.getDecoder().decode(p.getProperty("network-key", "")),
                p.getProperty("realm", ""), p.getProperty("node-id", ""), p.getProperty("proxy-id", ""), System::currentTimeMillis);
            if (proxy && !configured.nodeId.equals(configured.proxyId)) throw new IllegalArgumentException("Proxy identity mismatch");
            return configured;
        } catch (IOException | RuntimeException invalid) {
            log.accept("Secure proxy messaging disabled: invalid/unreadable security.properties.");
            return null;
        }
    }

    private static boolean identifier(String value) {
        return value != null && value.matches("[A-Za-z0-9_.-]{1,64}");
    }

    public byte[] encode(String direction, String sender, String recipient, String payload) throws IOException {
        if (!direction(direction) || !identifier(sender) || !identifier(recipient)
                || payload == null || payload.getBytes(StandardCharsets.UTF_8).length > MAX_PAYLOAD) {
            throw new IOException("Invalid message envelope");
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(0x55505331); out.writeUTF(realm); out.writeUTF(direction);
            out.writeUTF(sender); out.writeUTF(recipient); out.writeLong(clock.getAsLong());
            out.writeLong(new SecureRandom().nextLong()); out.writeLong(new SecureRandom().nextLong());
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            out.writeInt(body.length); out.write(body);
        }
        byte[] data = bytes.toByteArray(), tag = mac(data);
        bytes.write(tag);
        return bytes.toByteArray();
    }

    /** Returns null on any failure. Never logs attacker-controlled content or secrets. */
    public synchronized String decode(byte[] wire, String direction, String sender, String recipient) {
        if (wire == null || wire.length < 64 || wire.length > MAX_WIRE || !direction(direction)
                || !identifier(sender) || !identifier(recipient)) return null;
        long now = clock.getAsLong();
        // Bound authentication CPU before any decoding, even for bad MACs.
        if (now - rateWindow >= 1000 || now < rateWindow) { rateWindow = now; attempts = 0; }
        if (++attempts > 100) return null;
        byte[] signed = Arrays.copyOf(wire, wire.length - 32);
        if (!MessageDigest.isEqual(mac(signed), Arrays.copyOfRange(wire, wire.length - 32, wire.length))) return null;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(signed))) {
            if (in.readInt() != 0x55505331 || !realm.equals(in.readUTF()) || !direction.equals(in.readUTF())
                    || !sender.equals(in.readUTF()) || !recipient.equals(in.readUTF())) return null;
            long timestamp = in.readLong();
            if (timestamp < now - WINDOW || timestamp > now + WINDOW) return null;
            String nonce = sender + ':' + in.readLong() + ':' + in.readLong();
            int length = in.readInt();
            if (length < 0 || length > MAX_PAYLOAD || length != in.available()) return null;
            seen.entrySet().removeIf(entry -> entry.getValue() < now - WINDOW * 2);
            if (seen.containsKey(nonce) || seen.size() >= 4096) return null;
            // Per authenticated sender as well as global bounds. A key holder cannot create unlimited tasks.
            if (senderRates.size() >= 256 && !senderRates.containsKey(sender)) return null;
            long[] rate = senderRates.computeIfAbsent(sender, ignored -> new long[]{now, 0});
            if (now - rate[0] >= 1000 || now < rate[0]) { rate[0] = now; rate[1] = 0; }
            if (++rate[1] > 40) return null;
            seen.put(nonce, timestamp);
            return new String(in.readNBytes(length), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException invalid) {
            return null;
        }
    }

    private static boolean direction(String value) {
        return "SERVER_TO_PROXY".equals(value) || "PROXY_TO_SERVER".equals(value);
    }

    private byte[] mac(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (GeneralSecurityException unavailable) { throw new IllegalStateException("HmacSHA256 unavailable", unavailable); }
    }
}
