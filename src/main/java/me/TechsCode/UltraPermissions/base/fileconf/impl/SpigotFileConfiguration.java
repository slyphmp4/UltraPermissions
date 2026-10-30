package me.TechsCode.UltraPermissions.base.fileconf.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import me.TechsCode.UltraPermissions.base.fileconf.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SpigotFileConfiguration implements Configuration {
   private final File file;
   private final YamlConfiguration cfg;

   public SpigotFileConfiguration(File var1) {
      super();
      this.file = var1;
      this.cfg = YamlConfiguration.loadConfiguration(var1);
   }

   @Override
   public Object get(String var1) {
      return this.cfg.get(var1);
   }

   @Override
   public int getInt(String var1) {
      return this.cfg.getInt(var1);
   }

   @Override
   public String getString(String var1) {
      return this.cfg.getString(var1);
   }

   @Override
   public boolean getBoolean(String var1) {
      return this.cfg.getBoolean(var1);
   }

   @Override
   public Set<String> getKeys(boolean var1) {
      return this.cfg.getKeys(var1);
   }

   @Override
   public boolean contains(String var1) {
      return this.cfg.contains(var1);
   }

   @Override
   public void set(String var1, Object var2) {
      this.cfg.set(var1, var2);
   }

   @Override
   public void save() {
      try {
         this.cfg.save(this.file);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }
}
