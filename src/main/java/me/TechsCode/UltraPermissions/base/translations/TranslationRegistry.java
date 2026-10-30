package me.TechsCode.UltraPermissions.base.translations;

import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class TranslationRegistry extends RegistryStorable {
   private String language = "English";

   public TranslationRegistry() {
      super("translation");
   }

   @Override
   public void setState(JsonObject var1) {
      this.language = var1.get("language").getAsString();
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("language", this.language);
      return var1;
   }

   public String getLanguage() {
      return this.language;
   }

   public void setLanguage(String var1) {
      this.language = var1;
      this.sync();
   }
}
