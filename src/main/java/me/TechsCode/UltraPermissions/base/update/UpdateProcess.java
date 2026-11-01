package me.TechsCode.UltraPermissions.base.update;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Retained for compatibility with integrations calling the old update process.
 * The former Discord authorization/download workflow has been removed.
 */
@Deprecated
public class UpdateProcess implements Listener {
   public UpdateProcess(SpigotTechPlugin plugin, Player player, String requestedVersion, Callback<UpdateProcess.CloseReason> callback) {
      Updater.sendManualInstallInstructions(plugin, player, UpdateServer.RELEASES_URL);
      // COMPLETE denotes that the informational flow has finished, not that the server was updated.
      if (callback != null) callback.run(CloseReason.COMPLETE);
   }

   /** No legacy update session is registered or polled. */
   @Deprecated
   public void check(UpdateEvent event) {}

   /** No session or async worker remains to tear down. */
   @Deprecated
   public void leave(PlayerQuitEvent event) {}

   enum CloseReason {
      QUIT,
      COMPLETE,
      NO_REQUIREMENTS,
      TIME_OUT,
      SERVER_OFFLINE
   }
}
