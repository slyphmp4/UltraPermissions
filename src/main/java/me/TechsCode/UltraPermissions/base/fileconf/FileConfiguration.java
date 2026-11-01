package me.TechsCode.UltraPermissions.base.fileconf;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.fileconf.impl.BungeeFileConfiguration;
import me.TechsCode.UltraPermissions.base.fileconf.impl.SpigotFileConfiguration;

public class FileConfiguration implements Configuration {
   private Configuration configuration;

   public FileConfiguration(TechPlugin<?> var1, File var2) {
      super();
      HashMap<Class<?>, Class<? extends Configuration>> var3 = new HashMap<>();
      var3.put(SpigotTechPlugin.class, SpigotFileConfiguration.class);
      var3.put(BungeeTechPlugin.class, BungeeFileConfiguration.class);
      Class var4 = var3.entrySet().stream().filter(var1x -> ((Class)var1x.getKey()).isInstance(var1)).map(Entry::getValue).findFirst().orElse(null);
      if (var4 == null) {
         throw new IllegalStateException("Could not find File Configuration Implementation");
      }

      try {
         this.configuration = (Configuration)var4.getDeclaredConstructor(File.class).newInstance(var2);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException var6) {
         throw new IllegalStateException("Could not load configuration " + var2 + "; refusing to overwrite existing data", var6);
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
      return this.configuration.getKeys(var1);
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
      this.configuration.save();
   }
}
