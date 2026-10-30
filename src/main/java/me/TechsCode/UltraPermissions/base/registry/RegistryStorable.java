package me.TechsCode.UltraPermissions.base.registry;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public abstract class RegistryStorable {
   private final String key;
   private RegistryStorageEntry storageEntry;

   public RegistryStorable(String var1) {
      super();
      this.key = var1;
   }

   public void setStorageEntry(RegistryStorageEntry var1) {
      this.storageEntry = var1;
      this.storageEntry.setStorable(this);
   }

   public String getKey() {
      return this.key;
   }

   public abstract void setState(JsonObject var1);

   public abstract JsonObject getState();

   public void sync() {
      this.storageEntry.setState(this.getState(), null);
      this.storageEntry.sync();
   }
}
