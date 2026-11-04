package me.techscode.compatibility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import me.TechsCode.UltraPermissions.base.translations.TranslationManager;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.fileconf.impl.SpigotFileConfiguration;
import me.TechsCode.UltraPermissions.base.item.CustomItem;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.base.views.settings.CustomisationPane;
import me.TechsCode.UltraPermissions.base.views.settings.UpdateSettingsView;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import me.TechsCode.UltraPermissions.base.reflection.utility.ReflectionUtil;
import me.TechsCode.UltraPermissions.base.security.SafeItems;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
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
                verifyStablePlayerHeads();
                verifyUpdateSettings();
                User user = permissions.registerUser(UUID.nameUUIDFromBytes("runtime-probe".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "StorageProbe", false);
                Group group = permissions.getGroupStorage().newGroup("compatibility_probe").create();
                user.addGroup(group);
                user.setPrefix("Admin");
                user.setPrefix("admin");
                check("admin".equals(user.getPrefix().orElse(null)), "case-only prefix change applied");
                group.newPermission("compatibility.allowed").create();
                check(user.getActiveGroups().contains(group), "group membership");
                check(!user.getAdditionalPermissions().isEmpty(), "inherited permission storage");
                check(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "upc ListGroups"), "console command dispatch");
                playerPermissions();
                rankPathIsolation();
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
        check("admin".equals(persisted.getPrefix().orElse(null)), "case-only prefix change persisted across restart");
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

    private void verifyStablePlayerHeads() throws Exception {
        String firstSkin = "https://textures.minecraft.net/texture/b3fbd454b599df593f57101bfca34e67d292a8861213d2202bb575da7fd091ac";
        String secondSkin = "https://textures.minecraft.net/texture/b3fbd454b599df593f57101bfca34e67d292a8861213d2202bb575da7fd091ad";
        SkinTexture firstTexture = new SkinTexture(firstSkin, System.currentTimeMillis());
        SkullMeta first = (SkullMeta) new CustomItem(Material.PLAYER_HEAD).setSkullTexture(firstTexture).get().getItemMeta();
        SkullMeta redrawn = (SkullMeta) new CustomItem(Material.PLAYER_HEAD).setSkullTexture(firstTexture).get().getItemMeta();
        SkullMeta changed = (SkullMeta) new CustomItem(Material.PLAYER_HEAD)
            .setSkullTexture(new SkinTexture(secondSkin, System.currentTimeMillis())).get().getItemMeta();
        check(first.getOwnerProfile() != null && redrawn.getOwnerProfile() != null && changed.getOwnerProfile() != null,
            "player-head profiles are applied through the runtime skull API");
        check(first.getOwnerProfile().getUniqueId().equals(redrawn.getOwnerProfile().getUniqueId()),
            "identical heads retain one profile identity across GUI redraws");
        check(first.getOwnerProfile().getTextures().getSkin().equals(new URL(firstSkin)),
            "runtime skull metadata preserves the intended skin URL");
        check(!first.getOwnerProfile().getUniqueId().equals(changed.getOwnerProfile().getUniqueId())
                && changed.getOwnerProfile().getTextures().getSkin().equals(new URL(secondSkin)),
            "changed skin URL produces a distinct profile with the new texture");
    }

    private void rankPathIsolation() {
        Group low = permissions.getGroupStorage().newGroup("probe_low").create();
        Group high = permissions.getGroupStorage().newGroup("probe_high").create();
        Group other = permissions.getGroupStorage().newGroup("probe_other").create();
        var first = new me.TechsCode.UltraPermissions.storage.objects.GroupPath("ProbePathA");
        first.setGroupPath(new java.util.ArrayList<>(List.of(low, high)));
        permissions.getGroupPathStorage().create(first);
        var second = new me.TechsCode.UltraPermissions.storage.objects.GroupPath("ProbePathB");
        second.setGroupPath(new java.util.ArrayList<>(List.of(other)));
        permissions.getGroupPathStorage().create(second);
        User alice = permissions.registerUser(UUID.nameUUIDFromBytes("rank-audit-alice".getBytes()), "RankAlice", false);
        User bob = permissions.registerUser(UUID.nameUUIDFromBytes("rank-audit-bob".getBytes()), "RankBob", false);
        alice.addGroup(low); bob.addGroup(low);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "promote RankAlice ProbePathA");
        check(alice.getActiveGroups().contains(high), "valid promote on real command path");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "promote RankBob ProbePathB");
        check(bob.getActiveGroups().contains(low) && !bob.getActiveGroups().contains(other), "cross-user/path promote rejected");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "demote RankAlice ProbePathA");
        check(alice.getActiveGroups().contains(low), "valid demote on real command path");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "demote RankBob ProbePathB");
        check(bob.getActiveGroups().contains(low) && !bob.getActiveGroups().contains(other), "cross-user/path demote rejected");
    }

    private void verifyUpdateSettings() throws Exception {
        Model parent = new Model();
        new CustomisationPane(null, null, permissions).construct(parent);
        check(parent.getEntries().keySet().equals(java.util.Set.of(20, 23, 26)),
            "customisation keeps the unrelated settings and exposes one updates entry");
        java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        UpdateViewFixture view = (UpdateViewFixture) unsafe.allocateInstance(UpdateViewFixture.class);
        java.lang.reflect.Field pluginField = UpdateSettingsView.class.getDeclaredField("plugin");
        pluginField.setAccessible(true);
        pluginField.set(view, permissions);
        Model model = new Model();
        view.construct(model);
        check(view.getCurrentSlots() == 45 && model.getEntries().keySet().equals(java.util.Set.of(12, 14, 16, 41))
                && Bukkit.createInventory(null, view.getCurrentSlots(), view.getCurrentTitle()).getSize() == 45,
            "updates subview has five rows, unchanged content slots and Back at the bottom center");
        Button notices = new Button(), downloads = new Button();
        model.getEntries().get(12).button(notices);
        model.getEntries().get(14).button(downloads);
        check(notices.item().get().getType() == Material.PAPER
                && downloads.item().get().getType() == Material.WRITABLE_BOOK,
            "update controls retain their original materials on Paper");

        TranslationManager translations = permissions.getTranslationManager();
        String originalLanguage = translations.getSelectedLanguage();
        Class<?> statusType = Class.forName(Updater.class.getName() + "$Status");
        java.lang.reflect.Constructor<?> status = statusType.getDeclaredConstructor(Updater.State.class, String.class, int.class);
        status.setAccessible(true);
        java.lang.reflect.Field lastStatus = Updater.class.getDeclaredField("lastStatus");
        lastStatus.setAccessible(true);
        Object originalStatus = lastStatus.get(null);
        Updater.clearNotifications();
        try {
            for (String language : List.of("English", "Russian", "Ukrainian")) {
                translations.setSelectedLanguage(language);
                for (Updater.State state : Updater.State.values()) {
                    lastStatus.set(null, status.newInstance(state, "99.0.0", 200));
                    Button info = new Button();
                    model.getEntries().get(16).button(info);
                    var metadata = info.item().get().getItemMeta();
                    String text = org.bukkit.ChatColor.stripColor(metadata.getDisplayName() + " " + String.join(" ", metadata.getLore()));
                    String expected = language.equals("Russian") ? "Статус обновления"
                        : language.equals("Ukrainian") ? "Стан оновлення" : "Update Status";
                    check(info.getActions().isEmpty() && text.contains(expected)
                            && text.contains(permissions.getVersion()) && !text.contains("%version%")
                            && !text.contains("**") && (!language.equals("English") ? !text.contains("Installed version") : true),
                        "real status item metadata, localization and no-action behavior: " + language + "/" + state);
                }
            }
        } finally {
            translations.setSelectedLanguage(originalLanguage);
            Updater.clearNotifications();
            lastStatus.set(null, originalStatus);
        }
    }

    public static final class UpdateViewFixture extends UpdateSettingsView {
        public UpdateViewFixture() { super(null, null); }
        @Override public void construct(Model model) { super.construct(model); }
        @Override public void onBack() {}
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
        check(russian.containsKey("updateSettingsView.status.checkFailed")
                && ukrainian.containsKey("updateSettingsView.status.checkFailed"),
            "new update settings and failure status phrases are registered at runtime");
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
