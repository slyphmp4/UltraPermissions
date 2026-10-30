package me.TechsCode.UltraPermissions.base.storage.syncing;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.base.messaging.QueuedMessage;
import me.TechsCode.UltraPermissions.base.messaging.SpigotMessagingListener;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class SpigotSyncingAgent extends SyncingAgent implements SpigotMessagingListener {
   private SpigotTechPlugin plugin;

   public SpigotSyncingAgent(SpigotTechPlugin var1) {
      super();
      this.plugin = var1;
      var1.getMessagingService().register(this);
   }

   @Override
   public void onMessage(Message var1) {
      if (var1.getKey().equals("cache")) {
         String var2 = var1.getData().get("model").getAsString();
         String var3 = var1.getData().get("dataKey").getAsString();
         this.executeLocalSynchronization(var2, var3);
      }
   }

   @Override
   public void notifyNewDataChanges(Storage var1, String var2) {
      JsonObject var3 = new JsonObject();
      var3.addProperty("model", var1.getModelName());
      var3.addProperty("dataKey", var2);
      this.plugin.getMessagingService().send(new QueuedMessage("cache", var3) {
         @Override
         public void onSend() {
         }
      });
   }
}
