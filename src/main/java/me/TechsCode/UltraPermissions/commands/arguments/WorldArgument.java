package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldArgument extends Argument<World> {
   private UltraPermissions plugin;
   private EmptyReason emptyReason;

   public WorldArgument(UltraPermissions var1, EmptyReason var2) {
      super();
      this.plugin = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<World> get(String var1) {
      Optional<World> var2 = Optional.ofNullable(Bukkit.getWorld(var1));
      return var1.equalsIgnoreCase("all")
         ? ArgumentValue.of(var1, null)
         : var2.<ArgumentValue<World>>map(var1x -> ArgumentValue.of(var1, var1x)).orElseGet(() -> ArgumentValue.empty(var1, this.emptyReason));
   }

   @Override
   public String getUsage() {
      return "<World>";
   }

   @Override
   public List<String> getTabCompletions() {
      ArrayList var1 = new ArrayList<>(Arrays.asList("all"));
      Bukkit.getWorlds().stream().forEach(var1x -> var1.add(var1x.getName()));
      return var1;
   }
}
