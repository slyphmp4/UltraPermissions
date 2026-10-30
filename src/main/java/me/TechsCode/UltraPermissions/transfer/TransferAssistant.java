package me.TechsCode.UltraPermissions.transfer;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;

public class TransferAssistant implements Listener {
   private static final Phrase KICK_MESSAGE = Phrase.create(
      "transferAssistant.kickMessage", "UltraPermissions will now convert data. The server will shut down when complete", Colors.GRAY
   );
   private static final Phrase LOGIN_MESSAGE = Phrase.create(
      "transferAssistant.loginMessage", "Ultra Permissions is currently transferring. Wait until the server is stopped.", Colors.GRAY
   );

   public TransferAssistant(UltraPermissions var1, boolean var2) {
      super();
      Bukkit.getOnlinePlayers().forEach(var0 -> var0.kickPlayer(KICK_MESSAGE.get()));
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
      Bukkit.getScheduler().runTaskAsynchronously((Plugin)var1.getBootstrap(), () -> {
         var1.log("Transferring from " + (var2 ? "File to MySQL" : "MySQL to File"));
         TransferStorageGroup var2x = new TransferStorageGroup(var1, false);
         TransferStorageGroup var3 = new TransferStorageGroup(var1, true);
         TransferStorageGroup var4 = var2 ? var2x : var3;
         TransferStorageGroup var5 = var2 ? var3 : var2x;
         var4.copyTo(var5);
         var1.getScheduler().runTaskTimer(() -> {
            long var1xx = System.currentTimeMillis() - var5.getLastWrite();
            if (var1xx > 3000L) {
               Bukkit.shutdown();
            }
         }, 0L, 1200L);
      });
   }

   @EventHandler
   public void login(PlayerLoginEvent var1) {
      var1.disallow(Result.KICK_OTHER, LOGIN_MESSAGE.get());
   }
}
