package me.TechsCode.UltraPermissions.base.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public abstract class Storable {
   protected volatile Storage<?> storage;
   private volatile JsonObject lastSyncedState;

   public Storable() {
      super();
   }

   public void setStorage(Storage<?> var1) {
      this.storage = var1;
   }

   /** Serialize changes with persistence and database refreshes. */
   protected final void mutate(Runnable action) {
      Storage<?> linked = this.storage;
      if (linked == null) {
         synchronized (this) {
            action.run();
         }
      } else {
         linked.plugin.writeModel(action);
      }
   }

   protected void sync() {
      Storage<?> linked = this.storage;
      if (linked == null) return;
      linked.plugin.writeModel(() -> {
         if (this.storage != linked) return;
         JsonObject state = this.getState();
         if (this.lastSyncedState == null || !this.lastSyncedState.equals(state)) {
            linked.update(this, state);
         }
      });
   }

   public void setLastSyncedState(JsonObject var1) {
      // Last saved state must not alias a mutable JSON model instance.
      this.lastSyncedState = var1 == null ? null : var1.deepCopy();
   }

   public JsonObject getLastSyncedState() {
      return this.lastSyncedState;
   }

   protected void destroy() {
      Storage<?> linked = this.storage;
      if (linked != null) linked.plugin.writeModel(() -> {
         if (this.storage == linked) linked.destroy(this);
      });
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
