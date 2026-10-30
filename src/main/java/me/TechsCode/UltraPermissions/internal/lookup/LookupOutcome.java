package me.TechsCode.UltraPermissions.internal.lookup;

public class LookupOutcome {
   private final boolean result;
   private final String source;

   public LookupOutcome(boolean var1, String var2) {
      super();
      this.result = var1;
      this.source = var2;
   }

   public boolean getResult() {
      return this.result;
   }

   public boolean hasSource() {
      return this.source != null;
   }

   public String getSource() {
      return this.source;
   }
}
