package me.TechsCode.UltraPermissions.base.update.responses;

public class IsUpToDateResponse {
   private int statusCode;
   private boolean isUpToDate;
   private String latestVersion;
   private String message;

   public IsUpToDateResponse(int var1, boolean var2, String var3, String var4) {
      super();
      this.statusCode = var1;
      this.isUpToDate = var2;
      this.latestVersion = var3;
      this.message = var4;
   }

   public IsUpToDateResponse(int var1) {
      super();
      this.statusCode = var1;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public boolean isUpToDate() {
      return this.isUpToDate;
   }

   public String getLatestVersion() {
      return this.latestVersion;
   }

   public String getMessage() {
      return this.message;
   }

   public boolean isSuccessFull() {
      return this.statusCode == 200;
   }
}
