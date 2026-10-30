package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PermissionArgument extends Argument<String> {
   private UltraPermissions plugin;

   public PermissionArgument(UltraPermissions var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public ArgumentValue<String> get(String var1) {
      return ArgumentValue.of(var1, var1);
   }

   @Override
   public String getUsage() {
      return "<Permission>";
   }

   @Override
   public List<String> getTabCompletions() {
      List var1 = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
      var1.add("Bukkit");
      return this.plugin.getPermissionDatabase().getAllPermissionInfos().plugins(var1).stream().map(PermissionInfo::getPermission).collect(Collectors.toList());
   }
}
