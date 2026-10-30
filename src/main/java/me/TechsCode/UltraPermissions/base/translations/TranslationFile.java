package me.TechsCode.UltraPermissions.base.translations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;

public class TranslationFile {
   private static final String[] disclaimer = new String[]{
      "#",
      "# Official Language File",
      "# To make your own language file, copy this file and edit the copy",
      "#",
      "# Changes made to this file will be reset!",
      "# Do not edit it!",
      "#",
      ""
   };
   private String name;
   private String language;
   private Map<String, String> phrases;

   public static TranslationFile createFile(File var0, String var1, String var2, Map<String, String> var3) {
      File var4 = new File(var0.getAbsolutePath() + "/" + var1 + "_" + var2 + ".lang");
      if (var4.exists() && !var4.delete()) {
         throw new IllegalStateException("No write access to directory");
      }

      try {
         if (var4.createNewFile()) {
            ArrayList var5 = new ArrayList<>(Arrays.asList(disclaimer));
            var3.entrySet()
               .stream()
               .sorted(Entry.comparingByKey())
               .forEach(var1x -> var5.add((String)var1x.getKey() + ": \"" + (String)var1x.getValue() + "\""));
            FileUtils.writeLines(var4, var5);
            return new TranslationFile(var4);
         } else {
            throw new IllegalStateException("No write access to file");
         }
      } catch (IOException var6) {
         var6.printStackTrace();
         return null;
      }
   }

   public TranslationFile(File var1) {
      super();
      String var2 = var1.getName();
      if (var2.endsWith(".lang") && var2.contains("_")) {
         this.name = var2.split("_")[0];
         this.language = var2.split("_")[1].replace(".lang", "");

         try {
            this.phrases = FileUtils.readLines(var1, StandardCharsets.UTF_8)
               .stream()
               .filter(var0 -> !var0.startsWith("#"))
               .map(var0 -> var0.split(": \""))
               .filter(var0 -> var0.length == 2)
               .filter(var0 -> !var0[0].contains("_"))
               .collect(Collectors.toMap(var0 -> (String)var0[0], var0 -> var0[1].replace("\"", "")));
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   public String getLanguage() {
      return this.language;
   }

   public String getName() {
      return this.name;
   }

   public Map<String, String> getPhrases() {
      return this.phrases;
   }

   public boolean isValid() {
      return this.name != null && this.language != null && this.phrases != null;
   }
}
