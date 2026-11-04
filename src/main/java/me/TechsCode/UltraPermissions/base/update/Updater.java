package me.TechsCode.UltraPermissions.base.update;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.loader.SpigotLoader;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Updater {
   private static final Map<UUID, String> notifiedVersions = new ConcurrentHashMap<>();
   private static final AtomicReference<Attempt> checking = new AtomicReference<>();
   private static final AtomicLong generation = new AtomicLong();
   private static volatile long lastStarted;
   private static volatile Status lastStatus;
   private static final long CHECK_GAP_MS = 60_000L;
   private static final Phrase MANUAL_INSTALL = Phrase.create(
      "Updater.manualInstall", "Download the verified update from **GitHub Releases**:", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase RESTART_MANUALLY = Phrase.create(
      "Updater.restartManually", "Replace the plugin JAR and **restart** your server", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase NO_UPDATE = Phrase.create(
      "Updater.noUpdate", "No plugin updates are **available** right now.", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase AVAILABLE_NOTICE = Phrase.create(
      "Updater.available", "Plugin update **%version%** is available on GitHub Releases.", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase STAGED_NOTICE = Phrase.create(
      "Updater.staged", "Update **%version%** downloaded. Restart the server to install it.", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase CHECK_FAILED = Phrase.create(
      "Updater.checkFailed", "Could not check **GitHub** for plugin updates.", Colors.GRAY, Colors.RED
   );
   private static final Phrase DOWNLOAD_FAILED = Phrase.create(
      "Updater.downloadFailed", "Update **download or verification** failed. Check the server console.", Colors.GRAY, Colors.RED
   );
   private static final Phrase STAGING_CONFLICT = Phrase.create(
      "Updater.stagingConflict", "Another update is already **staged**. Check the server console.", Colors.GRAY, Colors.RED
   );

   public enum State {
      NOT_CHECKED, CHECKING, DOWNLOADING, UP_TO_DATE, AVAILABLE, CHECK_FAILED,
      DOWNLOAD_FAILED, STAGING_CONFLICT, STAGED, ALREADY_STAGED, CANCELLED, NO_RELEASE
   }

   /** Immutable, read-only GUI snapshot; obtaining it never performs I/O. */
   public record UpdateStatus(State state, String version) {}

   private static final class Attempt {
      private volatile UpdateStatus status = new UpdateStatus(State.CHECKING, null);
   }

   private static final class Status {
      private final State state;
      private final String version;
      private final int httpStatus;
      private final String detail;

      private Status(State state, String version, int httpStatus) {
         this(state, version, httpStatus, "");
      }

      private Status(State state, String version, int httpStatus, String detail) {
         this.state = state;
         this.version = version;
         this.httpStatus = httpStatus;
         this.detail = detail;
      }

      private String noticeKey() { return state.name() + ":" + (version == null ? "" : version) + ":" + httpStatus + ":" + detail; }
   }

   /**
    * Compatibility entrypoint: the updater no longer opens a confirmation GUI.
    * A joining superadmin receives deduplicated status feedback if chat notices are enabled.
    */
   public static void suggestUpdateIfAvailable(SpigotTechPlugin plugin, Player player, Callback<Player> onDecline, boolean force) {
      if (!plugin.getAppearanceRegistry().isUpdaterEnabled()
         && !plugin.getAppearanceRegistry().isUpdaterNotificationsEnabled()) return;
      if (isSuperadmin(plugin, player)) notifyStatus(plugin, player, lastStatus, force);
      checkForUpdates(plugin);
   }

   /**
    * Called synchronously at startup and every quarter-hour. Fetching, digest
    * verification and all file operations run on the asynchronous scheduler.
    */
   public static void checkForUpdates(SpigotTechPlugin plugin) {
      checkForUpdates(plugin, false);
   }

   /** Explicit GUI entry may inspect metadata with both automatic preferences off. */
   public static void refreshStatus(SpigotTechPlugin plugin) {
      checkForUpdates(plugin, true);
   }

   public static UpdateStatus getStatus() {
      Attempt attempt = checking.get();
      if (attempt != null) return attempt.status;
      Status completed = lastStatus;
      return completed == null ? new UpdateStatus(State.NOT_CHECKED, null)
         : new UpdateStatus(completed.state, completed.version);
   }

   private static void checkForUpdates(SpigotTechPlugin plugin, boolean requestedFromGui) {
      final boolean autoDownload = plugin.getAppearanceRegistry().isUpdaterEnabled();
      if (!requestedFromGui && !autoDownload && !plugin.getAppearanceRegistry().isUpdaterNotificationsEnabled()) return;
      long now = System.currentTimeMillis();
      if (now >= lastStarted && now - lastStarted < CHECK_GAP_MS) return;
      Attempt attempt = new Attempt();
      if (!checking.compareAndSet(null, attempt)) return;
      lastStarted = now;
      long token = generation.get();
      final String pluginName = plugin.getName();
      final String installedVersion = plugin.getVersion();
      final File currentJar = plugin.getBootstrap() instanceof SpigotLoader loader ? loader.getLoadedPluginFile() : null;
      final File updateFolder = Bukkit.getUpdateFolderFile();
      final File dataFolder = plugin.getPluginFolder();
      BooleanSupplier permitted = () -> generation.get() == token;

      try {
         plugin.getScheduler().runAsync(() -> {
            Status result;
            UpdateServer server = new UpdateServer();
            try {
               IsUpToDateResponse response = server.isUpToDate(pluginName, installedVersion);
               if (response.getStatusCode() == 404) {
                  result = new Status(State.NO_RELEASE, null, 404);
               } else if (!response.isSuccessFull()) {
                  result = new Status(State.CHECK_FAILED, null, response.getStatusCode(), response.getMessage());
               } else if (response.isUpToDate()) {
                  result = new Status(State.UP_TO_DATE, response.getLatestVersion(), 200);
               } else if (!autoDownload) {
                  Status prepared = lastStatus;
                  result = isPrepared(prepared) && response.getLatestVersion().equals(prepared.version)
                     ? prepared : new Status(State.AVAILABLE, response.getLatestVersion(), 200);
               } else if (response.getReleaseAsset() == null) {
                  result = new Status(State.DOWNLOAD_FAILED, response.getLatestVersion(), 200,
                     "Published release has no unambiguous executable JAR with a GitHub SHA-256 digest");
               } else {
                  attempt.status = new UpdateStatus(State.DOWNLOADING, response.getLatestVersion());
                  VerifiedReleaseInstaller.Outcome staged = VerifiedReleaseInstaller.downloadAndStageDetailed(
                     response.getReleaseAsset(), response.getLatestVersion(), installedVersion, currentJar,
                     updateFolder, dataFolder, permitted
                  );
                  State state = switch (staged.result()) {
                     case STAGED -> State.STAGED;
                     case ALREADY_STAGED -> State.ALREADY_STAGED;
                     case CONFLICT -> State.STAGING_CONFLICT;
                     case CANCELLED -> State.CANCELLED;
                     case FAILED -> State.DOWNLOAD_FAILED;
                  };
                  result = new Status(state, response.getLatestVersion(), 200, staged.detail());
               }
            } catch (RuntimeException failed) {
               result = new Status(State.CHECK_FAILED, null, 503,
                  "Unexpected updater error (" + failed.getClass().getSimpleName() + ")");
            } finally {
               server.destroy();
            }
            if (!permitted.getAsBoolean()) {
               checking.compareAndSet(attempt, null);
               return;
            }
            Status completed = result;
            try {
               plugin.getScheduler().run(() -> {
                  try {
                     if (!permitted.getAsBoolean() || (!requestedFromGui && !plugin.getAppearanceRegistry().isUpdaterEnabled()
                        && !plugin.getAppearanceRegistry().isUpdaterNotificationsEnabled())) return;
                     Status previous = lastStatus;
                     lastStatus = completed;
                     if (previous == null || !previous.noticeKey().equals(completed.noticeKey())) {
                        switch (completed.state) {
                           case STAGED -> plugin.log("Verified GitHub Release " + completed.version
                              + " staged in the Bukkit update folder; restart manually to apply it.");
                           case DOWNLOAD_FAILED -> plugin.log("Automatic update download/verification failed: " + completed.detail);
                           case STAGING_CONFLICT -> plugin.log("Automatic update staging conflict: " + completed.detail);
                           case CHECK_FAILED -> plugin.log("Could not check GitHub Releases (HTTP " + completed.httpStatus + "). " + completed.detail);
                           default -> {}
                        }
                     }
                     for (Player online : Bukkit.getOnlinePlayers()) {
                        if (isSuperadmin(plugin, online)) notifyStatus(plugin, online, completed, false);
                     }
                  } finally {
                     checking.compareAndSet(attempt, null);
                  }
               });
            } catch (RuntimeException disabled) {
               // Bukkit rejects scheduling tasks after plugin disable; do not notify stale sessions.
               checking.compareAndSet(attempt, null);
            }
         });
      } catch (RuntimeException schedulingFailed) {
         if (permitted.getAsBoolean()) {
            lastStatus = new Status(State.CHECK_FAILED, null, 503, "Could not schedule the asynchronous update check");
         }
         checking.compareAndSet(attempt, null);
      }
   }

   private static boolean isSuperadmin(SpigotTechPlugin plugin, Player player) {
      return plugin instanceof UltraPermissions permissions
         && permissions.getUsers().uuid(player.getUniqueId()).filter(User::isSuperadmin).isPresent();
   }

   private static void notifyStatus(SpigotTechPlugin plugin, Player player, Status status, boolean force) {
      if (status == null || !player.isOnline()
         || !plugin.getAppearanceRegistry().isUpdaterNotificationsEnabled()) return;
      Phrase message = switch (status.state) {
         case UP_TO_DATE, NO_RELEASE -> NO_UPDATE;
         case AVAILABLE -> AVAILABLE_NOTICE;
         case STAGED, ALREADY_STAGED -> STAGED_NOTICE;
         case CHECK_FAILED -> CHECK_FAILED;
         case DOWNLOAD_FAILED -> DOWNLOAD_FAILED;
         case STAGING_CONFLICT -> STAGING_CONFLICT;
         default -> null;
      };
      if (message != null && markNotified(player.getUniqueId(), status.noticeKey(), force)) {
         player.sendMessage(plugin.getPrefix() + message.get().replace("%version%", status.version == null ? "" : status.version));
         if (status.state == State.AVAILABLE) player.sendMessage(Colors.YELLOW + UpdateServer.RELEASES_URL);
      }
   }

   /** Retained for integrations using the older manual-update workflow. */
   public static void sendManualInstallInstructions(SpigotTechPlugin plugin, Player player, String releaseUrl) {
      player.sendMessage(plugin.getPrefix() + MANUAL_INSTALL.get());
      player.sendMessage(Colors.YELLOW + (releaseUrl == null || !releaseUrl.startsWith(UpdateServer.RELEASES_URL + "/tag/")
         ? UpdateServer.RELEASES_URL : releaseUrl));
      player.sendMessage(plugin.getPrefix() + RESTART_MANUALLY.get());
   }

   private static boolean markNotified(UUID player, String state, boolean force) {
      if (state == null || state.isEmpty()) return false;
      String previous = notifiedVersions.put(player, state);
      return force || !state.equals(previous);
   }

   /** Called on shutdown or when either updater preference changes. */
   public static void clearNotifications() {
      generation.incrementAndGet(); // In-flight downloads must not stage a JAR after disable.
      notifiedVersions.clear();
      lastStatus = null;
      lastStarted = 0L;
      checking.set(null);
   }

   private static boolean isPrepared(Status status) {
      return status != null && (status.state == State.STAGED || status.state == State.ALREADY_STAGED);
   }

   /** Keep a verified, prepared release visible when its preference is switched off. */
   public static void preferencesChanged(SpigotTechPlugin plugin) {
      Status prepared = lastStatus;
      clearNotifications();
      if (isPrepared(prepared)) lastStatus = prepared;
      checkForUpdates(plugin);
   }

   public static String getOSSystem() {
      return System.getProperty("os.name");
   }
}
