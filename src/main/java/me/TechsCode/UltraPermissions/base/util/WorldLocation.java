package me.TechsCode.UltraPermissions.base.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class WorldLocation extends Location {
   private final String worldName;

   public static String serialize(WorldLocation var0) {
      return var0.getWorldName() + ";" + var0.getBlockX() + ";" + var0.getBlockY() + ";" + var0.getBlockZ();
   }

   public static WorldLocation deserialize(String var0) {
      String[] var1 = var0.split(";");
      return new WorldLocation(var1[0], Integer.parseInt(var1[1]), Integer.parseInt(var1[2]), Integer.parseInt(var1[3]));
   }

   public static WorldLocation toObj(Location var0) {
      if (var0.getWorld() == null) {
         throw new IllegalArgumentException("Location world cannot be null when converting to ShopLocation");
      } else {
         return new WorldLocation(var0.getWorld().getName(), var0.getX(), var0.getY(), var0.getZ());
      }
   }

   public WorldLocation(@NonNls String var1, double var2, double var4, double var6) {
      super(Bukkit.getWorld(var1), var2, var4, var6);
      this.worldName = var1;
   }

   public boolean blockEqual(Location var1) {
      if (var1.getWorld() != null && this.getWorld() != null) {
         return !var1.getWorld().equals(this.getWorld())
            ? false
            : var1.getBlockX() == this.getBlockX() && var1.getBlockY() == this.getBlockY() && var1.getBlockZ() == this.getBlockZ();
      } else {
         return false;
      }
   }

   @Nullable
   public World getWorld() {
      World var1 = super.getWorld();
      if (var1 == null) {
         super.setWorld(Bukkit.getWorld(this.worldName));
      }

      return super.getWorld();
   }

   public String getWorldName() {
      return this.worldName;
   }
}
