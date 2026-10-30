package me.TechsCode.UltraPermissions.base.util;

import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import org.bukkit.entity.Player;

public class Experience {
   public Experience() {
      super();
   }

   public static int levelToExp(int var0) {
      MinecraftVersion var1 = MinecraftVersion.getServersVersion();
      if (var1.isBelow(MinecraftVersion.V1_8_R1)) {
         if (var0 <= 15) {
            return 17 * var0;
         } else {
            return var0 <= 30 ? 3 * var0 * var0 / 2 - 59 * var0 / 2 + 360 : 7 * var0 * var0 / 2 - 303 * var0 / 2 + 2220;
         }
      } else if (var0 <= 15) {
         return var0 * var0 + 6 * var0;
      } else {
         return var0 <= 30 ? (int)(2.5 * var0 * var0 - 40.5 * var0 + 360.0) : (int)(4.5 * var0 * var0 - 162.5 * var0 + 2220.0);
      }
   }

   public static int totalExpToLevelUp(int var0) {
      MinecraftVersion var1 = MinecraftVersion.getServersVersion();
      if (var1.isBelow(MinecraftVersion.V1_8_R1)) {
         if (var0 <= 16) {
            return 17;
         } else {
            return var0 <= 31 ? 3 * var0 - 31 : 7 * var0 - 155;
         }
      } else if (var0 <= 15) {
         return 2 * var0 + 7;
      } else {
         return var0 <= 30 ? 5 * var0 - 38 : 9 * var0 - 158;
      }
   }

   public static int getLevel(Player var0) {
      return var0 == null ? 0 : var0.getLevel();
   }

   public static void setLevel(Player var0, int var1) {
      if (var0 != null) {
         var0.setLevel(0);
         var0.setExp(0.0F);
         var0.setTotalExperience(0);
         var0.setLevel(var1);
      }
   }

   public static int levelFromExp(long var0) {
      return (int)Math.floor((Math.sqrt(72.0 * var0 - 54215.0) + 325.0) / 18.0);
   }

   public static void setTotalExperience(Player var0, long var1) {
      if (var0 != null) {
         var1 = (long)Number.clamp(var1, 0.0, 3.4028234663852886E38);
         MinecraftVersion var3 = MinecraftVersion.getServersVersion();
         if (!var3.isBelow(MinecraftVersion.V1_8_R1) && var1 >= 2147483647L) {
            int var4 = levelFromExp(var1);
            float var5 = (float)(var1 - levelToExp(var4));
            float var6 = totalExpToLevelUp(var4);
            var0.setTotalExperience(0);
            var0.setLevel(var4);
            var0.setExp((float)Number.clamp(var5 / var6, 0.0, 1.0));
         } else {
            var0.setLevel(0);
            var0.setExp(0.0F);
            var0.setTotalExperience(0);
            var0.giveExp((int)var1);
         }
      }
   }

   public static void addExp(Player var0, int var1) {
      if (var0 != null) {
         setTotalExperience(var0, getTotalExp(var0) + var1);
      }
   }

   public static void takeExp(Player var0, int var1) {
      if (var0 != null) {
         setTotalExperience(var0, getTotalExp(var0) - var1);
      }
   }

   public static long getMissingExp(Player var0) {
      return var0 == null ? 0L : totalExpToLevelUp(var0.getLevel()) - getExpForCurrentLevel(var0);
   }

   public static long getExpForCurrentLevel(Player var0) {
      return var0 == null ? 0L : getTotalExp(var0) - levelToExp(var0.getLevel());
   }

   public static long getTotalExp(Player var0) {
      return var0 == null ? 0L : levelToExp(var0.getLevel()) + Math.round(totalExpToLevelUp(var0.getLevel()) * var0.getExp());
   }
}
