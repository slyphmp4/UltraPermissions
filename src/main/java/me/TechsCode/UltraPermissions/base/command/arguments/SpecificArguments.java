package me.TechsCode.UltraPermissions.base.command.arguments;

import java.util.Arrays;
import java.util.List;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;

public class SpecificArguments extends Argument<String> {
   private final String[] arguments;

   public SpecificArguments(String[] var1) {
      super();
      this.arguments = var1;
   }

   @Override
   public ArgumentValue<String> get(String var1) {
      return Arrays.stream(this.arguments)
         .filter(var1x -> var1x.equalsIgnoreCase(var1))
         .findFirst()
         .map(var1x -> ArgumentValue.of(var1, var1x))
         .orElse(ArgumentValue.empty(var1, EmptyReason.NO_MATCH));
   }

   @Override
   public String getUsage() {
      return "<" + String.join(" / ", this.arguments) + ">";
   }

   @Override
   public List<String> getTabCompletions() {
      return Arrays.asList(this.arguments);
   }
}
