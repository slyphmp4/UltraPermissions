package me.TechsCode.UltraPermissions.base.update.networkUpdate;

import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.BungeeMessagingListener;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.UpdateServer;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeUpdateAgent implements BungeeMessagingListener {
   private BungeeTechPlugin plugin;
   private static final Phrase RESTART_MANUALLY = Phrase.create(
      "BungeeUpdateAgent.restartManually", "A manually restart of your proxy is required to update **%pluginName%**", Colors.GRAY, Colors.WHITE
   );

   public BungeeUpdateAgent(BungeeTechPlugin var1) {
      super();
      this.plugin = var1;
      var1.getMessagingService().register(this);
   }

   @Override
   public void onMessage(ServerInfo server, Message message) {
      // No network-initiated restart or executable update, even from a signed peer.
   }


}
