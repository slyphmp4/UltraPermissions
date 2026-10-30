package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public abstract class PurgePlayerPermissionDialog extends UserInput {
   private final Player p;
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private static final Phrase TITLE = Phrase.create("purgePlayerPermissionDialog.title", "Purge Player Permission", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create("purgePlayerPermissionDialog.subtitle", "Type in a Permission via Chat", Colors.GRAY);
   private static final Phrase CONFIRM = Phrase.create(
      "purgePlayerPermissionDialog.confirm", "Successfully **purged** **%permission%** from all Players", Colors.GRAY, Colors.RED, Colors.GREEN
   );

   public PurgePlayerPermissionDialog(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, TITLE.get(), SUBTITLE);
      this.p = var1;
      this.holder = var3;
      this.plugin = var2;
   }

   @Override
   public boolean onResult(String var1) {
      String var2 = var1.replace(" ", ".");

      for (User var4 : this.plugin.getUsers()) {
         PermissionList var5 = var4.getPermissions().name(var2);
         var5.forEach(Permission::remove);
      }

      this.p.sendMessage(this.plugin.getPrefix() + CONFIRM.get().replace("%permission%", var2));
      this.reopen();
      return true;
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   public abstract void reopen();
}
