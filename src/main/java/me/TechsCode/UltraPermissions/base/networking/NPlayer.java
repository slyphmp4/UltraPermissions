package me.TechsCode.UltraPermissions.base.networking;

import java.util.UUID;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class NPlayer {
   private UUID uuid;
   private String name;
   private NServer server;

   public NPlayer(UUID var1, String var2, NServer var3) {
      super();
      this.uuid = var1;
      this.name = var2;
      this.server = var3;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public NServer getServer() {
      return this.server;
   }

   public JsonObject toJsonObject() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("uuid", this.uuid.toString());
      var1.addProperty("name", this.name);
      return var1;
   }

   public static NPlayer fromJsonObject(JsonObject var0, NServer var1) {
      UUID var2 = UUID.fromString(var0.get("uuid").getAsString());
      String var3 = var0.get("name").getAsString();
      return new NPlayer(var2, var3, var1);
   }
}
