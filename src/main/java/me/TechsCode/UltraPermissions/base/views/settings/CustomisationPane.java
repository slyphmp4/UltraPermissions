package me.TechsCode.UltraPermissions.base.views.settings;

import me.TechsCode.UltraPermissions.base.AppearanceRegistry;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.update.Updater;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.base.visual.Text;
import org.bukkit.entity.Player;

public class CustomisationPane extends SettingsPane {
   private static final Phrase TITLE = Phrase.create("customisationPane.title", "Customisation");
   private final SpigotTechPlugin plugin;
   private static final Phrase PREFIX_TITLE = Phrase.create("customisationPane.prefix.title", "Prefix");
   private static final Phrase PREFIX_RESET_ACTION = Phrase.create(
      "customisationPane.prefix.resetAction", "**Left Click** to **reset** the prefix", Colors.GRAY, Colors.AQUA, Colors.GOLD
   );
   private static final Phrase PREFIX_CHANGE_ACTION = Phrase.create(
      "customisationPane.prefix.changeAction", "**Left Click** to **change** the prefix", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase PREFIX_DISABLE_ACTION = Phrase.create(
      "customisationPane.prefix.disableAction", "**Press Q** to **disable** the prefix", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase PREFIX_ENABLE_ACTION = Phrase.create(
      "customisationPane.prefix.enableAction", "Click to **enable** the prefix", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase PREFIX_ATTRIBUTE = Phrase.create("customisationPane.prefix.attribute", "Prefix: **%prefix%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase UPDATER_TITLE = Phrase.create("customisationPane.updater.title", "Ingame Updater");
   private static final Phrase UPDATER_ACTION = Phrase.create("customisationPane.updater.action", "**Click** to %action% ingame Updater", Colors.GRAY, Colors.AQUA);
   private static final Phrase UPDATER_DESC1 = Phrase.create("customisationPane.updater.desc1", "Automatically downloads verified GitHub Release updates", Colors.GRAY);
   private static final Phrase UPDATER_DESC2 = Phrase.create("customisationPane.updater.desc2", "Applies them on your next server restart", Colors.GRAY);
   private static final Phrase GUIINTERVANL_TITLE = Phrase.create("customisationPane.guiUpdateInterval.title", "GUI Update Interval");
   private static final Phrase GUIINTERVANL_RESET_ACTION = Phrase.create(
      "customisationPane.guiUpdateInterval.resetAction", "**Left Click** to **reset** the GUI update interval", Colors.GRAY, Colors.AQUA, Colors.GOLD
   );
   private static final Phrase GUIINTERVANL_CHANGE_ACTION = Phrase.create(
      "customisationPane.guiUpdateInterval.changeAction", "**Left Click** to **change** the GUI update interval", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase GUIINTERVANL_ATTRIBUTE = Phrase.create(
      "customisationPane.guiUpdateInterval.attribute", "Interval: **%interval%** ticks", Colors.GRAY, Colors.WHITE
   );

   public CustomisationPane(Player var1, SettingsView var2, SpigotTechPlugin var3) {
      super(var1, var2);
      this.plugin = var3;
   }

   @Override
   public String getName() {
      return TITLE.get();
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.CRAFTING_TABLE;
   }

   @Override
   public void construct(Model var1) {
      var1.button(21, this::PrefixButton);
      var1.button(23, this::UpdaterButton);
      var1.button(25, this::GuiUpdateIntervalButton);
   }

   private void PrefixButton(Button var1) {
      AppearanceRegistry var2 = this.plugin.getAppearanceRegistry();
      var1.material(XMaterial.NAME_TAG)
         .name(Animation.wave(PREFIX_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore((var2.isPrefixModified() ? PREFIX_RESET_ACTION : PREFIX_CHANGE_ACTION).get());
      if (var2.isPrefixModified()) {
         var1.item().lore(PREFIX_RESET_ACTION.get(), PREFIX_DISABLE_ACTION.get());
      } else if (var2.isPrefixDisabled()) {
         var1.item().lore(PREFIX_ENABLE_ACTION.get());
      } else {
         var1.item().lore(PREFIX_CHANGE_ACTION.get(), PREFIX_DISABLE_ACTION.get());
      }

      var1.item().appendLore("", PREFIX_ATTRIBUTE.get().replace("%prefix%", var2.isPrefixDisabled() ? Colors.RED + Words.DISABLED.get() : var2.getPrefix()));
      var1.action(var2x -> {
         if (var2.isPrefixDisabled()) {
            var2.enablePrefix();
         } else if (var2x == ActionType.Q) {
            var2.disablePrefix();
         } else {
            if (var2.isPrefixModified()) {
               var2.resetPrefix();
            } else {
               new CustomisationPane.PrefixSetDialog(this.p, this.plugin) {
                  @Override
                  void setPrefix(String var1) {
                     var2.setPrefix(var1);
                  }

                  @Override
                  void reopen() {
                     CustomisationPane.this.reopen();
                  }
               };
            }
         }
      });
   }

   private void UpdaterButton(Button var1) {
      AppearanceRegistry var2 = this.plugin.getAppearanceRegistry();
      var1.material(XMaterial.WRITABLE_BOOK)
         .name(Animation.wave(UPDATER_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(
            UPDATER_ACTION.get().replace("%action%", var2.isUpdaterEnabled() ? "§c" + Words.DISABLE.get() + "§7" : "§a" + Words.ENABLE.get() + "§7"),
            "",
            UPDATER_DESC1.get(),
            UPDATER_DESC2.get()
         );
      var1.action(var1x -> {
         var2.toggleUpdater();
         if (var2.isUpdaterEnabled()) Updater.checkForUpdates(this.plugin);
         else Updater.clearNotifications();
      });
   }

   private void GuiUpdateIntervalButton(Button var1) {
      AppearanceRegistry var2 = this.plugin.getAppearanceRegistry();
      var1.material(XMaterial.CLOCK)
         .name(Animation.wave(GUIINTERVANL_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore((var2.isPrefixModified() ? GUIINTERVANL_RESET_ACTION : GUIINTERVANL_CHANGE_ACTION).get());
      if (var2.isGuiUpdateIntervalModified()) {
         var1.item().lore(GUIINTERVANL_RESET_ACTION.get());
      } else {
         var1.item().lore(GUIINTERVANL_CHANGE_ACTION.get());
      }

      var1.item().appendLore("", GUIINTERVANL_ATTRIBUTE.get().replace("%interval%", String.valueOf(var2.getGuiUpdateInterval())));
      var1.action(var2x -> {
         if (var2.isGuiUpdateIntervalModified()) {
            var2.resetGuiUpdateInterval();
         } else {
            new CustomisationPane.GuiUpdateIntervalSetDialog(this.p, this.plugin) {
               @Override
               void setInterval(int var1) {
                  var2.setGuiUpdateInterval(var1);
               }

               @Override
               void reopen() {
                  CustomisationPane.this.reopen();
               }
            };
         }
      });
   }

   abstract static class GuiUpdateIntervalSetDialog extends UserInput {
      private static final Phrase TITLE = Phrase.create("customisationPane.guiUpdateInterval.change.title", "GUI Update Interval", Colors.AQUA);
      private static final Phrase SUBTITLE = Phrase.create(
         "customisationPane.guiUpdateInterval.change.subtitle", "Type in a interval (ticks) Min: 2 Max: 20", Colors.GRAY
      );

      public GuiUpdateIntervalSetDialog(Player var1, SpigotTechPlugin var2) {
         super(var1, var2, TITLE, SUBTITLE);
      }

      @Override
      public boolean onResult(String var1) {
         int var2 = (int)Math.round(Double.parseDouble(var1));
         if (var2 < 2) {
            var2 = 2;
         }

         if (var2 > 20) {
            var2 = 20;
         }

         this.setInterval(var2);
         this.reopen();
         return true;
      }

      @Override
      public void onClose(Player var1) {
         this.reopen();
      }

      abstract void setInterval(int var1);

      abstract void reopen();
   }

   abstract static class PrefixSetDialog extends UserInput {
      private static final Phrase TITLE = Phrase.create("customisationPane.prefix.change.title", "Prefix", Colors.AQUA);
      private static final Phrase SUBTITLE = Phrase.create("customisationPane.prefix.change.subtitle", "Type in a prefix", Colors.GRAY);

      public PrefixSetDialog(Player var1, SpigotTechPlugin var2) {
         super(var1, var2, TITLE, SUBTITLE);
      }

      @Override
      public boolean onResult(String var1) {
         this.setPrefix(Text.color(var1));
         this.reopen();
         return true;
      }

      @Override
      public void onClose(Player var1) {
         this.reopen();
      }

      abstract void setPrefix(String var1);

      abstract void reopen();
   }
}
