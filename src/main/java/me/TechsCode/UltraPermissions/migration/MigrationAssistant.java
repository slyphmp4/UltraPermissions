package me.TechsCode.UltraPermissions.migration;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class MigrationAssistant implements Listener {
   private static boolean migrating;
   private static final Phrase KICK_MESSAGE = Phrase.create(
      "migrationAssistant.kickMessage", "Please remove **%plugin%** after the server has stopped", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase LOGIN_MESSAGE = Phrase.create("migrationAssistant.loginMessage", "The Server will be back in a few minutes", Colors.GRAY);

   public MigrationAssistant() {
      super();
   }

   public static boolean isMigrating() {
      return migrating;
   }

   public abstract String getPluginName();

   protected abstract void migrate(UltraPermissions var1, Runnable var2);

   public void doMigration(UltraPermissions var1) {
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
      Bukkit.getOnlinePlayers().forEach(var1x -> var1x.kickPlayer(KICK_MESSAGE.get().replace("%plugin%", this.getPluginName())));
      Bukkit.getScheduler().runTaskAsynchronously((Plugin)var1.getBootstrap(), () -> {
         var1.getUsers().forEach(User::remove);
         var1.getPermissions().forEach(Permission::remove);
         var1.getGroups().forEach(Group::remove);
         Runnable var2 = () -> (new BukkitRunnable() {
            public void run() {
               MigrationAssistant.migrating = false;
               Bukkit.shutdown();
            }
         }).runTaskLater((Plugin)var1.getBootstrap(), 60L);
         migrating = true;
         this.migrate(var1, var2);
      });
   }

   @EventHandler
   public void login(PlayerLoginEvent var1) {
      var1.disallow(Result.KICK_WHITELIST, LOGIN_MESSAGE.get());
   }
}
