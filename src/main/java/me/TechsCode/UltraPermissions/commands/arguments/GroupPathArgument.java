package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;

public class GroupPathArgument extends Argument<GroupPath> {
   private UltraPermissions plugin;
   private EmptyReason emptyReason;

   public GroupPathArgument(UltraPermissions var1, EmptyReason var2) {
      super();
      this.plugin = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<GroupPath> get(String var1) {
      Optional<GroupPath> var2 = this.plugin.getGroupPaths().name(var1);
      return var2.<ArgumentValue<GroupPath>>map(var1x -> ArgumentValue.of(var1, var1x)).orElseGet(() -> ArgumentValue.empty(var1, this.emptyReason));
   }

   @Override
   public String getUsage() {
      return "<GroupPath>";
   }

   @Override
   public List<String> getTabCompletions() {
      return this.plugin.getGroupPaths().stream().map(GroupPath::getName).collect(Collectors.toList());
   }
}
