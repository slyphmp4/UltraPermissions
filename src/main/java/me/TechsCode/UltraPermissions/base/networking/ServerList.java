package me.TechsCode.UltraPermissions.base.networking;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonArray;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class ServerList extends ArrayList<NServer> {
   public ServerList() {
      super();
   }

   public ServerList(JsonArray var1) {
      super();
      var1.forEach(var1x -> this.add(NServer.fromJsonObject((JsonObject)var1x)));
   }

   public JsonArray toJsonArray() {
      JsonArray var1 = new JsonArray();

      for (NServer var3 : this) {
         var1.add(var3.toJsonObject());
      }

      return var1;
   }
}
