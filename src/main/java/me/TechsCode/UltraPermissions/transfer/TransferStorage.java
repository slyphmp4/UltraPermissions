package me.TechsCode.UltraPermissions.transfer;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;

public class TransferStorage<S extends Storable> extends Storage<S> {
   private long lastWrite = System.currentTimeMillis();

   public TransferStorage(TechPlugin var1, String var2, Class<S> var3, Class<? extends StorageImplementation> var4, boolean var5) {
      super(var1, var2, var3, var4, var5);
   }

   @Override
   public void onMount(S var1) {
      this.lastWrite = System.currentTimeMillis();
   }

   @Override
   public void onCreation(S var1) {
      this.lastWrite = System.currentTimeMillis();
   }

   @Override
   public void onDestroy(S var1) {
      this.lastWrite = System.currentTimeMillis();
   }

   @Override
   public void onChange(S var1, S var2) {
      this.lastWrite = System.currentTimeMillis();
   }

   public long getLastWrite() {
      return this.lastWrite;
   }

   @Override
   public void onDataSynchronization() {
   }
}
