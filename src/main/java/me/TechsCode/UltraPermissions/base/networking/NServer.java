package me.TechsCode.UltraPermissions.base.networking;

import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonArray;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class NServer {
   private String name;
   private String ip;
   private int port;
   private List<NPlayer> players;

   public NServer(String var1, String var2, int var3, List<NPlayer> var4) {
      super();
      this.name = var1;
      this.ip = var2;
      this.port = var3;
      this.players = var4;
   }

   public String getName() {
      return this.name;
   }

   public String getIp() {
      return this.ip;
   }

   public int getPort() {
      return this.port;
   }

   public List<NPlayer> getPlayers() {
      return this.players;
   }

   public JsonObject toJsonObject() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("name", this.name);
      var1.addProperty("ip", this.ip);
      var1.addProperty("port", this.port);
      JsonArray var2 = new JsonArray();
      this.players.forEach(var1x -> var2.add(var1x.toJsonObject()));
      var1.add("players", var2);
      return var1;
   }

   public static NServer fromJsonObject(JsonObject var0) {
      String var1 = var0.get("name").getAsString();
      String var2 = var0.get("ip").getAsString();
      int var3 = var0.get("port").getAsInt();
      NServer var4 = new NServer(var1, var2, var3, new ArrayList<>());
      var0.get("players").getAsJsonArray().forEach(var1x -> var4.players.add(NPlayer.fromJsonObject((JsonObject)var1x, var4)));
      return var4;
   }
}
