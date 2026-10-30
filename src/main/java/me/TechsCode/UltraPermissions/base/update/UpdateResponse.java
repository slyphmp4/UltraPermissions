package me.TechsCode.UltraPermissions.base.update;

import java.io.File;

public class UpdateResponse {
   private ResponseType type;
   private File file;

   public UpdateResponse(ResponseType var1, File var2) {
      super();
      this.type = var1;
      this.file = var2;
   }

   public ResponseType getType() {
      return this.type;
   }

   public File getFile() {
      return this.file;
   }
}
