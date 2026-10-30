package me.TechsCode.UltraPermissions.migration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryOptions;

public class LuckPermsMigration extends MigrationAssistant {
   public LuckPermsMigration() {
      super();
   }

   @Override
   public void migrate(UltraPermissions var1, Runnable var2) {
      LuckPerms var3 = LuckPermsProvider.get();
      HashMap<Group, me.TechsCode.UltraPermissions.storage.objects.Group> var4 = new HashMap<>();
      List<Group> var5 = var3.getGroupManager()
         .getLoadedGroups()
         .stream()
         .sorted(
            (var0, var1x) -> var0.getWeight().isPresent() && var1x.getWeight().isPresent() ? var0.getWeight().getAsInt() - var1x.getWeight().getAsInt() : 0
         )
         .collect(Collectors.toList());
      int var6 = 1;

      for (Group var8 : var5) {
         CachedMetaData var9 = var8.getCachedData().getMetaData();
         me.TechsCode.UltraPermissions.storage.objects.Group var10 = var1.newGroup(var8.getName())
            .setDefault(var8.getName().equals("default"))
            .setPrefix(var9.getPrefix())
            .setSuffix(var9.getSuffix())
            .setPriority(var6)
            .create();
         this.addNodes(var10, var8.getNodes(NodeType.PERMISSION));
         var4.put(var8, var10);
         var6++;
      }

      var4.forEach((var1x, var2x) -> {
         for (Group var4x : var1x.getInheritedGroups(QueryOptions.nonContextual())) {
            var2x.addInheritance((me.TechsCode.UltraPermissions.storage.objects.Group)var4.get(var4x));
         }
      });
      AtomicInteger var12 = new AtomicInteger();

      try {
         Set<java.util.UUID> var13 = var3.getUserManager().getUniqueUsers().get();
         var13.forEach(var7 -> var3.getUserManager().loadUser(var7).whenComplete((var6xx, var7x) -> {
            User var8x = var1.registerUser(var6xx.getUniqueId(), var6xx.getUsername() == null ? "NOT DEFINED" : var6xx.getUsername(), false);
            this.addNodes(var8x, var6xx.getNodes(NodeType.PERMISSION));

            for (Group var10x : var6xx.getInheritedGroups(QueryOptions.nonContextual())) {
               var8x.addGroup((me.TechsCode.UltraPermissions.storage.objects.Group)var4.get(var10x));
            }

            int var11x = var12.incrementAndGet();
            if (var13.size() == var11x) {
               var2.run();
            }
         }));
      } catch (InterruptedException | ExecutionException var11) {
         var11.printStackTrace();
      }
   }

   private void addNodes(PermissionHolder var1, Collection<PermissionNode> var2) {
      var2.forEach(
         var1x -> {
            Optional var2x = var1x.getContexts().getAnyValue("server");
            Optional var3 = var1x.getContexts().getAnyValue("world");
            var1.newPermission(var1x.getPermission())
               .setServer((String)var2x.orElse(null))
               .setWorld((String)var3.orElse(null))
               .setPositive(!var1x.isNegated())
               .setExpiration(var1x.getExpiry() != null ? var1x.getExpiry().toEpochMilli() : 0L)
               .create();
         }
      );
   }

   @Override
   public String getPluginName() {
      return "Luck Perms";
   }
}
