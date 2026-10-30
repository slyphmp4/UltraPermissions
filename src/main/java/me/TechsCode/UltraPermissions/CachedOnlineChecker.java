package me.TechsCode.UltraPermissions;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CachedOnlineChecker {
   private Set<UUID> cache;

   public static CachedOnlineChecker scan() {
      return new CachedOnlineChecker(Bukkit.getOnlinePlayers().stream().<UUID>map(OfflinePlayer::getUniqueId).collect(Collectors.toSet()));
   }

   private CachedOnlineChecker(Set<UUID> var1) {
      super();
      this.cache = var1;
   }

   public boolean isOnline(UUID var1) {
      return this.cache.contains(var1);
   }

   public boolean isOnline(Player var1) {
      return this.isOnline(var1.getUniqueId());
   }
}
