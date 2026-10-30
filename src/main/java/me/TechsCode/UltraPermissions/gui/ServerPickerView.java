package me.TechsCode.UltraPermissions.gui;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.networking.NPlayer;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class ServerPickerView extends PageableGUI<NServer> {
   private final UltraPermissions plugin;
   private final NServer thisServer;
   private static final Phrase ENTRY_ACTION = Phrase.create("serverPickerView.entry.action", "Click to select", Colors.GRAY);
   private static final Phrase ENTRY_CURRENT_INDICATOR = Phrase.create(
      "serverPickerView.entry.currentIndicator", "You are currently on this Server", Colors.GREEN
   );
   private static final Phrase ENTRY_ADDRESS_ATTRIBUTE = Phrase.create(
      "serverPickerView.entry.addressAttribute", "Address: **%ip%**:**%port%**", Colors.GRAY, Colors.YELLOW, Colors.YELLOW
   );
   private static final Phrase ENTRY_ADDRESS_ONLINE_PLAYERS_TITLE = Phrase.create("serverPickerView.entry.onlinePlayers.title", "Online Players:", Colors.GRAY);
   private static final Phrase ENTRY_ADDRESS_ONLINE_PLAYERS_ENTRY = Phrase.create(
      "serverPickerView.entry.onlinePlayers.entry", "- **%player%**", Colors.GRAY, Colors.YELLOW
   );

   public ServerPickerView(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.plugin = var2;
      this.thisServer = var2.getThisServer().orElse(null);
   }

   public abstract void onSelect(NServer var1);

   @Override
   public SearchFeature<NServer> getSearch() {
      return new BasicSearch<NServer>() {
         public String[] getSearchableText(NServer var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   public NServer[] getObjects() {
      Optional<me.TechsCode.UltraPermissions.base.networking.NetworkData> var1 = this.plugin.getNetworkManager().getData();
      return var1.<NServer[]>map(var0 -> var0.getServerList().toArray(new NServer[0])).orElseGet(() -> new NServer[0]);
   }

   public void construct(Button var1, NServer var2) {
      boolean var3 = this.thisServer != null && this.thisServer.getName().equals(var2.getName());
      var1.material(XMaterial.BLUE_STAINED_GLASS).name(Animation.wave(var2.getName(), Colors.DarkBlue, Colors.WHITE)).lore(ENTRY_ACTION.get());
      if (var3) {
         var1.item().appendLore("", ENTRY_CURRENT_INDICATOR.get());
      }

      var1.item()
         .appendLore(
            "",
            ENTRY_ADDRESS_ATTRIBUTE.get().replace("%ip%", var2.getIp()).replace("%port%", Integer.toString(var2.getPort())),
            "",
            ENTRY_ADDRESS_ONLINE_PLAYERS_TITLE.get()
         );

      for (NPlayer var5 : var2.getPlayers()) {
         var1.item().appendLore(ENTRY_ADDRESS_ONLINE_PLAYERS_ENTRY.get().replace("%player%", var5.getName()));
      }

      if (var2.getPlayers().isEmpty()) {
         var1.item().appendLore(Colors.RED + Words.NONE.get());
      }

      var1.action(var2x -> this.onSelect(var2));
   }
}
