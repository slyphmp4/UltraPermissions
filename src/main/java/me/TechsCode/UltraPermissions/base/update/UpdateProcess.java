package me.TechsCode.UltraPermissions.base.update;

import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.responses.CreateUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.responses.GetUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.view.RestartView;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class UpdateProcess implements Listener {
   private static final Phrase CLICK_TO_AUTHENTICATE = Phrase.create(
      "update.clickToAuthenticate", "Click the link below to authenticate using **Discord**:", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase UNAUTHORIZED_TEXT = Phrase.create(
      "update.unauthorized", "Authorization Failed, You are not verified on our **Discord**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase UPDATED_PLUGIN = Phrase.create("update.updatedPlugin", "Updated the plugin to **%**, reloading..", Colors.GRAY, Colors.YELLOW);
   private static final Phrase SERVER_OFFLINE = Phrase.create("update.serverOffline", "There was an error communicating with our update Server", Colors.GRAY);
   private static final Phrase DOWNLOAD_FAILED = Phrase.create("update.downloadFailed", "The download has failed, please try again later", Colors.GRAY);
   private final SpigotTechPlugin plugin;
   private final Player p;
   private final UpdateServer updateServer;
   private final CreateUpdateRequestResponse createUpdateRequestResponse;
   private final String version;
   private final long start;
   private final Callback<UpdateProcess.CloseReason> callback;

   public UpdateProcess(SpigotTechPlugin var1, Player var2, String var3, Callback<UpdateProcess.CloseReason> var4) {
      super();
      this.plugin = var1;
      this.p = var2;
      this.updateServer = new UpdateServer();
      this.version = var3;
      this.start = System.currentTimeMillis();
      this.callback = var4;
      this.createUpdateRequestResponse = this.updateServer.createUpdateRequest(var1);
      String var5 = this.updateServer.getAuthorizeUrl(this.createUpdateRequestResponse.getUpdateToken());
      var2.sendMessage("");
      var2.sendMessage(var1.getPrefix() + CLICK_TO_AUTHENTICATE);
      var2.sendMessage(Colors.YELLOW + var5);
      var2.sendMessage("");
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   @EventHandler
   public void check(UpdateEvent var1) {
      if (var1.getUpdateTime() == UpdateTime.SLOW) {
         long var2 = System.currentTimeMillis() - this.start;
         if (TimeUnit.MILLISECONDS.toMinutes(var2) >= 15L) {
            this.close(UpdateProcess.CloseReason.TIME_OUT);
         } else {
            GetUpdateRequestResponse var4 = this.updateServer.getUpdateRequest(this.createUpdateRequestResponse.getUpdateToken());
            if (var4.getStatusCode() != 200) {
               this.close(UpdateProcess.CloseReason.SERVER_OFFLINE);
            } else {
               UpdateRequest var5 = var4.getUpdateRequest();
               if (var5 == null) {
                  this.close(UpdateProcess.CloseReason.SERVER_OFFLINE);
               } else {
                  switch (var5.getStatus()) {
                     case WAITING_AUTHENTICATION:
                     default:
                        break;
                     case UNAUTHORIZED:
                        this.p.sendMessage(this.plugin.getPrefix() + UNAUTHORIZED_TEXT);
                        this.close(UpdateProcess.CloseReason.NO_REQUIREMENTS);
                        break;
                     case AUTHORIZED:
                        this.plugin.getUpdateAgent().sendUpdateRequestToBungeeCord(var5, this.version);
                        Thread var6 = this.updateServer.downloadJar(this.plugin, var5.getUpdateToken());
                        Thread var7 = new Thread(() -> {
                           while (var6.isAlive()) {
                              try {
                                 Thread.sleep(5000L);
                              } catch (InterruptedException var3) {
                                 throw new RuntimeException(var3);
                              }
                           }

                           if (!this.updateServer.hasJarDownloadFailed()) {
                              new RestartView(this.p, this.plugin) {};
                           } else {
                              this.p.sendMessage(this.plugin.getPrefix() + DOWNLOAD_FAILED.get());
                           }
                        });
                        var7.start();
                        this.close(UpdateProcess.CloseReason.COMPLETE);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void leave(PlayerQuitEvent var1) {
      this.close(UpdateProcess.CloseReason.QUIT);
   }

   private void close(UpdateProcess.CloseReason var1) {
      HandlerList.unregisterAll(this);
      this.callback.run(var1);
   }

   enum CloseReason {
      QUIT,
      COMPLETE,
      NO_REQUIREMENTS,
      TIME_OUT,
      SERVER_OFFLINE;

      CloseReason() {
      }
   }
}
