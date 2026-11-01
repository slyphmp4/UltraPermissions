package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.CachedPlayerPermissionProvider;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.PermissionLookup;
import me.TechsCode.UltraPermissions.internal.lookup.checks.BlankCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefaultPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefinedPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.OperatorLookupCheck;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultPermissionHook extends Permission {
   private final UltraPermissions plugin;
   private final CachedPlayerPermissionProvider permissionProvider;

   public VaultPermissionHook(UltraPermissions var1) {
      super();
      this.plugin = var1;
      this.permissionProvider = new CachedPlayerPermissionProvider(var1);
   }

   public void clearCache() {
      this.permissionProvider.clearCache();
   }

   public String getName() {
      return "UltraPermissions";
   }

   public boolean isEnabled() {
      return true;
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean playerHas(String var1, final String var2, final String var3) {
      if (var2 == null) {
         return false;
      }

      if (var1 == null) {
         var1 = UUID.randomUUID().toString();
      }

      final String var4 = var1;
      PermissionLookup var5 = new PermissionLookup(var3) {
         @Override
         public LookupCheck[] getChecks() {
            OfflinePlayer var1x = Bukkit.getOfflinePlayer(var2);
            return new LookupCheck[]{
               var1x != null ? new OperatorLookupCheck(VaultPermissionHook.this.plugin, var1x) : new BlankCheck(),
               VaultPermissionHook.this.plugin.isDefaultPermissionsEnabled() && var1x != null ? new DefaultPermissionCheck(var1x) : new BlankCheck(),
               new DefinedPermissionCheck() {
                  @Override
                  public List<me.TechsCode.UltraPermissions.storage.objects.Permission> getDefinedPermissions() {
                     Optional var1x = VaultPermissionHook.this.user(var2);
                     if (!var1x.isPresent()) {
                        return Collections.emptyList();
                     }

                     User var2x = (User)var1x.get();
                     PermissionList var3x = VaultPermissionHook.this.plugin
                        .getPermissions()
                        .holder(Collections.singletonList(Holder.fromUser(var2x)))
                        .servers(true, VaultPermissionHook.this.plugin.getThisServer().map(NServer::getName).orElse(null))
                        .worlds(true, var4);
                     Optional var4x = var3x.name(var3).stream().findFirst();
                     if (var4x.isPresent()) {
                        return Collections.singletonList((me.TechsCode.UltraPermissions.storage.objects.Permission)var4x.get());
                     }

                     for (Group var7 : var2x.getActiveGroups()
                        .servers(true, VaultPermissionHook.this.plugin.getThisServer().map(NServer::getName).orElse(null))
                        .worlds(true, var4 != null && !var4.isEmpty() ? var4 : "world")
                        .stream()
                        .sorted(Comparator.comparingInt(Group::getPriority))
                        .toList()) {
                        PermissionList var8 = VaultPermissionHook.this.plugin
                           .getPermissions()
                           .holder(Collections.singletonList(Holder.fromGroup(var7)))
                           .servers(true, VaultPermissionHook.this.plugin.getThisServer().map(NServer::getName).orElse(null))
                           .worlds(true, var4);
                        Optional var9 = var8.name(var3).stream().findFirst();
                        if (var9.isPresent()) {
                           return Collections.singletonList((me.TechsCode.UltraPermissions.storage.objects.Permission)var9.get());
                        }
                     }

                     return Collections.emptyList();
                  }
               }
            };
         }
      };
      return var5.perform().getResult();
   }

   public boolean playerHas(String var1, OfflinePlayer var2, String var3) {
      return this.playerHas(var1, var2.getName(), var3);
   }

   public boolean playerHas(Player var1, String var2) {
      return var1.hasPermission(var2);
   }

   public boolean playerAdd(String var1, String var2, String var3) {
      return this.add(var1, this.user(var2).orElse(null), var3);
   }

   public boolean playerAdd(String var1, OfflinePlayer var2, String var3) {
      return this.playerAdd(var1, var2.getName(), var3);
   }

   public boolean playerAdd(Player var1, String var2) {
      return this.playerAdd(var1.getWorld().getName(), var1.getName(), var2);
   }

   public boolean playerRemove(String var1, String var2, String var3) {
      return this.remove(var1, this.user(var2).orElse(null), var3);
   }

   public boolean playerRemove(String var1, OfflinePlayer var2, String var3) {
      return this.playerRemove(var1, var2.getName(), var3);
   }

   public boolean playerRemove(Player var1, String var2) {
      return this.playerRemove(var1.getWorld().getName(), var1.getName(), var2);
   }

   public boolean groupHas(String world, String groupName, String permission) {
      Preconditions.checkArgument(groupName != null, "Group Name has to be defined");
      if (permission == null) {
         return false;
      }

      Optional<Group> group = this.group(world, groupName);
      if (group.isEmpty()) {
         return false;
      }

      PermissionLookup lookup = new PermissionLookup(permission) {
         @Override
         public LookupCheck[] getChecks() {
            return new LookupCheck[]{new DefinedPermissionCheck() {
               @Override
               public List<me.TechsCode.UltraPermissions.storage.objects.Permission> getDefinedPermissions() {
                  // These are fresh lists; do not change the group's stored permissions.
                  PermissionList defined = group.get().getPermissions();
                  defined.addAll(group.get().getInheritedPermissions());
                  return defined
                     .servers(true, VaultPermissionHook.this.plugin.getThisServer().map(NServer::getName).orElse(null))
                     .worlds(true, world);
               }
            }};
         }
      };
      return lookup.perform().getResult();
   }

   public boolean groupHas(World world, String groupName, String permission) {
      return this.groupHas(world == null ? null : world.getName(), groupName, permission);
   }

   public boolean groupAdd(String var1, String var2, String var3) {
      return this.add(var1, this.group(var1, var2).orElse(null), var3);
   }

   public boolean groupAdd(World var1, String var2, String var3) {
      return this.groupAdd(var1.getName(), var2, var3);
   }

   public boolean groupRemove(String var1, String var2, String var3) {
      return this.remove(var1, this.group(var1, var2).orElse(null), var3);
   }

   public boolean groupRemove(World var1, String var2, String var3) {
      return this.groupRemove(var1.getName(), var2, var3);
   }

   public boolean playerInGroup(String var1, String var2, String var3) {
      Optional var4 = this.user(var2);
      Optional var5 = this.group(var1, var3);
      return var4.isPresent() && var5.isPresent() ? ((User)var4.get()).getGroups().contains(((Group)var5.get()).toStored()) : false;
   }

   public boolean playerInGroup(World var1, String var2, String var3) {
      return this.playerInGroup(var1.getName(), var2, var3);
   }

   public boolean playerInGroup(String var1, OfflinePlayer var2, String var3) {
      return this.playerInGroup(var1, var2.getName(), var3);
   }

   public boolean playerInGroup(Player var1, String var2) {
      return this.playerInGroup(var1.getWorld().getName(), var1.getName(), var2);
   }

   public boolean playerAddGroup(String var1, String var2, String var3) {
      Optional var4 = this.user(var2);
      Optional var5 = this.group(var1, var3);
      if (var4.isPresent() && var5.isPresent()) {
         ((User)var4.get()).addGroup((Group)var5.get());
         return true;
      } else {
         return false;
      }
   }

   public boolean playerAddGroup(String var1, OfflinePlayer var2, String var3) {
      return this.playerAddGroup(var1, var2.getName(), var3);
   }

   public boolean playerAddGroup(Player var1, String var2) {
      return this.playerAddGroup(var1.getWorld().getName(), var1.getName(), var2);
   }

   public boolean playerRemoveGroup(String var1, String var2, String var3) {
      Optional var4 = this.user(var2);
      Optional var5 = this.group(var1, var3);
      if (var4.isPresent() && var5.isPresent()) {
         if (!((User)var4.get()).getGroups().contains(((Group)var5.get()).toStored())) {
            return false;
         }

         ((User)var4.get()).removeGroup((Group)var5.get());
         return true;
      } else {
         return false;
      }
   }

   public boolean playerRemoveGroup(String var1, OfflinePlayer var2, String var3) {
      return this.playerRemoveGroup(var1, var2.getName(), var3);
   }

   public boolean playerRemoveGroup(Player var1, String var2) {
      return this.playerRemoveGroup(var1.getWorld().getName(), var1.getName(), var2);
   }

   public String[] getPlayerGroups(String var1, String var2) {
      Optional<User> var3 = this.user(var2);
      return var3.<String[]>map(
            var2x -> var2x.getActiveGroups()
               .servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null))
               .worlds(true, var1 != null && !var1.isEmpty() ? var1 : "world")
               .stream()
               .map(Group::getName)
               .toArray(String[]::new)
         )
         .orElseGet(() -> new String[0]);
   }

   public String[] getPlayerGroups(String var1, OfflinePlayer var2) {
      return this.getPlayerGroups(var1, var2.getName());
   }

   public String[] getPlayerGroups(Player var1) {
      return this.getPlayerGroups(var1.getWorld().getName(), var1.getName());
   }

   public String getPrimaryGroup(String var1, String var2) {
      String[] var3 = this.getPlayerGroups(var1, var2);
      return var3.length == 0 ? null : var3[0];
   }

   public String getPrimaryGroup(String var1, OfflinePlayer var2) {
      return this.getPrimaryGroup(var1, var2.getName());
   }

   public String getPrimaryGroup(Player var1) {
      return this.getPrimaryGroup(var1.getWorld().getName(), var1.getName());
   }

   public String[] getGroups() {
      return this.plugin
         .getGroups()
         .servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null))
         .stream()
         .map(Group::getName)
         .toArray(String[]::new);
   }

   public boolean hasGroupSupport() {
      return true;
   }

   public boolean has(Player var1, String var2) {
      return var1.hasPermission(var2);
   }

   public boolean has(CommandSender var1, String var2) {
      return var1.hasPermission(var2);
   }

   public Optional<User> user(String var1) {
      return this.plugin.getUsers().name(var1);
   }

   public Optional<Group> group(String var1, String var2) {
      return this.plugin.getGroups().servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null)).worlds(true, var1).name(var2);
   }

   public boolean add(String var1, PermissionHolder var2, String var3) {
      if (var2 == null) {
         return false;
      }

      var2.newPermission(var3)
         .setWorld(var1 != null && !var1.isEmpty() ? var1 : "world")
         .setServer(this.plugin.getThisServer().map(NServer::getName).orElse(null))
         .create();
      return true;
   }

   public boolean remove(String var1, PermissionHolder var2, String var3) {
      if (var2 == null) {
         return false;
      }

      var2.getPermissions()
         .name(var3)
         .servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null))
         .worlds(true, var1 != null && !var1.isEmpty() ? var1 : "world")
         .forEach(me.TechsCode.UltraPermissions.storage.objects.Permission::remove);
      return true;
   }
}
