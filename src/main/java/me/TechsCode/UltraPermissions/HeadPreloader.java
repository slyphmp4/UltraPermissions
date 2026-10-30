package me.TechsCode.UltraPermissions;

import java.util.Optional;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class HeadPreloader implements Listener {
   private UltraPermissions plugin;

   public HeadPreloader(UltraPermissions var1) {
      super();
      this.plugin = var1;
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   @EventHandler
   public void login(PlayerJoinEvent var1) {
      this.plugin.getScheduler().runTaskLaterAsync(() -> {
         SkinTexture var2 = Bukkit.getOnlineMode() ? SkinTexture.fromPlayer(var1.getPlayer()) : SkinTexture.fromMojangAPI(var1.getPlayer().getName());
         if (var2 != null && var2.getUrl() != null) {
            Optional<me.TechsCode.UltraPermissions.storage.objects.User> var3 = this.plugin.getUsers().uuid(var1.getPlayer().getUniqueId());
            var3.ifPresent(var1xx -> var1xx.setSkinTexture(var2));
         }
      }, 90L);
   }
}
