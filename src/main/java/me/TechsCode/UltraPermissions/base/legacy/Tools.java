package me.TechsCode.UltraPermissions.base.legacy;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.visual.Text;

public class Tools {
   public Tools() {
      super();
   }

   public static String getNumberString(long var0) {
      DecimalFormat var2 = new DecimalFormat("#,###");
      return var2.format(var0);
   }

   public static String getNumberString(double var0) {
      DecimalFormat var2 = new DecimalFormat("#,###");
      return var2.format(var0);
   }

   public static String getNumberString(float var0) {
      DecimalFormat var1 = new DecimalFormat("#,###");
      return var1.format(var0);
   }

   public static String readableFileSize(long var0) {
      if (var0 <= 0L) {
         return "0";
      }

      String[] var2 = new String[]{"B", "kB", "MB", "GB", "TB"};
      int var3 = (int)(Math.log10(var0) / Math.log10(1024.0));
      return new DecimalFormat("#,##0.#").format(var0 / Math.pow(1024.0, var3)) + " " + var2[var3];
   }

   public static String[] splitCamelCase(String var0) {
      return var0.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
   }

   public static String firstUpperCase(String var0) {
      return var0.substring(0, 1).toUpperCase() + var0.substring(1);
   }

   public static String[] lineSplitter(String var0, int var1) {
      ArrayList<String> var2 = new ArrayList<>();
      String var3 = "";

      for (String var7 : var0.split(" ")) {
         var3 = var3.trim();
         if (var3.length() + var7.length() > var1) {
            var2.add(var3);
            var3 = "";
         }

         var3 = var3 + " " + var7;
      }

      var3 = var3.trim();
      if (var3.length() != 0) {
         var2.add(var3);
      }

      return var2.toArray(new String[0]);
   }

   public static String removeEofsAndLineBreakers(String var0) {
      if (var0 != null && var0.length() != 0) {
         int var1 = var0.length();
         char[] var2 = new char[var1];
         int var3 = 0;
         int var4 = 0;
         boolean var5 = true;

         for (int var6 = 0; var6 < var1; var6++) {
            char var7 = var0.charAt(var6);
            boolean var8 = Character.isWhitespace(var7);
            if (!var8) {
               var5 = false;
               var2[var3++] = var7 == 160 ? ' ' : var7;
               var4 = 0;
            } else {
               if (var4 == 0 && !var5) {
                  var2[var3++] = " ".charAt(0);
               }

               var4++;
            }
         }

         return var5 ? "" : new String(var2, 0, var3 - (var4 > 0 ? 1 : 0)).trim();
      } else {
         return var0;
      }
   }

   public static String getEnumName(Enum<?> var0) {
      return String.join(
         " ",
         Arrays.stream(var0.name().split("_"))
            .map(var0x -> var0x.toUpperCase().charAt(0) + var0x.toLowerCase().substring(1))
            .collect(Collectors.toCollection(ArrayList::new))
      );
   }

   public static String getSecretPassword(String var0) {
      return String.join("", Collections.nCopies(var0.length(), "*"));
   }

   public static String getFormattedAgoString(long var0, TimeUnit var2) {
      var0 = var2.toSeconds(var0);
      if (var0 < 1L) {
         return "Now";
      } else if (var0 > TimeUnit.DAYS.toSeconds(1L)) {
         return getTimeString(TimeUnit.SECONDS.toDays(var0), TimeUnit.DAYS);
      } else if (var0 > TimeUnit.HOURS.toSeconds(1L)) {
         return getTimeString(TimeUnit.SECONDS.toHours(var0), TimeUnit.HOURS);
      } else {
         return var0 > TimeUnit.MINUTES.toSeconds(1L)
            ? getTimeString(TimeUnit.SECONDS.toMinutes(var0), TimeUnit.MINUTES)
            : getTimeString(TimeUnit.SECONDS.toSeconds(var0), TimeUnit.SECONDS);
      }
   }

   public static String getTicksString(long var0) {
      return var0 == 0L ? var0 + " Ticks" : var0 + " Tick";
   }

   public static String getTimeString(long var0) {
      return getTimeString(var0, TimeUnit.SECONDS);
   }

   public static String getTimeString(long var0, TimeUnit var2) {
      return getTimeString(var0, var2, 0);
   }

   public static String getTimeString(long var0, TimeUnit var2, int var3) {
      var0 = var2.toSeconds(var0);
      if (var0 < 1L) {
         return "Now";
      }

      long var4 = TimeUnit.SECONDS.toDays(var0);
      var0 -= TimeUnit.DAYS.toSeconds(var4);
      long var6 = TimeUnit.SECONDS.toHours(var0);
      var0 -= TimeUnit.HOURS.toSeconds(var6);
      long var8 = TimeUnit.SECONDS.toMinutes(var0);
      var0 -= TimeUnit.MINUTES.toSeconds(var8);
      long var10 = TimeUnit.SECONDS.toSeconds(var0);
      ArrayList var12 = new ArrayList();
      if (var4 != 0L) {
         Phrase var13 = var4 == 1L ? Words.DAY : Words.DAYS;
         var12.add(var4 + " " + var13.firstUpper());
      }

      if (var6 != 0L) {
         Phrase var18 = var6 == 1L ? Words.HOUR : Words.HOURS;
         var12.add(var6 + " " + var18.firstUpper());
      }

      if (var8 != 0L) {
         Phrase var19 = var8 == 1L ? Words.MINUTE : Words.MINUTES;
         var12.add(var8 + " " + var19.firstUpper());
      }

      if (var10 != 0L) {
         Phrase var20 = var10 == 1L ? Words.SECOND : Words.SECONDS;
         var12.add(var10 + " " + var20.firstUpper());
      }

      if (var3 != 0) {
         while (var12.size() > var3) {
            var12.remove(var12.size() - 1);
         }
      }

      return String.join(", ", var12);
   }

   public static File exportFile(String var0, boolean var1, SpigotTechPlugin var2) {
      File var3 = new File(var2.getPluginFolder() + "/" + var0);
      if (var1 || !var3.exists()) {
         var2.getBootstrap().saveResource(var0, var1);
      }

      return var3;
   }

   public static String getProgressBar(int var0, int var1, int var2, String var3, String var4, String var5) {
      float var6 = (float)var0 / var1;
      int var7 = (int)(var2 * var6);
      int var8 = var2 - var7;
      StringBuilder var9 = new StringBuilder();
      var9.append(Text.chatColor(var4));
      IntStream.range(0, var7).mapToObj(var1x -> var3).forEach(var9::append);
      var9.append(Text.chatColor(var5));
      IntStream.range(0, var8).mapToObj(var1x -> var3).forEach(var9::append);
      return var9.toString();
   }

   public static long getTimeSecondsFromString(String var0) {
      return Arrays.stream(var0.split(" ")).mapToLong(Tools::getTimeFromWord).sum();
   }

   private static long getTimeFromWord(String var0) {
      try {
         if (var0.length() < 2) {
            return 0L;
         }

         String var1 = var0.toCharArray()[var0.length() - 1] + "";
         int var2 = Integer.parseInt(var0.substring(0, var0.length() - 1));
         TimeUnit[] var3 = new TimeUnit[]{TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};
         TimeUnit var4 = Arrays.stream(var3).filter(var1x -> var1x.toString().toLowerCase().startsWith(var1)).findFirst().orElse(null);
         return var4 == null ? 0L : var4.toSeconds(var2);
      } catch (NumberFormatException var5) {
         return 0L;
      }
   }
}
