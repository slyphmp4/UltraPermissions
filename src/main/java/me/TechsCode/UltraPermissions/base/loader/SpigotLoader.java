package me.TechsCode.UltraPermissions.base.loader;

import java.io.File;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotLoader extends JavaPlugin {
   private Loader loader;

   public SpigotLoader() {
      super();
   }

   /** Actual JAR filename is required by Bukkit's native plugin-update folder. */
   public File getLoadedPluginFile() {
      return this.getFile();
   }

   @Override
   public void onEnable() {
      // Open-source builds have no Spigot Premium authorization/phone-home gate.
      try {
         this.loader = new Loader(this, SpigotTechPlugin.class, this.getClassLoader());
      } catch (RuntimeException failure) {
         if (me.TechsCode.UltraPermissions.base.storage.StorageStartupException.isStorageFailure(failure)) {
            this.getLogger().severe("UltraPermissions could not open its configured permissions store. "
               + "Stopping the server to prevent startup with missing or stale rights. "
               + "Check mysql.properties, database access/TLS and storage files, then restart.");
            this.getServer().shutdown();
         }
         throw failure;
      }
   }

   @Override
   public void onDisable() {
      if (this.loader != null) {
         this.loader.unload();
      }
   }
}
