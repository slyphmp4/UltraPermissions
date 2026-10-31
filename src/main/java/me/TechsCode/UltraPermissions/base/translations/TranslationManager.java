package me.TechsCode.UltraPermissions.base.translations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.reflection.indexer.ClasspathIndexer;
import me.TechsCode.UltraPermissions.base.reflection.indexer.Node;
import me.TechsCode.UltraPermissions.base.registry.RegistrationChoice;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;

public class TranslationManager {
   private final TranslationRegistry translationRegistry;
   private final Map<String, Phrase> phrases;
   private List<TranslationFile> translationFiles;
   private Map<String, String> translationCache;

   public TranslationManager(TechPlugin<?> var1) {
      super();
      this.translationRegistry = var1.getRegistry().register(new TranslationRegistry(), RegistrationChoice.GLOBAL_IF_AVAILABLE);
      File var2 = new File(var1.getPluginFolder().getAbsolutePath() + "/Languages");
      var2.mkdirs();
      Map<Phrase, Class<?>> var3 = this.loadPhrasesFromClasses(var1);
      validatePhrases(var3.keySet());
      this.phrases = var3.entrySet().stream().collect(Collectors.toMap(var0 -> ((Phrase)var0.getKey()).getName(), Entry::getKey));
      this.phrases.values().forEach(var1x -> var1x.inject(this));
      var1.getScheduler()
         .runAsync(
            () -> {
               createDefaultLanguageFile(var3, var2, "Base", var1xx -> var1xx.getValue().getName().startsWith("me.TechsCode." + var1.getName() + ".base"));
               createDefaultLanguageFile(
                  var3,
                  var2,
                  var1.getName(),
                  var1xx -> var1xx.getValue().getName().startsWith("me.TechsCode." + var1.getName()) && !var1xx.getValue().getName().contains(".base")
               );
               installBundledTranslations(var1, var2);
               this.translationFiles = Arrays.stream(Objects.requireNonNull(var2.listFiles()))
                  .map(TranslationFile::new)
                  .filter(TranslationFile::isValid)
                  .collect(Collectors.toList());
               var1.log("Translations successfully loaded & applied (" + this.translationFiles.size() + " Translation files found)");
            }
         );
   }

   public String getTranslation(String var1) {
      if (this.translationCache != null) {
         return this.translationCache.get(var1);
      }

      this.translationCache = this.getPhrases(this.getSelectedLanguage());
      return this.translationCache == null ? null : this.translationCache.get(var1);
   }

   public Map<String, String> getPhrases(String var1) {
      return this.translationFiles == null
         ? null
         : this.translationFiles
            .stream()
            .filter(var1x -> var1x.getLanguage().equalsIgnoreCase(var1))
            .flatMap(var0 -> var0.getPhrases().entrySet().stream())
            .filter(var1x -> this.phrases.containsKey(var1x.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
   }

   // Ship our maintained locales with the plugin. No network dependency and no
   // upstream overwrite of community translations on every server restart.
   private static void installBundledTranslations(TechPlugin<?> plugin, File languageFolder) {
      String[] bundled = {
         "Base_Russian.lang", "UltraPermissions_Russian.lang",
         "Base_Ukrainian.lang", "UltraPermissions_Ukrainian.lang"
      };
      for (String fileName : bundled) {
         try (InputStream resource = plugin.getResource("translations/" + fileName)) {
            if (resource == null) {
               plugin.log("Bundled translation missing: " + fileName);
               continue;
            }
            FileUtils.copyInputStreamToFile(resource, new File(languageFolder, fileName));
         } catch (IOException exception) {
            plugin.log("Could not install bundled translation " + fileName + ": " + exception.getMessage());
         }
      }
   }

   private static void createDefaultLanguageFile(Map<Phrase, Class<?>> var0, File var1, String var2, Predicate<? super Entry<Phrase, Class<?>>> var3) {
      Map var4 = var0.entrySet().stream().filter(var3).map(Entry::getKey).collect(Collectors.toMap(Phrase::getName, Phrase::getDefaultPhrase));
      TranslationFile.createFile(var1, var2, "English", var4);
   }

   private static void validatePhrases(Collection<Phrase> var0) {
      ArrayList var1 = new ArrayList();

      for (Phrase var3 : var0) {
         if (var1.contains(var3.getName())) {
            throw new IllegalStateException("Duplicate Phrase '" + var3.getName() + "'");
         }

         if (var3.getDefaultPhrase().contains("§")) {
            throw new IllegalStateException("Phrase '" + var3.getName() + "' contains illegal symbol");
         }

         var1.add(var3.getName());
      }
   }

   private Map<Phrase, Class<?>> loadPhrasesFromClasses(TechPlugin<?> var1) {
      HashMap var2 = new HashMap();

      for (Node var4 : ClasspathIndexer.index()
         .classes()
         .stream()
         .filter(
            var1x -> var1x.getClassName().startsWith("me.TechsCode")
               && !var1x.getClassName().startsWith("me.TechsCode." + var1.getName() + ".dependencies")
               && !var1x.getClassName().startsWith("me.TechsCode." + var1.getName() + ".internal")
               && !var1x.getClassName().startsWith("me.TechsCode." + var1.getName() + ".reflection")
         )
         .collect(Collectors.toList())) {
         try {
            Optional var5 = var4.getAsClass();
            if (var5.isPresent()) {
               for (Field var9 : ((Class)var5.get()).getDeclaredFields()) {
                  if (Modifier.isStatic(var9.getModifiers()) && var9.getType() == Phrase.class) {
                     try {
                        var9.setAccessible(true);
                        Phrase var10 = (Phrase)var9.get(null);
                        var2.put(var10, (Class)var5.get());
                     } catch (IllegalAccessException var11) {
                     }
                  }
               }
            }
         } catch (NoClassDefFoundError | ExceptionInInitializerError var12) {
         }
      }

      return var2;
   }

   public String getSelectedLanguage() {
      return this.translationRegistry.getLanguage();
   }

   public void setSelectedLanguage(String var1) {
      this.translationRegistry.setLanguage(var1);
      this.translationCache = null;
   }

   public List<String> getAvailableLanguages() {
      return this.translationFiles.stream().map(TranslationFile::getLanguage).distinct().collect(Collectors.toList());
   }
}
