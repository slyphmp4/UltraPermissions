package me.TechsCode.UltraPermissions.base.messaging;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public abstract class QueuedMessage extends Message {
   public QueuedMessage(String var1, JsonObject var2) {
      super(var1, var2);
   }

   public abstract void onSend();
}
