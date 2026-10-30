package me.TechsCode.UltraPermissions.internal;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

public class PermissibleInjector {
   private static final String CRAFTBUKKIT_PREFIX = "org.bukkit.craftbukkit";
   private static final String VERSION;
   protected static final String className;
   protected static final String fieldName;

   public PermissibleInjector() {
      super();
   }

   public static String getCBClassName(String var0) {
      return VERSION == null ? null : "org.bukkit.craftbukkit" + VERSION + var0;
   }

   public static void inject(Player var0, PermissibleBase var1) throws IllegalAccessException {
      Field var2 = null;

      try {
         var2 = getPermissibleField(var0);
      } catch (Exception var4) {
      }

      if (var2 != null) {
         var2.set(var0, var1);
      }
   }

   private static Field getPermissibleField(Player var0) throws Exception {
      Class var1;
      try {
         var1 = Class.forName(className);
      } catch (ClassNotFoundException var3) {
         throw new Exception("Invalid Server Jar");
      }

      if (!var1.isAssignableFrom(var0.getClass())) {
         System.out.println("Failed to inject permissions for " + var0.getName());
         return null;
      } else {
         Field var2 = var1.getDeclaredField(fieldName);
         var2.setAccessible(true);
         return var2;
      }
   }

   static {
      Class var0 = Bukkit.getServer().getClass();
      if (!var0.getSimpleName().equals("CraftServer")) {
         VERSION = null;
      } else if (var0.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
         VERSION = ".";
      } else {
         String var1 = var0.getName();
         var1 = var1.substring("org.bukkit.craftbukkit".length());
         var1 = var1.substring(0, var1.length() - "CraftServer".length());
         VERSION = var1;
      }

      className = getCBClassName("entity.CraftHumanEntity");
      fieldName = "perm";
   }
}
