package me.TechsCode.UltraPermissions.commands.requirements;

import java.util.List;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Requirement;
import me.TechsCode.UltraPermissions.base.command.requirements.PermissionRequirement;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuperadminRequirement implements Requirement {
   private final UltraPermissions plugin;

   public SuperadminRequirement(UltraPermissions var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public boolean isMatching(CommandSender var1, Player var2, List<String> var3) {
      return var2 == null || this.plugin.getUsers().uuid(var2.getUniqueId()).map(User::isSuperadmin).orElse(false);
   }

   @Override
   public void onUnmatched(CommandSender var1, Player var2) {
      var1.sendMessage(this.plugin.getPrefix() + PermissionRequirement.NO_PERMISSION_MESSAGE);
   }
}
