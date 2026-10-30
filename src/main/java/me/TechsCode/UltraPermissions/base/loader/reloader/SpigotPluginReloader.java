package me.TechsCode.UltraPermissions.base.loader.reloader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

public class SpigotPluginReloader extends PluginReloader<SpigotTechPlugin> {
   public SpigotPluginReloader(SpigotTechPlugin var1) {
      super(var1);
   }

   @Override
   public void unload() {
      Bukkit.getPluginManager().disablePlugin((Plugin)this.plugin.getBootstrap());

      try {
         Field var1 = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
         var1.setAccessible(true);
         Map<?, java.util.Set<org.bukkit.plugin.RegisteredListener>> var2 = (Map)var1.get(Bukkit.getPluginManager());
         var2.values().forEach(var1x -> var1x.removeIf(var1xx -> var1xx.getPlugin() == this.plugin.getBootstrap()));
      } catch (Exception var10) {
      }

      try {
         Field var12 = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
         var12.setAccessible(true);
         SimpleCommandMap var14 = (SimpleCommandMap)var12.get(Bukkit.getPluginManager());
         Field var3 = SimpleCommandMap.class.getDeclaredField("knownCommands");
         var3.setAccessible(true);
         Map var4 = (Map)var3.get(var14);
         Iterator var5 = var4.entrySet().iterator();

         while (var5.hasNext()) {
            Entry var6 = (Entry)var5.next();
            if (var6.getValue() instanceof PluginCommand) {
               PluginCommand var7 = (PluginCommand)var6.getValue();
               if (var7.getPlugin() == this.plugin.getBootstrap()) {
                  var7.unregister(var14);
                  var5.remove();
               }
            }
         }
      } catch (NoSuchFieldException | IllegalAccessException var11) {
         var11.printStackTrace();
      }

      ClassLoader var13 = this.plugin.getBootstrap().getClass().getClassLoader();
      if (var13 instanceof URLClassLoader) {
         try {
            Field var15 = var13.getClass().getDeclaredField("plugin");
            var15.setAccessible(true);
            var15.set(var13, null);
            Field var16 = var13.getClass().getDeclaredField("pluginInit");
            var16.setAccessible(true);
            var16.set(var13, null);
         } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException var9) {
            var9.printStackTrace();
         }

         try {
            ((URLClassLoader)var13).close();
         } catch (IOException var8) {
            var8.printStackTrace();
         }
      }

      System.gc();
   }

   @Override
   public void load() {
      try {
         File var1 = new File(URLDecoder.decode(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
         Plugin var2 = Bukkit.getPluginManager().loadPlugin(var1);
         var2.onLoad();
         Bukkit.getPluginManager().enablePlugin(var2);
      } catch (UnsupportedEncodingException | InvalidDescriptionException | InvalidPluginException var3) {
         var3.printStackTrace();
      }
   }
}
