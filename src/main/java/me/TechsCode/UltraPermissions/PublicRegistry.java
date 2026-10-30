package me.TechsCode.UltraPermissions;

import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class PublicRegistry extends RegistryStorable {
   private boolean defaultPermissions;
   private DefaultGroupAssignOption defaultGroupAssignOption;
   private boolean negativePermsOverride = false;

   public PublicRegistry() {
      super("public");
      this.defaultPermissions = false;
      this.defaultGroupAssignOption = DefaultGroupAssignOption.FIRST_JOIN;
   }

   @Override
   public void setState(JsonObject var1) {
      this.defaultPermissions = var1.get("defaultPermissions").getAsBoolean();
      this.defaultGroupAssignOption = DefaultGroupAssignOption.valueOf(var1.get("defaultAssigner").getAsString());
      if (var1.get("negativePermsOverride") == null) {
         var1.addProperty("negativePermsOverride", this.negativePermsOverride);
      }

      this.negativePermsOverride = var1.get("negativePermsOverride").getAsBoolean();
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("defaultPermissions", this.defaultPermissions);
      var1.addProperty("defaultAssigner", this.defaultGroupAssignOption.name());
      var1.addProperty("negativePermsOverride", this.negativePermsOverride);
      return var1;
   }

   public boolean isDefaultPermissions() {
      return this.defaultPermissions;
   }

   public void setDefaultPermissions(boolean var1) {
      this.defaultPermissions = var1;
      this.sync();
   }

   public DefaultGroupAssignOption getDefaultAssignOption() {
      return this.defaultGroupAssignOption;
   }

   public void setDefaultGroupAssignOption(DefaultGroupAssignOption var1) {
      this.defaultGroupAssignOption = var1;
      this.sync();
   }

   public void setNegativePermsOverride(boolean var1) {
      this.negativePermsOverride = var1;
      this.sync();
   }

   public boolean isNegativePermsOverride() {
      return this.negativePermsOverride;
   }
}
