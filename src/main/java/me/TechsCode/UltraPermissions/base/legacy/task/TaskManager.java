package me.TechsCode.UltraPermissions.base.legacy.task;

import java.util.Arrays;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.Bukkit;

public class TaskManager {
   public TaskManager(SpigotTechPlugin var1) {
      super();
      Arrays.stream(UpdateTime.values())
         .forEach(var1x -> var1.getScheduler().runTaskTimer(() -> Bukkit.getPluginManager().callEvent(new UpdateEvent(var1x)), 0L, var1x.getTime()));
   }
}
