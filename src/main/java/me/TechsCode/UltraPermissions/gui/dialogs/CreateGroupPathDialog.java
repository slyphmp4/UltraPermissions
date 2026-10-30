package me.TechsCode.UltraPermissions.gui.dialogs;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import org.bukkit.entity.Player;

public abstract class CreateGroupPathDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("createGroupPathDialog.title", "Create Group Path");
   private static final Phrase SUB_TITLE = Phrase.create("createGroupPathDialog.subTitle", "Type in a group path name", Colors.GRAY);
   private static final Phrase NO_SPACES = Phrase.create(
      "createGroupPathDialog.noSpaces", "The Group Path Name **should not** contain spaces", Colors.GRAY, Colors.RED
   );
   private static final Phrase NO_COLORS = Phrase.create(
      "createGroupPathDialog.noColors", "The Group Path Name **should not** contain Color Codes", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAKEN = Phrase.create("createGroupPathDialog.nameTaken", "This Group Path Name is already in use", Colors.GRAY);
   private final Player p;
   private final UltraPermissions plugin;

   public CreateGroupPathDialog(Player var1, UltraPermissions var2) {
      super(var1, var2, TITLE.get(), SUB_TITLE.get());
      this.p = var1;
      this.plugin = var2;
   }

   @Override
   public boolean onResult(String var1) {
      if (var1.contains(" ")) {
         this.p.sendMessage(this.plugin.getPrefix() + NO_SPACES);
         return false;
      } else if (var1.contains("&")) {
         this.p.sendMessage(this.plugin.getPrefix() + NO_COLORS);
         return false;
      } else {
         boolean var2 = this.plugin.getGroupPaths().name(var1).isPresent();
         if (var2) {
            this.p.sendMessage(this.plugin.getPrefix() + NAME_TAKEN);
            return false;
         } else {
            this.plugin.getGroupPathStorage().create(new GroupPath(var1));
            this.reopen();
            return true;
         }
      }
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   public abstract void reopen();
}
