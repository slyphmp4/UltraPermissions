package me.TechsCode.UltraPermissions.base.command.arguments;

import java.util.Collections;
import java.util.List;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.translations.Phrase;

public class AnyArgument extends Argument<String> {
   private final String usage;
   private final Phrase localizedUsage;

   public AnyArgument(String var1) {
      super();
      this.usage = var1;
      this.localizedUsage = null;
   }

   public AnyArgument(Phrase usage) {
      this.usage = null;
      this.localizedUsage = usage;
   }

   @Override
   public String getUsage() {
      return this.localizedUsage == null ? this.usage : this.localizedUsage.get();
   }

   @Override
   public ArgumentValue<String> get(String var1) {
      return ArgumentValue.of(var1, var1);
   }

   @Override
   public List<String> getTabCompletions() {
      return Collections.emptyList();
   }
}
