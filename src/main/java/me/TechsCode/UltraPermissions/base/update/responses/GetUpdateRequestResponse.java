package me.TechsCode.UltraPermissions.base.update.responses;

import me.TechsCode.UltraPermissions.base.update.UpdateRequest;

public class GetUpdateRequestResponse {
   private final int statusCode;
   private UpdateRequest updateRequest;
   private String message;

   public GetUpdateRequestResponse(int var1, UpdateRequest var2, String var3) {
      super();
      this.statusCode = var1;
      this.updateRequest = var2;
      this.message = var3;
   }

   public GetUpdateRequestResponse(int var1) {
      super();
      this.statusCode = var1;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public UpdateRequest getUpdateRequest() {
      return this.updateRequest;
   }

   public String getMessage() {
      return this.message;
   }
}
