package me.TechsCode.UltraPermissions;

import java.util.UUID;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
   public void login(PlayerJoinEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      String playerName = event.getPlayer().getName();
      this.plugin.getScheduler().runTaskLater(() -> {
         if (Bukkit.getOnlineMode()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
               this.applySkinTexture(uuid, SkinTexture.fromPlayer(player));
            }
         } else {
            this.plugin.getScheduler().runAsync(() -> {
               SkinTexture texture = SkinTexture.fromMojangAPI(playerName);
               if (texture != null && texture.getUrl() != null) {
                  this.plugin.getScheduler().run(() -> this.applySkinTexture(uuid, texture));
               }
            });
         }
      }, 90L);
   }

   private void applySkinTexture(UUID uuid, SkinTexture texture) {
      if (texture != null && texture.getUrl() != null) {
         this.plugin.getUsers().uuid(uuid).ifPresent(user -> user.setSkinTexture(texture));
      }
   }
}
