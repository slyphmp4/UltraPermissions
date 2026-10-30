package me.TechsCode.UltraPermissions.base.visual;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text {
   private static boolean gradientEnabled = false;
   private static boolean isBuneecord = false;
   private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("\\{#(([0-9A-Fa-f]{6})|([0-9A-Fa-f]{3}))}");
   private static final Pattern HEX_GRADIENT_COLOR_PATTERN = Pattern.compile("((?:[&§][uUlLoOkKrR])*)(\\{(#[^{}>]+)>})(.*?)(\\{(#.*?)<(>?)})");
   private static final Pattern STRIP_CHAT_COLOR_PATTERN = Pattern.compile("(§[0-9A-FK-ORk-orlumkr]|&[0-9A-FK-ORk-orlumkr])");

   public Text() {
      super();
   }

   public static String color(String var0) {
      if (isGradientEnabled()) {
         var0 = gradientColor(var0);
      }

      return chatColor(var0);
   }

   public static String stripColor(String var0) {
      return stripHexColor(stripChatColor(var0));
   }

   public static String stripChatColor(String var0) {
      return var0.replaceAll(STRIP_CHAT_COLOR_PATTERN.pattern(), "");
   }

   public static String stripHexColor(String var0) {
      return var0.replaceAll(HEX_COLOR_PATTERN.pattern(), "");
   }

   public static String chatColor(String var0) {
      Preconditions.checkArgument(var0 != null, "Text must be defined.");
      char[] var1 = var0.toCharArray();

      for (int var2 = 0; var2 < var1.length - 1; var2++) {
         if (var1[var2] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(var1[var2 + 1]) > -1) {
            var1[var2] = 167;
            var1[var2 + 1] = Character.toLowerCase(var1[var2 + 1]);
         }
      }

      return new String(var1);
   }

   public static String gradientColor(String var0) {
      Matcher var1 = HEX_GRADIENT_COLOR_PATTERN.matcher(var0);

      while (var1.find()) {
         String var2 = var1.group(0);
         String var3 = var1.group(1);
         String var4 = var1.group(3);
         String var5 = var1.group(4);
         String var6 = var1.group(6);
         boolean var7 = !var1.group(7).isEmpty();
         Color var8 = Color.from(var4.replace("#", ""));
         Color var9 = Color.from(var6.replace("#", ""));
         if (var8.isValid() && var9.isValid()) {
            LinkedHashSet<String> var10 = new LinkedHashSet<>(getFormats(var3));
            var10.addAll(ColorCalculations.getFormats(var5));
            var5 = stripColor(var5);
            StringBuilder var11 = new StringBuilder();

            for (int var12 = 0; var12 < var5.length(); var12++) {
               char var13 = var5.charAt(var12);
               double var14 = var12 * 100.0 / Math.max(1, var5.length() - 1);
               Color var16 = ColorCalculations.getColorInBetweenPercent(var8, var9, var14);
               var11.append("{#").append(var16.getColorCode()).append("}");

               for (String var18 : var10) {
                  var11.append(var18);
               }

               var11.append(var13);
            }

            if (var7) {
               var11.append("{#").append(var6.replace("#", "")).append(">").append("}");
            }

            var0 = var0.replace(var2, var11.toString());
            if (var7) {
               var0 = gradientColor(var0);
            }
         }
      }

      return hexColor(chatColor(var0));
   }

   public static String gradient(String var0, boolean var1, Color... var2) {
      Preconditions.checkArgument(var2.length > 1, "Define 2 or more colors.");
      int var3 = Math.max(1, var0.length() / var2.length);
      ArrayList var4 = new ArrayList();
      int var5 = 0;

      for (Color var9 : var2) {
         Color var10 = var2.length == var5 + 1 ? null : var2[var5 + 1];
         if (var10 != null) {
            var4.addAll(ColorCalculations.getColorsInBetween(var9, var10, var3));
         }

         var5++;
      }

      StringBuilder var13 = new StringBuilder();
      var5 = 0;

      for (char var17 : var0.toCharArray()) {
         Color var11 = var4.size() <= var5 ? (Color)var4.get(var4.size() - 1) : (Color)var4.get(var5);
         var13.append(var11.getAppliedTag()).append(var1 ? "§l" : "").append(var17);
         var5++;
      }

      return var13.toString();
   }

   private static Set<String> getFormats(String var0) {
      LinkedHashSet var1 = new LinkedHashSet();
      if (var0 == null) {
         return var1;
      }

      Matcher var2 = Pattern.compile("[&§]([uUlLoOkKrR])").matcher(var0);

      while (var2.find()) {
         var1.add(var2.group(1).toLowerCase());
      }

      return var1;
   }

   public static String hexColor(String var0) {
      Preconditions.checkArgument(var0 != null, "Text must be defined.");
      Matcher var1 = HEX_COLOR_PATTERN.matcher(var0);

      while (var1.find()) {
         String var2 = var1.group();
         String var3 = var2.replace("{", "").replace("}", "");
         if (isBungeeCord()) {
            var0 = var0.replace(var2, BungeeColor.colorOf(var3));
         } else {
            var3 = var3.replace("#", "");
            var0 = var0.replace(var2, Color.from(var3).getAppliedTag());
         }
      }

      return var0;
   }

   public static String firstUpperCase(String var0) {
      return var0 != null && !var0.isEmpty() ? var0.substring(0, 1).toUpperCase() + var0.substring(1) : var0;
   }

   public static void setEnableGradient(boolean var0) {
      gradientEnabled = var0;
   }

   public static boolean isGradientEnabled() {
      return gradientEnabled;
   }

   public static void setIsBungeeCord(boolean var0) {
      isBuneecord = var0;
   }

   public static boolean isBungeeCord() {
      return isBuneecord;
   }

   public static String capitalizeFirstLetter(String var0) {
      return var0 != null && !var0.isEmpty() ? var0.substring(0, 1).toUpperCase() + var0.substring(1) : var0;
   }
}
