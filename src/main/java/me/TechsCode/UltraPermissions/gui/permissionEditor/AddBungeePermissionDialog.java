package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class AddBungeePermissionDialog extends UserInput {
   private final Player p;
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private static final Phrase TITLE = Phrase.create("addBungeePermissionDialog.title", "Add Bungee Permission", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create("addBungeePermissionDialog.subtitle", "Type in a Permission via Chat", Colors.GRAY);

   public AddBungeePermissionDialog(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, TITLE, SUBTITLE);
      this.p = var1;
      this.plugin = var2;
      this.holder = var3;
   }

   @Override
   public boolean onResult(String var1) {
      String var2 = var1.replace(" ", ".");
      this.holder.newPermission(var2).setServer("BungeeCord").create();
      new FullPermissionListView(this.p, this.plugin, this.holder) {
         @Override
         public void onBack() {
            AddBungeePermissionDialog.this.reopen();
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
