package me.TechsCode.UltraPermissions.base.networking;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class NetworkData {
   private String proxyPluginVersion;
   private ServerList serverList;

   public NetworkData(String var1, ServerList var2) {
      super();
      this.proxyPluginVersion = var1;
      this.serverList = var2;
   }

   public String getProxyPluginVersion() {
      return this.proxyPluginVersion;
   }

   public ServerList getServerList() {
      return this.serverList;
   }

   public JsonObject toJsonObject() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("proxyVersion", this.proxyPluginVersion);
      var1.add("serverList", this.serverList.toJsonArray());
      return var1;
   }

   public static NetworkData fromJsonObject(JsonObject var0) {
      String var1 = var0.get("proxyVersion").getAsString();
      ServerList var2 = new ServerList(var0.getAsJsonArray("serverList"));
      return new NetworkData(var1, var2);
   }
}
