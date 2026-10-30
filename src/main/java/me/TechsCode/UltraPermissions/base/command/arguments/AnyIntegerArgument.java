package me.TechsCode.UltraPermissions.base.command.arguments;

import java.util.Collections;
import java.util.List;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;

public class AnyIntegerArgument extends Argument<Integer> {
   private final String usage;
   private final EmptyReason emptyReason;

   public AnyIntegerArgument(String var1, EmptyReason var2) {
      super();
      this.usage = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<Integer> get(String var1) {
      try {
         return ArgumentValue.of(var1, Integer.parseInt(var1));
      } catch (NumberFormatException var3) {
         return ArgumentValue.empty(var1, this.emptyReason);
      }
   }

   @Override
   public String getUsage() {
      return this.usage;
   }

   @Override
   public List<String> getTabCompletions() {
      return Collections.emptyList();
   }
}
