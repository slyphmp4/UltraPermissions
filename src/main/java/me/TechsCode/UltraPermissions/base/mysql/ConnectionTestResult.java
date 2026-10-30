package me.TechsCode.UltraPermissions.base.mysql;

public class ConnectionTestResult {
   private final boolean valid;
   private final String error;

   public ConnectionTestResult(boolean var1, String var2) {
      super();
      this.valid = var1;
      this.error = var2;
   }

   public boolean isValid() {
      return this.valid;
   }

   public String getError() {
      return this.error;
   }
}
