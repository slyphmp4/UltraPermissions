package me.TechsCode.UltraPermissions.base.update.responses;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class GetPluginsResponse {
   private int statusCode;
   private JsonObject data;
   private String message;

   public GetPluginsResponse(int var1, JsonObject var2, String var3) {
      super();
      this.statusCode = var1;
      this.data = var2;
      this.message = var3;
   }

   public GetPluginsResponse(int var1) {
      super();
      this.statusCode = var1;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public JsonObject getData() {
      return this.data;
   }

   public String getMessage() {
      return this.message;
   }
}
