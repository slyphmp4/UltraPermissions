package me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import org.bukkit.Bukkit;

public class Reflection {
   public Reflection() {
      super();
   }

   public static Class<?> getNMSClass(String var0) {
      return MinecraftVersion.getServersVersion().isBelow(MinecraftVersion.V1_20_R4)
         ? getClass("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + "." + var0)
         : getClass(Bukkit.getServer().getClass().getPackage().getName() + "." + var0);
   }

   public static Class<?> getCraftClass(String var0) {
      return MinecraftVersion.getServersVersion().isBelow(MinecraftVersion.V1_20_R4)
         ? getClass("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + "." + var0)
         : getClass(Bukkit.getServer().getClass().getPackage().getName() + "." + var0);
   }

   public static Class<?> getClass(String var0) {
      try {
         return Class.forName(var0);
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static Object getValue(Object var0, String var1) {
      try {
         Field var2 = var0.getClass().getDeclaredField(var1);
         if (!var2.isAccessible()) {
            var2.setAccessible(true);
         }

         return var2.get(var0);
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static Method getMethod(Object var0, String var1, Class<?>... var2) {
      try {
         Method var3 = var0.getClass().getMethod(var1, var2);
         if (!var3.isAccessible()) {
            var3.setAccessible(true);
         }

         return var3;
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public static Field getField(Field var0) {
      var0.setAccessible(true);
      return var0;
   }
}
