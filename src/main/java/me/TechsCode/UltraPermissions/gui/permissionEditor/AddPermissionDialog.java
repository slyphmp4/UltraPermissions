package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class AddPermissionDialog extends UserInput {
   private final Player p;
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private static final Phrase TITLE = Phrase.create("addPermissionDialog.title", "Add Permission", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create("addPermissionDialog.subtitle", "Type in a Permission via Chat", Colors.GRAY);

   public AddPermissionDialog(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, TITLE.get(), SUBTITLE);
      this.p = var1;
      this.plugin = var2;
      this.holder = var3;
   }

   @Override
   public boolean onResult(String var1) {
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { this.close(true); return false; }
      String var2 = var1.replace(" ", ".");
      this.holder.newPermission(var2).create();
      new PermissionView(this.p, this.plugin, this.holder, new PermissionWithInfo(var2, this.plugin.getPermissionDatabase().getInfo(var2))) {
         @Override
         public void onBack() {
            new FullPermissionListView(this.p, AddPermissionDialog.this.plugin, AddPermissionDialog.this.holder) {
               @Override
               public void onBack() {
                  AddPermissionDialog.this.reopen();
               }
            };
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
