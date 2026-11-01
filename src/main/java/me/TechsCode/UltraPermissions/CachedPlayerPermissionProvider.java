package me.TechsCode.UltraPermissions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class CachedPlayerPermissionProvider {
   // Vault and PlaceholderAPI can query arbitrarily many offline players.
   // Keep only the 2048 most recently used users to avoid indefinite retention.
   private static final int MAX_ENTRIES = 2048;
   private Optional<NServer> thisServer;
   private final UltraPermissions plugin;
   private final Map<UUID, CacheEntry> cache = new LinkedHashMap<>(64, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<UUID, CacheEntry> oldest) {
         return size() > MAX_ENTRIES;
      }
   };

   public CachedPlayerPermissionProvider(UltraPermissions plugin) {
      this.plugin = plugin;
      this.thisServer = plugin.getThisServer();
   }

   public synchronized PermissionList retrieve(UUID uuid, String world) {
      if (!thisServer.isPresent()) {
         Optional<NServer> detected = this.plugin.getThisServer();
         if (detected.isPresent()) {
            thisServer = detected;
            cache.clear();
         }
      }

      CacheEntry existing = cache.get(uuid);
      if (existing != null && Objects.equals(existing.world, world)) return existing.list;

      Optional<User> found = this.plugin.getUsers().uuid(uuid);
      if (!found.isPresent()) return new PermissionList();

      String server = thisServer.map(NServer::getName).orElse(null);
      GroupList groups = found.get().getActiveGroups()
         .servers(true, server).worlds(true, world)
         .getWithRecursiveInherits().servers(true, server).worlds(true, world);
      ArrayList<Holder> holders = new ArrayList<>(groups.holders());
      holders.add(Holder.fromUser(found.get()));
      PermissionList resolved = this.plugin.getPermissions().holder(holders).servers(true, server).worlds(true, world);
      cache.put(uuid, new CacheEntry(resolved, world));
      return resolved;
   }

   public synchronized void clearCache() {
      cache.clear();
   }

   public class CacheEntry {
      final PermissionList list;
      final String world;

      public CacheEntry(PermissionList list, String world) {
         this.list = list;
         this.world = world;
      }
   }
}
