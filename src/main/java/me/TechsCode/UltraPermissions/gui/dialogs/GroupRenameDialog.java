package me.TechsCode.UltraPermissions.gui.dialogs;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GroupRenameDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("groupRenameDialog.title", "Rename Group");
   private static final Phrase SUB_TITLE = Phrase.create("groupRenameDialog.subtitle", "Type in a new name for **%group%**", Colors.GRAY, Colors.YELLOW);
   private final Group group;

   public GroupRenameDialog(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2, TITLE.get(), SUB_TITLE.get().replace("%group%", var3.getName()));
      this.group = var3;
   }

   public abstract void reopen();

   @Override
   public boolean onResult(String var1) {
      this.group.setName(var1.replace(" ", ""));
      this.reopen();
      return true;
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }
}
