package me.TechsCode.UltraPermissions.base.util;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import me.TechsCode.UltraPermissions.base.debug.TechException;

public final class TimeUtil {
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
   private static final DateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
   private static final DateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("dd.MM HH:mm");
   private static final Pattern TOKEN_PATTERN = Pattern.compile(
      "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?",
      2
   );

   public static long currentTimeSeconds() {
      return System.currentTimeMillis() / 1000L;
   }

   public static long currentTimeTicks() {
      return System.currentTimeMillis() / 50L;
   }

   public static String getFormattedDate() {
      return getFormattedDate(System.currentTimeMillis());
   }

   public static String getFormattedDate(long var0) {
      return DATE_FORMAT.format(var0);
   }

   public static String getFormattedDateShort() {
      return DATE_FORMAT_SHORT.format(System.currentTimeMillis());
   }

   public static String getFormattedDateShort(long var0) {
      return DATE_FORMAT_SHORT.format(var0);
   }

   public static String getFormattedDateMonth(long var0) {
      return DATE_FORMAT_MONTH.format(var0);
   }

   public static long toTicks(String var0) {
      if (var0 == null) {
         throw new TechException("Time is null");
      }

      long var1 = 0L;
      String[] var3 = var0.split(" ");
      if (var3.length < 2) {
         throw new IllegalArgumentException("Expected human readable time like '1 second', got '" + var0 + "' instead");
      }

      for (int var4 = 1; var4 < var3.length; var4++) {
         String var5 = var3[var4].toLowerCase();
         int var6 = 0;
         long var7 = 0L;
         boolean var9 = false;

         try {
            var6 = Integer.parseInt(var3[var4 - 1]);
         } catch (NumberFormatException var11) {
            continue;
         }

         if (var5.startsWith("tick")) {
            var9 = true;
         } else if (var5.startsWith("second")) {
            var7 = 1L;
         } else if (var5.startsWith("minute")) {
            var7 = 60L;
         } else if (var5.startsWith("hour")) {
            var7 = 3600L;
         } else if (var5.startsWith("day")) {
            var7 = 86400L;
         } else if (var5.startsWith("week")) {
            var7 = 604800L;
         } else if (var5.startsWith("month")) {
            var7 = 2629743L;
         } else if (var5.startsWith("year")) {
            var7 = 31556926L;
         } else {
            if (!var5.startsWith("potato")) {
               throw new IllegalArgumentException("Must define date type! Example: '1 second' (Got '" + var5 + "')");
            }

            var7 = 1337L;
         }

         var1 += var6 * (var9 ? 1L : var7 * 20L);
      }

      return var1;
   }

   public static String formatTimeGeneric(long var0) {
      long var2 = var0 % 60L;
      long var4 = var0 / 60L;
      String var6 = "";
      if (var4 >= 60L) {
         long var7 = var0 / 60L / 60L;
         var4 %= 60L;
         var6 = var7 + (var7 == 1L ? " hour" : " hours") + " ";
      }

      return var6
         + (var4 != 0L ? var4 : "")
         + (var4 > 0L ? (var4 == 1L ? " minute" : " minutes") + " " : "")
         + Long.parseLong(String.valueOf(var2))
         + (Long.parseLong(String.valueOf(var2)) == 1L ? " second" : " seconds");
   }

   public static String formatTimeDays(long var0) {
      long var2 = var0 / 60L;
      long var4 = var2 / 60L;
      long var6 = var4 / 24L;
      return var6 + " days " + var4 % 24L + " hours " + var2 % 60L + " minutes " + var0 % 60L + " seconds";
   }

   public static String formatTimeShort(long var0) {
      long var2 = var0 / 60L;
      long var4 = var2 / 60L;
      long var6 = var4 / 24L;
      var4 %= 24L;
      var2 %= 60L;
      var0 %= 60L;
      return (var6 > 0L ? var6 + "d " : "") + (var4 > 0L ? var4 + "h " : "") + (var2 > 0L ? var2 + "m " : "") + var0 + "s";
   }

   public static String formatTimeColon(long var0) {
      long var2 = var0 / 60L;
      long var4 = var2 / 60L;
      long var6 = var4 / 24L;
      var4 %= 24L;
      var2 %= 60L;
      var0 %= 60L;
      return (var6 > 0L ? (var6 < 10L ? "0" : "") + var6 + ":" : "")
         + (var4 > 0L ? (var4 < 10L ? "0" : "") + var4 + ":" : "")
         + (var2 > 0L ? (var2 < 10L ? "0" : "") + var2 + ":" : "00")
         + (var0 < 10L ? "0" : "")
         + var0;
   }

   public static long parseToken(String var0) {
      Matcher var1 = TOKEN_PATTERN.matcher(var0);
      long var2 = 0L;
      long var4 = 0L;
      long var6 = 0L;
      long var8 = 0L;
      long var10 = 0L;
      long var12 = 0L;
      long var14 = 0L;
      boolean var16 = false;

      while (var1.find()) {
         if (var1.group() != null && !var1.group().isEmpty()) {
            for (int var17 = 0; var17 < var1.groupCount(); var17++) {
               if (var1.group(var17) != null && !var1.group(var17).isEmpty()) {
                  var16 = true;
                  break;
               }
            }

            if (var16) {
               for (int var20 = 1; var20 < 8; var20++) {
                  if (var1.group(var20) != null && !var1.group(var20).isEmpty()) {
                     long var18 = Long.parseLong(var1.group(var20));
                     if (var20 == 1) {
                        checkLimit("years", var18, 10);
                        var2 = var18;
                     } else if (var20 == 2) {
                        checkLimit("months", var18, 1200);
                        var4 = var18;
                     } else if (var20 == 3) {
                        checkLimit("weeks", var18, 400);
                        var6 = var18;
                     } else if (var20 == 4) {
                        checkLimit("days", var18, 3100);
                        var8 = var18;
                     } else if (var20 == 5) {
                        checkLimit("hours", var18, 2400);
                        var10 = var18;
                     } else if (var20 == 6) {
                        checkLimit("minutes", var18, 6000);
                        var12 = var18;
                     } else if (var20 == 7) {
                        checkLimit("seconds", var18, 6000);
                        var14 = var18;
                     }
                  }
               }
               break;
            }
         }
      }

      if (!var16) {
         throw new NumberFormatException("Date not found from: " + var0);
      } else {
         return (var14 + var12 * 60L + var10 * 3600L + var8 * 86400L + var6 * 7L * 86400L + var4 * 30L * 86400L + var2 * 365L * 86400L) * 1000L;
      }
   }

   private static void checkLimit(String var0, long var1, int var3) {
      if (var1 > var3) {
         throw new IllegalArgumentException("Value type " + var0 + " is out of bounds! Max limit: " + var3 + ", given: " + var1);
      }
   }

   public static String toSQLTimestamp() {
      return toSQLTimestamp(System.currentTimeMillis());
   }

   public static String toSQLTimestamp(long var0) {
      Date var2 = new Date(var0);
      return new Timestamp(var2.getTime()).toString();
   }

   public static long fromSQLTimestamp(String var0) {
      return Timestamp.valueOf(var0).getTime();
   }

   @Generated
   private TimeUtil() {
      super();
   }
}
