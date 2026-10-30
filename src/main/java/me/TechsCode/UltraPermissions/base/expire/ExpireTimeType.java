package me.TechsCode.UltraPermissions.base.expire;

public enum ExpireTimeType {
   DEVELOPMENT("Development", "This version should only be used for developmental purposes only.", true),
   TESTING("Testing", "This version should only be used for testing purposes only.", true),
   BETA("Beta", "This version is in beta; use at your own risk.", true),
   RELEASE("Release", "Release version.", false);

   private final String name;
   private final String description;
   private final boolean expirable;

   ExpireTimeType(String var3, String var4, boolean var5) {
      this.name = var3;
      this.description = var4;
      this.expirable = var5;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isExpirable() {
      return this.expirable;
   }

   @Override
   public String toString() {
      return "ExpireTimeType{name='" + this.name + "', description='" + this.description + "', expirable=" + this.expirable + "}";
   }
}
