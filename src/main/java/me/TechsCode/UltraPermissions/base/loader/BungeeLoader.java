package me.TechsCode.UltraPermissions.base.loader;

import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLoader extends Plugin {
   private Loader loader;

   public BungeeLoader() {
      super();
   }

   public void onEnable() {
      this.loader = new Loader(this, BungeeTechPlugin.class, this.getClass().getClassLoader());
   }

   public void onDisable() {
      if (this.loader != null) {
         this.loader.unload();
      }
   }
}
