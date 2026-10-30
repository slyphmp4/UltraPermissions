package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserArgument extends Argument<User> {
   private UltraPermissions plugin;
   private EmptyReason emptyReason;

   public UserArgument(UltraPermissions var1, EmptyReason var2) {
      super();
      this.plugin = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<User> get(String var1) {
      Optional<User> var2 = this.plugin.getUsers().name(var1);
      return var2.<ArgumentValue<User>>map(var1x -> ArgumentValue.of(var1, var1x)).orElseGet(() -> ArgumentValue.empty(var1, this.emptyReason));
   }

   @Override
   public String getUsage() {
      return "<User>";
   }

   @Override
   public List<String> getTabCompletions() {
      return this.plugin.getUsers().stream().map(User::getName).collect(Collectors.toList());
   }
}
