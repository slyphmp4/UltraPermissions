package me.TechsCode.UltraPermissions.base.item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Entity picker display names use the selected plugin language, independent of
 * the Minecraft client's language or the item chosen to represent an entity.
 */
public final class LocalizedEntityNames {
   private static final Map<String, String> RUSSIAN = load("translations/entities_Russian.properties");
   private static final Map<String, String> UKRAINIAN = load("translations/entities_Ukrainian.properties");

   private LocalizedEntityNames() {
   }

   public static String get(XEntityType entity, String language) {
      Map<String, String> labels;
      if ("Russian".equalsIgnoreCase(language)) {
         labels = RUSSIAN;
      } else if ("Ukrainian".equalsIgnoreCase(language)) {
         labels = UKRAINIAN;
      } else {
         return entity.name().replace('_', ' ');
      }
      return labels.getOrDefault(entity.name().toLowerCase(Locale.ROOT), entity.name().replace('_', ' '));
   }

   private static Map<String, String> load(String resourcePath) {
      try (InputStream resource = LocalizedEntityNames.class.getClassLoader().getResourceAsStream(resourcePath)) {
         if (resource == null) {
            return Collections.emptyMap();
         }
         Properties properties = new Properties();
         properties.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
         Map<String, String> result = new HashMap<>();
         for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
         }
         return Collections.unmodifiableMap(result);
      } catch (IOException ignored) {
         return Collections.emptyMap();
      }
   }
}
