package me.TechsCode.UltraPermissions.base.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;

public class SimpleStorage<S extends Storable> extends Storage<S> {
   public SimpleStorage(TechPlugin<?> var1, String var2, Class<S> var3, Class<? extends StorageImplementation> var4, boolean var5) {
      super(var1, var2, var3, var4, var5);
   }

   @Override
   public void onMount(S var1) {
   }

   @Override
   public void onCreation(S var1) {
   }

   @Override
   public void onChange(S var1, S var2) {
   }

   @Override
   public void onDestroy(S var1) {
   }

   @Override
   public void onDataSynchronization() {
   }
}
