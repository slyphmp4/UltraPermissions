package me.TechsCode.UltraPermissions.base.addons.gui;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.InstalledAddon;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudAddon;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class InstalledAddonsView extends PageableGUI<InstalledAddon> {
   protected final SpigotTechPlugin plugin;
   private static final Phrase GUI_TITLE = Phrase.create("InstalledAddonsView.gui.title", "Addons > Installed");
   private static final Phrase BUTTON_LORE_PRESS_Q = Phrase.create(
      "InstalledAddonsView.lore.pressQ", "**Press Q** to **delete**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase BUTTON_LORE_ADDON_DELETED = Phrase.create("InstalledAddonsView.lore.addon.deleted", "This addon has been deleted!", Colors.RED);
   private static final Phrase BUTTON_LORE_ADDON_INSTALLED = Phrase.create(
      "InstalledAddonsView.lore.addon.installed", "This addon is **installed** and is currently **running**", Colors.GRAY, Colors.YELLOW, Colors.GREEN
   );
   private static final Phrase BUTTON_LORE_ADDON_REQUIREMENT = Phrase.create(
      "InstalledAddonsView.lore.addon.requirement", "This addon requires **%requirement%** to load", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase BUTTON_LORE_ADDON_ERROR_LINE_1 = Phrase.create(
      "InstalledAddonsView.lore.addon.error.line1", "An error occurred while trying to load this addon,", Colors.RED
   );
   private static final Phrase BUTTON_LORE_ADDON_ERROR_LINE_2 = Phrase.create(
      "InstalledAddonsView.lore.addon.error.line2", "Please contact the addon author!", Colors.YELLOW
   );
   private static final Phrase BUTTON_LORE_ADDON_DESCRIPTION = Phrase.create("InstalledAddonsView.lore.addon.description", "Description:", Colors.GRAY);
   private static final Phrase BUTTON_LORE_ADDON_INSTALLED_FROM_CLOUD = Phrase.create(
      "InstalledAddonsView.lore.addon.installedFromCloud", "This addon was installed from the Cloud", Colors.YELLOW
   );
   private static final Phrase BUTTON_LORE_PRESS_NOT_IN_CLOUD = Phrase.create(
      "InstalledAddonsView.lore.addon.notFoundInCloud", "This addon could not be found in the Cloud", Colors.GRAY
   );

   public InstalledAddonsView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
   }

   @Override
   public abstract void onBack();

   @Override
   public String getTitle() {
      return GUI_TITLE.get();
   }

   @Override
   public SearchFeature<InstalledAddon> getSearch() {
      return new BasicSearch<InstalledAddon>() {
         public String[] getSearchableText(InstalledAddon var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   public InstalledAddon[] getObjects() {
      return this.plugin.getAddonManager().getInstalledAddons().toArray(new InstalledAddon[0]);
   }

   public void construct(Button var1, InstalledAddon var2) {
      XMaterial var3 = XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE;
      var1.material(var2.isLoaded() ? var3 : XMaterial.ORANGE_STAINED_GLASS_PANE)
         .name(Animation.wave(var2.getName(), var2.isLoaded() ? Colors.GREEN : Colors.Orange, Colors.WHITE) + " §7v" + var2.getVersion());
      File var4 = var2.getAddonFile();
      if (var4.exists()) {
         var1.item().appendLore(BUTTON_LORE_PRESS_Q.get(), "");
      }

      if (!var4.exists()) {
         var1.item().appendLore(BUTTON_LORE_ADDON_DELETED.get());
      } else if (var2.isLoaded()) {
         var1.item().addEnchantment(XEnchantment.LUCK_OF_THE_SEA.getEnchant(), 3);
         var1.item().appendLore(BUTTON_LORE_ADDON_INSTALLED.get());
      } else if (var2.getDepend() != null) {
         var1.item().appendLore(BUTTON_LORE_ADDON_REQUIREMENT.get().replace("%requirement%", var2.getDepend()));
      } else {
         var1.item().appendLore(BUTTON_LORE_ADDON_ERROR_LINE_1.get());
         var1.item().appendLore(BUTTON_LORE_ADDON_ERROR_LINE_2.get());
      }

      Optional var5 = var2.getCloudAddon();
      if (var5.isPresent()) {
         CloudAddon var6 = (CloudAddon)var5.get();
         if (var6.getDescription() != null) {
            var1.item().appendLore("");
            var1.item().appendLore(BUTTON_LORE_ADDON_DESCRIPTION.get());
            Arrays.stream(Tools.lineSplitter(var6.getDescription(), 45)).forEach(var1x -> var1.item().appendLore("§f" + var1x));
            var1.item().appendLore("");
         }

         var1.item().appendLore(BUTTON_LORE_ADDON_INSTALLED_FROM_CLOUD.get());
      } else {
         var1.item().appendLore(BUTTON_LORE_PRESS_NOT_IN_CLOUD.get());
      }

      var1.action(var1x -> {
         if (var1x == ActionType.Q) {
            var2.uninstall();
         }
      });
   }
}
