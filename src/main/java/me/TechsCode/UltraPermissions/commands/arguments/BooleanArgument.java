package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;

public class BooleanArgument extends Argument<Boolean> {
   private UltraPermissions plugin;
   private EmptyReason emptyReason;

   public BooleanArgument(UltraPermissions var1, EmptyReason var2) {
      super();
      this.plugin = var1;
      this.emptyReason = var2;
   }

   @Override
   public ArgumentValue<Boolean> get(String var1) {
      if (!var1.equalsIgnoreCase("true") && !var1.equalsIgnoreCase("false")) return ArgumentValue.empty(var1, this.emptyReason);
      return ArgumentValue.of(var1, Boolean.parseBoolean(var1));
   }

   @Override
   public String getUsage() {
      return "<AssignDefaultGroup>";
   }

   @Override
   public List<String> getTabCompletions() {
      return new ArrayList<>(Arrays.asList("True", "False"));
   }
}
