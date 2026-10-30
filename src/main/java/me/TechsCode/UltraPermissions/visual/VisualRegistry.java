package me.TechsCode.UltraPermissions.visual;

import java.util.HashMap;
import java.util.Map;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.base.registry.Switchable;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class VisualRegistry extends RegistryStorable implements Switchable {
   private boolean switchedTo = false;
   private Map<VisualType, String> formats = new HashMap<>();
   private boolean editingNametags = true;

   public VisualRegistry() {
      super("visual");

      for (VisualType var4 : VisualType.values()) {
         this.formats.put(var4, var4.getDefaultFormat());
      }
   }

   @Override
   public void setState(JsonObject var1) {
      this.switchedTo = var1.get("switchedTo").getAsBoolean();
      this.formats = new HashMap<>();
      JsonObject var2 = var1.getAsJsonObject("formats");
      var2.entrySet().forEach(var1x -> this.formats.put(VisualType.valueOf(var1x.getKey()), Text.color(var1x.getValue().getAsString())));
      this.editingNametags = !var1.has("editingNametags") || var1.get("editingNametags").getAsBoolean();
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("switchedTo", this.switchedTo);
      JsonObject var2 = new JsonObject();
      this.formats.forEach((var1x, var2x) -> var2.addProperty(var1x.name(), var2x));
      var1.add("formats", var2);
      var1.addProperty("editingNametags", this.editingNametags);
      return var1;
   }

   @Override
   public boolean isSwitchedTo() {
      return this.switchedTo;
   }

   @Override
   public void setSwitchedTo(boolean var1) {
      this.switchedTo = var1;
      this.sync();
   }

   public void setFormat(VisualType var1, String var2) {
      this.formats.put(var1, Text.color(var2));
      this.sync();
   }

   public String getFormat(VisualType var1) {
      String var2 = this.formats.get(var1);
      return var2.equalsIgnoreCase("none") ? null : var2;
   }

   public void resetFormat(VisualType var1) {
      this.setFormat(var1, var1.getDefaultFormat());
   }

   public boolean isEditingNametags() {
      return this.editingNametags;
   }

   public void setEditingNametags(boolean var1) {
      this.editingNametags = var1;
      this.sync();
   }
}
