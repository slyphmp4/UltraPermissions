package me.TechsCode.UltraPermissions.base.update;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;
import me.TechsCode.UltraPermissions.base.update.view.UpdateView;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public class Updater {
   private static final Set<UUID> updateAskedPlayers = ConcurrentHashMap.newKeySet();
   private static final Phrase MANUAL_INSTALL = Phrase.create(
      "Updater.manualInstall", "Download the verified update from **GitHub Releases**:", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase RESTART_MANUALLY = Phrase.create(
      "Updater.restartManually", "Replace the plugin JAR and **restart** your server", Colors.GRAY, Colors.YELLOW
   );

   public static void suggestUpdateIfAvailable(SpigotTechPlugin plugin, Player player, Callback<Player> onDecline, boolean force) {
      if (!plugin.getAppearanceRegistry().isUpdaterEnabled() || (!force && updateAskedPlayers.contains(player.getUniqueId()))) {
         return;
      }
      // Never perform HTTP requests on the Minecraft server thread.
      plugin.getScheduler().runAsync(() -> {
         UpdateServer server = new UpdateServer();
         IsUpToDateResponse result;
         try {
            result = server.isUpToDate(plugin);
         } finally {
            server.destroy();
         }
         if (!result.isSuccessFull() || result.isUpToDate() || result.getLatestVersion() == null) {
            return;
         }
         plugin.getScheduler().run(() -> {
            if (!player.isOnline() || !plugin.getAppearanceRegistry().isUpdaterEnabled()) {
               return;
            }
            if (!force && !updateAskedPlayers.add(player.getUniqueId())) {
               return;
            }
            updateAskedPlayers.add(player.getUniqueId());
            new UpdateView(player, plugin, plugin.getVersion(), result.getLatestVersion()) {
               @Override
               public void onResponse(boolean accepted) {
                  if (accepted) {
                     this.p.sendMessage(plugin.getPrefix() + MANUAL_INSTALL.get());
                     this.p.sendMessage(Colors.YELLOW + UpdateServer.RELEASES_URL);
                     this.p.sendMessage(plugin.getPrefix() + RESTART_MANUALLY.get());
                  } else if (onDecline != null) {
                     onDecline.run(this.p);
                  }
               }
            };
         });
      });
   }

   public static String getOSSystem() {
      return System.getProperty("os.name");
   }
}
