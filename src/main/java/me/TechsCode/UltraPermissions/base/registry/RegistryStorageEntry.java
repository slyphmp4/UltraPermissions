package me.TechsCode.UltraPermissions.base.registry;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class RegistryStorageEntry extends Storable {
   private String key;
   private JsonObject value;
   private RegistryStorable storable;

   public RegistryStorageEntry(String var1, JsonObject var2) {
      super();
      this.key = var1;
      this.value = var2;
   }

   public void setStorable(RegistryStorable var1) {
      this.storable = var1;
   }

   @Override
   public String getKey() {
      return this.key;
   }

   @Override
   public void setKey(String var1) {
      this.key = var1;
   }

   @Override
   public JsonObject getState() {
      return this.value;
   }

   @Override
   public void setState(JsonObject var1, TechPlugin var2) {
      this.value = var1;
      if (this.storable != null) {
         this.storable.setState(var1);
      }
   }

   @Override
   public void onMount(TechPlugin var1) {
   }

   @Override
   protected void sync() {
      super.sync();
   }
}
