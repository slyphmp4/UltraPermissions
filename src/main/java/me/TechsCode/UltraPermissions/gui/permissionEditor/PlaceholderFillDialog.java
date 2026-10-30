package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public abstract class PlaceholderFillDialog {
   private final Player p;
   private final UltraPermissions plugin;
   private String permission;
   private String[] placeholders;
   private int internalCounter = 0;
   private static final Phrase SUB_TITLE = Phrase.create(
      "placeholderFillDialog.subtitle", "Type in a value for **[%placeholder%]**", Colors.GRAY, Colors.YELLOW
   );

   public PlaceholderFillDialog(Player var1, UltraPermissions var2, String var3) {
      super();
      this.p = var1;
      this.plugin = var2;
      this.permission = var3;
      this.placeholders = StringUtils.substringsBetween(var3, "[", "]");
      this.run();
   }

   public abstract void onBack();

   public void run() {
      if (this.placeholders == null) {
         this.onComplete(this.permission);
      } else {
         for (final String var4 : this.placeholders) {
            new UserInput(this.p, this.plugin, this.permission, SUB_TITLE.get().replace("%placeholder%", var4)) {
               @Override
               public void onClose(Player var1) {
                  PlaceholderFillDialog.this.onBack();
               }

               @Override
               public boolean onResult(String var1) {
                  PlaceholderFillDialog.this.permission = PlaceholderFillDialog.this.permission.replace("[" + var4 + "]", var1);
                  PlaceholderFillDialog.this.internalCounter++;
                  if (PlaceholderFillDialog.this.internalCounter == PlaceholderFillDialog.this.placeholders.length) {
                     PlaceholderFillDialog.this.onComplete(PlaceholderFillDialog.this.permission);
                  }

                  return true;
               }
            };
         }
      }
   }

   public abstract void onComplete(String var1);
}
