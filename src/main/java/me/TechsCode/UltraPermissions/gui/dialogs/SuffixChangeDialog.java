package me.TechsCode.UltraPermissions.gui.dialogs;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class SuffixChangeDialog extends UserInput {
   private static final Phrase MAIN_TITLE = Phrase.create("suffixDialog.title", "Edit Suffix", Colors.AQUA);
   private static final Phrase SUB_TITLE = Phrase.create("suffixDialog.subtitle", "Type in a new suffix for **%holder%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase ACTION_BAR = Phrase.create("suffixDialog.actionBar", "Type in **none** to clear the suffix", Colors.GRAY, Colors.RED);
   private final PermissionHolder holder;

   public SuffixChangeDialog(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, MAIN_TITLE.get(), SUB_TITLE.get().replace("%holder%", var3.getName()), ACTION_BAR.get());
      this.holder = var3;
   }

   public abstract void reopen();

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   @Override
   public boolean onResult(String var1) {
      this.holder.setSuffix(var1.equalsIgnoreCase("none") ? null : var1);
      this.reopen();
      return true;
   }
}
