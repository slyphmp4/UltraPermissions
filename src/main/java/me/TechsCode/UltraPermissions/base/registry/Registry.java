package me.TechsCode.UltraPermissions.base.registry;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.implementations.LocalFile;
import me.TechsCode.UltraPermissions.base.storage.implementations.MySQL;

public class Registry {
   private TechPlugin<?> plugin;
   private RegistryStorage local;
   private RegistryStorage global;

   public Registry(TechPlugin<?> var1) {
      super();
      this.plugin = var1;
      this.local = new RegistryStorage(var1, LocalFile.class);
   }

   private void startRemoteRegistry() {
      if (this.global == null) {
         this.global = this.plugin.getMySQLManager() != null && this.plugin.getMySQLManager().isEnabled()
            ? new RegistryStorage(this.plugin, MySQL.class)
            : null;
      }
   }

   public <T extends RegistryStorable> BiRegistry<T> register(Class<? extends T> var1) {
      try {
         RegistryStorable var2 = (RegistryStorable)var1.newInstance();
         return this.register((T)var2);
      } catch (InstantiationException | IllegalAccessException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public <T extends RegistryStorable> BiRegistry<T> register(T var1) {
      RegistryStorable var2 = this.register(var1, RegistrationChoice.LOCAL);
      RegistryStorable var3 = this.register(var1, RegistrationChoice.GLOBAL);
      return new BiRegistry<>((T)var2, (T)var3);
   }

   public <T extends RegistryStorable> T register(Class<? extends T> var1, RegistrationChoice var2) {
      try {
         RegistryStorable var3 = (RegistryStorable)var1.newInstance();
         return this.register((T)var3, var2);
      } catch (InstantiationException | IllegalAccessException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public <T extends RegistryStorable> T register(T var1, RegistrationChoice var2) {
      this.startRemoteRegistry();
      Registration var3 = null;
      switch (var2) {
         case LOCAL:
            var3 = Registration.LOCAL;
            break;
         case GLOBAL:
            var3 = this.global != null ? Registration.GLOBAL : null;
            break;
         case GLOBAL_IF_AVAILABLE:
            var3 = this.global != null ? Registration.GLOBAL : Registration.LOCAL;
      }

      if (var3 == null) {
         return null;
      }

      RegistryStorage var4 = var3 == Registration.LOCAL ? this.local : this.global;
      RegistryStorageEntry var5 = var4.retrieve(new RegistryStorageEntry(var1.getKey(), var1.getState()));
      var1.setStorageEntry(var5);
      var1.setState(var5.getState());
      return (T)var1;
   }
}
