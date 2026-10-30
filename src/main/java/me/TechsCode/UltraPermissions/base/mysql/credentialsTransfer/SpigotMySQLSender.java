package me.TechsCode.UltraPermissions.base.mysql.credentialsTransfer;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.messaging.QueuedMessage;
import me.TechsCode.UltraPermissions.base.mysql.MySQLManager;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class SpigotMySQLSender implements Listener {
   private SpigotTechPlugin plugin;
   private MySQLManager manager;

   public SpigotMySQLSender(SpigotTechPlugin var1, MySQLManager var2) {
      super();
      this.plugin = var1;
      this.manager = var2;
      this.sendMySQLCredentials(false);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   private void sendMySQLCredentials(boolean force) {
      // Credentials are local-only. Never send secrets through a player connection.
   }

   @EventHandler
   public void send(UpdateEvent var1) {
      if (var1.getUpdateTime() == UpdateTime.HALFMIN) {
         this.sendMySQLCredentials(false);
      }
   }

   @EventHandler
   public void join(PlayerJoinEvent var1) {
      this.sendMySQLCredentials(false);
   }

   public void forceUpdateBungeeMySQLCredentials() {
      this.plugin.log("MySQL credentials are local-only. Configure mysql.properties independently on the proxy.");
   }
}
