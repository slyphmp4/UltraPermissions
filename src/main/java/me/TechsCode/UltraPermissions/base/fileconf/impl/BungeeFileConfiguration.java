package me.TechsCode.UltraPermissions.base.fileconf.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.fileconf.AtomicFileSave;
import me.TechsCode.UltraPermissions.base.fileconf.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeFileConfiguration implements Configuration {
   private final File file;
   private net.md_5.bungee.config.Configuration configuration;

   public BungeeFileConfiguration(File var1) {
      super();
      this.file = var1;
      if (!var1.exists()) {
         try {
            File parent = var1.getAbsoluteFile().getParentFile();
            if (parent != null) {
               parent.mkdirs();
            }
            var1.createNewFile();
         } catch (IOException var4) {
            throw new UncheckedIOException("Could not initialize configuration " + var1, var4);
         }
      }

      try {
         this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(var1);
      } catch (IOException var3) {
         throw new UncheckedIOException("Could not load configuration " + var1 + "; refusing to overwrite existing data", var3);
      }
   }

   @Override
   public Object get(String var1) {
      return this.configuration.get(var1);
   }

   @Override
   public int getInt(String var1) {
      return this.configuration.getInt(var1);
   }

   @Override
   public String getString(String var1) {
      return this.configuration.getString(var1);
   }

   @Override
   public boolean getBoolean(String var1) {
      return this.configuration.getBoolean(var1);
   }

   @Override
   public Set<String> getKeys(boolean var1) {
      HashSet var2 = new HashSet();
      if (var1) {
         this.recursiveKeys(var2, "", this.configuration);
      } else {
         var2.addAll(this.configuration.getKeys());
      }

      return var2;
   }

   private void recursiveKeys(Set<String> var1, String var2, net.md_5.bungee.config.Configuration var3) {
      var1.addAll(var3.getKeys().stream().map(var1x -> var2 + var1x).collect(Collectors.toList()));

      for (String var5 : var3.getKeys()) {
         try {
            net.md_5.bungee.config.Configuration var6 = var3.getSection(var5);
            if (var6 != null) {
               this.recursiveKeys(var1, var2 + var5 + ".", var6);
            }
         } catch (ClassCastException var7) {
         }
      }
   }

   @Override
   public boolean contains(String var1) {
      return this.configuration.contains(var1);
   }

   @Override
   public void set(String var1, Object var2) {
      this.configuration.set(var1, var2);
   }

   @Override
   public void save() {
      try {
         AtomicFileSave.save(this.file, staged -> ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.configuration, staged));
      } catch (IOException failure) {
         throw new UncheckedIOException("Could not save configuration " + this.file, failure);
      }
   }
}
