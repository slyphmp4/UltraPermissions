package me.TechsCode.UltraPermissions.base.debug;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import lombok.NonNull;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.util.TimeUtil;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;

public class Debugger {
   public Debugger() {
      super();
   }

   public static void debug(String var0, String... var1) {
      DebugRegistery var2 = TechPlugin.getInstance().getDebugRegistery();
      if (!var2.getAvailableDebuggers().contains(var0)) {
         var2.addAvailableDebugger(var0);
      }

      if (TechPlugin.isDebugging && TechPlugin.getInstance().getDebugRegistery().isDebugging(var0)) {
         for (String var6 : var1) {
            TechPlugin.getInstance().log("§e[" + var0 + "] §r" + var6);
         }
      }
   }

   public static void printStackTrace(String var0) {
      StackTraceElement[] var1 = new Exception().getStackTrace();
      TechPlugin.getInstance().log("!----------------------------------------------------------------------------------------------------------!");
      TechPlugin.getInstance().log(var0);
      TechPlugin.getInstance().log("!----------------------------------------------------------------------------------------------------------!");

      for (int var2 = 1; var2 < var1.length; var2++) {
         String var3 = var1[var2].toString();
         TechPlugin.getInstance().log("\tat " + var3);
      }

      TechPlugin.getInstance().log("--------------------------------------------------------------------------------------------------------end-");
   }

   public static void printStackTrace(@NonNull Throwable var0) {
      if (var0 == null) {
         throw new NullPointerException("throwable is marked non-null but is null");
      }

      ArrayList var1 = new ArrayList();
      if (var0.getCause() != null) {
         Throwable var2 = var0.getCause();

         do {
            var1.add(var2);
         } while ((var2 = var2.getCause()) != null);
      }

      if (var0 instanceof TechException && !var1.isEmpty()) {
         TechPlugin.getInstance().log(var0.getMessage());
      } else {
         TechPlugin.getInstance().log(var0.toString());
         printStackTraceElements(var0);
      }

      if (!var1.isEmpty()) {
         Throwable var3 = (Throwable)var1.get(var1.size() - 1);
         TechPlugin.getInstance().log(var3.toString());
         printStackTraceElements(var3);
      }
   }

   private static void printStackTraceElements(Throwable var0) {
      for (StackTraceElement var4 : var0.getStackTrace()) {
         String var5 = var4.toString();
         TechPlugin.getInstance().log("\tat " + var5);
      }
   }

   public static void saveError(Throwable var0, String... var1) {
      String var2;
      if (TechPlugin.getInstance() != null) {
         var2 = "Running " + Bukkit.getName() + " " + Bukkit.getBukkitVersion() + " and Java " + System.getProperty("java.version");
      } else {
         var2 = "Running "
            + ProxyServer.getInstance().getName()
            + " "
            + ProxyServer.getInstance().getVersion()
            + " and Java "
            + System.getProperty("java.version");
      }

      try {
         ArrayList var3 = new ArrayList();
         String var4 = TechPlugin.getInstance().getName() + " " + TechPlugin.getInstance().getVersion() + " encountered " + var0.getClass().getSimpleName();
         fill(
            var3,
            "------------------------------------[ " + TimeUtil.getFormattedDate() + " ]-----------------------------------",
            var4,
            var2,
            "Plugins: "
               + joinListWithCommas(
                  Arrays.asList(
                     SpigotTechPlugin.getInstance() != null
                        ? Bukkit.getPluginManager().getPlugins()
                        : new Object[]{ProxyServer.getInstance().getPluginManager().getPlugins()}
                  )
               ),
            "----------------------------------------------------------------------------------------------"
         );
         if (var1 != null && !String.join("", var1).isEmpty()) {
            fill(var3, "\nMore Information: ");
            fill(var3, var1);
         }

         do {
            fill(
               var3,
               var0 == null
                  ? "Unknown error"
                  : var0.getClass().getSimpleName() + " " + getOrDefault(var0.getMessage(), getOrDefault(var0.getLocalizedMessage(), "(Unknown cause)"))
            );
            int var5 = 0;

            for (StackTraceElement var9 : var0.getStackTrace()) {
               var5++;
               String var10 = var9.toString();
               if (!var10.contains("sun.reflect")) {
                  if (var5 > 6 && var10.startsWith("net.minecraft.server")) {
                     break;
                  }

                  fill(var3, "\t at " + var9.toString());
               }
            }
         } while ((var0 = var0.getCause()) != null);

         fill(var3, "----------------------------------------------------------------------------------------------", System.lineSeparator());
         if (TechPlugin.getInstance() != null) {
            Bukkit.getLogger().severe(var4 + "! Please check your error.log and report this issue with the information in that file. " + var2);
         } else {
            ProxyServer.getInstance()
               .getLogger()
               .severe(var4 + "! Please check your error.log and report this issue with the information in that file. " + var2);
         }

         File var12 = new File(TechPlugin.getInstance().getPluginFolder(), "error.log");
         if (!var12.exists()) {
            var12.createNewFile();
         }

         FileWriter var13 = new FileWriter(var12, true);
         Iterator var14 = var3.iterator();

         while (var14.hasNext()) {
            String var15 = (String)var14.next();
            var13.write(var15 + (var14.hasNext() ? System.lineSeparator() : ""));
         }

         var13.close();
      } catch (Throwable var11) {
         if (TechPlugin.getInstance() != null) {
            Bukkit.getLogger().log(Level.SEVERE, "Got error when saving another error! Saving error:", var11);
            Bukkit.getLogger().log(Level.SEVERE, "Original error that is not saved:", var0);
         } else {
            ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Got error when saving another error! Saving error:", var11);
            ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Original error that is not saved:", var0);
         }

         saveError(var11, "Original error that is not saved:");
      }
   }

   private static void fill(List<String> var0, String... var1) {
      var0.addAll(Arrays.asList(var1));
   }

   public static String joinListWithCommas(List<?> var0) {
      if (var0 != null && !var0.isEmpty()) {
         StringBuilder var1 = new StringBuilder();

         for (int var2 = 0; var2 < var0.size(); var2++) {
            var1.append(var0.get(var2).toString());
            if (var2 < var0.size() - 1) {
               var1.append(", ");
            }
         }

         return var1.toString();
      } else {
         return "";
      }
   }

   public static <T> T getOrDefault(T var0, T var1) {
      return (T)(!(var0 instanceof String) || !"none".equalsIgnoreCase((String)var0) && !"".equals(var0) ? getOrDefaultStrict((T)var0, (T)var1) : var1);
   }

   public static <T> T getOrDefaultStrict(T var0, T var1) {
      return (T)(var0 != null ? var0 : var1);
   }
}
