package me.TechsCode.UltraPermissions.base.addons.gui;

import java.util.Optional;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.InstalledAddon;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudAddon;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class AddonsMarketplaceListView extends PageableGUI<CloudAddon> {
   private final SpigotTechPlugin plugin;
   private boolean loading;
   private static final Phrase LOADING = Phrase.create("AddonsMarketplaceListView.loading", "Loading...");
   private static final Phrase TITLE = Phrase.create("AddonsMarketplaceListView.gui.title", "Addons > Marketplace");
   private static final Phrase BUTTON_LORE_ADDON_INSTALL = Phrase.create(
      "AddonsMarketplaceListView.button.lore.addon.install", "Click to **install** this addon", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase BUTTON_LORE_ADDON_UPDATE = Phrase.create(
      "AddonsMarketplaceListView.button.lore.addon.update", "Click to **update** this addon", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase BUTTON_LORE_ADDON_INSTALLED = Phrase.create(
      "AddonsMarketplaceListView.button.lore.addon.installed", "This addon is **installed**", Colors.Gray, Colors.GREEN
   );
   private static final Phrase BUTTON_LORE_DESCRIPTION = Phrase.create("AddonsMarketplaceListView.button.lore.description", "Description:", Colors.GRAY);
   private static final Phrase BUTTON_LORE_CREATOR = Phrase.create(
      "AddonsMarketplaceListView.button.lore.creator", "Creator: **%creatorName%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase BUTTON_LORE_DOWNLOADS = Phrase.create(
      "AddonsMarketplaceListView.button.lore.downloads", "Downloads: **%amount%**", Colors.GRAY, Colors.YELLOW
   );

   public AddonsMarketplaceListView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
   }

   @Override
   public abstract void onBack();

   @Override
   public String getTitle() {
      return this.loading ? LOADING.get() : TITLE.get();
   }

   @Override
   public SearchFeature<CloudAddon> getSearch() {
      return new BasicSearch<CloudAddon>() {
         public String[] getSearchableText(CloudAddon var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   public CloudAddon[] getObjects() {
      return this.plugin.getAddonManager().getCloudAddons().toArray(new CloudAddon[0]);
   }

   public void construct(Button var1, CloudAddon var2) {
      if (this.loading) {
         var1.material(XMaterial.YELLOW_STAINED_GLASS_PANE).name("§f§l" + LOADING.get());
      } else {
         Optional var3 = this.plugin.getAddonManager().getInstalledAddons().stream().filter(var1x -> var1x.getName().equals(var2.getName())).findFirst();
         boolean var4 = var3.isPresent();
         boolean var5 = var3.isPresent() && ((InstalledAddon)var3.get()).isOutOfDate();
         var1.material(
               var4
                  ? (var5 ? XMaterial.ORANGE_STAINED_GLASS : XMaterial.from(var2.getMaterial()).orElse(XMaterial.GREEN_STAINED_GLASS))
                  : XMaterial.WHITE_STAINED_GLASS
            )
            .name(Animation.wave(var2.getName(), var4 ? (var5 ? Colors.Orange : Colors.GREEN) : Colors.Gray, Colors.WHITE) + " §fv" + var2.getVersion())
            .lore(var4 ? (var5 ? BUTTON_LORE_ADDON_UPDATE.get() : BUTTON_LORE_ADDON_INSTALLED.get()) : BUTTON_LORE_ADDON_INSTALL.get());
         var1.item().appendLore("");
         if (var2.getDescription() != null) {
            var1.item().appendLore(BUTTON_LORE_DESCRIPTION.get());

            for (String var9 : Tools.lineSplitter(var2.getDescription(), 35)) {
               var1.item().appendLore("§f" + var9);
            }

            var1.item().appendLore("");
         }

         var1.item()
            .appendLore(
               BUTTON_LORE_CREATOR.get().replace("%creatorName%", var2.getAuthor()),
               BUTTON_LORE_DOWNLOADS.get().replace("%amount%", String.valueOf(var2.getDownloads()))
            );
         var1.action(var4x -> {
            if (!var4 || var5) {
               this.loading = true;
               this.plugin.getAddonManager().download(var2);
               this.loading = false;
            }
         });
      }
   }
}
