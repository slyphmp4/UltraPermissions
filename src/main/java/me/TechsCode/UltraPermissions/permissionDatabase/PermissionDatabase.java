package me.TechsCode.UltraPermissions.permissionDatabase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

public class PermissionDatabase {
   // Publish completed snapshots only. The GUI reads this map on the server thread,
   // while the remote permission catalogue is refreshed asynchronously.
   private volatile java.util.Map<String, PermissionInfo> map = java.util.Collections.emptyMap();
   public static final String githubLink = "https://raw.githubusercontent.com/TechsCode-Team/PluginPermissions/master/Permissions/Database.updb";
   private static final int MAX_DATABASE_BYTES = 8 * 1024 * 1024;

   public PermissionDatabase(UltraPermissions plugin) {
      // Bukkit's plugin manager and descriptors must be inspected on the primary thread.
      HashMap<String, PermissionInfo> builtIn = new HashMap<>();
      for (Plugin installed : Bukkit.getPluginManager().getPlugins()) {
         for (Permission permission : installed.getDescription().getPermissions()) {
            builtIn.put(permission.getName(), new PermissionInfo(permission.getName(), installed.getName(),
                permission.getDescription(), new String[0], "Extracted from .jar"));
         }
      }
      this.map = java.util.Collections.unmodifiableMap(builtIn);
      plugin.log("- " + builtIn.size() + " Permissions extracted from .jars");

      Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)plugin.getBootstrap(), () -> {
         File cache = new File(plugin.getPluginFolder(), "Main.updb");
         boolean downloaded = false;
         try {
            java.net.URLConnection connection = new URL(githubLink).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            byte[] bytes;
            try (java.io.InputStream stream = connection.getInputStream()) {
               bytes = me.TechsCode.UltraPermissions.base.security.BoundedInput.read(stream, MAX_DATABASE_BYTES);
            }
            // A transient error page must not overwrite a working cached database.
            String remoteText = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            if (countValidRecords(java.util.Arrays.asList(remoteText.split("\\R"))) == 0) {
               throw new IOException("Cloud catalogue has no valid records");
            }
            me.TechsCode.UltraPermissions.base.fileconf.AtomicFileSave.save(cache,
                temporary -> java.nio.file.Files.write(temporary.toPath(), bytes));
            downloaded = true;
         } catch (IOException unavailable) {
            plugin.log("Permission cloud catalogue unavailable; using the last validated local cache.");
         }

         if (!cache.isFile()) {
            plugin.log("No cached permission catalogue available.");
            return;
         }
         try {
            if (java.nio.file.Files.size(cache.toPath()) > MAX_DATABASE_BYTES) {
               throw new IOException("Cached permission catalogue exceeds the size limit");
            }
            HashMap<String, PermissionInfo> next = new HashMap<>(builtIn);
            int accepted = 0;
            int skipped = 0;
            for (String line : java.nio.file.Files.readAllLines(cache.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
               if (line.isBlank() || line.startsWith("#")) continue;
               if (!safeRecord(line)) {
                  skipped++;
                  continue;
               }
               String[] parts = line.split("\\+", -1);
               String[] commands = parts.length == 4 ? parts[3].split(",") : new String[0];
               next.put(parts[1], new PermissionInfo(parts[1], parts[0],
                   parts.length >= 3 ? parts[2] : "", commands, "Permission Database (" + cache.getName() + ")"));
               accepted++;
            }
            this.map = java.util.Collections.unmodifiableMap(next);
            plugin.log("- " + accepted + " Permissions " + (downloaded ? "downloaded from Cloud" : "loaded from Cache"));
            if (skipped != 0) plugin.log("- " + skipped + " malformed permission catalogue records ignored");
         } catch (IOException unavailable) {
            plugin.log("Could not read permission catalogue cache; keeping built-in permissions.");
         }
      }, 20L);
   }

   /** Untrusted remote metadata must not inject control characters or giant GUI text. */
   public static boolean safeRecord(String line) {
      if (line == null || line.isEmpty() || line.length() > 2048
          || line.chars().anyMatch(Character::isISOControl)) return false;
      String[] fields = line.split("\\+", -1);
      return fields.length >= 2 && fields.length <= 4
          && !fields[0].isBlank() && fields[0].length() <= 96
          && !fields[1].isBlank() && fields[1].length() <= 192
          && (fields.length < 3 || fields[2].length() <= 1024)
          && (fields.length < 4 || fields[3].length() <= 512);
   }

   private static int countValidRecords(java.util.List<String> lines) {
      int count = 0;
      for (String line : lines) if (safeRecord(line)) count++;
      return count;
   }

   public PermissionInfo getInfo(String var1) {
      if (this.map.containsKey(var1)) {
         return this.map.get(var1);
      }

      for (PermissionInfo var3 : this.map.values()) {
         if (var3.isThisPermission(var1)) {
            return var3;
         }
      }

      return null;
   }

   public List<String> getSupportedPlugins() {
      return this.map.entrySet().stream().map(var0 -> var0.getValue().getPlugin()).distinct().collect(Collectors.toList());
   }

   public PermissionInfoList getPermissionInfosFromPlugin(String var1) {
      return this.map.values().stream().filter(var1x -> var1x.getPlugin().equalsIgnoreCase(var1)).collect(Collectors.toCollection(PermissionInfoList::new));
   }

   public PermissionInfoList getAllPermissionInfos() {
      return new PermissionInfoList(this.map.values());
   }
}
