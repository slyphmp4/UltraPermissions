package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class SearchPermissionDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("searchPermissionDialog.title", "Search for Permission", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create(
      "searchPermissionDialog.subtitle", "Type in a **Search Term** in Chat to search", Colors.GRAY, Colors.YELLOW
   );
   private final Player p;
   private final UltraPermissions plugin;
   private final PermissionHolder holder;

   public SearchPermissionDialog(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, TITLE, SUBTITLE);
      this.p = var1;
      this.plugin = var2;
      this.holder = var3;
   }

   @Override
   public boolean onResult(String var1) {
      new SearchPermissionListView(this.p, this.plugin, this.holder, var1) {
         @Override
         public void onBack() {
            SearchPermissionDialog.this.reopen();
         }
      };
      return true;
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   public abstract void reopen();
}
