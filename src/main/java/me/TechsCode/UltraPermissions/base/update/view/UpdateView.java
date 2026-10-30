package me.TechsCode.UltraPermissions.base.update.view;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class UpdateView extends GUI {
   private static final Phrase TITLE = Phrase.create("updateView.title", "Update from %from% to %to%");
   private static final Phrase YES_TITLE = Phrase.create("updateView.yes.title", "Update");
   private static final Phrase YES_ACTION = Phrase.create("updateView.yes.action", "Click to load **update**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase YES_DESCRIPTION = Phrase.create(
      "updateView.yes.desc", "This feature requires **Discord** to verify your purchase", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase NO_TITLE = Phrase.create("updateView.no.title", "Stay on this Version");
   private static final Phrase NO_ACTION = Phrase.create("updateView.no.action", "Click to stay on this Version", Colors.GRAY);
   private static final Phrase NO_DESC_YOURVERSION = Phrase.create("updateView.no.desc.yourVersion", "Your Version: **%**", Colors.GRAY, Colors.RED);
   private static final Phrase NO_DESC_NEWVERSION = Phrase.create("updateView.no.desc.newVersion", "New Version: **%**", Colors.GRAY, Colors.GREEN);
   private final String localVersion;
   private final String globalVersion;

   public UpdateView(Player var1, SpigotTechPlugin var2, String var3, String var4) {
      super(var1, var2);
      this.localVersion = var3;
      this.globalVersion = var4;
   }

   public abstract void onResponse(boolean var1);

   @Override
   protected void construct(Model var1) {
      var1.button(12, this::YesButton);
      var1.button(16, this::NoButton);
   }

   @Override
   public int getCurrentSlots() {
      return 27;
   }

   @Override
   public String getCurrentTitle() {
      return TITLE.get().replace("%from%", this.localVersion).replace("%to%", this.globalVersion);
   }

   private void YesButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(YES_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(YES_ACTION.get(), "");
      var1.item().appendLore(YES_DESCRIPTION.split(25));
      var1.action(var1x -> {
         this.p.closeInventory();
         this.onResponse(true);
      });
   }

   private void NoButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK)
         .name(Animation.wave(NO_TITLE.get(), Colors.RED, Colors.WHITE))
         .lore(NO_ACTION.get(), "", NO_DESC_YOURVERSION.get().replace("%", this.localVersion), NO_DESC_NEWVERSION.get().replace("%", this.globalVersion));
      var1.action(var1x -> {
         this.p.closeInventory();
         this.onResponse(false);
      });
   }
}
