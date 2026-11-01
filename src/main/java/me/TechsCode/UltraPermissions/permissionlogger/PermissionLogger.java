package me.TechsCode.UltraPermissions.permissionlogger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.TechsCode.UltraPermissions.UltraPermissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/** Bounded, latest-per-player permission log; writes are O(1) in the hot permission path. */
public class PermissionLogger implements Listener {
   private static final int MAX_ENTRIES = 500;
   private final LinkedHashMap<LogKey, LoggedPermission> entries = new LinkedHashMap<>();

   private record LogKey(UUID player, String permission) {}

   public PermissionLogger(UltraPermissions plugin) {
      Bukkit.getPluginManager().registerEvents(this, (Plugin)plugin.getBootstrap());
   }

   public synchronized void log(Player player, String permission, boolean granted, String source) {
      if (player == null || permission == null) return;
      LogKey key = new LogKey(player.getUniqueId(), permission);
      // A repeated check replaces the previous entry and becomes the most recent.
      entries.remove(key);
      entries.put(key, new LoggedPermission(player, permission, granted, System.currentTimeMillis(), source));
      if (entries.size() > MAX_ENTRIES) {
         Iterator<LogKey> oldest = entries.keySet().iterator();
         oldest.next();
         oldest.remove();
      }
   }

   public synchronized LoggedPermission[] getLoggedPermissions() {
      return entries.values().stream()
         .sorted(Comparator.comparing(LoggedPermission::getTime, Comparator.reverseOrder())
            .thenComparing(LoggedPermission::getPermission))
         .toArray(LoggedPermission[]::new);
   }

   @EventHandler
   public void clear(PlayerQuitEvent event) {
      if (event != null && event.getPlayer() != null) clearPlayer(event.getPlayer().getUniqueId());
   }

   public synchronized void clearPlayer(UUID playerId) {
      if (playerId != null) entries.keySet().removeIf(key -> key.player().equals(playerId));
   }

   public synchronized void clearManually(Player player, String search) {
      if (player == null || search == null) return;
      UUID id = player.getUniqueId();
      entries.keySet().removeIf(key -> key.player().equals(id) && key.permission().contains(search));
   }
}
