package me.TechsCode.UltraPermissions.base.legacy.utils;

import org.bukkit.util.Vector;

public class MathUtils {
   public MathUtils() {
      super();
   }

   public static Vector rotateAroundAxisX(Vector var0, double var1) {
      double var3 = Math.toRadians(var1);
      double var5 = Math.cos(var3);
      double var7 = Math.sin(var3);
      double var9 = var0.getY() * var7 - var0.getZ() * var5;
      double var11 = var0.getY() * var5 + var0.getZ() * var7;
      return var0.setY(var9).setZ(var11);
   }

   public static Vector rotateAroundAxisY(Vector var0, double var1) {
      double var3 = Math.toRadians(var1);
      double var5 = Math.cos(var3);
      double var7 = Math.sin(var3);
      double var9 = var0.getX() * var7 + var0.getZ() * var5;
      double var11 = var0.getX() * -var5 + var0.getZ() * var7;
      return var0.setX(var9).setZ(var11);
   }

   public static Vector rotateAroundAxisZ(Vector var0, double var1) {
      double var3 = Math.toRadians(var1);
      double var5 = Math.cos(var3);
      double var7 = Math.sin(var3);
      double var9 = var0.getX() * var7 - var0.getY() * var5;
      double var11 = var0.getX() * var5 + var0.getY() * var7;
      return var0.setX(var9).setY(var11);
   }
}
