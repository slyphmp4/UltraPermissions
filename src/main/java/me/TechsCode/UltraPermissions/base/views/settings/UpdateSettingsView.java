package me.TechsCode.UltraPermissions.base.views.settings;

import me.TechsCode.UltraPermissions.base.AppearanceRegistry;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

/** Original TechsCode controls grouped in a small, native editor subview. */
public abstract class UpdateSettingsView extends GUI {
   private static final Phrase TITLE = Phrase.create("updateSettingsView.title", "Settings > Updates");
   private static final Phrase STATUS_TITLE = Phrase.create("updateSettingsView.status.title", "Update Status");
   private static final Phrase INSTALLED_VERSION = Phrase.create(
      "updateSettingsView.status.installed", "Installed version: **%version%**", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase LATEST_VERSION = Phrase.create(
      "updateSettingsView.status.latest", "Latest release: **%version%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase NOT_CHECKED = Phrase.create(
      "updateSettingsView.status.notChecked", "Updates have **not been checked** yet", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase CHECKING = Phrase.create(
      "updateSettingsView.status.checking", "**Checking** GitHub for updates...", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase DOWNLOADING = Phrase.create(
      "updateSettingsView.status.downloading", "**Downloading** update %version%...", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase UP_TO_DATE = Phrase.create(
      "updateSettingsView.status.upToDate", "The installed version is **up to date**", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase AVAILABLE = Phrase.create(
      "updateSettingsView.status.available", "Update **%version%** is available", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase STAGED = Phrase.create(
      "updateSettingsView.status.staged", "Update **%version%** is ready; restart the server", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase CHECK_FAILED = Phrase.create(
      "updateSettingsView.status.checkFailed", "**Could not check** GitHub for updates", Colors.GRAY, Colors.RED
   );
   private static final Phrase DOWNLOAD_FAILED = Phrase.create(
      "updateSettingsView.status.downloadFailed", "Update **download or verification failed**; check the console", Colors.GRAY, Colors.RED
   );
   private static final Phrase CONFLICT = Phrase.create(
      "updateSettingsView.status.conflict", "Another update is already **staged**; check the console", Colors.GRAY, Colors.RED
   );
   private static final Phrase NO_RELEASE = Phrase.create(
      "updateSettingsView.status.noRelease", "No **published stable release** was found", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase UPDATER_TITLE = Phrase.create("customisationPane.updater.title", "Ingame Updater");
   private static final Phrase UPDATER_ACTION = Phrase.create("customisationPane.updater.action", "**Click** to %action% ingame Updater", Colors.GRAY, Colors.AQUA);
   private static final Phrase UPDATER_DESC1 = Phrase.create("customisationPane.updater.desc1", "Automatically downloads verified GitHub Release updates", Colors.GRAY);
   private static final Phrase UPDATER_DESC2 = Phrase.create("customisationPane.updater.desc2", "Applies them on your next server restart", Colors.GRAY);
   private static final Phrase UPDATER_STATE = Phrase.create(
      "customisationPane.updater.state", "Automatic download: **%status%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase UPDATER_NOTICES_TITLE = Phrase.create("customisationPane.updaterNotices.title", "Update Chat Notifications");
   private static final Phrase UPDATER_NOTICES_ENABLE = Phrase.create(
      "customisationPane.updaterNotices.enable", "Click to **enable** update chat notifications", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase UPDATER_NOTICES_DISABLE = Phrase.create(
      "customisationPane.updaterNotices.disable", "Click to **disable** update chat notifications", Colors.GRAY, Colors.RED
   );
   private static final Phrase UPDATER_NOTICES_STATE = Phrase.create(
      "customisationPane.updaterNotices.state", "Chat notifications: **%status%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase UPDATER_NOTICES_DESC1 = Phrase.create(
      "customisationPane.updaterNotices.desc1", "Shows update status to superadmins", Colors.GRAY
   );
   private static final Phrase UPDATER_NOTICES_DESC2 = Phrase.create(
      "customisationPane.updaterNotices.desc2", "Works even when auto-download is disabled", Colors.GRAY
   );
   private final SpigotTechPlugin plugin;

   public UpdateSettingsView(Player player, SpigotTechPlugin plugin) {
      super(player, plugin);
      this.plugin = plugin;
      // One asynchronous request on entry, never on animated GUI redraws.
      Updater.refreshStatus(plugin);
   }

   public abstract void onBack();

   @Override
   public String getCurrentTitle() {
      return TITLE.get();
   }

   @Override
   protected void construct(Model model) {
      model.button(12, this::UpdaterNoticesButton);
      model.button(14, this::UpdaterButton);
      model.button(16, this::StatusButton);
      model.button(23, button -> Common.BackButton(button, action -> this.onBack()));
   }

   private void UpdaterNoticesButton(Button var1) {
      AppearanceRegistry registry = this.plugin.getAppearanceRegistry();
      var1.material(XMaterial.PAPER)
         .name(Animation.wave(UPDATER_NOTICES_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(
            (registry.isUpdaterNotificationsEnabled() ? UPDATER_NOTICES_DISABLE : UPDATER_NOTICES_ENABLE).get(),
            "",
            UPDATER_NOTICES_STATE.get().replace("%status%",
               (registry.isUpdaterNotificationsEnabled() ? Words.ENABLED : Words.DISABLED).get()),
            "",
            UPDATER_NOTICES_DESC1.get(),
            UPDATER_NOTICES_DESC2.get()
         );
      var1.action(action -> {
         registry.toggleUpdaterNotifications();
         Updater.preferencesChanged(this.plugin);
      });
   }

   private void UpdaterButton(Button var1) {
      AppearanceRegistry var2 = this.plugin.getAppearanceRegistry();
      var1.material(XMaterial.WRITABLE_BOOK)
         .name(Animation.wave(UPDATER_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(
            UPDATER_ACTION.get().replace("%action%", var2.isUpdaterEnabled() ? "§c" + Words.DISABLE.get() + "§7" : "§a" + Words.ENABLE.get() + "§7"),
            "",
            UPDATER_STATE.get().replace("%status%", (var2.isUpdaterEnabled() ? Words.ENABLED : Words.DISABLED).get()),
            "",
            UPDATER_DESC1.get(),
            UPDATER_DESC2.get()
         );
      var1.action(var1x -> {
         var2.toggleUpdater();
         Updater.preferencesChanged(this.plugin);
      });
   }

   private void StatusButton(Button button) {
      Updater.UpdateStatus status = Updater.getStatus();
      Phrase description = switch (status.state()) {
         case NOT_CHECKED, CANCELLED -> NOT_CHECKED;
         case CHECKING -> CHECKING;
         case DOWNLOADING -> DOWNLOADING;
         case UP_TO_DATE -> UP_TO_DATE;
         case AVAILABLE -> AVAILABLE;
         case STAGED, ALREADY_STAGED -> STAGED;
         case CHECK_FAILED -> CHECK_FAILED;
         case DOWNLOAD_FAILED -> DOWNLOAD_FAILED;
         case STAGING_CONFLICT -> CONFLICT;
         case NO_RELEASE -> NO_RELEASE;
      };
      button.material(XMaterial.BOOK)
         .name(Animation.wave(STATUS_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(description.get().replace("%version%", status.version() == null ? "" : status.version()), "",
            INSTALLED_VERSION.get().replace("%version%", this.plugin.getVersion()));
      if (status.version() != null) {
         button.item().appendLore(LATEST_VERSION.get().replace("%version%", status.version()));
      }
      // Information only: clicking this item cannot download or install anything.
   }
}
