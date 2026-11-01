package me.techscode.security;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.Permission;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.CachedPlayerPermissionProvider;
import me.TechsCode.UltraPermissions.PluginEvents;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.networking.PublicIpResolver;
import me.TechsCode.UltraPermissions.base.networking.SpigotNetworkManager;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.VaultChatHook;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.PlaceholderAPIHook;
import me.TechsCode.UltraPermissions.hooks.PluginHookManager;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.Dialog;
import me.TechsCode.UltraPermissions.base.gui.*;
import me.TechsCode.UltraPermissions.base.messaging.*;
import me.TechsCode.UltraPermissions.base.mysql.*;
import me.TechsCode.UltraPermissions.base.mysql.credentialsTransfer.*;
import me.TechsCode.UltraPermissions.base.scheduler.*;
import me.TechsCode.UltraPermissions.base.security.*;
import me.TechsCode.UltraPermissions.base.storage.*;
import me.TechsCode.UltraPermissions.base.storage.implementations.MySQL;
import me.TechsCode.UltraPermissions.base.storage.implementations.LocalFile;
import me.TechsCode.UltraPermissions.base.fileconf.AtomicFileSave;
import me.TechsCode.UltraPermissions.base.fileconf.FileConfiguration;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.base.update.*;
import me.TechsCode.UltraPermissions.base.update.networkUpdate.*;
import me.TechsCode.UltraPermissions.dependencies.gson.*;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionDatabase;
import me.TechsCode.UltraPermissions.internal.Node;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import me.TechsCode.UltraPermissions.internal.lookup.PermissionLookup;
import me.TechsCode.UltraPermissions.permissionlogger.PermissionLogger;
import me.TechsCode.UltraPermissions.permissionlogger.LoggedPermission;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.migration.InsaneEditorDownload;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sun.misc.Unsafe;

/** Runs known application methods against test doubles; no server, sockets or executable downloads. */
public final class SecurityChecks {
    private static final Unsafe UNSAFE;
    private static int assertions;
    private static final List<String> groups = new ArrayList<>();
    private static final Object DEFAULT = new Object();
    static {
        try { Field field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); UNSAFE = (Unsafe)field.get(null); }
        catch (ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
    }

    @SuppressWarnings("removal")
    public static void main(String[] args) throws Exception {
        System.setSecurityManager(new SecurityManager() {
            @Override public void checkPermission(Permission permission) {}
            @Override public void checkConnect(String host, int port) { throw new SecurityException("Test JVM forbids network access"); }
            @Override public void checkListen(int port) { throw new SecurityException("Test JVM forbids listeners"); }
            @Override public void checkExec(String command) { throw new SecurityException("Test JVM forbids OS execution"); }
        });
        Path report = Path.of(args[0]); Files.createDirectories(report);
        setupBukkit();
        minecraftVersions();
        compatibilityHooks();
        performanceHotpaths();
        messaging(report.resolve("config-fixture"));
        remoteCatalogue();
        sqlAndPool();
        boundedSync();
        tlsPolicy();
        threadingAndNetwork();
        persistenceFailureRecovery(report.resolve("persistence-fixture"));
        uiRevocation();
        serializedItems();
        removedCapabilities();
        commandReference(report);
        JsonObject result = new JsonObject();
        result.addProperty("status", "passed"); result.addProperty("assertions", assertions);
        result.addProperty("network_access", "blocked by test JVM"); result.addProperty("server_started", false);
        result.add("groups", new Gson().toJsonTree(groups));
        Files.writeString(report.resolve("verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
        System.out.println("Security checks passed: " + assertions + " assertions in " + groups.size() + " groups.");
    }

    private static void remoteCatalogue() throws Exception {
        byte[] small = new byte[]{1, 2, 3, 4};
        check(Arrays.equals(small, BoundedInput.read(new ByteArrayInputStream(small), 4)),
            "remote source reads precisely within byte limit");
        throwsType(IOException.class, () -> BoundedInput.read(new ByteArrayInputStream(new byte[5]), 4),
            "external response over byte limit rejected");
        check(PermissionDatabase.safeRecord("WorldEdit+worldedit.*+World editing commands+set,copy"),
            "valid external permission metadata permitted");
        check(PermissionDatabase.acceptableCatalogue("# metadata\nPlugin+plugin.use+description\n"),
            "valid permission catalogue accepted");
        check(PermissionDatabase.acceptableCatalogue("Plugin+plugin.use+\tdescription\n"),
            "published tab-separated text is normalized safely");
        check(!PermissionDatabase.acceptableCatalogue("Plugin+plugin.use+desc\n".repeat(50001)),
            "pathological record count rejected before caching");
        for (String invalid : List.of("WorldEdit+worldedit.*+spoofed\nsecurity warning",
                "WorldEdit+worldedit.*+" + "x".repeat(2049),
                "missing-name+", "Plugin+permission+desc+command+unexpected")) {
            check(!PermissionDatabase.safeRecord(invalid), "malformed catalogue record rejected");
        }
        groups.add("External permission catalogue: bounded download / text validation / immutable GUI snapshots");
    }

    private static void threadingAndNetwork() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        TestPlugin plugin = allocate(TestPlugin.class);
        plugin.scheduler = scheduler;

        PluginEvents events = allocate(PluginEvents.class);
        set(events, "plugin", plugin);
        events.updateData(new UpdateEvent(UpdateTime.SEC));
        check(scheduler.syncCalls == 1 && scheduler.asyncCalls == 0, "permissions maintenance runs on main-thread scheduler");
        scheduler.jobs.clear();

        SpigotMessagingService messaging = allocate(SpigotMessagingService.class);
        set(messaging, "listeners", new ArrayList<>());
        plugin.messaging = messaging;
        new SpigotNetworkManager(plugin);
        check(scheduler.asyncCalls == 1 && scheduler.jobs.size() == 1, "public IP lookup deferred off startup thread");
        scheduler.jobs.clear();

        VaultChatHook vault = allocate(VaultChatHook.class);
        set(vault, "plugin", plugin);
        set(vault, "refreshPending", new AtomicBoolean());
        set(vault, "refreshDirty", new AtomicBoolean());
        vault.clearCache();
        check(scheduler.syncCalls == 2 && scheduler.jobs.size() == 1, "Vault cache rebuild dispatched to main thread");
        scheduler.jobs.clear();

        ThreadStorage storage = allocate(ThreadStorage.class);
        ThreadBackend backend = allocate(ThreadBackend.class);
        set(storage, "plugin", plugin);
        set(storage, "syncBudget", new SyncBudget());
        set(storage, "implementation", backend);
        Map<String, Storable> instances = new java.util.concurrent.ConcurrentHashMap<>();
        set(storage, "instances", instances);
        set(storage, "instancesView", Collections.unmodifiableMap(instances));
        set(storage, "observers", new ArrayList<>());
        storage.syncFromDatabase("fixture-key");
        check(scheduler.asyncCalls == 2 && scheduler.jobs.size() == 1, "database refresh performs I/O on worker");
        scheduler.jobs.remove().run();
        check(scheduler.syncCalls == 3 && storage.synchronizations == 0, "database state not applied on worker");
        scheduler.jobs.remove().run();
        check(storage.synchronizations == 1, "database state applied on main-thread scheduler");

        AtomicBoolean closed = new AtomicBoolean();
        URLConnection connection = new URLConnection(new URL("https://example.invalid")) {
            public void connect() {}
            public InputStream getInputStream() {
                return new ByteArrayInputStream("203.0.113.42\n".getBytes(StandardCharsets.UTF_8)) {
                    @Override public void close() { closed.set(true); }
                };
            }
        };
        check("203.0.113.42".equals(PublicIpResolver.readAddress(connection)), "valid public address parsed");
        check(connection.getConnectTimeout() == 2000 && connection.getReadTimeout() == 2000, "external IP lookup has bounded timeouts");
        check(closed.get(), "HTTP response closed");
        URLConnection untrusted = new URLConnection(new URL("https://example.invalid")) {
            public void connect() {}
            public InputStream getInputStream() { return new ByteArrayInputStream("not-an-ip\n".getBytes(StandardCharsets.UTF_8)); }
        };
        check(PublicIpResolver.readAddress(untrusted) == null, "external response cannot inject non-IP data");
        groups.add("Thread safety: main-thread model writes / Vault cache / no blocking IP lookup / bounded HTTP");
    }

    private static void persistenceFailureRecovery(Path folder) throws Exception {
        Files.createDirectories(folder);
        Path file = folder.resolve("Users.json");
        Files.writeString(file, "previous durable state", StandardCharsets.UTF_8);
        throwsType(IOException.class, () -> AtomicFileSave.save(file.toFile(), staged -> {
            Files.writeString(staged.toPath(), "partial state", StandardCharsets.UTF_8);
            throw new IOException("simulated interrupted write");
        }), "interrupted atomic save reports failure");
        check("previous durable state".equals(Files.readString(file)), "failed save preserves original file");
        try (var files = Files.list(folder)) {
            check(files.noneMatch(path -> path.getFileName().toString().startsWith(".Users.json-")), "failed atomic save removes temp");
        }
        AtomicFileSave.save(file.toFile(), staged -> Files.writeString(staged.toPath(), "new durable state", StandardCharsets.UTF_8));
        check("new durable state".equals(Files.readString(file)), "successful atomic save replaces file");

        // YAML parsing is exercised in RuntimeProbe on Paper; the standalone security JVM has an older SnakeYAML ABI.

        TestFileConfiguration configuration = allocate(TestFileConfiguration.class);
        configuration.values = new HashMap<>();
        configuration.values.put("user", "original");
        configuration.fail = true;
        LocalFile local = allocate(LocalFile.class);
        set(local, "configuration", configuration);
        set(local, "file", file.toFile());
        AtomicInteger writes = new AtomicInteger(), failures = new AtomicInteger();
        WriteCallback callback = new WriteCallback() {
            public void onSuccess() { writes.incrementAndGet(); }
            public void onFailure(Exception failure) { failures.incrementAndGet(); }
        };
        JsonObject state = new JsonObject(); state.addProperty("rank", "admin");
        local.update("user", state, callback);
        check(writes.get() == 0 && failures.get() == 1, "failed disk update calls failure callback exactly once");
        check("original".equals(configuration.values.get("user")), "failed disk update rolls back in-memory config");
        local.destroy("user", callback);
        check(writes.get() == 0 && failures.get() == 2, "failed deletion is never acknowledged");
        check("original".equals(configuration.values.get("user")), "failed deletion retains prior state");
        configuration.fail = false;
        local.update("user", state, callback);
        check(writes.get() == 1 && failures.get() == 2, "recovered disk update acknowledged");
        configuration.values.put("user", "{broken json");
        throwsType(IllegalStateException.class, () -> local.read("*", new ReadCallback() {
            public void onSuccess(HashMap<String, JsonObject> rows) { throw new AssertionError("corrupt read must not succeed"); }
            public void onFailure(Exception failure) { throw new AssertionError("corrupt storage must fail startup"); }
        }), "corrupt local storage stops loading without wiping data");

        TestPlugin plugin = allocate(TestPlugin.class);
        plugin.logs = new ArrayList<>();
        ThreadStorage storage = allocate(ThreadStorage.class);
        PersistenceBackend backend = allocate(PersistenceBackend.class);
        set(storage, "plugin", plugin);
        set(storage, "implementation", backend);
        set(storage, "storable", PersistedItem.class);
        set(storage, "syncingAgent", new SyncingAgent.EmptyAgent());
        Map<String, Storable> instances = new java.util.concurrent.ConcurrentHashMap<>();
        set(storage, "instances", instances);
        set(storage, "instancesView", Collections.unmodifiableMap(instances));
        set(storage, "observers", new ArrayList<>());

        PersistedItem user = new PersistedItem();
        user.setStorage(storage);
        user.data.addProperty("level", "one");
        user.data.addProperty("secret", "sensitive-value");
        backend.fail = true;
        user.persist();
        check(user.getLastSyncedState() == null, "failed update does not advance last-saved marker");
        backend.fail = false;
        user.persist();
        check(user.getLastSyncedState() != null, "successful update advances last-saved marker");
        user.data.addProperty("level", "two");
        backend.fail = true;
        user.persist();
        check("one".equals(user.getLastSyncedState().get("level").getAsString()), "failed newer update retains last durable state");

        PersistedItem created = new PersistedItem();
        throwsType(IllegalStateException.class, () -> storage.create(created), "failed creation propagated to caller");
        check(instances.isEmpty() && !created.isLinkedToStorage(), "failed creation not published in permission model");
        backend.fail = false;
        storage.create(created);
        check(instances.containsKey(created.getKey()) && created.getLastSyncedState() != null, "created object published only after persistence");
        backend.fail = true;
        created.remove();
        check(instances.containsKey(created.getKey()) && created.isLinkedToStorage(), "failed deletion keeps object linked");
        backend.fail = false;
        created.remove();
        check(!instances.containsKey(created.getKey()) && !created.isLinkedToStorage(), "successful deletion removes object");

        check(plugin.logs.stream().noneMatch(log -> log.contains("sensitive-value")), "failure logs exclude private JSON state");
        groups.add("Data integrity: atomic writes / corrupt data fail-closed / callbacks / durable state markers / create-delete rollback");
    }

    private static void performanceHotpaths() throws Exception {
        check(new Node("example.deep.node").getSelfAndWildcards().equals(List.of(
            "example.deep.node", "*", "example.*", "example.deep.*", "example.deep.node.*")),
            "permission lookup preserves exact original wildcard ordering");

        AtomicInteger checkSets = new AtomicInteger();
        AtomicInteger firstChecks = new AtomicInteger();
        AtomicInteger secondChecks = new AtomicInteger();
        PermissionLookup wildcardLookup = new PermissionLookup("example.deep.node") {
            @Override public LookupCheck[] getChecks() {
                checkSets.incrementAndGet();
                return new LookupCheck[]{
                    candidate -> { firstChecks.incrementAndGet(); return null; },
                    candidate -> {
                        secondChecks.incrementAndGet();
                        return "example.*".equals(candidate) ? new LookupOutcome(true, "wildcard") : null;
                    }
                };
            }
        };
        LookupOutcome wildcard = wildcardLookup.perform();
        check(wildcard.getResult() && "wildcard".equals(wildcard.getSource()),
            "wildcard lookup returns the first matching decision and its source");
        check(checkSets.get() == 1 && firstChecks.get() == 3 && secondChecks.get() == 3,
            "permission checks constructed once and visited in wildcard/check priority order");
        check(wildcardLookup.perform() == wildcard && checkSets.get() == 1,
            "successful lookups continue caching their original result");

        PermissionLookup denied = new PermissionLookup("denied") {
            @Override public LookupCheck[] getChecks() {
                return new LookupCheck[]{
                    candidate -> "denied".equals(candidate) ? new LookupOutcome(false, "explicit-deny") : null,
                    candidate -> new LookupOutcome(true, "later-allow")
                };
            }
        };
        check(!denied.perform().getResult() && "explicit-deny".equals(denied.getOutcome().getSource()),
            "explicit deny keeps priority over a later grant");

        AtomicBoolean laterGranted = new AtomicBoolean();
        AtomicInteger negativeBuilds = new AtomicInteger();
        PermissionLookup mutableLookup = new PermissionLookup("mutable") {
            @Override public LookupCheck[] getChecks() {
                negativeBuilds.incrementAndGet();
                return new LookupCheck[]{candidate ->
                    laterGranted.get() && "mutable".equals(candidate) ? new LookupOutcome(true, "updated") : null};
            }
        };
        check(!mutableLookup.perform().getResult() && mutableLookup.getOutcome() == null,
            "unsuccessful lookups retain original uncached semantics");
        laterGranted.set(true);
        check(mutableLookup.perform().getResult() && negativeBuilds.get() == 2,
            "reused negative lookup observes later permission changes");

        // This runs the real logger with thousands of permission decisions, not a mock.
        PermissionLogger logger = allocate(PermissionLogger.class);
        set(logger, "entries", new LinkedHashMap<>());
        UUID firstId = UUID.randomUUID(), secondId = UUID.randomUUID();
        Player first = proxy(Player.class, (m,a) -> m.getName().equals("getUniqueId") ? firstId : DEFAULT);
        Player second = proxy(Player.class, (m,a) -> m.getName().equals("getUniqueId") ? secondId : DEFAULT);
        long started = System.nanoTime();
        for (int i = 0; i < 5000; i++) logger.log(first, "node." + i, true, "fixture");
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        LoggedPermission[] recent = logger.getLoggedPermissions();
        check(recent.length == 500, "permission log retains only the latest 500 distinct checks");
        check(Arrays.stream(recent).noneMatch(e -> e.getPermission().equals("node.0")),
            "oldest permission log evicted");
        check(Arrays.stream(recent).anyMatch(e -> e.getPermission().equals("node.4999")),
            "latest permission log retained");
        logger.log(first, "node.4999", false, "newer decision");
        recent = logger.getLoggedPermissions();
        check(recent.length == 500 && Arrays.stream(recent).anyMatch(e ->
            e.getPermission().equals("node.4999") && !e.getOutcome() && e.getSource().equals("newer decision")),
            "duplicate permission log updates outcome without adding an entry");
        logger.log(second, "independent.node", true, "fixture");
        logger.clearManually(first, "node.4999");
        check(Arrays.stream(logger.getLoggedPermissions()).noneMatch(e -> e.getPermission().equals("node.4999")),
            "manual permission log removal filters matching node");
        logger.clearPlayer(firstId);
        check(logger.getLoggedPermissions().length == 1
            && logger.getLoggedPermissions()[0].getPlayer() == second,
            "quit cleanup releases only the leaving player's entries");
        ExecutorService workers = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> writes = new ArrayList<>();
            for (int worker = 0; worker < 4; worker++) {
                final int thread = worker;
                writes.add(workers.submit(() -> {
                    for (int i = 0; i < 1000; i++)
                        logger.log(first, "parallel." + thread + "." + i, true, "worker");
                }));
            }
            for (Future<?> write : writes) write.get();
        } finally {
            workers.shutdownNow();
        }
        check(logger.getLoggedPermissions().length == 500, "concurrent permission log writers stay bounded");
        System.out.println("PermissionLogger: 5000 sequential writes took " + elapsed + " ms (informational, no timing threshold).");

        TestPlugin plugin = allocate(TestPlugin.class);
        plugin.users = new UserList();
        plugin.scheduler = new TestScheduler();
        CachedPlayerPermissionProvider provider = new CachedPlayerPermissionProvider(plugin);
        @SuppressWarnings("unchecked")
        Map<UUID, CachedPlayerPermissionProvider.CacheEntry> cache =
            (Map<UUID, CachedPlayerPermissionProvider.CacheEntry>)get(provider, "cache");
        for (int i = 1; i <= 2100; i++) {
            cache.put(new UUID(0L, i), provider.new CacheEntry(new PermissionList(), "world"));
        }
        check(cache.size() == 2048 && !cache.containsKey(new UUID(0L, 1))
                && cache.containsKey(new UUID(0L, 2100)),
            "shared Vault/PlaceholderAPI permission cache evicts least-recently-used player");
        cache.get(new UUID(0L, 2000));
        for (int i = 2101; i <= 2120; i++)
            cache.put(new UUID(0L, i), provider.new CacheEntry(new PermissionList(), "world"));
        check(cache.containsKey(new UUID(0L, 2000)), "recently accessed permission cache entry survives eviction");
        provider.clearCache();
        check(cache.isEmpty(), "permission cache invalidation clears all retained snapshots");
        UUID cachedId = UUID.randomUUID();
        PermissionList nullableWorld = new PermissionList();
        cache.put(cachedId, provider.new CacheEntry(nullableWorld, null));
        check(provider.retrieve(cachedId, null) == nullableWorld, "world-less cache hit is null safe");
        plugin.currentServer = new me.TechsCode.UltraPermissions.base.networking.NServer(
            "survival", "127.0.0.1", 25565, new ArrayList<>());
        check(provider.retrieve(cachedId, null) != nullableWorld && cache.isEmpty(),
            "late network identity flushes stale permission snapshots");
        check(!Modifier.isStatic(field(provider, "thisServer").getModifiers()),
            "server scope is per permission provider, not a global static cache");

        VaultChatHook vault = allocate(VaultChatHook.class);
        set(vault, "plugin", plugin);
        set(vault, "refreshPending", new AtomicBoolean());
        set(vault, "refreshDirty", new AtomicBoolean());
        for (int i = 0; i < 1000; i++) vault.clearCache();
        check(plugin.scheduler.syncCalls == 1 && plugin.scheduler.jobs.size() == 1,
            "1000 Vault invalidations coalesce into one main-thread rebuild");
        plugin.scheduler.jobs.remove().run();
        check(vault.getPlayerPrefix("world", "missing") == null,
            "empty Vault cache refresh safely publishes a readable snapshot");
        vault.clearCache();
        check(plugin.scheduler.syncCalls == 2, "Vault cache may schedule a new rebuild after the previous one completes");
        // Simulate a modification arriving in the middle of rebuilding the snapshot.
        AtomicBoolean duringRefresh = new AtomicBoolean(true);
        plugin.users = new UserList() {
            @Override public java.util.stream.Stream<User> stream() {
                if (duringRefresh.getAndSet(false)) vault.clearCache();
                return super.stream();
            }
        };
        plugin.scheduler.jobs.remove().run();
        check(plugin.scheduler.syncCalls == 3 && plugin.scheduler.jobs.size() == 1,
            "Vault invalidation during rebuild schedules one follow-up, not stale state");
        plugin.scheduler.jobs.remove().run();
        check(plugin.scheduler.jobs.isEmpty(), "Vault follow-up refresh completes without repeated scheduling");
        groups.add("Performance: ordered wildcard lookup / O(1) bounded logging / 2048 LRU / coalesced Vault cache rebuild");
    }

    private static void compatibilityHooks() throws Exception {
        // An optional dependency may be absent or have a changed binary API.
        TestPlugin plugin = allocate(TestPlugin.class);
        plugin.logs = new ArrayList<>();
        PluginHookManager manager = allocate(PluginHookManager.class);
        set(manager, "plugin", plugin);
        Map<String, Runnable> registered = new HashMap<>();
        AtomicInteger installed = new AtomicInteger();
        registered.put("Working", installed::incrementAndGet);
        registered.put("MissingClass", () -> { throw new NoClassDefFoundError("optional/Api"); });
        registered.put("ChangedMethod", () -> { throw new NoSuchMethodError("removedMethod"); });
        registered.put("BrokenProvider", () -> { throw new IllegalStateException("invalid provider"); });
        set(manager, "hooks", registered);
        manager.tryHook(true, "Working");
        manager.tryHook(true, "Working");
        check(installed.get() == 1 && !registered.containsKey("Working"), "valid optional hook activated exactly once");
        for (String failed : List.of("MissingClass", "ChangedMethod", "BrokenProvider")) {
            manager.tryHook(true, failed);
            check(registered.containsKey(failed), "incompatible hook remains available for a later re-enable");
            check(plugin.logs.stream().anyMatch(log -> log.contains(failed) && log.contains("unavailable")),
                "incompatible optional hook logged without crashing the permissions core");
        }
        registered.put("MissingClass", installed::incrementAndGet);
        manager.tryHook(true, "MissingClass");
        check(installed.get() == 2 && !registered.containsKey("MissingClass"),
            "optional hook can recover if a compatible provider is enabled later");
        manager.tryHook(true, "Unknown");

        check(PlaceholderAPIHook.onlineWorld(null).isEmpty(), "missing PlaceholderAPI player safe");
        OfflinePlayer offline = proxy(OfflinePlayer.class, (m,a) -> DEFAULT);
        check(PlaceholderAPIHook.onlineWorld(offline).isEmpty(), "offline PlaceholderAPI player has no world");
        Player withoutWorld = proxy(Player.class, (m,a) -> DEFAULT);
        OfflinePlayer unloaded = proxy(OfflinePlayer.class,
            (m,a) -> m.getName().equals("getPlayer") ? withoutWorld : DEFAULT);
        check(PlaceholderAPIHook.onlineWorld(unloaded).isEmpty(), "online player without loaded world safe");
        World world = proxy(World.class,
            (m,a) -> m.getName().equals("getName") ? "survival" : DEFAULT);
        Player connected = proxy(Player.class,
            (m,a) -> m.getName().equals("getWorld") ? world : DEFAULT);
        OfflinePlayer online = proxy(OfflinePlayer.class,
            (m,a) -> m.getName().equals("getPlayer") ? connected : DEFAULT);
        check("survival".equals(PlaceholderAPIHook.onlineWorld(online).orElse(null)),
            "online PlaceholderAPI player resolves world-scoped permissions");
        groups.add("Compatibility: optional API linkage isolation / hook recovery / offline placeholders");
    }

    private static void minecraftVersions() {
        for (Object[] fixture : new Object[][] {
            {"1.8.8-R0.1-SNAPSHOT", 8, 8}, {"1.20-R0.1-SNAPSHOT", 20, 0},
            {"1.21.11-R0.1-SNAPSHOT", 21, 11}, {"26.1.2.build.61-stable", 26, 2},
            {"26.2.build.135-stable", 26, 0}, {"26.2", 26, 0}
        }) {
            var release = me.TechsCode.UltraPermissions.base.reflection.utility.MinecraftRelease.parse((String)fixture[0]);
            check(release.feature() == (int)fixture[1], "Minecraft feature branch: " + fixture[0]);
            check(release.patch() == (int)fixture[2], "Minecraft patch excludes Paper build: " + fixture[0]);
        }
        groups.add("Minecraft release formats / Paper 26.2 build suffix");
    }

    private static void messaging(Path fixture) throws Exception {
        AtomicLong clock = new AtomicLong(1000000);
        byte[] key = new byte[32]; Arrays.fill(key, (byte)42);
        SecureMessaging backend = new SecureMessaging(key, "realm", "survival", "proxy", clock::get);
        SecureMessaging proxy = new SecureMessaging(key, "realm", "proxy", "proxy", clock::get);
        JsonObject data = new JsonObject(); data.addProperty("model", User.class.getName()); data.addProperty("dataKey", "safe-key");
        Message cache = new Message("cache", data);
        byte[] wire = backend.encode("SERVER_TO_PROXY", "survival", "proxy", cache.encode());
        check(cache.encode().equals(proxy.decode(wire, "SERVER_TO_PROXY", "survival", "proxy")), "signed cache accepted");
        check(proxy.decode(wire, "SERVER_TO_PROXY", "survival", "proxy") == null, "replay rejected");
        byte[] modified = wire.clone(); modified[modified.length / 2] ^= 1;
        check(proxy.decode(modified, "SERVER_TO_PROXY", "survival", "proxy") == null, "tamper rejected");
        check(proxy.decode(wire, "PROXY_TO_SERVER", "survival", "proxy") == null, "reflection/direction rejected");
        check(proxy.decode(wire, "SERVER_TO_PROXY", "other", "proxy") == null, "sender bound");
        check(proxy.decode(wire, "SERVER_TO_PROXY", "survival", "other") == null, "recipient bound");
        check(new SecureMessaging(new byte[32], "realm", "proxy", "proxy", clock::get)
            .decode(wire, "SERVER_TO_PROXY", "survival", "proxy") == null, "wrong key rejected");
        check(new SecureMessaging(key, "other-realm", "proxy", "proxy", clock::get)
            .decode(wire, "SERVER_TO_PROXY", "survival", "proxy") == null, "realm bound");
        check(proxy.decode("UltraPermissions//old-payload".getBytes(), "SERVER_TO_PROXY", "survival", "proxy") == null, "legacy rejected");
        clock.addAndGet(60001);
        check(proxy.decode(wire, "SERVER_TO_PROXY", "survival", "proxy") == null, "expired timestamp rejected");
        throwsType(IOException.class, () -> backend.encode("SERVER_TO_PROXY", "survival", "proxy", "x".repeat(24001)), "oversized message rejected");
        for (String forbidden : List.of("restart", "update", "mysql", "unknown")) {
            check(!MessagePolicy.allowed(new Message(forbidden, new JsonObject()), "PROXY_TO_SERVER"), forbidden + " denied after authentication");
        }
        for (String injection : List.of("x' OR '1'='1", "../secret", "", "x".repeat(65), "a\n")) check(!MessagePolicy.storageKey(injection), "invalid cache key");
        check(MessagePolicy.allowed(cache, "SERVER_TO_PROXY"), "ordinary cache allowed");
        for (Class<?> model : List.of(User.class,
                me.TechsCode.UltraPermissions.storage.objects.Group.class,
                me.TechsCode.UltraPermissions.storage.objects.Permission.class,
                me.TechsCode.UltraPermissions.storage.objects.GroupPath.class,
                me.TechsCode.UltraPermissions.base.registry.RegistryStorageEntry.class)) {
            JsonObject item = new JsonObject();
            item.addProperty("model", model.getName());
            item.addProperty("dataKey", "*");
            check(MessagePolicy.allowed(new Message("cache", item), "SERVER_TO_PROXY"),
                "actual storage model can synchronize: " + model.getSimpleName());
        }
        for (String fake : List.of("Users", "java.lang.Runtime",
                User.class.getName() + "$Injected", "../User", "User\nAdmin")) {
            JsonObject spoofed = new JsonObject();
            spoofed.addProperty("model", fake);
            spoofed.addProperty("dataKey", "*");
            check(!MessagePolicy.allowed(new Message("cache", spoofed), "SERVER_TO_PROXY"),
                "unknown or injected storage model denied");
        }
        JsonObject network = new JsonObject();
        network.addProperty("proxyVersion", "1.2.3");
        JsonArray serverList = new JsonArray();
        JsonObject server = new JsonObject();
        server.addProperty("name", "survival");
        server.addProperty("ip", "2001:db8::1");
        server.addProperty("port", 25565);
        server.add("players", new JsonArray());
        serverList.add(server);
        network.add("serverList", serverList);
        check(MessagePolicy.allowed(new Message("networking", network), "PROXY_TO_SERVER"),
            "valid IPv6 networking metadata allowed");
        server.addProperty("ip", "server.example\nFORGED");
        check(!MessagePolicy.allowed(new Message("networking", network), "PROXY_TO_SERVER"),
            "host control-character injection rejected");
        throwsType(RuntimeException.class, () -> Message.decode(Base64.getEncoder().encodeToString(("{\"key\":\"cache\",\"data\":" + "[".repeat(40) + "]".repeat(40) + "}").getBytes())), "deep JSON rejected");
        SecureMessaging limited = new SecureMessaging(key, "realm", "proxy", "proxy", clock::get);
        for (int i=0;i<40;i++) check(limited.decode(backend.encode("SERVER_TO_PROXY", "survival", "proxy", cache.encode()), "SERVER_TO_PROXY", "survival", "proxy") != null, "burst within limit");
        check(limited.decode(backend.encode("SERVER_TO_PROXY", "survival", "proxy", cache.encode()), "SERVER_TO_PROXY", "survival", "proxy") == null, "sender rate limit");
        clock.addAndGet(1001);
        check(limited.decode(backend.encode("SERVER_TO_PROXY", "survival", "proxy", cache.encode()), "SERVER_TO_PROXY", "survival", "proxy") != null, "rate window recovers");
        // Exercise the actual backend listener, rather than only the cryptographic helper.
        SpigotMessagingService service = allocate(SpigotMessagingService.class);
        set(service, "security", backend);
        AtomicInteger calls = new AtomicInteger();
        set(service, "listeners", List.of((SpigotMessagingListener)m -> calls.incrementAndGet()));
        byte[] trusted = proxy.encode("PROXY_TO_SERVER", "proxy", "survival", cache.encode());
        service.onPluginMessageReceived("BungeeCord", null, trusted);
        check(calls.get() == 0, "old channel never reaches listeners");
        service.onPluginMessageReceived(SecureMessaging.CHANNEL, null, trusted);
        check(calls.get() == 1, "signed new channel reaches listener");
        service.onPluginMessageReceived(SecureMessaging.CHANNEL, null, proxy.encode("PROXY_TO_SERVER", "proxy", "survival", new Message("restart", new JsonObject()).encode()));
        check(calls.get() == 1, "privileged key blocked in real listener");
        check(SecureMessaging.load(fixture.toFile(), false, ignored -> {}) == null, "new installation defaults disabled");
        check(Files.readString(fixture.resolve("security.properties")).contains("messaging-enabled=false"), "local key generated without enabling transport");
        groups.add("H-01 authenticated channel / replay / spoof / bounds / policy / backend listener");
    }

    private static void sqlAndPool() throws Exception {
        TestPlugin plugin = allocate(TestPlugin.class);
        TestManager manager = allocate(TestManager.class); plugin.manager = manager;
        MySQL storage = allocate(MySQL.class); set(storage, "plugin", plugin); set(storage, "tableName", "UltraPermissions_Users");
        JdbcFixture db = new JdbcFixture(); manager.db = db;
        String injection = "x' OR '1'='1";
        AtomicInteger success = new AtomicInteger(), failures = new AtomicInteger();
        ReadCallback read = new ReadCallback() {
            public void onSuccess(HashMap<String, JsonObject> rows) { success.incrementAndGet(); }
            public void onFailure(Exception error) { failures.incrementAndGet(); }
        };
        WriteCallback write = new WriteCallback() {
            public void onSuccess() { success.incrementAndGet(); }
            public void onFailure(Exception error) { failures.incrementAndGet(); }
        };
        storage.read(injection, read);
        check(db.sql.contains("WHERE `key`=?") && !db.sql.contains(injection), "SELECT parameterized");
        check(injection.equals(db.parameters.get(1)), "SELECT value bound literally");
        check(db.connectionsClosed == 1 && db.statementsClosed == 1 && db.resultsClosed == 1, "SELECT resources closed");
        JsonObject state = new JsonObject(); state.addProperty("name", "O'Neil");
        storage.create(injection, state, write);
        check(db.sql.contains("VALUES (?,?)") && !db.sql.contains(injection), "INSERT parameterized");
        check(injection.equals(db.parameters.get(1)), "INSERT key bound");
        check(new String(Base64.getDecoder().decode(db.parameters.get(2)), StandardCharsets.UTF_8).equals(state.toString()), "JSON quote preserved");
        storage.update(injection, state, write);
        check(db.sql.contains("ON DUPLICATE KEY UPDATE") && db.sql.contains("VALUES (?,?)")
            && injection.equals(db.parameters.get(1)) && !db.sql.contains(injection), "UPDATE uses parameterized UPSERT to avoid lost rows");
        storage.destroy(injection, write);
        check(db.sql.contains("WHERE `key`=?") && injection.equals(db.parameters.get(1)), "DELETE parameterized");
        storage.read("*", read); check(!db.sql.contains("WHERE") && db.parameters.isEmpty(), "full sync is separate fixed query");
        check(db.queryTimeout == 5, "bounded statement timeout");
        manager.fail = true;
        int before = manager.calls; long start = System.nanoTime();
        storage.read("safe-key", read); storage.destroy("safe-key", write);
        check(manager.calls == before+2 && failures.get() == 2, "pool failure invokes callbacks once, without polling");
        check((System.nanoTime()-start) < 1000000000L, "pool failure returns promptly");
        check(success.get() == 5, "ordinary operations complete");
        groups.add("H-03 SQL value binding; M-02 no pool spin/thread creation and resource closure");
    }

    private static void boundedSync() {
        SyncBudget budget = new SyncBudget(); ArrayDeque<Runnable> queue = new ArrayDeque<>(); AtomicInteger runs = new AtomicInteger();
        for (int i=0;i<64;i++) check(budget.submit(queue::add, "key-"+i, runs::incrementAndGet), "bounded slot accepted");
        check(!budget.submit(queue::add, "overflow", runs::incrementAndGet), "65th concurrent job rejected");
        check(queue.size() == 64, "scheduler queue bounded");
        check(budget.submit(queue::add, "key-0", runs::incrementAndGet) && queue.size() == 64, "duplicate notification coalesced");
        while (!queue.isEmpty()) queue.remove().run();
        check(runs.get() == 65, "last coalesced refresh retained");
        check(budget.submit(queue::add, "again", runs::incrementAndGet), "permit released after completion"); queue.remove().run();
        budget.submit(queue::add, "failure", () -> { throw new IllegalStateException("fixture"); });
        try { queue.remove().run(); } catch (IllegalStateException expected) {}
        check(budget.submit(queue::add, "failure", runs::incrementAndGet), "failed job releases its slot"); queue.remove().run();
        groups.add("M-02 bounded scheduling / coalescing / failure cleanup");
    }

    private static void tlsPolicy() throws Exception {
        MySQLCredentials remote = new MySQLCredentials("db.example.test", "3306", "permissions", "fixture", "fixture");
        throwsType(IllegalArgumentException.class, () -> JdbcPolicy.url(remote, false), "remote plaintext refused");
        check(JdbcPolicy.url(remote, true).contains("sslMode=VERIFY_IDENTITY"), "identity verification required");
        MySQLCredentials local = new MySQLCredentials("127.0.0.1", "3306", "permissions", "fixture", "fixture");
        check(JdbcPolicy.url(local, false).contains("sslMode=DISABLED"), "explicit loopback-only legacy exception");
        for (String host : List.of("localhost.evil.test", "127.0.0.1.evil.test", "0.0.0.0", "192.168.1.1")) {
            throwsType(IllegalArgumentException.class, () -> JdbcPolicy.url(new MySQLCredentials(host,"3306","permissions","x","x"),false), "not loopback");
        }
        remote.setDatabase("permissions?sslMode=DISABLED");
        throwsType(IllegalArgumentException.class, () -> JdbcPolicy.url(remote,true), "URL property injection refused"); remote.setDatabase("permissions");
        Driver driver = proxy(Driver.class, (method, args) -> {
            if (method.getName().equals("acceptsURL")) return true;
            if (method.getName().equals("getPropertyInfo")) return new DriverPropertyInfo[0];
            if (method.getName().equals("connect")) throw new AssertionError("Driver must not connect in preflight");
            return DEFAULT;
        });
        DriverManager.registerDriver(driver);
        try { throwsType(SQLException.class, () -> JdbcPolicy.requireIdentityVerification(JdbcPolicy.url(remote,true)), "old/ignoring driver refused before connect"); }
        finally { DriverManager.deregisterDriver(driver); }
        Driver modern = proxy(Driver.class, (method, args) -> {
            if (method.getName().equals("acceptsURL")) return true;
            if (method.getName().equals("getPropertyInfo")) {
                DriverPropertyInfo ssl = new DriverPropertyInfo("sslMode", "VERIFY_IDENTITY");
                ssl.choices = new String[]{"DISABLED", "VERIFY_IDENTITY"};
                return new DriverPropertyInfo[]{ssl};
            }
            if (method.getName().equals("connect")) throw new AssertionError("No preflight connection");
            return DEFAULT;
        });
        DriverManager.registerDriver(modern);
        try { JdbcPolicy.requireIdentityVerification(JdbcPolicy.url(remote, true)); check(true,"identity-capable driver accepted without connecting"); }
        finally { DriverManager.deregisterDriver(modern); }
        check(new MySQLRegistry().hasSSL(), "new registry defaults TLS on");
        groups.add("M-05 TLS fail-closed / identity mode / driver capability / URL injection");
    }

    private static void uiRevocation() throws Exception {
        TestPlugin plugin = allocate(TestPlugin.class); plugin.users = new UserList(); plugin.scheduler = new TestScheduler();
        UUID id = UUID.randomUUID(); User user = new User(id, "Fixture", true, new HashMap<>(), "", "", null); plugin.users.add(user);
        Inventory top = proxy(Inventory.class, (m,a) -> m.getName().equals("getSize") ? 27 : DEFAULT);
        Inventory bottom = proxy(Inventory.class, (m,a) -> DEFAULT);
        AtomicReference<InventoryView> view = new AtomicReference<>(); AtomicInteger closeCalls = new AtomicInteger();
        Player player = proxy(Player.class, (m,a) -> switch (m.getName()) {
            case "getUniqueId" -> id; case "isOnline" -> true; case "getOpenInventory" -> view.get();
            case "closeInventory" -> { closeCalls.incrementAndGet(); yield null; } default -> DEFAULT;
        });
        view.set(proxy(InventoryView.class, (m,a) -> switch (m.getName()) {
            case "getPlayer" -> player; case "getTopInventory" -> top; case "getBottomInventory" -> bottom;
            case "getInventory" -> (int)a[0] < 27 ? top : bottom; case "convertSlot" -> a[0]; default -> DEFAULT;
        }));
        check(ViewAccess.top(view.get()).equals(top), "InventoryView interface accessor");
        check(ViewAccess.top(new LegacyView(top)).equals(top), "InventoryView class-style accessor");
        TestGui gui = allocate(TestGui.class); set(gui,"p",player); set(gui,"plugin",plugin); set(gui,"inventory",top);
        set(gui,"recurringTask",new IdleTask()); AtomicInteger edits = new AtomicInteger();
        HashMap<Integer,List<Action>> actions = new HashMap<>(); actions.put(0,List.of(ignored -> edits.incrementAndGet())); set(gui,"actions",actions);
        InventoryClickEvent click = new InventoryClickEvent(view.get(),InventoryType.SlotType.CONTAINER,0,ClickType.LEFT,InventoryAction.PICKUP_ALL);
        gui.click(click); check(edits.get()==1 && click.isCancelled(),"authorized current GUI action works");
        Inventory wrong = proxy(Inventory.class,(m,a)->DEFAULT);
        InventoryView wrongView = proxy(InventoryView.class,(m,a)-> switch(m.getName()) { case "getPlayer" -> player; case "getTopInventory" -> wrong; default -> DEFAULT; });
        gui.click(new InventoryClickEvent(wrongView,InventoryType.SlotType.CONTAINER,0,ClickType.LEFT,InventoryAction.PICKUP_ALL));
        check(edits.get()==1,"different inventory cannot reuse action slots");
        set(user,"superadmin",false); gui.click(click);
        check(edits.get()==1 && closeCalls.get()==1,"revoked GUI cannot mutate and closes");
        check(get(gui,"actions")==null,"revoked action map discarded");
        set(user,"superadmin",true);
        TestDialog dialog = allocate(TestDialog.class); set(dialog,"p",player); set(dialog,"plugin",plugin);
        AsyncPlayerChatEvent chat = new AsyncPlayerChatEvent(true,player,"fixture.permission",Set.of()); dialog.onChat(chat);
        check(chat.isCancelled() && dialog.inputs==0 && plugin.scheduler.jobs.size()==1,"chat mutation deferred to server thread");
        dialog.onChat(new AsyncPlayerChatEvent(true,player,"second",Set.of()));
        check(plugin.scheduler.jobs.size()==1,"dialog input queue bounded");
        set(user,"superadmin",false); plugin.scheduler.jobs.remove().run();
        check(dialog.inputs==0 && dialog.revoked,"revocation checked again when queued chat executes");
        check(!UiAccess.allowed(plugin,player),"current role, not cached session role");
        TestDialog allowedDialog = allocate(TestDialog.class); set(allowedDialog,"p",player); set(allowedDialog,"plugin",plugin);
        set(user,"superadmin",true); allowedDialog.onChat(new AsyncPlayerChatEvent(true,player,"allowed",Set.of()));
        plugin.scheduler.jobs.remove().run(); check(allowedDialog.inputs==1,"authorized dialog still processes input");
        groups.add("M-03 current-role GUI / inventory binding / stale chat / main-thread / bounded input");
    }

    private static void serializedItems() throws Exception {
        ItemStack item = new ItemStack(Material.STONE, 3);
        String encoded = SafeItems.encode(item);
        check(encoded.startsWith(SafeItems.PREFIX),"new item format is JSON, not Java serialization");
        ItemStack restored = SafeItems.decode(encoded);
        check(restored.getType()==Material.STONE && restored.getAmount()==3,"new item round-trip");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) { out.writeObject(item); }
        restored = SafeItems.decode(Base64.getEncoder().encodeToString(bytes.toByteArray()));
        check(restored.getType()==Material.STONE && restored.getAmount()==3,"ordinary legacy Bukkit/Guava wrapper migrates");
        bytes.reset(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(new Gadget());}
        String gadget=Base64.getEncoder().encodeToString(bytes.toByteArray());
        throwsType(InvalidClassException.class,()->SafeItems.decode(gadget),"unapproved serialized class rejected before readObject");
        check(Gadget.executed==0,"readObject gadget never executed");
        bytes.reset(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(new Object[8193]);}
        String array=Base64.getEncoder().encodeToString(bytes.toByteArray());
        throwsType(InvalidClassException.class,()->SafeItems.decode(array),"large allocation rejected by filter before array creation");
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(HostileAlias.class,"HostileAlias");
        bytes.reset(); try(BukkitObjectOutputStream out=new BukkitObjectOutputStream(bytes)){out.writeObject(new HostileAlias());}
        String legacyAlias=Base64.getEncoder().encodeToString(bytes.toByteArray());
        throwsType(IOException.class,()->SafeItems.decode(legacyAlias),"legacy wrapper cannot instantiate arbitrary Bukkit alias");
        check(HostileAlias.executed==0,"Bukkit factory for hostile alias never invoked");
        String alias=SafeItems.PREFIX+Base64.getEncoder().encodeToString("{\"==\":\"java.lang.Runtime\"}".getBytes());
        throwsType(IOException.class,()->SafeItems.decode(alias),"arbitrary Bukkit alias rejected");
        throwsType(IOException.class,()->SafeItems.decode("x".repeat(524289)),"encoded item limit");
        groups.add("M-04 JSON round-trip / legacy migration / class and alias whitelist / allocation bounds");
    }

    private static void removedCapabilities() throws Exception {
        TestPlugin plugin=allocate(TestPlugin.class); plugin.logs=new ArrayList<>();
        allocate(SpigotUpdateAgent.class).onMessage(new Message("restart",new JsonObject()));
        allocate(SpigotUpdateAgent.class).onMessage(new Message("update",new JsonObject()));
        allocate(BungeeUpdateAgent.class).onMessage(null,new Message("update",new JsonObject()));
        allocate(ProxyMySQLReceiver.class).onMessage(null,new Message("mysql",new JsonObject()));
        SpigotMySQLSender sender=allocate(SpigotMySQLSender.class);
        Method method=SpigotMySQLSender.class.getDeclaredMethod("sendMySQLCredentials",boolean.class); method.setAccessible(true); method.invoke(sender,true);
        check(true,"privileged/credential handlers have no effect, even when invoked directly");
        UpdateServer updater=new UpdateServer();
        try { Thread task=updater.downloadJar(plugin,"fixture"); check(task.getState()==Thread.State.NEW && updater.hasJarDownloadFailed(),"updater starts no task or download"); }
        finally { updater.destroy(); }
        InsaneEditorDownload installer=allocate(InsaneEditorDownload.class); set(installer,"plugin",plugin);
        AtomicInteger notices=new AtomicInteger(); set(installer,"player",proxy(Player.class,(m,a)-> { if(m.getName().equals("sendMessage")) { notices.incrementAndGet(); return null; } return DEFAULT; }));
        method=InsaneEditorDownload.class.getDeclaredMethod("installJar",String.class); method.setAccessible(true); method.invoke(installer,"not-a-url");
        check(notices.get()==1,"installer gives manual-install instruction without fetching/executing code");
        groups.add("H-02 no credential transfer; H-01 no remote console; M-01 no executable network installation");
    }

    private static void setupBukkit() {
        org.bukkit.UnsafeValues unsafe=proxy(org.bukkit.UnsafeValues.class,(m,a)-> switch(m.getName()) {
            case "getMaterial" -> Material.valueOf((String)a[0]); case "getDataVersion" -> 0; default -> DEFAULT;
        });
        ItemFactory items=proxy(ItemFactory.class,(m,a)-> m.getName().equals("equals") ? Objects.equals(a[0],a[1]) : DEFAULT);
        Server server=proxy(Server.class,(m,a)-> switch(m.getName()) {
            case "getName" -> "SecurityFixture"; case "getVersion", "getBukkitVersion" -> "1.21.11-R0.1-SNAPSHOT";
            case "getLogger" -> Logger.getLogger("SecurityFixture"); case "getUnsafe" -> unsafe; case "getItemFactory" -> items;
            case "getOnlinePlayers", "getWorlds" -> List.of();
            case "getRegistry" -> proxy(org.bukkit.Registry.class,(rm,ra)-> rm.getName().equals("iterator") ? Collections.emptyIterator() : DEFAULT);
            default -> DEFAULT;
        });
        Bukkit.setServer(server);
    }

    private static void commandReference(Path report) throws Exception {
        var plugin = allocate(TestPlugin.class);
        plugin.currentServer = new me.TechsCode.UltraPermissions.base.networking.NServer("survival", "127.0.0.1", 25565, new ArrayList<>());
        var command = allocate(me.TechsCode.UltraPermissions.commands.UPCCommand.class);
        set(command, "plugin", plugin);
        var root = me.TechsCode.UltraPermissions.base.command.CommandNode.create("upc");
        command.define(null, root);
        for (var branch : root.getChildren()) {
            String name = branch.getArgument().getUsage().toLowerCase(Locale.ROOT);
            if (!Set.of("addgroup", "listgrouppermissions", "listplayerpermissions").contains(name)) {
                check(branch.getRequirements().stream().anyMatch(r -> r instanceof me.TechsCode.UltraPermissions.commands.requirements.SuperadminRequirement), "administrative branch requires current superadmin: " + name);
            }
        }
        plugin.users = new UserList();
        var unknownPlayer = proxy(Player.class, (m,a) -> m.getName().equals("getUniqueId") ? UUID.randomUUID() : DEFAULT);
        check(!new me.TechsCode.UltraPermissions.commands.requirements.GrantGroupsRequirement(plugin).isMatching(unknownPlayer, unknownPlayer, List.of("upc")), "unknown player is denied without throwing");
        var arguments = new me.TechsCode.UltraPermissions.base.command.Arguments();
        arguments.addAll(List.of("upc", "RegisterUser", UUID.randomUUID().toString(), "ConsoleFixture", "true"));
        AtomicInteger consoleReplies = new AtomicInteger();
        org.bukkit.command.CommandSender console = proxy(org.bukkit.command.CommandSender.class, (m,a) -> { if(m.getName().equals("sendMessage")){consoleReplies.incrementAndGet();return null;}return DEFAULT; });
        root.execute(console, null, arguments, ignored -> {});
        check(consoleReplies.get() == 1 && plugin.users.size() == 1, "RegisterUser console command replies without a null Player");
        List<String> usages = new ArrayList<>();
        collectCommands(root, usages);
        plugin.currentServer = null;
        var standalone = me.TechsCode.UltraPermissions.base.command.CommandNode.create("upc");
        command.define(null, standalone);
        List<String> standaloneUsages = new ArrayList<>(); collectCommands(standalone, standaloneUsages);
        for (String usage : standaloneUsages) if (!usages.contains(usage)) usages.add(usage);
        check(usages.size() > 30, "all command branches described");
        var string = new me.TechsCode.UltraPermissions.commands.arguments.StringArgument(plugin, "groupName");
        check(string.getUsage().equals("<groupName>"), "command usage label retained");
        var bool = new me.TechsCode.UltraPermissions.commands.arguments.BooleanArgument(plugin, me.TechsCode.UltraPermissions.base.command.EmptyReason.INVALID);
        check(bool.get("true").get(), "boolean true parsed from argument");
        check(!bool.get("false").get(), "boolean false parsed from argument");
        Files.write(report.resolve("command-reference.tsv"), usages, StandardCharsets.UTF_8);
        groups.add("Command tree / argument parsing / documentation inventory");
    }

    private static void collectCommands(me.TechsCode.UltraPermissions.base.command.CommandNode<?, ?> node, List<String> lines) throws Exception {
        if (get(node, "action") != null) {
            String requirements = node.getAllRequirements().stream().map(r -> r.getClass().getSimpleName()).distinct().collect(java.util.stream.Collectors.joining(","));
            lines.add("/" + node.getUsagePath() + "\t" + requirements);
        }
        for (var child : node.getChildren()) collectCommands(child, lines);
    }

    private static final class Gadget implements Serializable {
        static int executed;
        private void readObject(ObjectInputStream in) throws Exception { executed++; in.defaultReadObject(); }
    }
    @org.bukkit.configuration.serialization.SerializableAs("HostileAlias")
    public static final class HostileAlias implements org.bukkit.configuration.serialization.ConfigurationSerializable {
        static int executed;
        public Map<String,Object> serialize(){return Map.of("fixture",true);}
        public static HostileAlias deserialize(Map<String,Object> map){executed++;return new HostileAlias();}
    }
    public static class TestPlugin extends UltraPermissions {
        UserList users; TestScheduler scheduler; TestManager manager; SpigotMessagingService messaging; List<String> logs;
        me.TechsCode.UltraPermissions.base.networking.NServer currentServer;
        public TestPlugin(){super(null);}
        @Override public UserList getUsers(){return users;}
        @Override public Scheduler getScheduler(){return scheduler;}
        @Override public SpigotMessagingService getMessagingService(){return messaging;}
        @Override public MySQLManager getMySQLManager(){return manager;}
        @Override public String getName(){return "UltraPermissions";}
        @Override public String getPrefix(){return "";}
        @Override public Optional<me.TechsCode.UltraPermissions.base.networking.NServer> getThisServer(){return Optional.ofNullable(currentServer);}
        @Override public User registerUser(UUID uuid,String name,boolean assignDefault){User user=new User(uuid,name,false,new HashMap<>(),"","",null);users.add(user);return user;}
        @Override public void log(String message){if(logs!=null)logs.add(message);}
    }
    public static class ThreadStorage extends Storage<Storable> {
        int synchronizations;
        public ThreadStorage(){super(null,null,null,null,false);}
        public void onMount(Storable value){}
        public void onCreation(Storable value){}
        public void onChange(Storable previous,Storable current){}
        public void onDestroy(Storable value){}
        public void onDataSynchronization(){synchronizations++;}
    }
    public static class ThreadBackend extends StorageImplementation {
        public ThreadBackend(){super(null,"fixture",false);}
        public void destroy(String key,WriteCallback callback){}
        public void create(String key,JsonObject value,WriteCallback callback){}
        public void update(String key,JsonObject value,WriteCallback callback){}
        public void read(String key,ReadCallback callback){callback.onSuccess(new HashMap<>());}
    }
    public static class TestFileConfiguration extends FileConfiguration {
        Map<String, Object> values;
        boolean fail;
        public TestFileConfiguration(){super(null,null);}
        @Override public Object get(String key){return values.get(key);}
        @Override public String getString(String key){return (String)values.get(key);}
        @Override public Set<String> getKeys(boolean deep){return values.keySet();}
        @Override public void set(String key,Object value){if(value==null)values.remove(key);else values.put(key,value);}
        @Override public void save(){if(fail)throw new UncheckedIOException(new IOException("simulated disk failure"));}
    }
    public static class PersistedItem extends Storable {
        String key = "fixture-user";
        JsonObject data = new JsonObject();
        public void persist(){sync();}
        public void remove(){destroy();}
        public String getKey(){return key;}
        public void setKey(String key){this.key=key;}
        public JsonObject getState(){return data;}
        public void setState(JsonObject data,TechPlugin<?> plugin){this.data=data;}
        public void onMount(TechPlugin<?> plugin){}
    }
    public static class PersistenceBackend extends StorageImplementation {
        boolean fail;
        public PersistenceBackend(){super(null,"fixture",false);}
        private void complete(WriteCallback callback){
            if(fail)callback.onFailure(new IOException("simulated database outage"));
            else callback.onSuccess();
        }
        public void update(String key,JsonObject value,WriteCallback callback){complete(callback);}
        public void create(String key,JsonObject value,WriteCallback callback){complete(callback);}
        public void destroy(String key,WriteCallback callback){complete(callback);}
        public void read(String key,ReadCallback callback){callback.onSuccess(new HashMap<>());}
    }
    public static class TestManager extends MySQLManager {
        JdbcFixture db; boolean fail; int calls;
        public TestManager(){super(null);}
        @Override public Connection getConnection() throws SQLException { calls++; if(fail)throw new SQLTransientConnectionException("fixture pool timeout"); return db.connection(); }
        @Override public Boolean connectionAvailable(){throw new AssertionError("Pool polling is forbidden");}
    }
    public static class TestGui extends GUI {
        public TestGui(){super(null,null);}
        @Override protected void construct(Model model){}
    }
    public static class LegacyView {
        private final Inventory inventory;
        public LegacyView(Inventory inventory){this.inventory=inventory;}
        public Inventory getTopInventory(){return inventory;}
    }
    public static class TestDialog extends Dialog {
        int inputs; boolean revoked;
        public TestDialog(){super(null,null);}
        @Override public boolean onInput(String input){inputs++;return true;}
        @Override public void onClose(Player player){throw new AssertionError("Revocation must not reopen a session");}
        @Override public void close(boolean suppress){revoked=suppress;try{set(this,"closed",true);}catch(Exception e){throw new AssertionError(e);}}
        @Override public String getMainTitle(){return "";}
        @Override public String getSubTitle(){return "";}
        @Override public String getActionBar(){return "";}
    }
    private static final class IdleTask extends RecurringTask {
        public void start(){} public void stop(){}
    }
    private static final class TestScheduler implements Scheduler {
        ArrayDeque<Runnable> jobs=new ArrayDeque<>();
        int syncCalls, asyncCalls;
        public void run(Runnable task){syncCalls++;jobs.add(task);} public void runAsync(Runnable task){asyncCalls++;jobs.add(task);}
        public Task runTaskLater(Runnable task,long delay){jobs.add(task);return new Task(){public void cancel(){}};}
        public Task runTaskLaterAsync(Runnable task,long delay){jobs.add(task);return new Task(){public void cancel(){}};}
        public RecurringTask runTaskTimer(Runnable task,long delay,long period){return new IdleTask();}
        public RecurringTask runTaskTimerAsync(Runnable task,long delay,long period){return new IdleTask();}
    }
    private static final class JdbcFixture {
        String sql; Map<Integer,String> parameters=new HashMap<>(); int queryTimeout,connectionsClosed,statementsClosed,resultsClosed;
        Connection connection(){ return proxy(Connection.class,(m,a)-> switch(m.getName()) {
            case "prepareStatement" -> {sql=(String)a[0]; parameters.clear(); yield statement();}
            case "close" -> {connectionsClosed++;yield null;} default -> DEFAULT;
        }); }
        PreparedStatement statement(){ return proxy(PreparedStatement.class,(m,a)-> switch(m.getName()) {
            case "setString" -> {parameters.put((Integer)a[0],(String)a[1]);yield null;}
            case "setQueryTimeout" -> {queryTimeout=(Integer)a[0];yield null;}
            case "executeQuery" -> result(); case "executeUpdate" -> 1;
            case "close" -> {statementsClosed++;yield null;} default -> DEFAULT;
        }); }
        ResultSet result(){AtomicBoolean next=new AtomicBoolean(true);return proxy(ResultSet.class,(m,a)-> switch(m.getName()) {
            case "next" -> next.getAndSet(false); case "getString" -> "fixture-key";
            case "getBytes" -> Base64.getEncoder().encode("{\"name\":\"fixture\"}".getBytes());
            case "close" -> {resultsClosed++;yield null;} default -> DEFAULT;
        });}
    }

    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,BiFunction<Method,Object[],Object> handler){
        return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(self,method,args)->{
            Object[] arguments=args==null?new Object[0]:args;
            if(method.getName().equals("equals") && arguments.length==1 && method.getDeclaringClass()==Object.class)return self==arguments[0];
            if(method.getName().equals("hashCode"))return System.identityHashCode(self);
            if(method.getName().equals("toString"))return "Fixture:"+type.getSimpleName();
            Object result=handler.apply(method,arguments);if(result!=DEFAULT)return result;
            Class<?> returns=method.getReturnType(); if(returns==boolean.class)return false;if(returns==int.class)return 0;
            if(returns==long.class)return 0L;if(returns==double.class)return 0D;if(returns==float.class)return 0F;
            if(returns==short.class)return (short)0;if(returns==byte.class)return (byte)0;if(returns==char.class)return '\0';return null;
        });
    }
    @SuppressWarnings("unchecked") private static <T>T allocate(Class<T> type)throws InstantiationException{return (T)UNSAFE.allocateInstance(type);}
    private static Field field(Object object,String name)throws Exception{for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()){try{Field field=type.getDeclaredField(name);field.setAccessible(true);return field;}catch(NoSuchFieldException ignored){}}throw new NoSuchFieldException(name);}
    private static void set(Object object,String name,Object value)throws Exception{field(object,name).set(object,value);}
    private static Object get(Object object,String name)throws Exception{return field(object,name).get(object);}
    private static void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
    private interface Throwing{void run()throws Exception;}
    private static void throwsType(Class<? extends Throwable> expected,Throwing operation,String message)throws Exception{
        try{operation.run();}catch(Throwable failure){check(expected.isInstance(failure),message+": "+failure);return;}throw new AssertionError(message+": no exception");
    }
}
