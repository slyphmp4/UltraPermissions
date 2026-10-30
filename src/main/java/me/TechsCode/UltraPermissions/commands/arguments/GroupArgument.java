package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.storage.objects.Group;

public class GroupArgument extends Argument<Group> {
   private UltraPermissions plugin;
   private EmptyReason emptyReason;

   public GroupArgument(UltraPermissions var1, EmptyReason var2) {
      super();
      this.plugin = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<Group> get(String var1) {
      Optional<Group> var2 = this.plugin.getGroups().name(var1);
      return var2.<ArgumentValue<Group>>map(var1x -> ArgumentValue.of(var1, var1x)).orElseGet(() -> ArgumentValue.empty(var1, this.emptyReason));
   }

   @Override
   public String getUsage() {
      return "<Group>";
   }

   @Override
   public List<String> getTabCompletions() {
      return this.plugin.getGroups().stream().map(Group::getName).collect(Collectors.toList());
   }
}
