package me.techscode.build;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.*;

/** Standalone package, dependency and API verification. No plugin startup or external services. */
public final class ComponentChecks {
    private static int assertions;
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]), artifact = Path.of(args[1]);
        String version = args[2];
        for (String line : Files.readAllLines(root.resolve("config/dependencies.sha256"))) {
            if (line.isBlank()) continue;
            String[] values = line.split("  ", 2);
            Path file = root.resolve(values[1]).normalize();
            require(file.startsWith(root), "Dependency path escapes project");
            require(sha(Files.readAllBytes(file)).equals(values[0]), "Dependency checksum mismatch: " + values[1]);
        }
        int libraries = 0, resources = 0, application = 0, contracts = 0;
        Path library = root.resolve("libs/repository/me/techscode/ultrapermissions/runtime-libraries/5.6.8/runtime-libraries-5.6.8.jar");
        try (ZipFile jar = new ZipFile(artifact.toFile()); ZipFile dependencies = new ZipFile(library.toFile())) {
            Set<String> vendor = new HashSet<>(), entries = new HashSet<>();
            for (ZipEntry entry : Collections.list(dependencies.entries())) {
                if (entry.isDirectory()) continue;
                require(entry.getName().endsWith(".class"), "Unexpected runtime dependency entry");
                vendor.add(entry.getName());
                require(Arrays.equals(read(dependencies, entry.getName()), read(jar, entry.getName())), "Packaged dependency differs: " + entry.getName());
                libraries++;
            }
            for (ZipEntry entry : Collections.list(jar.entries())) {
                String name = entry.getName();
                require(entries.add(name), "Duplicate archive entry: " + name);
                for (String prefix : List.of("org/bukkit/", "net/md_5/", "net/luckperms/", "net/milkbowl/", "be/maximvdw/", "lombok/", "ru/tehkode/", "com/nametagedit/", "me/techscode/security/", "me/techscode/build/")) {
                    require(!name.startsWith(prefix), "Provided API/test class leaked into release: " + name);
                }
                if (name.endsWith(".class") && !vendor.contains(name)) {
                    require(metadata(read(jar, name)).major <= 61, "Class exceeds Java 17 target: " + name);
                    application++;
                }
            }
            Path mainResources = root.resolve("src/main/resources");
            try (var files = Files.walk(mainResources)) {
                for (Path path : files.filter(Files::isRegularFile).toList()) {
                    String name = mainResources.relativize(path).toString().replace('\\', '/');
                    if (name.equals("META-INF/MANIFEST.MF")) continue;
                    require(Arrays.equals(Files.readAllBytes(path), read(jar, name)), "Resource mismatch: " + name);
                    resources++;
                }
            }
            for (String file : List.of("plugin.yml", "bungee.yml")) {
                require(new String(read(jar, file), StandardCharsets.UTF_8).contains("version: " + version), "Metadata version mismatch");
            }
            Map<String, Metadata> types = new HashMap<>();
            for (String line : Files.readAllLines(root.resolve("config/api-contracts.tsv"))) {
                String[] fields = line.split("\t", -1);
                String owner = fields[0].equals("C") ? fields[1] : fields[0];
                Metadata type = types.get(owner);
                if (type == null) { type = metadata(read(jar, owner + ".class")); types.put(owner, type); }
                if (!fields[0].equals("C")) {
                    require(type.members.contains(fields[1] + "\t" + fields[2] + "\t" + fields[3]), "Missing API contract: " + line);
                    contracts++;
                }
            }
            require(entries.contains("me/TechsCode/UltraPermissions/base/security/SecureMessaging.class"), "Secure transport missing");
            require(entries.contains("me/TechsCode/UltraPermissions/base/security/SafeItems.class"), "Safe item codec missing");
        }
        Path report = root.resolve("build/reports/components/verification.json"); Files.createDirectories(report.getParent());
        Files.writeString(report, "{\n  \"status\": \"passed\",\n  \"assertions\": " + assertions
            + ",\n  \"runtime_library_classes\": " + libraries + ",\n  \"application_classes\": " + application
            + ",\n  \"packaged_resources\": " + resources + ",\n  \"api_contracts\": " + contracts
            + ",\n  \"server_started\": false,\n  \"jar_sha256\": \"" + sha(Files.readAllBytes(artifact)) + "\"\n}\n");
        System.out.println("Components passed: " + libraries + " library classes, " + application + " application classes, " + contracts + " API contracts.");
    }

    private static String sha(byte[] bytes) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
    private static void require(boolean value, String message) { assertions++; if (!value) throw new AssertionError(message); }
    private static byte[] read(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name); if (entry == null) throw new IOException("Missing artifact entry: " + name);
        try (InputStream input = zip.getInputStream(entry)) { return input.readAllBytes(); }
    }
    private record Metadata(int major, Set<String> members) {}
    private static Metadata metadata(byte[] bytes) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (in.readInt() != 0xCAFEBABE) throw new IOException("Invalid class");
            in.readUnsignedShort(); int major = in.readUnsignedShort(); int count = in.readUnsignedShort(); String[] utf = new String[count];
            for (int i=1;i<count;i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> utf[i] = in.readUTF();
                    case 3,4,9,10,11,12,17,18 -> in.skipNBytes(4);
                    case 5,6 -> { in.skipNBytes(8); i++; }
                    case 7,8,16,19,20 -> in.skipNBytes(2);
                    case 15 -> in.skipNBytes(3);
                    default -> throw new IOException("Unsupported class constant");
                }
            }
            in.skipNBytes(6); in.skipNBytes(in.readUnsignedShort()*2L);
            Set<String> members = new HashSet<>();
            for (String kind : List.of("F", "M")) {
                int size = in.readUnsignedShort();
                for (int i=0;i<size;i++) {
                    int access = in.readUnsignedShort(); String name = utf[in.readUnsignedShort()], descriptor = utf[in.readUnsignedShort()];
                    if ((access & 5) != 0) members.add(kind + "\t" + name + "\t" + descriptor);
                    int attributes = in.readUnsignedShort();
                    for (int a=0;a<attributes;a++) { in.readUnsignedShort(); in.skipNBytes(Integer.toUnsignedLong(in.readInt())); }
                }
            }
            return new Metadata(major, members);
        }
    }
}
