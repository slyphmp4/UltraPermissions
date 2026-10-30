package me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Title {
   public Title() {
      super();
   }

   public static void sendPacket(Player var0, Object var1) {
      try {
         Object var2 = var0.getClass().getMethod("getHandle").invoke(var0);
         Object var3 = var2.getClass().getField("playerConnection").get(var2);
         var3.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(var3, var1);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public static Class<?> getNMSClass(String var0) {
      String var1 = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

      try {
         return Class.forName("net.minecraft.server." + var1 + "." + var0);
      } catch (ClassNotFoundException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static void sendTitle(Player var0, Integer var1, Integer var2, Integer var3, String var4) {
      sendTitle(var0, var1, var2, var3, var4, null);
   }

   public static void sendSubtitle(Player var0, Integer var1, Integer var2, Integer var3, String var4) {
      sendTitle(var0, var1, var2, var3, null, var4);
   }

   public static void sendFullTitle(Player var0, Integer var1, Integer var2, Integer var3, String var4, String var5) {
      sendTitle(var0, var1, var2, var3, var4, var5);
   }

   public static void sendTitle(Player var0, Integer var1, Integer var2, Integer var3, String var4, String var5) {
      try {
         Method var6 = var0.getClass().getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
         if (var6 != null) {
            var6.invoke(var0, var4, var5, var1, var2, var3);
            return;
         }
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException var11) {
      }

      try {
         if (var4 != null) {
            String var12 = ChatColor.translateAlternateColorCodes('&', var4);
            var4 = var12.replaceAll("%player%", var0.getDisplayName());
            Object var15 = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
            Object var7 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var4 + "\"}");
            Constructor var8 = getNMSClass("PacketPlayOutTitle")
               .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object var9 = var8.newInstance(var15, var7, var1, var2, var3);
            sendPacket(var0, var9);
            var15 = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            var7 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var4 + "\"}");
            var8 = getNMSClass("PacketPlayOutTitle")
               .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
            var9 = var8.newInstance(var15, var7);
            sendPacket(var0, var9);
         }

         if (var5 != null) {
            var5 = ChatColor.translateAlternateColorCodes('&', var5);
            var5 = var5.replaceAll("%player%", var0.getDisplayName());
            Object var17 = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
            Object var20 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var4 + "\"}");
            Constructor var23 = getNMSClass("PacketPlayOutTitle")
               .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object var26 = var23.newInstance(var17, var20, var1, var2, var3);
            sendPacket(var0, var26);
            var17 = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            var20 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var5 + "\"}");
            var23 = getNMSClass("PacketPlayOutTitle")
               .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            var26 = var23.newInstance(var17, var20, var1, var2, var3);
            sendPacket(var0, var26);
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }
   }

   public static void clearTitle(Player var0) {
      sendTitle(var0, 0, 1, 0, "", "");
   }

   public static void sendTabTitle(Player var0, String var1, String var2) {
      if (var1 == null) {
         var1 = "";
      }

      var1 = ChatColor.translateAlternateColorCodes('&', var1);
      if (var2 == null) {
         var2 = "";
      }

      var2 = ChatColor.translateAlternateColorCodes('&', var2);
      var1 = var1.replaceAll("%player%", var0.getDisplayName());
      var2 = var2.replaceAll("%player%", var0.getDisplayName());

      try {
         Object var3 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var1 + "\"}");
         Object var4 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + var2 + "\"}");
         Constructor var5 = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
         Object var6 = var5.newInstance();
         Field var7 = var6.getClass().getDeclaredField("a");
         var7.setAccessible(true);
         var7.set(var6, var3);
         Field var8 = var6.getClass().getDeclaredField("b");
         var8.setAccessible(true);
         var8.set(var6, var4);
         sendPacket(var0, var6);
      } catch (Exception var9) {
         var9.printStackTrace();
      }
   }
}
