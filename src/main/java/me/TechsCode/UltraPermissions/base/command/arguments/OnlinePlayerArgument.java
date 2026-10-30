package me.TechsCode.UltraPermissions.base.command.arguments;

import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OnlinePlayerArgument extends Argument<Player> {
   private final String usage;
   private final EmptyReason emptyReason;

   public OnlinePlayerArgument(String var1, EmptyReason var2) {
      super();
      this.usage = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<Player> get(String var1) {
      Player var2 = Bukkit.getPlayer(var1);
      return var2 != null && var2.isOnline() ? ArgumentValue.of(var1, var2) : ArgumentValue.empty(var1, this.emptyReason);
   }

   @Override
   public String getUsage() {
      return this.usage;
   }

   @Override
   public List<String> getTabCompletions() {
      return Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).collect(Collectors.toList());
   }
}
