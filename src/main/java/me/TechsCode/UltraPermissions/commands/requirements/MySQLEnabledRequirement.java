package me.TechsCode.UltraPermissions.commands.requirements;

import java.util.List;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Requirement;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MySQLEnabledRequirement implements Requirement {
   private final UltraPermissions plugin;
   private static final Phrase MESSAGE = Phrase.create("mysqlEnabledRequirement.message", "Command not available if MySQL is not configured", Colors.GRAY);

   public MySQLEnabledRequirement(UltraPermissions var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public boolean isMatching(CommandSender var1, Player var2, List<String> var3) {
      return this.plugin.getMySQLManager().isEnabled();
   }

   @Override
   public void onUnmatched(CommandSender var1, Player var2) {
      var1.sendMessage(this.plugin.getPrefix() + MESSAGE.get());
   }
}
