package me.TechsCode.UltraPermissions.gui.dialogs;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class TogglePluginDialog extends UserInput {
   private UltraPermissions plugin;
   private ArrayList<String> toggledPlugins;
   private boolean isIn;
   private Player p;
   private static final Phrase TITLE = Phrase.create("togglePluginDialog.title", "Toggle Plugin");
   private static final Phrase SUB_TITLE = Phrase.create("togglePluginDialog.subtitle", "Type in the **Plugin Name**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase TOGGLED_EXCLUDE = Phrase.create(
      "togglePluginDialog.plugin.exclude", "**%PluginName%** is now excluded from the Permissions Log", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase TOGGLED_INCLUDE = Phrase.create(
      "togglePluginDialog.plugin.include", "**%PluginName%** is now included again in the Permissions Log", Colors.GRAY, Colors.YELLOW
   );

   public TogglePluginDialog(Player var1, UltraPermissions var2) {
      super(var1, var2, TITLE.get(), SUB_TITLE.get());
      this.plugin = var2;
      this.toggledPlugins = var2.getToggledPluginsRegistry();
      this.p = var1;
   }

   public abstract void reopen();

   @Override
   public boolean onResult(String var1) {
      this.isIn = false;

      for (String var3 : this.plugin.getToggledPluginsRegistry()) {
         if (var3.equalsIgnoreCase(var1)) {
            var1 = var3;
            this.isIn = true;
         }
      }

      if (this.isIn) {
         this.p.sendMessage(this.plugin.getPrefix() + TOGGLED_INCLUDE.get().replace("%PluginName%", var1));
         this.toggledPlugins.remove(var1);
         this.plugin.setToggledPluginsRegistry(this.toggledPlugins);
         this.plugin.getToggledPluginsRegistry().remove(var1);
         this.reopen();
         return true;
      } else {
         this.p.sendMessage(this.plugin.getPrefix() + TOGGLED_EXCLUDE.get().replace("%PluginName%", var1));
         this.toggledPlugins.add(var1);
         this.plugin.setToggledPluginsRegistry(this.toggledPlugins);
         this.plugin.getToggledPluginsRegistry().add(var1);
         this.plugin.getPermissionLogger().clearManually(this.p, var1);
         this.reopen();
         return true;
      }
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }
}
