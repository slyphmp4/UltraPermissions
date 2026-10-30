package me.TechsCode.UltraPermissions.base.update.responses;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class CreateUpdateRequestResponse {
   private int statusCode;
   private String updateToken;
   private String message;

   public CreateUpdateRequestResponse(int var1, String var2, String var3) {
      super();
      this.statusCode = var1;
      this.updateToken = var2;
      this.message = var3;
   }

   public CreateUpdateRequestResponse(JsonObject var1) {
      super();
      this.statusCode = var1.has("statusCode") ? var1.get("statusCode").getAsInt() : 500;
      this.updateToken = var1.has("updateToken") ? var1.get("updateToken").getAsString() : null;
      this.message = var1.has("message") ? var1.get("message").getAsString() : null;
   }

   public CreateUpdateRequestResponse(int var1) {
      super();
      this.statusCode = var1;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public String getUpdateToken() {
      return this.updateToken;
   }

   public String getMessage() {
      return this.message;
   }

   public JsonObject getAsJsonObject() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("statusCode", this.statusCode);
      var1.addProperty("updateToken", this.updateToken);
      var1.addProperty("message", this.message);
      return var1;
   }
}
