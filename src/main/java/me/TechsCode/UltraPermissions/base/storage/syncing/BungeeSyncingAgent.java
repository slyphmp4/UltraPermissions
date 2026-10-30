package me.TechsCode.UltraPermissions.base.storage.syncing;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.BungeeMessagingListener;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeSyncingAgent extends SyncingAgent implements BungeeMessagingListener {
   private BungeeTechPlugin plugin;

   public BungeeSyncingAgent(BungeeTechPlugin var1) {
      super();
      this.plugin = var1;
      var1.getMessagingService().register(this);
   }

   @Override
   public void onMessage(ServerInfo var1, Message var2) {
      if (var2.getKey().equals("cache")) {
         ArrayList<ServerInfo> var3 = new ArrayList<>(ProxyServer.getInstance().getServers().values());
         var3.remove(var1);
         this.plugin.getMessagingService().send(var2, var3.toArray(new ServerInfo[0]));
         String var4 = var2.getData().get("model").getAsString();
         String var5 = var2.getData().get("dataKey").getAsString();
         this.executeLocalSynchronization(var4, var5);
      }
   }

   @Override
   public void notifyNewDataChanges(Storage var1, String var2) {
      JsonObject var3 = new JsonObject();
      var3.addProperty("model", var1.getModelName());
      var3.addProperty("dataKey", var2);
      this.plugin.getMessagingService().send(new Message("cache", var3));
   }
}
