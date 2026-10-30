package me.TechsCode.UltraPermissions.base.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public abstract class Storable {
   protected Storage<?> storage;
   private JsonObject lastSyncedState;

   public Storable() {
      super();
   }

   public void setStorage(Storage<?> var1) {
      this.storage = var1;
   }

   protected void sync() {
      if (this.storage != null) {
         JsonObject var1 = this.getState();
         if (this.lastSyncedState == null || !this.lastSyncedState.toString().equalsIgnoreCase(var1.toString())) {
            this.storage.update(this, var1);
            this.lastSyncedState = var1;
         }
      }
   }

   public void setLastSyncedState(JsonObject var1) {
      this.lastSyncedState = var1;
   }

   public JsonObject getLastSyncedState() {
      return this.lastSyncedState;
   }

   protected void destroy() {
      if (this.storage != null) {
         this.storage.destroy(this);
         this.storage = null;
      }
   }

   public abstract String getKey();

   public abstract void setKey(String var1);

   public abstract JsonObject getState();

   public abstract void setState(JsonObject var1, TechPlugin<?> var2);

   public boolean isLinkedToStorage() {
      return this.storage != null;
   }

   @Override
   public boolean equals(Object var1) {
      return !(var1 instanceof Storable var2) ? false : var2.getKey().equals(this.getKey());
   }

   @Override
   public int hashCode() {
      return this.getKey().hashCode();
   }

   public abstract void onMount(TechPlugin<?> var1);
}
