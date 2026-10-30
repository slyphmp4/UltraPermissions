package me.TechsCode.UltraPermissions.base.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public abstract class StorageImplementation {
   protected TechPlugin plugin;
   protected String name;
   private boolean multiServerSupport;

   public StorageImplementation(TechPlugin var1, String var2, boolean var3) {
      super();
      this.plugin = var1;
      this.name = var2;
      this.multiServerSupport = var3;
   }

   public abstract void destroy(String var1, WriteCallback var2);

   public abstract void create(String var1, JsonObject var2, WriteCallback var3);

   public abstract void update(String var1, JsonObject var2, WriteCallback var3);

   public abstract void read(String var1, ReadCallback var2);

   public boolean hasMultiServerSupport() {
      return this.multiServerSupport;
   }
}
