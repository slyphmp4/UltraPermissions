package me.TechsCode.UltraPermissions.base.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Public-address discovery must never run on a server startup or Bukkit thread.
 * Call {@link #resolve()} from a plugin-managed asynchronous task.
 */
public final class PublicIpResolver {
   private static final String LOOKUP_URL = "https://checkip.amazonaws.com";
   private static final int TIMEOUT_MS = 2000;

   private PublicIpResolver() {
   }

   public static String resolve() {
      try {
         return readAddress(new URL(LOOKUP_URL).openConnection());
      } catch (IOException | SecurityException failure) {
         return null;
      }
   }

   public static String readAddress(URLConnection connection) throws IOException {
      connection.setConnectTimeout(TIMEOUT_MS);
      connection.setReadTimeout(TIMEOUT_MS);
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
         String response = reader.readLine();
         if (response == null) {
            return null;
         }
         String address = response.trim();
         return address.length() <= 45 && address.matches("[0-9a-fA-F:.]{3,45}")
            && (address.contains(".") || address.contains(":")) ? address : null;
      }
   }
}
