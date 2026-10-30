package me.TechsCode.UltraPermissions.base.legacy.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class Armorstands {
   private static final Method[] methods = ((java.util.function.Supplier<Method[]>)(() -> {
         try {
            Method var0 = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
            return new Method[]{
               var0, var0.getReturnType().getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)
            };
         } catch (Exception var1) {
            return null;
         }
      }))
      .get();

   public Armorstands() {
      super();
   }

   public static void move(ArmorStand var0, Location var1) {
      try {
         methods[1].invoke(methods[0].invoke(var0), var1.getX(), var1.getY(), var1.getZ(), var1.getYaw(), var1.getPitch());
      } catch (IllegalAccessException | InvocationTargetException var3) {
         var3.printStackTrace();
      }
   }
}
