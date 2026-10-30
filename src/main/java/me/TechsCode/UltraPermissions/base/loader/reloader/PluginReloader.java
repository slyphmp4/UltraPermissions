package me.TechsCode.UltraPermissions.base.loader.reloader;

import me.TechsCode.UltraPermissions.base.TechPlugin;

public abstract class PluginReloader<PLUGIN extends TechPlugin<?>> {
   protected PLUGIN plugin;

   public PluginReloader(PLUGIN var1) {
      super();
      this.plugin = (PLUGIN)var1;
   }

   public abstract void unload();

   public abstract void load();
}
