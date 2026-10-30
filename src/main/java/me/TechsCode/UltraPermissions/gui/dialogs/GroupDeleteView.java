package me.TechsCode.UltraPermissions.gui.dialogs;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.views.ConfirmationView;
import me.TechsCode.UltraPermissions.gui.GroupListView;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GroupDeleteView extends ConfirmationView {
   private static final Phrase TITLE = Phrase.create("groupDeleteView.title", "Confirm deletion of %group%");
   private final UltraPermissions plugin;
   private final Group group;

   public GroupDeleteView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2, TITLE.get().replace("%group%", var3.getName()));
      this.plugin = var2;
      this.group = var3;
   }

   @Override
   public void choose(boolean var1) {
      if (var1) {
         this.group.remove();
         new GroupListView(this.p, this.plugin) {
            @Override
            public void onBack() {
               GroupDeleteView.this.reopen();
            }
         };
      } else {
         this.reopen();
      }
   }

   @Override
   public abstract void reopen();
}
