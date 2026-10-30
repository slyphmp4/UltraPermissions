package me.TechsCode.UltraPermissions.base.messaging;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import me.TechsCode.UltraPermissions.base.security.SafeJson;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParser;

public class Message {
   private final String key;
   private final JsonObject data;

   public Message(String var1, JsonObject var2) {
      super();
      this.key = var1;
      this.data = var2;
   }

   public String getKey() {
      return this.key;
   }

   public JsonObject getData() {
      return this.data;
   }

   public String encode() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("key", this.key);
      var1.add("data", this.data);
      return Base64.getEncoder().encodeToString(var1.toString().getBytes(StandardCharsets.UTF_8));
   }

   public static Message decode(String var0) {
      if (var0 == null || var0.length() > 24000) throw new IllegalArgumentException("Message exceeds limit");
      String var1 = new String(Base64.getDecoder().decode(var0), StandardCharsets.UTF_8);
      JsonObject var2 = SafeJson.object(var1, 18000);
      if (var2.size() != 2 || !var2.get("key").getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Invalid message");
      String var3 = var2.get("key").getAsString();
      JsonObject var4 = var2.get("data").getAsJsonObject();
      return new Message(var3, var4);
   }
}
