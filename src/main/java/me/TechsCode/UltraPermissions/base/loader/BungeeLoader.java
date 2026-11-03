package me.TechsCode.UltraPermissions.base.loader;

import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLoader extends Plugin {
   private Loader loader;

   public BungeeLoader() {
      super();
   }

   public void onEnable() {
      try {
         this.loader = new Loader(this, BungeeTechPlugin.class, this.getClass().getClassLoader());
      } catch (RuntimeException failure) {
         if (me.TechsCode.UltraPermissions.base.storage.StorageStartupException.isStorageFailure(failure)) {
            this.getLogger().severe("UltraPermissions could not open its configured permissions store. "
               + "Stopping the proxy; check mysql.properties, database access/TLS and restart.");
            this.getProxy().stop("UltraPermissions permissions storage unavailable");
         }
         throw failure;
      }
   }

   public void onDisable() {
      if (this.loader != null) {
         this.loader.unload();
      }
   }
}
