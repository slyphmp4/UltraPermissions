package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import com.nametagedit.plugin.NametagEdit;
import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.events.group.GroupPrefixChangeEvent;
import me.TechsCode.UltraPermissions.events.group.GroupSuffixChangeEvent;
import me.TechsCode.UltraPermissions.events.user.UserGroupAddedEvent;
import me.TechsCode.UltraPermissions.events.user.UserGroupRemovedEvent;
import me.TechsCode.UltraPermissions.events.user.UserPrefixChangeEvent;
import me.TechsCode.UltraPermissions.events.user.UserSuffixChangeEvent;
import me.TechsCode.UltraPermissions.hooks.placeholders.PrefixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SuffixPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class NametagEditHook implements Listener {
   private final UltraPermissions plugin;
   private final NServer thisServer;

   public NametagEditHook(UltraPermissions var1) {
      super();
      this.plugin = var1;
      this.thisServer = var1.getThisServer().orElse(null);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
      var1.getScheduler().runTaskLater(() -> var1.getUsers().stream().filter(var0 -> Bukkit.getPlayer(var0.getUuid()) != null).forEach(var1xx -> {
         if (this.isEnabled()) {
            String var2 = this.color(new PrefixPlaceholder().get(var1xx, this.thisServer));
            String var3 = this.color(new SuffixPlaceholder().get(var1xx, this.thisServer));
            NametagEdit.getApi().setNametag(var1xx.getName(), var2.equals("") ? "" : var2 + " ", var3.equals("") ? "" : " " + var3);
         }
      }), 5L);
   }

   private boolean isEnabled() {
      // An optional provider may be disabled/reloaded after this listener was registered.
      return Bukkit.getPluginManager().isPluginEnabled("NametagEdit")
         && this.plugin.getVisualRegistry().get().isEditingNametags();
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent var1) {
      if (this.isEnabled()) {
         Optional var2 = this.plugin.getUserStorage().getUsers().uuid(var1.getPlayer().getUniqueId());
         if (var2.isPresent()) {
            this.plugin.getScheduler().runTaskLater(() -> {
               if (!this.isEnabled()) return;
               String var2x = this.color(new PrefixPlaceholder().get((User)var2.get(), this.thisServer));
               String var3 = this.color(new SuffixPlaceholder().get((User)var2.get(), this.thisServer));
               NametagEdit.getApi().setNametag(((User)var2.get()).getName(), var2x.equals("") ? "" : var2x + " ", var3.equals("") ? "" : " " + var3);
            }, 5L);
         }
      }
   }

   @EventHandler
   public void onUserPrefixChange(UserPrefixChangeEvent var1) {
      if (this.isEnabled()) {
         Optional var2 = Optional.ofNullable(Bukkit.getPlayer(var1.getUser().getUuid()));
         String var3 = this.color(new PrefixPlaceholder().get(var1.getUser(), this.thisServer));
         if (var2.isPresent()) {
            NametagEdit.getApi().setPrefix(var1.getUser().getName(), var3.equals("") ? "" : var3 + " ");
         }
      }
   }

   @EventHandler
   public void onUserSuffixChange(UserSuffixChangeEvent var1) {
      if (this.isEnabled()) {
         Optional var2 = Optional.ofNullable(Bukkit.getPlayer(var1.getUser().getUuid()));
         String var3 = this.color(new SuffixPlaceholder().get(var1.getUser(), this.thisServer));
         if (var2.isPresent()) {
            NametagEdit.getApi().setSuffix(var1.getUser().getName(), var3.equals("") ? "" : " " + var3);
         }
      }
   }

   @EventHandler
   public void onGroupPrefixChange(GroupPrefixChangeEvent var1) {
      if (this.isEnabled()) {
         var1.getGroup().getUsers().forEach(var1x -> Optional.ofNullable(Bukkit.getPlayer(var1x.getUuid())).ifPresent(var2 -> {
            String var3 = this.color(new PrefixPlaceholder().get(var1x, this.thisServer));
            NametagEdit.getApi().setPrefix(var2, var3.equals("") ? "" : var3 + " ");
         }));
      }
   }

   @EventHandler
   public void onGroupSuffixChange(GroupSuffixChangeEvent var1) {
      if (this.isEnabled()) {
         var1.getGroup().getUsers().forEach(var1x -> Optional.ofNullable(Bukkit.getPlayer(var1x.getUuid())).ifPresent(var2 -> {
            String var3 = this.color(new SuffixPlaceholder().get(var1x, this.thisServer));
            NametagEdit.getApi().setSuffix(var2, var3.equals("") ? "" : " " + var3);
         }));
      }
   }

   @EventHandler
   public void onGroupAdd(UserGroupAddedEvent var1) {
      if (this.isEnabled()) {
         Optional var2 = Optional.ofNullable(var1.getUser());
         if (var2.isPresent()) {
            this.plugin.getScheduler().runTaskLater(() -> {
               if (!this.isEnabled()) return;
               String var2x = this.color(new PrefixPlaceholder().get((User)var2.get(), this.thisServer));
               String var3 = this.color(new SuffixPlaceholder().get((User)var2.get(), this.thisServer));
               NametagEdit.getApi().setNametag(((User)var2.get()).getName(), var2x.equals("") ? "" : var2x + " ", var3.equals("") ? "" : " " + var3);
            }, 5L);
         }
      }
   }

   @EventHandler
   public void onGroupRemove(UserGroupRemovedEvent var1) {
      if (this.isEnabled()) {
         Optional var2 = Optional.ofNullable(var1.getUser());
         if (var2.isPresent()) {
            this.plugin.getScheduler().runTaskLater(() -> {
               if (!this.isEnabled()) return;
               String var2x = this.color(new PrefixPlaceholder().get((User)var2.get(), this.thisServer));
               String var3 = this.color(new SuffixPlaceholder().get((User)var2.get(), this.thisServer));
               NametagEdit.getApi().setNametag(((User)var2.get()).getName(), var2x.equals("") ? "" : var2x + " ", var3.equals("") ? "" : " " + var3);
            }, 5L);
         }
      }
   }

   private String color(String var1) {
      return ChatColor.translateAlternateColorCodes('&', var1);
   }
}
