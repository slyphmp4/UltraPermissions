package me.TechsCode.UltraPermissions.gui.settings;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.views.settings.CustomisationPane;
import me.TechsCode.UltraPermissions.base.views.settings.LanguagePane;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsPane;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsView;
import me.TechsCode.UltraPermissions.base.views.settings.mysqlpane.MySQLPane;
import me.TechsCode.UltraPermissions.gui.settings.formatPane.FormatPane;
import org.bukkit.entity.Player;

public abstract class UpermsSettingsView extends SettingsView {
   public UpermsSettingsView(Player var1, UltraPermissions var2) {
      super(var1, var2);
   }

   @Override
   public SettingsPane[] getPanes() {
      return new SettingsPane[]{
         new CustomisationPane(this.p, this, this.plugin),
         new FormatPane(this.p, this, (UltraPermissions)this.plugin),
         new LanguagePane(this.p, this, this.plugin),
         new MiscPane(this.p, this, (UltraPermissions)this.plugin),
         new MySQLPane(this.p, this, this.plugin)
      };
   }
}
