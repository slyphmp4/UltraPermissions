package me.TechsCode.UltraPermissions.base.update.networkUpdate;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.base.messaging.QueuedMessage;
import me.TechsCode.UltraPermissions.base.messaging.SpigotMessagingListener;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.UpdateRequest;
import me.TechsCode.UltraPermissions.base.update.UpdateServer;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpigotUpdateAgent implements SpigotMessagingListener {
   private static final Phrase MESSAGE = Phrase.create("update.remote.notify", "Updated from **%from%** to **%to%**", Colors.GRAY, Colors.RED, Colors.GREEN);
   private static final Phrase SERVER_RESTARTING = Phrase.create(
      "update.remote.restart", "Server is **now** restarting to update **%pluginName%**", Colors.GRAY, Colors.GOLD, Colors.WHITE
   );
   private SpigotTechPlugin plugin;

   public SpigotUpdateAgent(SpigotTechPlugin var1) {
      super();
      this.plugin = var1;
      var1.getMessagingService().register(this);
   }

   @Override
   public void onMessage(Message message) {
      // Console control and executable updates are not accepted over plugin messaging.
   }

   public void sendUpdateRequestToBungeeCord(UpdateRequest request, String version) {
      this.plugin.log("Install verified updates manually on each server.");
   }

   public void sendRestartRequestToBungee() {
      this.plugin.log("Restart each server manually after verifying the installed update.");
   }


}
