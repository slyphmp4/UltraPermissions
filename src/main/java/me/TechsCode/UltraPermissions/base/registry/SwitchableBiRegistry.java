package me.TechsCode.UltraPermissions.base.registry;

public class SwitchableBiRegistry<T extends RegistryStorable & Switchable> {
   private BiRegistry<T> registry;

   public SwitchableBiRegistry(BiRegistry<T> var1) {
      super();
      this.registry = var1;
   }

   public T get() {
      return this.registry.local.isSwitchedTo() ? this.registry.local : (this.registry.global != null ? this.registry.global : this.registry.local);
   }

   public void switchToLocal() {
      this.registry.local.setSwitchedTo(true);
   }

   public void switchToGlobal() {
      this.registry.local.setSwitchedTo(false);
   }

   public T local() {
      return this.registry.local;
   }

   public T global() {
      return this.registry.global;
   }

   public boolean hasGlobal() {
      return this.registry.global != null;
   }
}
