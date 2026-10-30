package me.TechsCode.UltraPermissions.base.update.responses;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class DownloadErrorResponse {
   private final String status;
   private final int code;
   private final String message;

   public DownloadErrorResponse(String var1, int var2, String var3) {
      super();
      this.status = var1;
      this.code = var2;
      this.message = var3;
   }

   public DownloadErrorResponse(JsonObject var1) {
      super();
      this.status = var1.has("status") ? var1.get("status").getAsString() : null;
      this.code = var1.has("code") ? var1.get("code").getAsInt() : 500;
      this.message = var1.has("message") ? var1.get("message").getAsString() : null;
   }

   public String getMessage() {
      return this.message;
   }

   public int getCode() {
      return this.code;
   }

   public String getStatus() {
      return this.status;
   }

   public JsonObject getAsJsonObject() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("status", this.status);
      var1.addProperty("code", this.code);
      var1.addProperty("message", this.message);
      return var1;
   }
}
