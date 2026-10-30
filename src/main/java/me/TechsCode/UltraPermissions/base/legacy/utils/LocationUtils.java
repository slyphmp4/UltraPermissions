package me.TechsCode.UltraPermissions.base.legacy.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class LocationUtils {
   public LocationUtils() {
      super();
   }

   public static Location getValidSpawnPoint(Location var0) {
      return getValidSpawnPoint(var0, 1);
   }

   private static Location getValidSpawnPoint(Location var0, int var1) {
      for (int var2 = var0.getBlockX() - var1; var2 <= var0.getBlockX() + var1; var2++) {
         for (int var3 = var0.getBlockY() - var1; var3 <= var0.getBlockY() + var1; var3++) {
            for (int var4 = var0.getBlockZ() - var1; var4 <= var0.getBlockZ() + var1; var4++) {
               Location var5 = new Location(var0.getWorld(), var2 + 0.5, var3, var4 + 0.5);
               Location var6 = var5.clone().add(0.0, 1.0, 0.0);
               if (var5.getBlock().getType() == Material.AIR && var6.getBlock().getType() == Material.AIR) {
                  return var5;
               }
            }
         }
      }

      return getValidSpawnPoint(var0, var1 + 1);
   }

   public static String serialize(Location var0) {
      return var0.getWorld().getName() + ";" + var0.getBlockX() + ";" + var0.getBlockY() + ";" + var0.getBlockZ();
   }

   public static Location deserialize(String var0) {
      String[] var1 = var0.split(";");
      return new Location(Bukkit.getWorld(var1[0]), Integer.parseInt(var1[1]), Integer.parseInt(var1[2]), Integer.parseInt(var1[3]));
   }
}
