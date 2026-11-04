package me.techscode.security;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.AppearanceRegistry;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorageEntry;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import me.TechsCode.UltraPermissions.base.scheduler.Scheduler;
import me.TechsCode.UltraPermissions.base.scheduler.Task;
import me.TechsCode.UltraPermissions.base.update.UpdateServer;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.base.views.settings.CustomisationPane;
import me.TechsCode.UltraPermissions.base.views.settings.UpdateSettingsView;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import org.bukkit.Material;

import static me.techscode.security.AuditRegressionChecks.alloc;
import static me.techscode.security.AuditRegressionChecks.set;

/** Offline production updater/GUI paths; metadata is cached, sockets are forbidden. */
final class UpdateSettingsChecks {
    private static int assertions;

    static int run() throws Exception {
        Field cache = UpdateServer.class.getDeclaredField("cached");
        cache.setAccessible(true);
        Object previousCache = cache.get(null);
        try {
            inspectWithoutEnablingPreferences();
            statusResults();
            cancellationAndStaging();
            menuControls();
        } finally {
            Updater.clearNotifications();
            cache.set(null, previousCache);
        }
        return assertions;
    }

    private static void inspectWithoutEnablingPreferences() throws Exception {
        FixturePlugin plugin = fixture(false, false);
        cacheRelease(200, "5.6.17");
        check(Updater.getStatus().state() == Updater.State.NOT_CHECKED, "initial status is unknown, never current");
        Updater.checkForUpdates(plugin);
        check(plugin.scheduler.async.isEmpty(), "both switches off suppress background checks");
        Updater.refreshStatus(plugin);
        check(plugin.scheduler.async.size() == 1 && Updater.getStatus().state() == Updater.State.CHECKING,
            "explicit GUI inspection starts one asynchronous metadata check");
        for (int i = 0; i < 100; i++) Updater.getStatus();
        Updater.refreshStatus(plugin);
        check(plugin.scheduler.async.size() == 1, "redraws and duplicate opens do not issue additional requests");
        plugin.scheduler.async.remove().run();
        check(Updater.getStatus().state() == Updater.State.CHECKING && plugin.scheduler.sync.size() == 1,
            "completion waits for the main server thread");
        Field lastStarted = Updater.class.getDeclaredField("lastStarted");
        lastStarted.setAccessible(true);
        lastStarted.setLong(null, System.currentTimeMillis() - 61_000L);
        Updater.refreshStatus(plugin);
        check(plugin.scheduler.async.isEmpty(), "slow main-thread publication cannot admit an overlapping check");
        plugin.scheduler.sync.remove().run();
        check(Updater.getStatus().state() == Updater.State.AVAILABLE
                && "5.6.17".equals(Updater.getStatus().version()), "read-only inspection reports the newer release");
        check(!plugin.appearance.isUpdaterEnabled() && !plugin.appearance.isUpdaterNotificationsEnabled()
                && plugin.logs.isEmpty(), "inspection does not enable downloads, notices or stage a JAR");
        // A completed check still obeys the existing one-minute request throttle.
        lastStarted.setLong(null, System.currentTimeMillis());
        Updater.refreshStatus(plugin);
        check(plugin.scheduler.async.isEmpty(), "quick menu reopening honors the request throttle");
    }

    private static void statusResults() throws Exception {
        check(inspect(false, false, 200, "5.6.16").state() == Updater.State.UP_TO_DATE,
            "equal installed and published versions are current");
        Updater.UpdateStatus ahead = inspect(false, false, 200, "5.6.15");
        check(ahead.state() == Updater.State.UP_TO_DATE && "5.6.15".equals(ahead.version()),
            "an installed development version retains the actual latest stable release number");
        check(inspect(false, false, 503, null).state() == Updater.State.CHECK_FAILED,
            "connection failure never claims the plugin is current");
        check(inspect(false, false, 429, null).state() == Updater.State.CHECK_FAILED,
            "rate-limit failure remains a check error");
        check(inspect(false, false, 404, null).state() == Updater.State.NO_RELEASE,
            "missing published release is distinct from a successful current-version comparison");
        check(inspect(false, true, 200, "5.6.17").state() == Updater.State.AVAILABLE,
            "chat-only preference never downloads the new version");
        check(inspect(true, false, 200, "5.6.17").state() == Updater.State.DOWNLOAD_FAILED,
            "auto-download without a verified release asset fails closed with notices off");
        check(inspect(true, true, 200, "5.6.17").state() == Updater.State.DOWNLOAD_FAILED,
            "both preferences retain verified-asset enforcement");
        FixturePlugin plugin = fixture(false, false);
        plugin.scheduler.rejectAsync = true;
        Updater.refreshStatus(plugin);
        check(Updater.getStatus().state() == Updater.State.CHECK_FAILED,
            "scheduler rejection releases the in-flight check and reports failure");
    }

    private static void cancellationAndStaging() throws Exception {
        FixturePlugin plugin = fixture(false, false);
        cacheRelease(200, "5.6.17");
        Updater.refreshStatus(plugin);
        Updater.clearNotifications();
        Updater.refreshStatus(plugin);
        plugin.scheduler.async.remove().run();
        check(Updater.getStatus().state() == Updater.State.CHECKING && plugin.scheduler.sync.isEmpty(),
            "cancelled worker cannot publish status or clear the replacement attempt");
        plugin.scheduler.async.remove().run();
        Updater.clearNotifications();
        plugin.scheduler.sync.remove().run();
        check(Updater.getStatus().state() == Updater.State.NOT_CHECKED,
            "shutdown invalidates a queued main-thread status publication");

        Class<?> statusType = Class.forName(Updater.class.getName() + "$Status");
        Constructor<?> status = statusType.getDeclaredConstructor(Updater.State.class, String.class, int.class);
        status.setAccessible(true);
        Field lastStatus = Updater.class.getDeclaredField("lastStatus");
        lastStatus.setAccessible(true);
        lastStatus.set(null, status.newInstance(Updater.State.STAGED, "5.6.17", 200));
        Updater.preferencesChanged(plugin);
        check(Updater.getStatus().state() == Updater.State.STAGED && plugin.scheduler.async.isEmpty(),
            "switching automatic preferences off retains the prepared-restart indicator");
        Updater.refreshStatus(plugin);
        plugin.scheduler.async.remove().run();
        plugin.scheduler.sync.remove().run();
        check(Updater.getStatus().state() == Updater.State.STAGED,
            "metadata-only inspection does not relabel an already prepared release as a new download");
    }

    private static void menuControls() throws Exception {
        FixturePlugin plugin = fixture(false, false);
        Model parent = new Model();
        new CustomisationPane(null, null, plugin).construct(parent);
        check(parent.getEntries().keySet().equals(Set.of(20, 23, 26)),
            "one update entry replaces both controls while unrelated settings keep their slots");
        FixtureView view = alloc(FixtureView.class);
        set(view, "plugin", plugin);
        Model model = new Model();
        view.construct(model);
        check(view.getCurrentSlots() == 27 && model.getEntries().keySet().equals(Set.of(12, 14, 16, 23)),
            "subview has exactly three content items and the standard back button");
        Button notices = button(model, 12), downloads = button(model, 14), info = button(model, 16);
        check(notices.item().get().getType() == Material.PAPER
                && downloads.item().get().getType() == Material.WRITABLE_BOOK,
            "moved controls retain their original icons");
        check(info.getActions().isEmpty(), "status item is informational and has no download action");
        notices.getActions().get(0).onClick(ActionType.LEFT);
        check(plugin.appearance.isUpdaterNotificationsEnabled() && !plugin.appearance.isUpdaterEnabled(),
            "notification button changes only the notification preference");
        downloads.getActions().get(0).onClick(ActionType.LEFT);
        check(plugin.appearance.isUpdaterNotificationsEnabled() && plugin.appearance.isUpdaterEnabled(),
            "download button enables automatic checks immediately and independently");
        button(model, 23).getActions().get(0).onClick(ActionType.LEFT);
        check(view.backs == 1, "standard back action returns to the parent");
    }

    private static Button button(Model model, int slot) {
        Button button = new Button();
        model.getEntries().get(slot).button(button);
        return button;
    }

    private static Updater.UpdateStatus inspect(boolean download, boolean notices, int status, String version) throws Exception {
        FixturePlugin plugin = fixture(download, notices);
        cacheRelease(status, version);
        Updater.refreshStatus(plugin);
        plugin.scheduler.async.remove().run();
        plugin.scheduler.sync.remove().run();
        return Updater.getStatus();
    }

    private static FixturePlugin fixture(boolean download, boolean notices) throws Exception {
        Updater.clearNotifications();
        FixturePlugin plugin = alloc(FixturePlugin.class);
        plugin.scheduler = new QueuedScheduler();
        plugin.logs = new ArrayList<>();
        plugin.appearance = new AppearanceRegistry(plugin);
        var state = plugin.appearance.getState();
        state.addProperty("updaterEnabled", download);
        state.addProperty("updaterNotificationsEnabled", notices);
        plugin.appearance.setState(state);
        plugin.appearance.setStorageEntry(new RegistryStorageEntry("appearance", plugin.appearance.getState()) {
            @Override public void sync() {}
        });
        return plugin;
    }

    private static void cacheRelease(int code, String version) throws Exception {
        Class<?> type = Class.forName(UpdateServer.class.getName() + "$LatestRelease");
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        long now = System.currentTimeMillis();
        Object value = constructor.newInstance(code, version, version == null ? null : "v" + version,
            null, now - 1_000L, now + 3_600_000L, "offline fixture");
        Field cache = UpdateServer.class.getDeclaredField("cached");
        cache.setAccessible(true);
        cache.set(null, value);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        assertions++;
    }

    public static class FixtureView extends UpdateSettingsView {
        int backs;
        public FixtureView() { super(null, null); }
        @Override public void construct(Model model) { super.construct(model); }
        @Override public void onBack() { backs++; }
    }

    public static class FixturePlugin extends UltraPermissions {
        AppearanceRegistry appearance;
        QueuedScheduler scheduler;
        List<String> logs;
        public FixturePlugin() { super(null); }
        @Override public AppearanceRegistry getAppearanceRegistry() { return appearance; }
        @Override public Scheduler getScheduler() { return scheduler; }
        @Override public String getName() { return "UltraPermissions"; }
        @Override public String getVersion() { return "5.6.16"; }
        @Override public File getPluginFolder() { return new File("unused-updater-fixture"); }
        @Override public UserList getUsers() { return new UserList(); }
        @Override public void log(String message) { logs.add(message); }
    }

    private static final class QueuedScheduler implements Scheduler {
        final ArrayDeque<Runnable> sync = new ArrayDeque<>(), async = new ArrayDeque<>();
        boolean rejectAsync;
        @Override public void run(Runnable job) { sync.add(job); }
        @Override public void runAsync(Runnable job) {
            if (rejectAsync) throw new IllegalStateException("Fixture rejects async scheduling");
            async.add(job);
        }
        @Override public Task runTaskLater(Runnable job, long delay) { throw new AssertionError("Unexpected delayed task"); }
        @Override public Task runTaskLaterAsync(Runnable job, long delay) { throw new AssertionError("Unexpected delayed task"); }
        @Override public RecurringTask runTaskTimer(Runnable job, long delay, long period) { throw new AssertionError("Unexpected timer"); }
        @Override public RecurringTask runTaskTimerAsync(Runnable job, long delay, long period) { throw new AssertionError("Unexpected timer"); }
    }
}
