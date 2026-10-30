package me.TechsCode.UltraPermissions.base.mysql.credentialsTransfer;

import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.BungeeMessagingListener;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.base.mysql.MySQLManager;
import me.TechsCode.UltraPermissions.base.mysql.MySQLRegistry;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParseException;
import net.md_5.bungee.api.config.ServerInfo;

public class ProxyMySQLReceiver implements BungeeMessagingListener {
   private BungeeTechPlugin plugin;
   private MySQLManager manager;

   public ProxyMySQLReceiver(BungeeTechPlugin var1, MySQLManager var2) {
      super();
      this.plugin = var1;
      this.manager = var2;
      var1.getMessagingService().register(this);
   }

   @Override
   public void onMessage(ServerInfo server, Message message) {
      // Provision the proxy Registry locally; network credential replacement is forbidden.
   }


}
