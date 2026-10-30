package me.TechsCode.UltraPermissions;

import java.util.UUID;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class PrivateRegistry extends RegistryStorable {
   private UUID serverIdentifier = UUID.randomUUID();

   public PrivateRegistry() {
      super("private");
   }

   @Override
   public void setState(JsonObject var1) {
      this.serverIdentifier = UUID.fromString(var1.get("serverIdentifier").getAsString());
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("serverIdentifier", this.serverIdentifier.toString());
      return var1;
   }

   public UUID getServerIdentifier() {
      return this.serverIdentifier;
   }

   public void setServerIdentifier(UUID var1) {
      this.serverIdentifier = var1;
      this.sync();
   }
}
