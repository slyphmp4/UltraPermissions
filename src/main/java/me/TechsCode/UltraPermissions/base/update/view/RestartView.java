package me.TechsCode.UltraPermissions.base.update.view;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RestartView extends GUI {
   private final SpigotTechPlugin plugin;
   private static final Phrase TITLE = Phrase.create("RestartView.gui.title", "Restart to Update");
   private static final Phrase SERVER_RESTART_TITLE = Phrase.create("RestartView.gui.serverRestart.title", "Server Restart");
   private static final Phrase SERVER_RESTART_LORE_CLICK = Phrase.create(
      "RestartView.gui.serverRestart.lore.click", "**Click** to restart **this** server", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase SERVER_RESTART_LORE_DESC1 = Phrase.create(
      "RestartView.gui.serverRestart.lore.desc1", "When choosing **Server Restart** this server will", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase SERVER_RESTART_LORE_DESC2 = Phrase.create(
      "RestartView.gui.serverRestart.lore.desc2", "restart right now to finish updating", Colors.GRAY
   );
   private static final Phrase SERVER_RESTART_LORE_DESC3 = Phrase.create(
      "RestartView.gui.serverRestart.lore.desc3", "(All other Servers in your Network", Colors.GRAY
   );
   private static final Phrase SERVER_RESTART_LORE_DESC4 = Phrase.create(
      "RestartView.gui.serverRestart.lore.desc4", "will **not** be affected)", Colors.GRAY, Colors.RED
   );
   private static final Phrase SERVER_RESTART = Phrase.create("RestartView.gui.serverRestart", "Restarting Server...", Colors.GRAY);
   private static final Phrase RESTART_SERVER_CONFIGURATION2 = Phrase.create(
      "RestartView.gui.restartServerConfiguration2", "or start up your server **manually** again to update the plugin", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase RESTART_SERVER_CONFIGURATION1 = Phrase.create(
      "RestartView.gui.restartServerConfiguration1",
      "If your server doesn't start up automatically, check your **servers restart configuration** (e.g Spigot.yml)",
      Colors.GRAY,
      Colors.GOLD
   );
   private static final Phrase NETWORK_RESTART_TITLE = Phrase.create("RestartView.gui.networkRestart.title", "Network Restart");
   private static final Phrase NETWORK_RESTART_LORE_CLICK = Phrase.create(
      "RestartView.gui.networkRestart.lore.click", "**Click** to restart **all** servers", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase NETWORK_RESTART_LORE_DESC1 = Phrase.create(
      "RestartView.gui.networkRestart.lore.desc1", "When choosing **Network Restart** all servers will", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase NETWORK_RESTART_LORE_DESC2 = Phrase.create(
      "RestartView.gui.networkRestart.lore.desc2", "restart right now to finish updating", Colors.GRAY
   );
   private static final Phrase RESTART_NETWORK = Phrase.create("RestartView.gui.restartNetwork", "Restarting Network...", Colors.GRAY);
   private static final Phrase RESTART_NETWORK_CONFIGURATION1 = Phrase.create(
      "RestartView.gui.restartNetworkConfiguration1",
      "If your Network don't start up automatically, check your **servers restart configuration** (e.g Spigot.yml)",
      Colors.GRAY,
      Colors.GOLD
   );
   private static final Phrase RESTART_NETWORK_CONFIGURATION2 = Phrase.create(
      "RestartView.gui.restartNetworkConfiguration2",
      "or start up your servers **manually** again to update the plugins (**Proxy** has to be restarted **manually**)",
      Colors.GRAY,
      Colors.WHITE,
      Colors.GOLD,
      Colors.GOLD
   );
   private static final Phrase RESTART_LATER_TITLE = Phrase.create("RestartView.gui.restartLater.title", "Restart Later");
   private static final Phrase RESTART_LATER_LORE_CLICK = Phrase.create(
      "RestartView.gui.restartLater.lore.click", "**Click** to restart later", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase RESTART_LATER_LORE_DESC1 = Phrase.create(
      "RestartView.gui.restartLater.lore.desc1", "When choosing **Restart Later** you will have to", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase RESTART_LATER_LORE_DESC2 = Phrase.create(
      "RestartView.gui.restartLater.lore.desc2", "restart the server manually to finish updating", Colors.GRAY
   );
   private static final Phrase RESTART_LATER_GUI_CLOSE_MESSAGE = Phrase.create(
      "RestartView.gui.restartLater.guiCloseMessage", "You can restart the server **manually** to finish updating", Colors.GRAY, Colors.GOLD
   );

   public RestartView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(TITLE.get());
      var1.setSlots(27);
      var1.button(12, this::ServerRestartButton);
      var1.button(16, this::RestartLaterButton);
   }

   public void ServerRestartButton(Button var1) {
      var1.material(XMaterial.EMERALD)
         .name(Animation.wave(SERVER_RESTART_TITLE.get(), Colors.GREEN, Colors.WHITE))
         .lore(
            SERVER_RESTART_LORE_CLICK.get(),
            "",
            SERVER_RESTART_LORE_DESC1.get(),
            SERVER_RESTART_LORE_DESC2.get(),
            this.plugin.getPrefix(),
            "",
            SERVER_RESTART_LORE_DESC3.get(),
            SERVER_RESTART_LORE_DESC4.get()
         );
      var1.action(var1x -> {
         this.p.closeInventory();
         this.p.sendMessage(this.plugin.getPrefix() + SERVER_RESTART.get());
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_SERVER_CONFIGURATION1.get());
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_SERVER_CONFIGURATION2.get());
         this.plugin.getScheduler().runTaskLater(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart"), 60L);
      });
   }

   public void NetworkRestartButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK)
         .name(Animation.wave(NETWORK_RESTART_TITLE.get(), Colors.GREEN, Colors.WHITE))
         .lore(NETWORK_RESTART_LORE_CLICK.get(), "", NETWORK_RESTART_LORE_DESC1.get(), NETWORK_RESTART_LORE_DESC2.get(), this.plugin.getPrefix());
      var1.action(var1x -> {
         this.p.closeInventory();
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_NETWORK.get());
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_NETWORK_CONFIGURATION1.get());
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_NETWORK_CONFIGURATION2.get());
         this.plugin.getScheduler().runTaskLater(() -> this.plugin.getUpdateAgent().sendRestartRequestToBungee(), 60L);
      });
   }

   public void RestartLaterButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK)
         .name(Animation.wave(RESTART_LATER_TITLE.get(), Colors.WHITE, Colors.RED))
         .lore(RESTART_LATER_LORE_CLICK.get(), "", RESTART_LATER_LORE_DESC1.get(), RESTART_LATER_LORE_DESC2.get(), this.plugin.getPrefix());
      var1.action(var1x -> {
         this.p.closeInventory();
         this.p.sendMessage(this.plugin.getPrefix() + RESTART_LATER_GUI_CLOSE_MESSAGE.get());
      });
   }
}
