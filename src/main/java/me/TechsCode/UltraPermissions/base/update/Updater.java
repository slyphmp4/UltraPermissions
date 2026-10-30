package me.TechsCode.UltraPermissions.base.update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.responses.IsAvailableResponse;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;
import me.TechsCode.UltraPermissions.base.update.view.UpdateView;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public class Updater {
   private static final List<Player> updateAskedPlayers = new ArrayList<>();
   private static final List<Player> restartAskedPlayers = new ArrayList<>();
   private static final Phrase UPDATE_READY = Phrase.create(
      "Updater.updateReady", "**An update is waiting to be loaded!** Restart your server to make this update take effect", Colors.GRAY, Colors.WHITE
   );

   public Updater() {
      super();
   }

   public static void suggestUpdateIfAvailable(SpigotTechPlugin var0, Player var1, Callback<Player> var2, boolean var3) {
      UpdateServer var4 = new UpdateServer();
      IsAvailableResponse var5 = var4.isAvailable();
      if (var5.isAvailable()) {
         var4.destroy();
         File var6 = var0.getPluginFolder();
         boolean var7 = new File(var6, "update/" + var0.getName() + ".jar").exists();
         if (var7 && restartAskedPlayers.contains(var1)) {
            restartAskedPlayers.add(var1);
            var1.sendMessage(var0.getPrefix() + UPDATE_READY.get());
         } else if (var3 || !updateAskedPlayers.contains(var1)) {
            if (var0.getAppearanceRegistry().isUpdaterEnabled()) {
               var0.getScheduler().runAsync(() -> {
                  UpdateServer var3x = new UpdateServer();
                  IsUpToDateResponse var4x = var3x.isUpToDate(var0);
                  var3x.destroy();
                  if (var4x.isSuccessFull() && !var4x.isUpToDate()) {
                     updateAskedPlayers.add(var1);
                     String var5x = var0.getVersion();
                     String var6x = var4x.getLatestVersion();
                     var0.getScheduler().run(() -> new UpdateView(var1, var0, var5x, var6x) {
                        @Override
                        public void onResponse(boolean var1x) {
                           if (var1x) {
                              new UpdateProcess(var0, this.p, var6x, var1xxx -> {
                                 if (var1xxx == UpdateProcess.CloseReason.NO_REQUIREMENTS) {
                                    Updater.updateAskedPlayers.remove(this.p);
                                 }
                              });
                           } else {
                              var2.run(this.p);
                              this.p.closeInventory();
                           }
                        }
                     });
                  }
               });
            }
         }
      }
   }

   public static String getOSSystem() {
      return System.getProperty("os.name");
   }
}
