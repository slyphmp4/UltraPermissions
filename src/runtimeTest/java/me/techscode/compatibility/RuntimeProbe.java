package me.techscode.compatibility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import me.TechsCode.UltraPermissions.base.translations.TranslationManager;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.fileconf.impl.SpigotFileConfiguration;
import me.TechsCode.UltraPermissions.base.item.CustomItem;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.reflection.utility.ReflectionUtil;
import me.TechsCode.UltraPermissions.base.security.SafeItems;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** Test-server companion only. Never install on a production server. */
public final class RuntimeProbe extends JavaPlugin {
    private UltraPermissions permissions;
    private int assertions;
    private boolean failed;

    @Override public void onEnable() {
        if (Boolean.getBoolean("ultrapermissions.performanceProbe")) {
            new PerformanceLoad(this).start();
            return;
        }
        boolean secondBoot = Files.exists(getDataFolder().toPath().resolve("phase-one.marker"));
        Bukkit.getScheduler().runTaskLater(this, () -> run(() -> {
            permissions = (UltraPermissions) TechPlugin.getInstance();
            check(permissions != null && permissions.getPluginEvents() != null, "plugin initialized on real Paper server");
            check(Bukkit.getPluginManager().isPluginEnabled("UltraPermissions"), "plugin enabled on real server");
            if (secondBoot) {
                verifyPersistenceAfterRestart();
                verifyLanguages();
                check(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "upc ListGroups"),
                    "console command works after plugin reload on a fresh JVM");
                report("passed", 2);
            } else {
                corruptYamlRecovery();
                check(XMaterial.getVersion() >= 21, "modern material mapping");
                check(ReflectionUtil.MINOR_VERSION >= 21, "modern reflection version");
                CustomItem item = new CustomItem(Material.DIAMOND).name("Compatibility probe").lore("metadata round trip");
                check(SafeItems.decode(SafeItems.encode(item.get())).isSimilar(item.get()), "real item metadata codec");
                check(CustomItem.deserialize(item.serialize()).get().isSimilar(item.get()), "GUI item codec");
                check(Bukkit.createInventory(null, 27, "Compatibility probe").getSize() == 27, "inventory creation");
                User user = permissions.registerUser(UUID.nameUUIDFromBytes("runtime-probe".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "StorageProbe", false);
                Group group = permissions.getGroupStorage().newGroup("compatibility_probe").create();
                user.addGroup(group);
                group.newPermission("compatibility.allowed").create();
                check(user.getActiveGroups().contains(group), "group membership");
                check(!user.getAdditionalPermissions().isEmpty(), "inherited permission storage");
                check(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "upc ListGroups"), "console command dispatch");
                playerPermissions();
                verifyLanguages();
                report("passed", 1);
                Files.createDirectories(getDataFolder().toPath());
                Files.writeString(getDataFolder().toPath().resolve("phase-one.marker"), "phase 1 passed\n");
            }
            Bukkit.shutdown();
        }), 80L);
    }

    private void verifyPersistenceAfterRestart() {
        UUID id = UUID.nameUUIDFromBytes("runtime-probe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        User persisted = permissions.getUsers().uuid(id).orElseThrow(
            () -> new AssertionError("User was not restored after full server restart"));
        Group group = permissions.getGroups().name("compatibility_probe").orElseThrow(
            () -> new AssertionError("Group was not restored after full server restart"));
        check(persisted.getActiveGroups().contains(group), "group membership persisted across full JVM restart");
        check(group.getPermissions().stream().anyMatch(p -> "compatibility.allowed".equals(p.getName())),
            "group permission persisted across server restart");
        check(persisted.getAdditionalPermissions().stream().anyMatch(p -> "compatibility.allowed".equals(p.getName())),
            "inherited permission resolves after restart");
        UUID permissionUser = UUID.nameUUIDFromBytes("permission-probe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        User user = permissions.getUsers().uuid(permissionUser).orElseThrow(
            () -> new AssertionError("Direct-permission user was not restored"));
        check(user.getPermissions().stream().anyMatch(p -> "compatibility.direct".equals(p.getName())),
            "direct permission persisted across restart");
        check(user.getPermissions().stream().anyMatch(p -> "compatibility.denied".equals(p.getName()) && !p.isPositive()),
            "explicit deny persisted across restart");
    }

    private void verifyLanguages() {
        TranslationManager translations = permissions.getTranslationManager();
        List<String> languages = translations.getAvailableLanguages();
        check(languages.contains("Russian") && languages.contains("Ukrainian"),
            "Russian and Ukrainian translations loaded on real server");
        Map<String, String> russian = translations.getPhrases("Russian");
        Map<String, String> ukrainian = translations.getPhrases("Ukrainian");
        check(russian != null && russian.size() >= 800 && ukrainian != null && ukrainian.size() >= 800,
            "both bundled localizations fully populated at runtime");
        check(russian.containsKey("Updater.noUpdate") && ukrainian.containsKey("Updater.noUpdate"),
            "automatic updater status is localized in both languages");
    }

    private void corruptYamlRecovery() throws Exception {
        Path folder = getDataFolder().toPath();
        Files.createDirectories(folder);
        Path malformed = folder.resolve("corrupt-config.yml");
        String original = "permissions: [unterminated";
        Files.writeString(malformed, original, java.nio.charset.StandardCharsets.UTF_8);
        boolean rejected = false;
        try {
            new SpigotFileConfiguration(malformed.toFile());
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(rejected, "corrupt YAML rejected instead of silently loading empty defaults");
        check(original.equals(Files.readString(malformed)), "corrupt YAML preserved for manual recovery");
    }

    private void playerPermissions() throws Exception {
        UUID uuid = UUID.nameUUIDFromBytes("permission-probe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        User user = permissions.registerUser(uuid, "PermissionProbe", false);
        user.newPermission("compatibility.direct").create();
        user.newPermission("compatibility.denied").setPositive(false).create();
        Player player = (Player) java.lang.reflect.Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class}, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getUniqueId" -> uuid;
                case "getName" -> "PermissionProbe";
                case "getWorld" -> Bukkit.getWorlds().get(0);
                case "isOnline" -> true;
                case "isOp" -> false;
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> method.getReturnType() == boolean.class ? false : null;
            };
        });
        ModifiedPermissible permissible = new ModifiedPermissible(player, permissions, permissions.getPermissionLogger());
        check(permissible.hasPermission("compatibility.direct"), "permission grant with real storage");
        check(!permissible.hasPermission("compatibility.denied"), "permission denial with real storage");
        java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        String craftPackage = Bukkit.getServer().getClass().getPackage().getName();
        Player craftPlayer = (Player) unsafe.allocateInstance(Class.forName(craftPackage + ".entity.CraftPlayer"));
        me.TechsCode.UltraPermissions.internal.PermissibleInjector.inject(craftPlayer, permissible);
        java.lang.reflect.Field field = Class.forName(craftPackage + ".entity.CraftHumanEntity").getDeclaredField("perm");
        field.setAccessible(true);
        check(field.get(craftPlayer) == permissible, "actual CraftPlayer permission field injection");
    }

    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        assertions++;
        getLogger().info("PASS: " + message);
    }

    private void run(Checked action) {
        if (failed) return;
        try { action.run(); }
        catch (Throwable error) {
            failed = true;
            getLogger().log(java.util.logging.Level.SEVERE, "Runtime probe failed", error);
            try { report("failed", Files.exists(getDataFolder().toPath().resolve("phase-one.marker")) ? 2 : 1); } catch (Exception ignored) {}
            Bukkit.shutdown();
        }
    }

    private void report(String status, int boot) throws Exception {
        Files.createDirectories(getDataFolder().toPath());
        String json = "{\"status\":\"" + status + "\",\"boot\":" + boot + ",\"assertions\":" + assertions
            + ",\"server\":\"" + Bukkit.getBukkitVersion() + "\",\"java\":\"" + System.getProperty("java.version") + "\"}\n";
        Files.writeString(getDataFolder().toPath().resolve("verification.json"), json);
        getLogger().info("RUNTIME_PROBE " + status + " (" + assertions + " assertions)");
    }

    private interface Checked { void run() throws Exception; }
}
