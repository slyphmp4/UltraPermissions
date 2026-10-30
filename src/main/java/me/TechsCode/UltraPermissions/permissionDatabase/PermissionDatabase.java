package me.TechsCode.UltraPermissions.permissionDatabase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

public class PermissionDatabase {
   private HashMap<String, PermissionInfo> map = new HashMap<>();
   public static final String githubLink = "https://raw.githubusercontent.com/TechsCode-Team/PluginPermissions/master/Permissions/Database.updb";

   public PermissionDatabase(UltraPermissions var1) {
      super();
      Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)var1.getBootstrap(), () -> {
         var1.log("Loading Permission Database...");

         for (Plugin var5 : Bukkit.getPluginManager().getPlugins()) {
            for (Permission var7 : var5.getDescription().getPermissions()) {
               this.map.put(var7.getName(), new PermissionInfo(var7.getName(), var5.getName(), var7.getDescription(), new String[0], "Extracted from .jar"));
            }
         }

         var1.log("- " + this.map.size() + " Permissions extracted from .jars");
         File var14 = new File(var1.getPluginFolder().getAbsolutePath() + "/Main.updb");
         boolean var15 = false;

         try {
            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/TechsCode-Team/PluginPermissions/master/Permissions/Database.updb"), var14);
            var15 = true;
         } catch (IOException var12) {
            var1.log("Failed to load Permissions from the Cloud");
         }

         if (var14.exists()) {
            try {
               int var16 = 0;

               for (String var18 : FileUtils.readLines(var14, "UTF-8")) {
                  if (!var18.startsWith("#")) {
                     String[] var19 = var18.split("[+]");
                     if (var19.length < 2) {
                        var1.log("Invalid line in " + var14.getName() + ":");
                        var1.log(var18);
                     } else {
                        String var8 = var19[0];
                        String var9 = var19[1];
                        String var10 = "";
                        String[] var11 = new String[0];
                        if (var19.length >= 3) {
                           var10 = var19[2];
                        }

                        if (var19.length >= 4) {
                           var11 = var19[3].split(",");
                        }

                        this.map.put(var9, new PermissionInfo(var9, var8, var10, var11, "Permission Database (" + var14.getName() + ")"));
                        var16++;
                     }
                  }
               }

               if (var15) {
                  var1.log("- " + var16 + " Permissions downloaded from the Cloud");
               } else {
                  var1.log("- " + var16 + " Permissions loaded from the Cache");
               }
            } catch (IOException var13) {
               var13.printStackTrace();
            }
         } else {
            var1.log("Connect this Server to the internet to load the Permission Database");
         }
      }, 20L);
   }

   public PermissionInfo getInfo(String var1) {
      if (this.map.containsKey(var1)) {
         return this.map.get(var1);
      }

      for (PermissionInfo var3 : this.map.values()) {
         if (var3.isThisPermission(var1)) {
            return var3;
         }
      }

      return null;
   }

   public List<String> getSupportedPlugins() {
      return this.map.entrySet().stream().map(var0 -> var0.getValue().getPlugin()).distinct().collect(Collectors.toList());
   }

   public PermissionInfoList getPermissionInfosFromPlugin(String var1) {
      return this.map.values().stream().filter(var1x -> var1x.getPlugin().equalsIgnoreCase(var1)).collect(Collectors.toCollection(PermissionInfoList::new));
   }

   public PermissionInfoList getAllPermissionInfos() {
      return new PermissionInfoList(this.map.values());
   }
}
