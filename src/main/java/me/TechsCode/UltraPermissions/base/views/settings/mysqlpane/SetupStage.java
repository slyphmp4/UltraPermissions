package me.TechsCode.UltraPermissions.base.views.settings.mysqlpane;

import me.TechsCode.UltraPermissions.base.misc.Setter;

public class SetupStage {
   private final String fieldName;
   private final String defaultValue;
   private final Setter<String> setFunction;

   public SetupStage(String var1, String var2, Setter<String> var3) {
      super();
      this.fieldName = var1;
      this.defaultValue = var2;
      this.setFunction = var3;
   }

   public String getFieldName() {
      return this.fieldName;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public Setter<String> getSetFunction() {
      return this.setFunction;
   }
}
