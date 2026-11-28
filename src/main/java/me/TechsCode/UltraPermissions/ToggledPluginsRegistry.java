package me.TechsCode.UltraPermissions;

import java.util.ArrayList;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class ToggledPluginsRegistry extends RegistryStorable {
   private volatile ArrayList<String> toggledPlugins = new ArrayList<>();

   public ToggledPluginsRegistry() {
      super("toggledPlugins");
   }

   @Override
   public void setState(JsonObject var1) {
      ArrayList<String> next = new ArrayList<>(this.toggledPlugins);
      for (String var3 : next) {
         if (var1.get(var3) == null) {
            var1.addProperty(var3, false);
         }
      }

      for (Entry var5 : var1.entrySet()) {
         if (!next.contains(var5.getKey())) {
            next.add((String)var5.getKey());
         }
      }
      this.toggledPlugins = next;
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
      return this.usesRegionThreads() ? new ArrayList<>(this.toggledPlugins) : this.toggledPlugins;
   }

   private boolean usesRegionThreads() {
      return UltraPermissions.getAPI() instanceof UltraPermissions plugin && plugin.isFolia();
   }

   public void setToggledPlugins(ArrayList<String> var1) {
      this.mutate(() -> {
         this.toggledPlugins = this.usesRegionThreads() ? new ArrayList<>(var1) : var1;
         this.sync();
      });
   }
}
