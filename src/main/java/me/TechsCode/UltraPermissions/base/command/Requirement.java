package me.TechsCode.UltraPermissions.base.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface Requirement {
   boolean isMatching(CommandSender var1, Player var2, List<String> var3);

   void onUnmatched(CommandSender var1, Player var2);
}
