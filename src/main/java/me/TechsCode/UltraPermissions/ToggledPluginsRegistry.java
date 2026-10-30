package me.TechsCode.UltraPermissions;

import java.util.ArrayList;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class ToggledPluginsRegistry extends RegistryStorable {
   private ArrayList<String> toggledPlugins = new ArrayList<>();

   public ToggledPluginsRegistry() {
      super("toggledPlugins");
   }

   @Override
   public void setState(JsonObject var1) {
      for (String var3 : this.toggledPlugins) {
         if (var1.get(var3) == null) {
            var1.addProperty(var3, false);
         }
      }

      for (Entry var5 : var1.entrySet()) {
         if (!this.toggledPlugins.contains(var5.getKey())) {
            this.toggledPlugins.add((String)var5.getKey());
         }
      }
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();

      for (String var3 : this.toggledPlugins) {
         var1.addProperty(var3, false);
      }

      return var1;
   }

   public ArrayList<String> getToggledPlugins() {
      return this.toggledPlugins;
   }

   public void setToggledPlugins(ArrayList<String> var1) {
      this.toggledPlugins = var1;
      this.sync();
   }
}
