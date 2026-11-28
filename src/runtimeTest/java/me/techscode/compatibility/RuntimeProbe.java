package me.techscode.compatibility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.TechsCode.UltraPermissions.base.translations.TranslationManager;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.fileconf.impl.SpigotFileConfiguration;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.item.CustomItem;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.base.views.settings.CustomisationPane;
import me.TechsCode.UltraPermissions.base.views.settings.UpdateSettingsView;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import me.TechsCode.UltraPermissions.base.reflection.utility.ReflectionUtil;
import me.TechsCode.UltraPermissions.base.reflection.utility.MinecraftRelease;
import me.TechsCode.UltraPermissions.base.security.SafeItems;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

/** Test-server companion only. Never install on a production server. */
public final class RuntimeProbe extends JavaPlugin {
    private UltraPermissions permissions;
    private final AtomicInteger assertions = new AtomicInteger();
    private boolean failed;
    private boolean distinctPlayerRegions;

    @Override public void onEnable() {
        if (Boolean.getBoolean("ultrapermissions.performanceProbe")) {
            new PerformanceLoad(this).start();
            return;
        }
        permissions = (UltraPermissions) TechPlugin.getInstance();
        if (permissions == null) throw new IllegalStateException("UltraPermissions did not initialize");
        boolean secondBoot = Files.exists(getDataFolder().toPath().resolve("phase-one.marker"));
        permissions.getScheduler().runTaskLater(() -> run(() -> {
            check(permissions.getPluginEvents() != null, "plugin initialized on the real server");
            check(Bukkit.getPluginManager().isPluginEnabled("UltraPermissions"), "plugin enabled on real server");
            if (secondBoot) {
                verifyPersistenceAfterRestart();
                if (permissions.isFolia()) verifyFoliaPlayerPersistence();
                verifyLanguages();
                check(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "upc ListGroups"),
                    "console command works after plugin reload on a fresh JVM");
                report("passed", 2);
            } else {
                corruptYamlRecovery();
                var release = MinecraftRelease.parse(Bukkit.getBukkitVersion());
                check(XMaterial.getVersion() == release.feature(), "material mapping matches the actual server release");
                check(ReflectionUtil.MINOR_VERSION == release.feature() && ReflectionUtil.PATCH_VERSION == release.patch(),
                    "reflection mapping matches the release without interpreting a Paper build as a patch");
                CustomItem item = new CustomItem(Material.DIAMOND).name("Compatibility probe").lore("metadata round trip");
                var source = item.get().clone();
                var sourceMeta = source.getItemMeta();
                sourceMeta.setUnbreakable(true);
                source.setItemMeta(sourceMeta);
                // Test the real inventory representation. Paper 1.20.6 intentionally
                // discards flags for absent components when a Bukkit item becomes native.
                var inventory = Bukkit.createInventory(null, 27, "Compatibility probe");
                inventory.setItem(0, source);
                var runtimeItem = inventory.getItem(0).clone();
                var runtimeMeta = runtimeItem.getItemMeta();
                check(runtimeItem.getType() == Material.DIAMOND && runtimeItem.getAmount() == source.getAmount()
                        && runtimeMeta.getDisplayName().equals(sourceMeta.getDisplayName())
                        && runtimeMeta.getLore().equals(sourceMeta.getLore())
                        && runtimeMeta.isUnbreakable()
                        && runtimeMeta.hasItemFlag(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE),
                    "actual inventory preserves the icon, text, lore and a flag for a present component");
                item = new CustomItem(runtimeItem, false);
                var decoded = SafeItems.decode(SafeItems.encode(item.get()));
                check(decoded.isSimilar(item.get()), "real item metadata codec; expected=" + item.get().serialize()
                    + "; actual=" + decoded.serialize());
                check(CustomItem.deserialize(item.serialize()).get().isSimilar(item.get()), "GUI item codec");
                check(inventory.getSize() == 27, "inventory creation");
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
                if (permissions.isFolia()) {
                    waitForFoliaPlayers(System.nanoTime() + TimeUnit.SECONDS.toNanos(90));
                    return;
                }
                finishFirstBoot();
            }
            Bukkit.shutdown();
        }), 80L);
    }

    private void waitForFoliaPlayers(long deadline) throws Exception {
        Player first = Bukkit.getPlayerExact(OfflineProbeClient.FIRST_PLAYER);
        Player second = Bukkit.getPlayerExact(OfflineProbeClient.SECOND_PLAYER);
        if (first == null || second == null || permissions.getUsers().uuid(first.getUniqueId()).isEmpty()
                || permissions.getUsers().uuid(second.getUniqueId()).isEmpty()) {
            if (System.nanoTime() >= deadline) throw new AssertionError("Both loopback clients did not join in time");
            permissions.getScheduler().runTaskLater(() -> run(() -> waitForFoliaPlayers(deadline)), 10L);
            return;
        }
        check(permissions.getUsers().uuid(first.getUniqueId()).isPresent()
            && permissions.getUsers().uuid(second.getUniqueId()).isPresent(), "both clients joined through the player lifecycle");
        permissions.getUsers().uuid(first.getUniqueId()).orElseThrow().setSuperadmin(true);
        permissions.getUsers().uuid(second.getUniqueId()).orElseThrow().setSuperadmin(true);

        CompletableFuture<Void> asynchronous = new CompletableFuture<>();
        permissions.getScheduler().runAsync(() -> {
            try {
                check(!Bukkit.isPrimaryThread(), "background work runs on Folia's asynchronous scheduler");
                asynchronous.complete(null);
            } catch (Throwable failure) {
                asynchronous.completeExceptionally(failure);
            }
        });

        onPlayer(second, 1L, () -> {
            var location = new Location(second.getWorld(), 2048.5, second.getLocation().getY(), 2048.5);
            return Entity.class.getMethod("teleportAsync", Location.class).invoke(second, location);
        }).thenCompose(teleport -> (CompletableFuture<?>) teleport).thenCompose(success -> {
            check(Boolean.TRUE.equals(success), "second client teleported to an independent region");
            return CompletableFuture.allOf(verifyFoliaViewer(first, second), verifyFoliaViewer(second, first), asynchronous);
        }).thenCompose(ignored -> verifyFoliaDialog(first)).whenComplete((ignored, error) ->
            permissions.getScheduler().run(() -> run(() -> {
                if (error != null) throw new AssertionError("Live Folia verification failed", error);
                verifyFoliaPlayerPersistence();
                distinctPlayerRegions = true;
                finishFirstBoot();
                Bukkit.shutdown();
            })));
    }

    private CompletableFuture<Void> verifyFoliaViewer(Player player, Player other) {
        return onPlayer(player, 1L, () -> {
            var scheduler = permissions.getSpigotScheduler();
            check(scheduler.owns(player) && !scheduler.owns(other), player.getName() + " owns only its own region");
            User user = permissions.getUsers().uuid(player.getUniqueId()).orElseThrow();
            Group group = permissions.getGroups().name("compatibility_probe").orElseThrow();
            user.addGroup(group);
            for (int i = 0; i < 8; i++) user.newPermission("compatibility.folia.grant." + i).create();
            user.newPermission("compatibility.folia.denied").setPositive(false).create();
            user.newPermission("compatibility.folia.world").setWorld(player.getWorld().getName()).create();
            user.newPermission("compatibility.folia.otherworld").setWorld("nonexistent_probe_world").create();
            check(player.hasPermission("compatibility.folia.grant.0") && player.hasPermission("compatibility.allowed"),
                "live player permissions include direct and inherited grants");
            check(!player.hasPermission("compatibility.folia.denied"), "live player explicit denial");
            check(player.hasPermission("compatibility.folia.world") && !player.hasPermission("compatibility.folia.otherworld"),
                "live player permissions respect world scope");
            player.addAttachment(this, "compatibility.folia.attachment", true, 16);
            check(player.hasPermission("compatibility.folia.attachment"), "live player timed attachment applies");
            check(Bukkit.dispatchCommand(player, "uperms"), "live player can open UltraPermissions via command");
            return null;
        }).thenCompose(ignored -> onPlayer(player, 8L, () -> {
            var view = player.getOpenInventory();
            var top = view.getTopInventory();
            check(top.getSize() == 45 && top.getItem(20) != null && top.getItem(20).getType() == Material.PLAYER_HEAD,
                "overview retains the original size and user icon slot");
            check(view.getTitle().equals("Ultra Permissions v" + permissions.getVersion()), "overview retains its title format");
            var click = new InventoryClickEvent(view, SlotType.CONTAINER, 24, ClickType.LEFT, InventoryAction.PICKUP_ALL);
            Bukkit.getPluginManager().callEvent(click);
            check(click.isCancelled(), "group menu action protects the source inventory");
            return null;
        })).thenCompose(ignored -> this.<Void>onPlayer(player, 18L, () -> {
            var top = player.getOpenInventory().getTopInventory();
            check(top.getSize() == 54 && top.getItem(52) != null, "navigation opens the original Groups inventory");
            check(!player.hasPermission("compatibility.folia.attachment"), "timed attachment expires on the owning region");
            player.closeInventory();
            return null;
        }));
    }

    private CompletableFuture<Void> verifyFoliaDialog(Player player) {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        return onPlayer(player, 1L, () -> {
            new UserInput(player, permissions, "Compatibility", "Chat input") {
                @Override public void onClose(Player ignored) {}

                @Override public boolean onResult(String input) {
                    try {
                        check(permissions.getSpigotScheduler().owns(player), "dialog handling returns to the owning region");
                        check("folia-probe-input".equals(input), "dialog preserves the entered text");
                        completed.complete(null);
                    } catch (Throwable failure) {
                        completed.completeExceptionally(failure);
                    }
                    return true;
                }
            };
            permissions.getScheduler().runAsync(() -> {
                try {
                    var event = new AsyncPlayerChatEvent(true, player, "folia-probe-input", Set.of());
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) completed.completeExceptionally(new AssertionError("Dialog input was broadcast"));
                } catch (Throwable failure) {
                    completed.completeExceptionally(failure);
                }
            });
            return null;
        }).thenCompose(ignored -> completed);
    }

    private <T> CompletableFuture<T> onPlayer(Player player, long delay, CheckedSupplier<T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();
        permissions.getSpigotScheduler().runForLater(player, this, () -> {
            try {
                future.complete(action.get());
            } catch (Throwable failure) {
                future.completeExceptionally(failure);
            }
        }, () -> future.completeExceptionally(new IllegalStateException("Test player disconnected")), delay);
        return future;
    }

    private void verifyFoliaPlayerPersistence() {
        var ids = new java.util.HashSet<Integer>();
        for (String name : List.of(OfflineProbeClient.FIRST_PLAYER, OfflineProbeClient.SECOND_PLAYER)) {
            User user = permissions.getUsers().name(name).orElseThrow();
            var checks = user.getPermissions().stream().filter(permission -> permission.getName().startsWith("compatibility.folia.grant.")).toList();
            check(checks.size() == 8, "all concurrent grants present for " + name);
            for (var permission : checks) ids.add(permission.getId());
        }
        check(ids.size() == 16, "concurrent grants have distinct storage IDs");
    }

    private void finishFirstBoot() throws Exception {
        report("passed", 1);
        Files.writeString(getDataFolder().toPath().resolve("phase-one.marker"), "phase 1 passed\n");
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
        SkullMeta fresh = (SkullMeta) new CustomItem(new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD), false)
            .setSkullTexture(firstTexture).get().getItemMeta();
        check(fresh.getOwnerProfile() != null && fresh.getOwnerProfile().getTextures().getSkin().equals(new URL(firstSkin)),
            "a fresh head receives its texture even without existing item metadata");
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
        if (!permissions.isFolia()) {
            check(permissible.hasPermission("compatibility.direct"), "permission grant with real storage");
            check(!permissible.hasPermission("compatibility.denied"), "permission denial with real storage");
        }
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
        assertions.incrementAndGet();
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
            + ",\"platform\":\"" + (permissions.isFolia() ? "Folia" : "Paper")
            + "\",\"distinct_player_regions\":" + distinctPlayerRegions
            + ",\"server\":\"" + Bukkit.getBukkitVersion() + "\",\"java\":\"" + System.getProperty("java.version") + "\"}\n";
        Files.writeString(getDataFolder().toPath().resolve("verification.json"), json);
        getLogger().info("RUNTIME_PROBE " + status + " (" + assertions + " assertions)");
    }

    private interface CheckedSupplier<T> { T get() throws Exception; }

    private interface Checked { void run() throws Exception; }
}
