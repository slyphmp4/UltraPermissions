package me.TechsCode.UltraPermissions.base.update;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonArray;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class UpdateRequest {
   private final String updateToken;
   private final UpdateRequestStatus status;
   private final String pluginName;
   private final String currentVersion;
   private final String updateTo;
   private final ArrayList<String> allowedPlugins = new ArrayList<>();
   private final boolean hasBetaAccess;
   private final String updatedAt;
   private final String createdAt;

   public UpdateRequest(JsonObject var1) {
      super();
      this.updateToken = var1.has("update_token") ? var1.get("update_token").getAsString() : null;
      this.status = var1.has("status") ? UpdateRequestStatus.fromString(var1.get("status").getAsString()) : null;
      this.pluginName = var1.has("plugin_name") ? var1.get("plugin_name").getAsString() : null;
      this.currentVersion = var1.has("current_version") ? var1.get("current_version").getAsString() : null;
      this.updateTo = var1.has("update_to") ? var1.get("update_to").getAsString() : null;
      JsonArray var2 = var1.has("allowed_plugins") ? var1.get("allowed_plugins").getAsJsonArray() : null;
      if (var2 != null) {
         var2.forEach(var1x -> this.allowedPlugins.add(var1x.getAsString()));
      }

      this.hasBetaAccess = var1.has("has_beta_access") ? var1.get("has_beta_access").getAsBoolean() : null;
      this.updatedAt = var1.has("updated_at") ? var1.get("updated_at").getAsString() : null;
      this.createdAt = var1.has("created_at") ? var1.get("created_at").getAsString() : null;
   }

   public String getUpdateToken() {
      return this.updateToken;
   }

   public UpdateRequestStatus getStatus() {
      return this.status;
   }

   public String getPluginName() {
      return this.pluginName;
   }

   public String getCurrentVersion() {
      return this.currentVersion;
   }

   public String getUpdateTo() {
      return this.updateTo;
   }

   public ArrayList<String> getAllowedPlugins() {
      return this.allowedPlugins;
   }

   public boolean hasBetaAccess() {
      return this.hasBetaAccess;
   }

   public String getUpdatedAt() {
      return this.updatedAt;
   }

   public String getCreatedAt() {
      return this.createdAt;
   }

   public String getUpdateToVersionName() {
      return this.updateTo.split("_")[0];
   }
}
