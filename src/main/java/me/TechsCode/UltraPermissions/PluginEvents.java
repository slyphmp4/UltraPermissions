package me.TechsCode.UltraPermissions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.internal.PermissibleInjector;
import me.TechsCode.UltraPermissions.permissionlogger.PermissionLogger;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;

public class PluginEvents implements Listener {
   private final UltraPermissions plugin;
   private final PermissionLogger permissionLogger;
   private final HashMap<Player, ModifiedPermissible> permissibleHashMap;

   public PluginEvents(UltraPermissions var1, PermissionLogger var2) {
      super();
      this.plugin = var1;
      this.permissionLogger = var2;
      this.permissibleHashMap = new HashMap<>();
      Bukkit.getOnlinePlayers().forEach(this::login);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   public void login(Player var1) {
      Optional var2 = this.plugin.getUsers().uuid(var1.getUniqueId());
      if (var2.isPresent()) {
         if (!((User)var2.get()).getName().equals(var1.getName())) {
            ((User)var2.get()).setPlayerName(var1.getName());
         }
      } else {
         this.plugin.registerUser(var1.getUniqueId(), var1.getName(), this.plugin.getDefaultGroupAssignOption() == DefaultGroupAssignOption.FIRST_JOIN);
      }

      try {
         ModifiedPermissible var3 = new ModifiedPermissible(var1, this.plugin, this.permissionLogger);
         this.permissibleHashMap.put(var1, var3);
         PermissibleInjector.inject(var1, var3);
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();
      }
   }

   public Collection<ModifiedPermissible> getPermissibles() {
      return this.permissibleHashMap.values();
   }

   @EventHandler(priority = EventPriority.LOW)
   public void login(PlayerLoginEvent var1) {
      if (var1.getResult() != Result.KICK_WHITELIST) {
         this.login(var1.getPlayer());
      }
   }

   @EventHandler
   public void join(PlayerJoinEvent event) {
      if (!this.plugin.getAppearanceRegistry().isUpdaterEnabled()) {
         return;
      }
      this.plugin.getScheduler().runTaskLater(() -> {
         Player player = event.getPlayer();
         if (!player.isOnline() || !this.plugin.getAppearanceRegistry().isUpdaterEnabled()) {
            return;
         }
         this.plugin.getUsers().uuid(player.getUniqueId())
            .filter(User::isSuperadmin)
            .ifPresent(user -> Updater.suggestUpdateIfAvailable(this.plugin, player, ignored -> {}, false));
      }, 40L);
   }

   @EventHandler
   public void quit(PlayerQuitEvent var1) {
      this.permissibleHashMap.remove(var1.getPlayer());
   }

   @EventHandler
   public void updateData(UpdateEvent var1) {
      if (var1.getUpdateTime() == UpdateTime.QUARTER_HOUR && this.plugin.getAppearanceRegistry().isUpdaterEnabled()) {
         // TaskManager dispatches UpdateEvent synchronously: Bukkit player APIs stay on the server thread.
         for (Player player : Bukkit.getOnlinePlayers()) {
            this.plugin.getUsers().uuid(player.getUniqueId()).filter(User::isSuperadmin)
               .ifPresent(user -> Updater.suggestUpdateIfAvailable(this.plugin, player, ignored -> {}, false));
         }
      }
      if (var1.getUpdateTime() == UpdateTime.SEC) {
         this.plugin.getScheduler().run(() -> {
            GroupList var1x = this.plugin.getGroups();
            UserList var2 = this.plugin.getUsers();
            PermissionList var3 = this.plugin.getPermissions();
            GroupList var4 = var1x.defaults(true);

            for (User var6 : var2) {
               DefaultGroupAssignOption var7 = this.plugin.getDefaultGroupAssignOption();
               boolean var8 = var6.getGroups().isEmpty() && var7 == DefaultGroupAssignOption.NO_GROUP;
               boolean var9 = var7 == DefaultGroupAssignOption.ALWAYS_ASSIGN;
               if (var8 || var9) {
                  var4.stream().filter(var1xx -> !var6.getGroups().contains(var1xx.toStored())).forEach(var6::addGroup);
               }

               var6.getRankups().stream().filter(UserRankup::isExpired).forEach(UserRankup::remove);
            }

            var1x.forEach(var0 -> var0.getActiveInheritedGroups().forEach(var1xx -> {
               boolean var2x = !var0.getServer().equals(var1xx.getServer());
               boolean var3x = !var0.getWorld().equals(var1xx.getWorld());
               if (var2x || var3x) {
                  var0.removeInheritance(var1xx);
               }
            }));
            var3.expired().forEach(Permission::remove);
         });
      }
   }
}
