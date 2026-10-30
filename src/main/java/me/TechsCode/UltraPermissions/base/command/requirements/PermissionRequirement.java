package me.TechsCode.UltraPermissions.base.command.requirements;

import java.util.List;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.command.Requirement;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionRequirement implements Requirement {
   public static Phrase NO_PERMISSION_MESSAGE = Phrase.create("command.noPermission", "You don't have **access** to that command", Colors.GRAY, Colors.RED);
   private final SpigotTechPlugin plugin;
   private final String permission;

   public PermissionRequirement(SpigotTechPlugin var1, String var2) {
      super();
      this.plugin = var1;
      this.permission = var2;
   }

   @Override
   public boolean isMatching(CommandSender var1, Player var2, List<String> var3) {
      return var1.hasPermission(this.permission);
   }

   @Override
   public void onUnmatched(CommandSender var1, Player var2) {
      var2.sendMessage(this.plugin.getPrefix() + NO_PERMISSION_MESSAGE);
      this.plugin.log("Player " + var2.getName() + " could not access a command because he does not have " + this.permission);
   }
}
