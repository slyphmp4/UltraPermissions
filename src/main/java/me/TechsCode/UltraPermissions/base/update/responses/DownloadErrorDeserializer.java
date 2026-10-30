package me.TechsCode.UltraPermissions.base.update.responses;

import java.lang.reflect.Type;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonDeserializationContext;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonDeserializer;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonElement;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class DownloadErrorDeserializer implements JsonDeserializer<DownloadErrorResponse> {
   public DownloadErrorDeserializer() {
      super();
   }

   public DownloadErrorResponse deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) {
      JsonObject var4 = var1.getAsJsonObject();
      return var3.deserialize(var4, DownloadErrorResponse.class);
   }
}
