package me.TechsCode.UltraPermissions.migration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;

public class InsaneEditorDownload {
   private UltraPermissions plugin;
   private Player player;
   private static final String currentLocation = System.getProperty("user.dir");
   private static final Phrase INSTALL_SUCCESSFUL = Phrase.create(
      "insaneEditorDownload.install.successful", "Successfully installed **Insane Editor**", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase INSTALL_UNSUCCESSFUL = Phrase.create(
      "insaneEditorDownload.install.unsuccessful", "There was a problem while downloading **Insane Editor**", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase INSTRUCTIONS = Phrase.create(
      "insaneEditorDownload.instructions", "Type in **/InsaneEditor help** to start configuring the Web Editor", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase WIKI = Phrase.create(
      "insaneEditorDownload.wiki", "Insane Editor Wiki: **https://wiki.insaneeditor.com**", Colors.GRAY, Colors.GREEN
   );

   public InsaneEditorDownload(Player var1, UltraPermissions var2) {
      super();
      this.plugin = var2;
      this.player = var1;
      if (this.isInstalled()) {
         this.showCommands();
      } else {
         this.installJar(this.getDownloadLink());
      }
   }

   private boolean isInstalled() {
      return Bukkit.getPluginManager().getPlugin("InsaneEditor") != null;
   }

   private void showCommands() {
      this.player.performCommand("insaneeditor help");
   }

   private void installJar(String ignored) {
      this.player.sendMessage(this.plugin.getPrefix()
         + "Install a verified InsaneEditor JAR manually, then restart the server. Automatic installation is disabled.");
   }

   private String getDownloadLink() {
      return "";
   }


}
