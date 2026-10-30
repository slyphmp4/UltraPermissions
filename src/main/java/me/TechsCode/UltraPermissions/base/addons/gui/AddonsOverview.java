package me.TechsCode.UltraPermissions.base.addons.gui;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class AddonsOverview extends GUI {
   private final SpigotTechPlugin plugin;
   private final Color color;
   private static final Phrase GUI_TITLE = Phrase.create("AddonsOverview.gui.title", "Overview > Addons");
   private static final Phrase MARKETPLACE_BUTTON_NAME = Phrase.create("AddonsOverview.marketplaceButton.name", "Marketplace");
   private static final Phrase MARKETPLACE_BUTTON_LORE_MARKETPLACE_ONLINE = Phrase.create(
      "AddonsOverview.marketplaceButton.lore.marketplace.online", "Click to browse our **marketplace**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase MARKETPLACE_BUTTON_LORE_MARKETPLACE_OFFLINE = Phrase.create(
      "AddonsOverview.marketplaceButton.lore.marketplace.offline", "Our Marketplace is currently **unavailable**", Colors.GOLD, Colors.RED
   );
   private static final Phrase MARKETPLACE_BUTTON_LORE_DESC_LINE_1 = Phrase.create(
      "AddonsOverview.marketplaceButton.lore.description.line1", "Our Marketplace provides you", Colors.GRAY
   );
   private static final Phrase MARKETPLACE_BUTTON_LORE_DESC_LINE_2 = Phrase.create(
      "AddonsOverview.marketplaceButton.lore.description.line2", "with various addons that are", Colors.GRAY
   );
   private static final Phrase MARKETPLACE_BUTTON_LORE_DESC_LINE_3 = Phrase.create(
      "AddonsOverview.marketplaceButton.lore.description.line3", "downloadable with just one click!", Colors.GRAY
   );
   private static final Phrase INSTALLED_ADDONS_BUTTON_NAME = Phrase.create("AddonsOverview.installedAddonsButton.name", "Installed");
   private static final Phrase INSTALLED_ADDONS_BUTTON_LORE_CLICK = Phrase.create(
      "AddonsOverview.installedAddonsButton.lore.click", "Click to view **installed Addons**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase INSTALLED_ADDONS_BUTTON_LORE_AMOUNT = Phrase.create(
      "AddonsOverview.installedAddonsButton.lore.amount", "Amount: **%amount%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase INSTALLED_ADDONS_BUTTON_LORE_DESC_LINE_1 = Phrase.create(
      "AddonsOverview.installedAddonsButton.lore.description.line1", "View, edit, and remove all installed addons.", Colors.GRAY
   );
   private static final Phrase INSTALLED_ADDONS_BUTTON_LORE_DESC_LINE_2 = Phrase.create(
      "AddonsOverview.installedAddonsButton.lore.description.line2", "Don't want an addon anymore? Then delete it!", Colors.GRAY
   );

   public AddonsOverview(Player var1, Color var2, SpigotTechPlugin var3) {
      super(var1, var3);
      this.color = var2;
      this.plugin = var3;
   }

   public abstract void onBack();

   @Override
   public String getCurrentTitle() {
      return GUI_TITLE.get();
   }

   @Override
   public void construct(Model var1) {
      var1.setSlots(36);
      var1.button(12, this::MarketplaceButton);
      var1.button(16, this::InstalledAddonsButton);
      var1.button(32, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
   }

   private void MarketplaceButton(Button var1) {
      boolean var2 = this.plugin.getAddonManager().isCloudOnline();
      var1.material(XMaterial.EMERALD)
         .name(Animation.wave(MARKETPLACE_BUTTON_NAME.get(), var2 ? this.color : Colors.RED, Colors.WHITE))
         .lore(
            var2 ? MARKETPLACE_BUTTON_LORE_MARKETPLACE_ONLINE.get() : MARKETPLACE_BUTTON_LORE_MARKETPLACE_OFFLINE.get(),
            "",
            MARKETPLACE_BUTTON_LORE_DESC_LINE_1.get(),
            MARKETPLACE_BUTTON_LORE_DESC_LINE_2.get(),
            MARKETPLACE_BUTTON_LORE_DESC_LINE_3.get()
         );
      var1.action(var2x -> {
         if (var2) {
            new AddonsMarketplaceListView(this.p, this.plugin) {
               @Override
               public void onBack() {
                  AddonsOverview.this.reopen();
               }
            };
         }
      });
   }

   private void InstalledAddonsButton(Button var1) {
      var1.material(XMaterial.HOPPER_MINECART)
         .name(Animation.wave(INSTALLED_ADDONS_BUTTON_NAME.get(), this.color, Colors.WHITE))
         .lore(
            INSTALLED_ADDONS_BUTTON_LORE_CLICK.get(),
            "",
            INSTALLED_ADDONS_BUTTON_LORE_AMOUNT.get().replace("%amount%", String.valueOf(this.plugin.getAddonManager().getInstalledAddons().size())),
            "",
            INSTALLED_ADDONS_BUTTON_LORE_DESC_LINE_1.get(),
            INSTALLED_ADDONS_BUTTON_LORE_DESC_LINE_2.get()
         );
      var1.action(var1x -> new InstalledAddonsView(this.p, this.plugin) {
         @Override
         public void onBack() {
            AddonsOverview.this.reopen();
         }
      });
   }
}
