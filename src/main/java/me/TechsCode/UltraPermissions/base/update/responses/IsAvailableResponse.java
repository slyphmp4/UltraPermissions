package me.TechsCode.UltraPermissions.base.update.responses;

public class IsAvailableResponse {
   private int statusCode;
   private boolean isAvailable;

   public IsAvailableResponse(int var1) {
      super();
      this.statusCode = var1;
      this.isAvailable = var1 == 200;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public boolean isAvailable() {
      return this.isAvailable;
   }
}
