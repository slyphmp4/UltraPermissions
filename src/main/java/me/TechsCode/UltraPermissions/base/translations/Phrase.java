package me.TechsCode.UltraPermissions.base.translations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.dependencies.commons.lang3.StringUtils;

public class Phrase {
   private final String name;
   private final String defaultPhrase;
   private final Future<Color> mainColor;
   private final List<Future<Color>> colors;
   private TranslationManager translationManager;

   public static Phrase create(String var0, String var1, Future<Color> var2, Future<Color>... var3) {
      return new Phrase(var0, var1, var2, Arrays.asList(var3));
   }

   public static Phrase create(String var0, String var1, Color var2, Color... var3) {
      ArrayList var4 = new ArrayList();

      for (Color var8 : var3) {
         Future var9 = () -> var8;
         var4.add(var9);
      }

      return new Phrase(var0, var1, () -> var2, Collections.unmodifiableList(var4));
   }

   public static Phrase create(String var0, String var1) {
      return new Phrase(var0, var1, null, Collections.emptyList());
   }

   private Phrase(String var1, String var2, Future<Color> var3, List<Future<Color>> var4) {
      super();
      this.name = var1;
      this.defaultPhrase = var2;
      this.mainColor = var3;
      this.colors = var4;
      int var5 = StringUtils.countMatches(var2, "**");
      if (var5 % 2 != 0) {
         throw new IllegalArgumentException("Invalid Coloring of Phrase '" + var1 + "' (Uneven Markers)");
      }

      if (var5 / 2 != var4.size()) {
         throw new IllegalArgumentException("Invalid Coloring of Phrase '" + var1 + "' (Invalid Amount of Markers)");
      }
   }

   public void inject(TranslationManager var1) {
      this.translationManager = var1;
   }

   public String firstUpper() {
      return Text.firstUpperCase(this.get());
   }

   public String lower() {
      return this.get().toLowerCase();
   }

   public String upper() {
      return this.get().toUpperCase();
   }

   public String get() {
      String var1 = null;
      if (this.translationManager != null) {
         var1 = this.translationManager.getTranslation(this.name);
      }

      boolean var2 = var1 != null && StringUtils.countMatches(this.defaultPhrase, "**") != StringUtils.countMatches(var1, "**");
      String var3 = var1 != null && !var2 ? var1 : this.defaultPhrase;
      if (this.mainColor != null) {
         List var4 = this.colors.stream().map(Future::get).collect(Collectors.toList());
         Pattern var5 = Pattern.compile("\\*\\*");
         Matcher var6 = var5.matcher(var3);
         StringBuffer var7 = new StringBuffer();

         for (boolean var8 = false; var6.find(); var8 = !var8) {
            if (var8) {
               var6.appendReplacement(var7, this.mainColor.get().getAppliedTag());
            } else {
               var6.appendReplacement(var7, ((Color)var4.get(0)).getAppliedTag());
               var4.remove(0);
            }
         }

         var6.appendTail(var7);
         var3 = this.mainColor.get().getAppliedTag() + Text.color(var7.toString());
      }

      return var3;
   }

   public String[] split(int var1) {
      String var2 = this.get();
      ArrayList<String> var3 = new ArrayList<>();
      String var4 = "";

      for (String var8 : var2.split(" ")) {
         int var9 = Text.stripColor(var4).length();
         if (var9 > var1) {
            var3.add(var4);
            var4 = "";
         }

         var4 = (var4 + " " + var8).trim();
      }

      if (!var4.isEmpty()) {
         var3.add(var4);
      }

      return var3.stream()
         .map(var1x -> this.mainColor != null && !var1x.startsWith(this.mainColor.get().getAppliedTag()) ? this.mainColor.get().getAppliedTag() + var1x : var1x)
         .toArray(String[]::new);
   }

   public String getName() {
      return this.name;
   }

   public String getDefaultPhrase() {
      return this.defaultPhrase;
   }

   @Override
   public String toString() {
      return this.get();
   }
}
