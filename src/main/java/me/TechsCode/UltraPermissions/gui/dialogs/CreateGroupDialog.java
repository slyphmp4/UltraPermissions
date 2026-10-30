package me.TechsCode.UltraPermissions.gui.dialogs;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class CreateGroupDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("createGroupDialog.title", "Create Group");
   private static final Phrase SUB_TITLE = Phrase.create("createGroupDialog.subTitle", "Type in a group name", Colors.GRAY);
   private static final Phrase NO_SPACES = Phrase.create("createGroupDialog.noSpaces", "The Group Name **should not** contain spaces", Colors.GRAY, Colors.RED);
   private static final Phrase NO_COLORS = Phrase.create(
      "createGroupDialog.noColors", "The Group Name **should not** contain Color Codes", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAKEN = Phrase.create("createGroupDialog.nameTaken", "This Group Name is already in use", Colors.GRAY);
   private final Player p;
   private final UltraPermissions plugin;

   public CreateGroupDialog(Player var1, UltraPermissions var2) {
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
         boolean var2 = this.plugin.getGroups().name((String)var1).isPresent();
         if (var2) {
            this.p.sendMessage(this.plugin.getPrefix() + NAME_TAKEN);
            return false;
         } else {
            this.plugin.newGroup((String)var1).setPrefix(Colors.GREEN + var1).create();
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
