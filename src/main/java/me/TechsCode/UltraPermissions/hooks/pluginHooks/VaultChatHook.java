package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.hooks.placeholders.PrefixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SuffixPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class VaultChatHook extends Chat {
   private final UltraPermissions plugin;
   private volatile CacheSnapshot cache = new CacheSnapshot(Map.of(), Map.of());

   private static final class CacheSnapshot {
      private final Map<String, String> prefixes;
      private final Map<String, String> suffixes;

      private CacheSnapshot(Map<String, String> prefixes, Map<String, String> suffixes) {
         this.prefixes = prefixes;
         this.suffixes = suffixes;
      }
   }

   public VaultChatHook(Permission var1, UltraPermissions var2) {
      super(var1);
      this.plugin = var2;
      this.loadInCache().run();
   }

   public String getName() {
      return "UltraPermissions";
   }

   public boolean isEnabled() {
      return true;
   }

   public void clearCache() {
      this.plugin.getScheduler().run(this.loadInCache());
   }

   private Runnable loadInCache() {
      return () -> {
         Map<String, String> prefixes = new HashMap<>();
         Map<String, String> suffixes = new HashMap<>();
         NServer var1 = this.plugin.getThisServer().orElse(null);
         this.plugin.getUsers().stream().filter(Objects::nonNull).forEach(var2 -> {
            String playerName = var2.getName() == null ? "UNDEFINED" : var2.getName();
            prefixes.put(playerName, new PrefixPlaceholder().get(var2, var1));
            suffixes.put(playerName, new SuffixPlaceholder().get(var2, var1));
         });
         this.cache = new CacheSnapshot(Map.copyOf(prefixes), Map.copyOf(suffixes));
      };
   }

   public String getPlayerPrefix(String var1, String var2) {
      return this.cache.prefixes.get(var2);
   }

   public void setPlayerPrefix(String var1, String var2, String var3) {
      Optional<User> var4 = this.user(var2);
      var4.ifPresent(var1x -> var1x.setPrefix(var3));
   }

   public String getPlayerSuffix(String var1, String var2) {
      return this.cache.suffixes.get(var2);
   }

   public void setPlayerSuffix(String var1, String var2, String var3) {
      Optional<User> var4 = this.user(var2);
      var4.ifPresent(var1x -> var1x.setSuffix(var3));
   }

   public String getGroupPrefix(String var1, String var2) {
      Optional<Group> var3 = this.group(var2);
      return var3.<String>map(var0 -> var0.getPrefix().orElse(null)).orElse(null);
   }

   public void setGroupPrefix(String var1, String var2, String var3) {
      Optional<Group> var4 = this.group(var2);
      var4.ifPresent(var1x -> var1x.setPrefix(var3));
   }

   public String getGroupSuffix(String var1, String var2) {
      Optional<Group> var3 = this.group(var2);
      return var3.<String>map(var0 -> var0.getSuffix().orElse(null)).orElse(null);
   }

   public void setGroupSuffix(String var1, String var2, String var3) {
      Optional<Group> var4 = this.group(var2);
      var4.ifPresent(var1x -> var1x.setSuffix(var3));
   }

   public int getPlayerInfoInteger(String var1, String var2, String var3, int var4) {
      Optional<User> var5 = this.user(var2);
      return var5.<Integer>map(
            var4x -> var4x.getPermissions()
               .name(var3)
               .servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null))
               .worlds(true, var1)
               .stream()
               .filter(me.TechsCode.UltraPermissions.storage.objects.Permission::isPositive)
               .map(var0 -> var0.getName().split("\\."))
               .filter(var0 -> var0.length > 1)
               .mapToInt(var2xx -> this.convertOrDefault(var2xx[var2xx.length - 1], var4))
               .max()
               .orElse(var4)
         )
         .orElse(var4);
   }

   private int convertOrDefault(String var1, int var2) {
      try {
         return Integer.parseInt(var1);
      } catch (NumberFormatException var4) {
         return var2;
      }
   }

   public void setPlayerInfoInteger(String var1, String var2, String var3, int var4) {
   }

   public int getGroupInfoInteger(String var1, String var2, String var3, int var4) {
      Optional<Group> var5 = this.group(var2);
      return var5.<Integer>map(
            var4x -> var4x.getPermissions()
               .name(var3)
               .servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null))
               .worlds(true, var1)
               .stream()
               .filter(me.TechsCode.UltraPermissions.storage.objects.Permission::isPositive)
               .map(var0 -> var0.getName().split("\\."))
               .filter(var0 -> var0.length > 1)
               .mapToInt(var2xx -> this.convertOrDefault(var2xx[var2xx.length - 1], var4))
               .max()
               .orElse(var4)
         )
         .orElse(var4);
   }

   public void setGroupInfoInteger(String var1, String var2, String var3, int var4) {
   }

   public double getPlayerInfoDouble(String var1, String var2, String var3, double var4) {
      return 0.0;
   }

   public void setPlayerInfoDouble(String var1, String var2, String var3, double var4) {
   }

   public double getGroupInfoDouble(String var1, String var2, String var3, double var4) {
      return 0.0;
   }

   public void setGroupInfoDouble(String var1, String var2, String var3, double var4) {
   }

   public boolean getPlayerInfoBoolean(String var1, String var2, String var3, boolean var4) {
      return false;
   }

   public void setPlayerInfoBoolean(String var1, String var2, String var3, boolean var4) {
   }

   public boolean getGroupInfoBoolean(String var1, String var2, String var3, boolean var4) {
      return false;
   }

   public void setGroupInfoBoolean(String var1, String var2, String var3, boolean var4) {
   }

   public String getPlayerInfoString(String var1, String var2, String var3, String var4) {
      return null;
   }

   public void setPlayerInfoString(String var1, String var2, String var3, String var4) {
   }

   public String getGroupInfoString(String var1, String var2, String var3, String var4) {
      return null;
   }

   public void setGroupInfoString(String var1, String var2, String var3, String var4) {
   }

   public Optional<User> user(String var1) {
      return this.plugin.getUsers().name(var1);
   }

   public Optional<Group> group(String var1) {
      return this.plugin.getGroups().name(var1);
   }
}
