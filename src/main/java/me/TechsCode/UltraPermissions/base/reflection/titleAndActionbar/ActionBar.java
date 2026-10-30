package me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.visual.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBar {
   private static SpigotTechPlugin plugin;
   private static String nmsver;
   private static boolean useOldMethods;

   public ActionBar(SpigotTechPlugin var1) {
      super();
      plugin = var1;
      if (nmsver == null) {
         nmsver = Bukkit.getServer().getClass().getPackage().getName();
         nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
         if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.startsWith("v1_7_")) {
            useOldMethods = true;
         }
      }
   }

   public void sendActionBar(Player var1, String var2) {
      this.sendActionBarNoColorReplacement(var1, Text.color(var2));
   }

   public void sendActionBarNoColorReplacement(Player var1, String var2) {
      try {
         var1.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(var2));
      } catch (NoSuchMethodError var21) {
         try {
            Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
         } catch (ClassNotFoundException var18) {
            return;
         }

         try {
            Class var4 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
            Object var5 = var4.cast(var1);
            Class var6 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
            Class var7 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
            Object var8;
            if (useOldMethods) {
               Class var22 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
               Class var24 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
               Method var26 = var22.getDeclaredMethod("a", String.class);
               Object var29 = var24.cast(var26.invoke(var22, "{\"text\": \"" + var2 + "\"}"));
               var8 = var6.getConstructor(var24, byte.class).newInstance(var29, (byte)2);
            } else {
               Class var9 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
               Class var10 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");

               try {
                  Class var11 = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
                  Object[] var28 = var11.getEnumConstants();
                  Object var13 = null;

                  for (Object var17 : var28) {
                     if (var17.toString().equals("GAME_INFO")) {
                        var13 = var17;
                     }
                  }

                  Object var32 = var9.getConstructor(String.class).newInstance(var2);
                  var8 = var6.getConstructor(var10, var11).newInstance(var32, var13);
               } catch (ClassNotFoundException var19) {
                  Object var12 = var9.getConstructor(String.class).newInstance(var2);
                  var8 = var6.getConstructor(var10, byte.class).newInstance(var12, (byte)2);
               }
            }

            Method var23 = var4.getDeclaredMethod("getHandle");
            Object var25 = var23.invoke(var5);
            Field var27 = var25.getClass().getDeclaredField("playerConnection");
            Object var30 = var27.get(var25);
            Method var31 = var30.getClass().getDeclaredMethod("sendPacket", var7);
            var31.invoke(var30, var8);
         } catch (Exception var20) {
            var21.printStackTrace();
         }
      }
   }

   public void sendActionBar(final Player var1, final String var2, int var3) {
      this.sendActionBar(var1, var2);
      if (var3 >= 0) {
         (new BukkitRunnable() {
            public void run() {
               ActionBar.this.sendActionBar(var1, "");
            }
         }).runTaskLater((Plugin)plugin.getBootstrap(), var3 + 1);
      }

      while (var3 > 40) {
         var3 -= 40;
         (new BukkitRunnable() {
            public void run() {
               ActionBar.this.sendActionBar(var1, var2);
            }
         }).runTaskLater((Plugin)plugin.getBootstrap(), var3);
      }
   }

   public void sendActionBarToAllPlayers(String var1) {
      this.sendActionBarToAllPlayers(var1, -1);
   }

   public void sendActionBarToAllPlayers(String var1, int var2) {
      for (Player var4 : Bukkit.getOnlinePlayers()) {
         this.sendActionBar(var4, var1, var2);
      }
   }
}
