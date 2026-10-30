package me.TechsCode.UltraPermissions.base.legacy.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockUtils {
   public BlockUtils() {
      super();
   }

   public static Block[] getBlocksInRadius(Location var0, int var1) {
      ArrayList<Block> var2 = new ArrayList<>();

      for (double var3 = var0.getX() - var1; var3 <= var0.getX() + var1; var3++) {
         for (double var5 = var0.getY() - var1; var5 <= var0.getY() + var1; var5++) {
            for (double var7 = var0.getZ() - var1; var7 <= var0.getZ() + var1; var7++) {
               Location var9 = new Location(var0.getWorld(), var3, var5, var7);
               var2.add(var9.getBlock());
            }
         }
      }

      return var2.toArray(new Block[0]);
   }

   public static BlockFace[] getBlockSides() {
      return new BlockFace[]{BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
   }

   public static Block[] getAttachedBlocks(Block var0) {
      return Arrays.stream(getBlockSides()).map(var0::getRelative).toArray(Block[]::new);
   }

   public static Set<Block> getBlocksInSphere(Location var0, int var1, boolean var2) {
      HashSet var3 = new HashSet();
      int var4 = var0.getBlockX();
      int var5 = var0.getBlockY();
      int var6 = var0.getBlockZ();

      for (int var7 = var4 - var1; var7 <= var4 + var1; var7++) {
         for (int var8 = var5 - var1; var8 <= var5 + var1; var8++) {
            for (int var9 = var6 - var1; var9 <= var6 + var1; var9++) {
               double var10 = (var4 - var7) * (var4 - var7) + (var6 - var9) * (var6 - var9) + (var5 - var8) * (var5 - var8);
               if (var10 < var1 * var1 && (!var2 || !(var10 < (var1 - 1) * (var1 - 1)))) {
                  Location var12 = new Location(var0.getWorld(), var7, var8, var9);
                  var3.add(var12.getBlock());
               }
            }
         }
      }

      return var3;
   }
}
