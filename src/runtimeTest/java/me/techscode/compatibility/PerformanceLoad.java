package me.techscode.compatibility;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Disposable CI-only synthetic load generator. It exercises the real plugin/storage
 * and permission lookup on Paper's primary thread; it does NOT create network players.
 */
final class PerformanceLoad {
    private static final int PROFILES = 128;
    private static final int USERS_PER_TICK = 8;
    private static final int WARMUP_TICKS = 60;
    private static final int BASELINE_TICKS = 80;
    private static final int MODERATE_TICKS = 160;
    private static final int HEAVY_TICKS = 160;
    private static final int MODERATE_CHECKS = 200;
    private static final int HEAVY_CHECKS = 500;
    private static final double MAX_HEAVY_P95_MS = 250.0;
    private static final double MAX_HEAVY_TICK_MS = 1200.0;
    private static final double MIN_CHECKS_PER_SECOND = 1200.0;
    private static final long MAX_HEAP_BYTES = 1200L * 1024 * 1024;

    private enum Phase { SEED, WARMUP, BASELINE, MODERATE, HEAVY }
    private record Profile(User user, ModifiedPermissible permissions) {}

    private final RuntimeProbe host;
    private final List<Profile> profiles = new ArrayList<>();
    private final List<Double> baselineIntervals = new ArrayList<>();
    private final List<Double> moderateIntervals = new ArrayList<>();
    private final List<Double> heavyIntervals = new ArrayList<>();
    private final List<Double> moderateBatches = new ArrayList<>();
    private final List<Double> heavyBatches = new ArrayList<>();
    private UltraPermissions permissions;
    private Group group;
    private BukkitTask task;
    private Phase phase = Phase.SEED;
    private long previousTickNanos;
    private long loadStartNanos;
    private long loadEndNanos;
    private long checks;
    private long measuredChecks;
    private long heapPeak;
    private long gcCountBefore;
    private long gcMsBefore;
    private long cpuNanosBefore;
    private int phaseTicks;
    private int cursor;
    private int mutations;

    PerformanceLoad(RuntimeProbe host) {
        this.host = host;
    }

    void start() {
        Bukkit.getScheduler().runTaskLater(host, () -> {
            try {
                permissions = (UltraPermissions) TechPlugin.getInstance();
                require(permissions != null && Bukkit.getPluginManager().isPluginEnabled("UltraPermissions"),
                    "UltraPermissions did not load before the performance test");
                group = permissions.newGroup("performance_probe").create();
                group.newPermission("performance.allowed").create();
                group.newPermission("performance.denied").setPositive(false).create();
                task = Bukkit.getScheduler().runTaskTimer(host, this::tickSafe, 1L, 1L);
            } catch (Throwable failure) {
                finish(failure);
            }
        }, 80L);
    }

    private void tickSafe() {
        try {
            tick();
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private void tick() {
        long now = System.nanoTime();
        if (previousTickNanos != 0L) {
            double intervalMs = (now - previousTickNanos) / 1_000_000.0;
            switch (phase) {
                case BASELINE -> baselineIntervals.add(intervalMs);
                case MODERATE -> moderateIntervals.add(intervalMs);
                case HEAVY -> heavyIntervals.add(intervalMs);
                default -> { }
            }
        }
        previousTickNanos = now;
        heapPeak = Math.max(heapPeak, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        switch (phase) {
            case SEED -> {
                for (int n = 0; n < USERS_PER_TICK && profiles.size() < PROFILES; n++) {
                    int index = profiles.size();
                    UUID uuid = UUID.nameUUIDFromBytes(("ci-load-" + index).getBytes(StandardCharsets.UTF_8));
                    User user = permissions.registerUser(uuid, "LoadProfile_" + index, false);
                    user.addGroup(group);
                    Player player = fakePlayer(uuid, user.getName());
                    ModifiedPermissible permissible = new ModifiedPermissible(player, permissions, permissions.getPermissionLogger());
                    require(permissible.hasPermission("performance.allowed"), "initial grant failed for " + index);
                    require(!permissible.hasPermission("performance.denied"), "initial deny failed for " + index);
                    profiles.add(new Profile(user, permissible));
                }
                if (profiles.size() == PROFILES) transition(Phase.WARMUP);
            }
            case WARMUP -> {
                checkPermissions(80);
                if (++phaseTicks >= WARMUP_TICKS) transition(Phase.BASELINE);
            }
            case BASELINE -> {
                if (++phaseTicks >= BASELINE_TICKS) transition(Phase.MODERATE);
            }
            case MODERATE -> {
                loadBatch(MODERATE_CHECKS, moderateBatches);
                if (++phaseTicks >= MODERATE_TICKS) transition(Phase.HEAVY);
            }
            case HEAVY -> {
                loadBatch(HEAVY_CHECKS, heavyBatches);
                if (phaseTicks % 20 == 0) {
                    Profile target = profiles.get((phaseTicks / 20) % profiles.size());
                    String marker = "load-prefix-" + phaseTicks;
                    target.user().setPrefix(marker);
                    require(marker.equals(target.user().getPrefix().orElse(null)), "prefix update lost during load");
                    mutations++;
                }
                if (++phaseTicks >= HEAVY_TICKS) finish(null);
            }
        }
    }

    private void transition(Phase next) {
        phase = next;
        phaseTicks = 0;
        if (next == Phase.MODERATE) {
            loadStartNanos = System.nanoTime();
            gcCountBefore = gcCount();
            gcMsBefore = gcTime();
            cpuNanosBefore = cpuTime();
        }
        if (next == Phase.HEAVY) {
            // The same plugin, data and profiles are stressed at a higher intensity.
            require(checks >= (long) MODERATE_TICKS * MODERATE_CHECKS,
                "moderate phase completed too few permission lookups");
        }
    }

    private void loadBatch(int count, List<Double> timings) {
        long start = System.nanoTime();
        checkPermissions(count);
        measuredChecks += count;
        timings.add((System.nanoTime() - start) / 1_000_000.0);
    }

    private void checkPermissions(int count) {
        for (int i = 0; i < count; i++) {
            Profile profile = profiles.get(cursor % profiles.size());
            boolean expect = (cursor & 1) == 0;
            boolean result = profile.permissions().hasPermission(
                expect ? "performance.allowed" : "performance.denied");
            require(result == expect, "permission result changed under load at operation " + checks);
            cursor++;
            checks++;
        }
    }

    private static Player fakePlayer(UUID uuid, String name) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getUniqueId" -> uuid;
                case "getName" -> name;
                case "getWorld" -> Bukkit.getWorlds().get(0);
                case "isOnline" -> true; // Simulated online profile: no real client connection.
                case "isOp" -> false;
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> method.getReturnType() == boolean.class ? false : null;
            });
    }

    private void finish(Throwable failure) {
        if (task != null) task.cancel();
        loadEndNanos = System.nanoTime();
        try {
            if (failure == null) {
                require(profiles.size() == PROFILES, "not all synthetic profiles were created");
                require(checks >= (long) MODERATE_TICKS * MODERATE_CHECKS
                    + (long) HEAVY_TICKS * HEAVY_CHECKS, "load completed too few permission lookups");
                require(mutations >= 8, "storage mutation phase not exercised");
                require(baselineIntervals.size() >= BASELINE_TICKS - 1, "baseline tick sample missing");
                require(moderateIntervals.size() >= MODERATE_TICKS - 1, "moderate tick sample missing");
                require(heavyIntervals.size() >= HEAVY_TICKS - 1, "heavy tick sample missing");
                require(percentile(heavyIntervals, 95) <= MAX_HEAVY_P95_MS,
                    "heavy tick p95 exceeded " + MAX_HEAVY_P95_MS + " ms");
                require(max(heavyIntervals) <= MAX_HEAVY_TICK_MS,
                    "heavy tick stall exceeded " + MAX_HEAVY_TICK_MS + " ms");
                require(checksPerSecond() >= MIN_CHECKS_PER_SECOND,
                    "permission throughput under " + MIN_CHECKS_PER_SECOND + " ops/s");
                require(heapPeak <= MAX_HEAP_BYTES, "heap exceeded 1200 MiB");
                require(Bukkit.getOnlinePlayers().isEmpty(), "test is not isolated from connected players");
            }
        } catch (Throwable thresholdFailure) {
            failure = thresholdFailure;
        }
        try {
            Path folder = host.getDataFolder().toPath();
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("performance.json"), report(failure), StandardCharsets.UTF_8);
            host.getLogger().info("PERFORMANCE_PROBE " + (failure == null ? "PASS" : "FAIL")
                + ": profiles=" + profiles.size() + ", checks=" + checks
                + ", heavy_p95_ms=" + String.format(Locale.ROOT, "%.2f", percentile(heavyIntervals, 95)));
        } catch (Exception writeFailure) {
            host.getLogger().log(java.util.logging.Level.SEVERE, "Could not write performance report", writeFailure);
        }
        if (failure != null) host.getLogger().log(java.util.logging.Level.SEVERE, "Performance load test failed", failure);
        Bukkit.shutdown();
    }

    private String report(Throwable failure) {
        double cpuPercent = cpuNanosBefore < 0 || cpuTime() < 0 ? -1.0
            : (cpuTime() - cpuNanosBefore) * 50.0 / Math.max(1L, loadEndNanos - loadStartNanos);
        return String.format(Locale.ROOT, """
            {
              "status": "%s",
              "failure": "%s",
              "scenario": "synthetic-permission-and-storage-load",
              "server": "%s",
              "java": "%s",
              "synthetic_profiles": %d,
              "connected_players": %d,
              "permission_checks": %d,
              "measured_permission_checks": %d,
              "measured_checks_per_second": %.2f,
              "baseline_tick_p95_ms": %.2f,
              "moderate_tick_p95_ms": %.2f,
              "heavy_tick_p50_ms": %.2f,
              "heavy_tick_p95_ms": %.2f,
              "heavy_tick_p99_ms": %.2f,
              "heavy_tick_max_ms": %.2f,
              "moderate_batch_p95_ms": %.2f,
              "heavy_batch_p95_ms": %.2f,
              "storage_mutations": %d,
              "heap_peak_mib": %.2f,
              "gc_collections": %d,
              "gc_pause_ms": %d,
              "process_cpu_percent_of_two_cores": %.2f,
              "paper_tps_1m": %.2f,
              "thresholds": {"heavy_tick_p95_ms": %.0f, "heavy_tick_max_ms": %.0f, "min_checks_per_second": %.0f, "heap_peak_mib": 1200},
              "connected_player_test": false
            }
            """, failure == null ? "passed" : "failed",
            failure == null ? "" : jsonEscape(failure.toString()),
            Bukkit.getBukkitVersion(), System.getProperty("java.version"),
            profiles.size(), Bukkit.getOnlinePlayers().size(), checks, measuredChecks, checksPerSecond(),
            percentile(baselineIntervals, 95), percentile(moderateIntervals, 95),
            percentile(heavyIntervals, 50), percentile(heavyIntervals, 95), percentile(heavyIntervals, 99),
            max(heavyIntervals), percentile(moderateBatches, 95), percentile(heavyBatches, 95),
            mutations, heapPeak / 1048576.0, gcCount() - gcCountBefore, gcTime() - gcMsBefore,
            cpuPercent, Bukkit.getTPS()[0], MAX_HEAVY_P95_MS, MAX_HEAVY_TICK_MS, MIN_CHECKS_PER_SECOND);
    }

    private double checksPerSecond() {
        long nanos = loadEndNanos - loadStartNanos;
        // Warmup lookups are excluded from both the stopwatch and the count.
        return nanos > 0 ? measuredChecks * 1_000_000_000.0 / nanos : 0.0;
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\r", " ").replace("\n", " ");
    }

    private static double percentile(List<Double> values, int percent) {
        if (values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = Math.max(0, Math.min(sorted.size() - 1,
            (int) Math.ceil(percent * sorted.size() / 100.0) - 1));
        return sorted.get(index);
    }

    private static double max(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private static long gcCount() {
        long count = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans())
            if (bean.getCollectionCount() > 0) count += bean.getCollectionCount();
        return count;
    }

    private static long gcTime() {
        long ms = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans())
            if (bean.getCollectionTime() > 0) ms += bean.getCollectionTime();
        return ms;
    }

    private static long cpuTime() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        return bean instanceof com.sun.management.OperatingSystemMXBean b ? b.getProcessCpuTime() : -1L;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
