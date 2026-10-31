package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class StringArgument extends Argument<String> {
   private UltraPermissions plugin;
   private String usage;
   private Phrase localizedUsage;

   public StringArgument(UltraPermissions var1, String var2) {
      super();
      this.plugin = var1;
      this.usage = var2;
   }

   public StringArgument(UltraPermissions plugin, Phrase usage) {
      this.plugin = plugin;
      this.localizedUsage = usage;
   }

   @Override
   public ArgumentValue<String> get(String var1) {
      return ArgumentValue.of(var1, var1);
   }

   @Override
   public String getUsage() {
      return "<" + (this.localizedUsage == null ? this.usage : this.localizedUsage.get()) + ">";
   }

   @Override
   public List<String> getTabCompletions() {
      List var1 = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
      var1.add("Bukkit");
      return this.plugin.getPermissionDatabase().getAllPermissionInfos().plugins(var1).stream().map(PermissionInfo::getPermission).collect(Collectors.toList());
   }
}
