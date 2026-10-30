package me.TechsCode.UltraPermissions.base.command.arguments;

import java.util.Collections;
import java.util.List;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;

public class SpecificArgument extends Argument<String> {
   private final String argument;

   public SpecificArgument(String var1) {
      super();
      this.argument = var1;
   }

   @Override
   public String getUsage() {
      return this.argument;
   }

   @Override
   public ArgumentValue<String> get(String var1) {
      return this.argument.equalsIgnoreCase(var1) ? ArgumentValue.of(var1, this.argument) : ArgumentValue.empty(var1, EmptyReason.NO_MATCH);
   }

   @Override
   public List<String> getTabCompletions() {
      return Collections.singletonList(this.argument);
   }
}
