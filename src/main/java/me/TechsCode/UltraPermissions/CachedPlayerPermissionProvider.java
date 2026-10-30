package me.TechsCode.UltraPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class CachedPlayerPermissionProvider {
   private static Optional<NServer> thisServer;
   private UltraPermissions plugin;
   private Map<UUID, CachedPlayerPermissionProvider.CacheEntry> cache;

   public CachedPlayerPermissionProvider(UltraPermissions var1) {
      super();
      thisServer = var1.getThisServer();
      this.plugin = var1;
      this.cache = new HashMap<>();
   }

   public PermissionList retrieve(UUID var1, String var2) {
      if ((thisServer == null || !thisServer.isPresent()) && this.plugin.getThisServer().isPresent()) {
         thisServer = this.plugin.getThisServer();
         this.cache.clear();
      }

      if (this.cache.containsKey(var1)) {
         CachedPlayerPermissionProvider.CacheEntry var3 = this.cache.get(var1);
         if (var3.world.equals(var2)) {
            return var3.list;
         }
      }

      Optional var8 = this.plugin.getUsers().uuid(var1);
      if (!var8.isPresent()) {
         return new PermissionList();
      }

      String var4 = thisServer.map(NServer::getName).orElse(null);
      GroupList var5 = ((User)var8.get())
         .getActiveGroups()
         .servers(true, var4)
         .worlds(true, var2)
         .getWithRecursiveInherits()
         .servers(true, var4)
         .worlds(true, var2);
      ArrayList var6 = new ArrayList<>(var5.holders());
      var6.add(Holder.fromUser((User)var8.get()));
      PermissionList var7 = this.plugin.getPermissions().holder(var6).servers(true, var4).worlds(true, var2);
      this.cache.put(var1, new CachedPlayerPermissionProvider.CacheEntry(var7, var2));
      return var7;
   }

   public void clearCache() {
      this.cache = new HashMap<>();
   }

   public class CacheEntry {
      final PermissionList list;
      final String world;

      public CacheEntry(PermissionList var2, String var3) {
         super();
         this.list = var2;
         this.world = var3;
      }
   }
}
