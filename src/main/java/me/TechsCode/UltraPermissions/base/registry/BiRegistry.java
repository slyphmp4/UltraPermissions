package me.TechsCode.UltraPermissions.base.registry;

public class BiRegistry<T extends RegistryStorable> {
   protected T local;
   protected T global;

   public BiRegistry(T var1, T var2) {
      super();
      this.local = (T)var1;
      this.global = (T)var2;
   }

   public T local() {
      return this.local;
   }

   public T global() {
      return this.global;
   }

   public T globalPreferred() {
      return this.hasGlobal() ? this.global : this.local;
   }

   public boolean hasGlobal() {
      return this.global != null;
   }
}
