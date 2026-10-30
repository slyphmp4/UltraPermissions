package me.TechsCode.UltraPermissions.base.spigot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParser;

public class SpigotPurchaseInfo {
   private static String USER_ID = "%%__USER__%%";
   private static String USERNAME = null;

   public SpigotPurchaseInfo() {
      super();
   }

   public static Optional<String> getUserId() {
      return USER_ID.contains("__USER__") ? Optional.empty() : Optional.of(USER_ID);
   }

   public static Optional<String> getUsername() {
      if (USERNAME != null) {
         return Optional.of(USERNAME);
      } else {
         Optional v0 = getUserId();
         if (v0.isPresent()) {
            USERNAME = fetchUsername((String)v0.get());
            return Optional.of(USERNAME);
         } else {
            return Optional.empty();
         }
      }
   }

   private static String fetchUsername(String v0) {
      try {
         java.net.URLConnection v1 = new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getAuthor&id=" + v0).openConnection();
         BufferedReader v2 = new BufferedReader(new InputStreamReader(v1.getInputStream()));
         StringBuilder v3 = new StringBuilder();

         String v4;
         while ((v4 = v2.readLine()) != null) {
            v3.append(v4);
         }

         v2.close();
         JsonParser v5 = new JsonParser();
         JsonObject v6 = (JsonObject)v5.parse(v3.toString());
         return !v6.has("username") ? "Unknown" : v6.get("username").getAsString();
      } catch (Exception var7) {
         return "Unknown";
      }
   }
}
