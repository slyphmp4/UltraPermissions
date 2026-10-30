package me.TechsCode.UltraPermissions.visual;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.migration.MigrationAssistant;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class VisualManager implements Listener {
   private final UltraPermissions plugin;
   private final VisualRegistry registry;

   public VisualManager(UltraPermissions var1, VisualRegistry var2) {
      super();
      this.plugin = var1;
      this.registry = var2;
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   @EventHandler
   public void chat(AsyncPlayerChatEvent var1) {
      if (!MigrationAssistant.isMigrating()) {
         String var2 = this.registry.getFormat(VisualType.CHAT);
         Optional var3 = this.plugin.getUsers().uuid(var1.getPlayer().getUniqueId());
         GroupList var4 = null;
         if (var3.isPresent()) {
            var4 = ((User)var3.get()).getActiveGroups().bestToWorst();
         }

         boolean var5 = false;
         boolean var6 = false;

         for (Group var8 : var4) {
            if (!var6) {
               String var9 = var8.getFormat();
               if (var9 != null && var9 != "none") {
                  var5 = true;
                  var9 = this.replacePlaceholders(var1.getPlayer(), var9).replace("%", "percent").replace("{Message}", "%2$s");
                  var1.setFormat(ChatColor.translateAlternateColorCodes('&', Text.hexColor(var9)));
                  var6 = !var6;
               }
            }
         }

         if (!var5) {
            if (var2 == null) {
               return;
            }

            var2 = this.replacePlaceholders(var1.getPlayer(), var2).replace("%", "percent").replace("{Message}", "%2$s");
            var1.setFormat(Text.hexColor(var2));
         }
      }
   }

   @EventHandler
   public void update(UpdateEvent var1) {
      if (!MigrationAssistant.isMigrating()) {
         if (var1.getUpdateTime() == UpdateTime.SLOWER) {
            if (this.registry.getFormat(VisualType.TABLIST) != null || this.registry.getFormat(VisualType.DISPLAY_NAME) != null) {
               Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin.getBootstrap(), () -> {
                  for (Player var2 : Bukkit.getOnlinePlayers()) {
                     String var3 = this.registry.getFormat(VisualType.TABLIST);
                     String var4 = this.registry.getFormat(VisualType.DISPLAY_NAME);
                     if (var3 != null) {
                        var2.setPlayerListName(this.replacePlaceholders(var2, var3));
                     }

                     if (var4 != null) {
                        var2.setDisplayName(this.replacePlaceholders(var2, var4));
                     }
                  }
               });
            }
         }
      }
   }

   private String replacePlaceholders(Player var1, String var2) {
      Optional var3 = this.plugin.getUsers().uuid(var1.getUniqueId());
      if (!var3.isPresent()) {
         return var2;
      }

      for (UpermsPlaceholder var7 : UltraPermissions.placeholders) {
         var2 = var2.replace(var7.getNativePlaceholder(), var7.get((User)var3.get(), this.plugin.getThisServer().orElse(null)));
      }

      var2 = this.plugin.replacePlaceholders(var1, var2);
      var2 = var2.replace("{Player}", var1.getName());
      var2 = var2.replace("{DisplayName}", var1.getDisplayName());
      return var2.trim();
   }
}
