package me.TechsCode.UltraPermissions.base.command.requirements;

import java.util.List;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.command.Requirement;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerRequirement implements Requirement {
   private static final Phrase NO_CONSOLE = Phrase.create("command.noConsole", "This command cannot be run through the console", Colors.GRAY);
   private final SpigotTechPlugin plugin;

   public PlayerRequirement(SpigotTechPlugin var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public boolean isMatching(CommandSender var1, Player var2, List<String> var3) {
      return var2 != null;
   }

   @Override
   public void onUnmatched(CommandSender var1, Player var2) {
      var1.sendMessage(this.plugin.getPrefix() + NO_CONSOLE);
   }
}
