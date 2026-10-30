package me.TechsCode.UltraPermissions.base.command;

import java.util.List;

public abstract class Argument<T> {
   public Argument() {
      super();
   }

   public boolean hasMatch(String var1) {
      return this.get(var1).getValidity() != Validity.NO_MATCH;
   }

   public abstract ArgumentValue<T> get(String var1);

   public abstract String getUsage();

   public abstract List<String> getTabCompletions();
}
