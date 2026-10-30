package me.TechsCode.UltraPermissions.base.storage.syncing;

import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.base.storage.Storage;

public abstract class SyncingAgent {
   private List<Storage> storages = new ArrayList<>();

   public SyncingAgent() {
      super();
   }

   public void executeLocalSynchronization(String var1, String var2) {
      if (!me.TechsCode.UltraPermissions.base.security.MessagePolicy.model(var1)
         || !me.TechsCode.UltraPermissions.base.security.MessagePolicy.storageKey(var2)) return;
      this.storages.forEach(var2x -> {
         if (var2x.getModelName().equals(var1)) {
            var2x.syncFromDatabase(var2);
         }
      });
   }

   public void register(Storage var1) {
      this.storages.add(var1);
   }

   public abstract void notifyNewDataChanges(Storage var1, String var2);

   public static class EmptyAgent extends SyncingAgent {
      public EmptyAgent() {
         super();
      }

      @Override
      public void notifyNewDataChanges(Storage var1, String var2) {
      }
   }
}
