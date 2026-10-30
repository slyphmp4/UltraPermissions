package me.TechsCode.UltraPermissions.base.registry;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.SimpleStorage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;

public class RegistryStorage extends SimpleStorage<RegistryStorageEntry> {
   public RegistryStorage(TechPlugin<?> var1, Class<? extends StorageImplementation> var2) {
      super(var1, "Registry", RegistryStorageEntry.class, var2, true);
   }

   public RegistryStorageEntry retrieve(RegistryStorageEntry var1) {
      for (RegistryStorageEntry var3 : this.get()) {
         if (var3.getKey().equalsIgnoreCase(var1.getKey())) {
            return var3;
         }
      }

      return this.create(var1);
   }
}
