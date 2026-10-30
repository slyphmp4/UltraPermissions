package me.TechsCode.UltraPermissions.base.debug;

import java.util.ArrayList;
import java.util.Arrays;
import lombok.Generated;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class DebugRegistery extends RegistryStorable {
   private ArrayList<String> debuggers;
   private final ArrayList<String> availableDebuggers = new ArrayList<>();

   public DebugRegistery() {
      super("debuggers");
      this.debuggers = new ArrayList<>();
   }

   @Override
   public void setState(JsonObject var1) {
      String var2 = var1.get("enabled").getAsString();
      String var3 = var2.substring(1, var2.length() - 1);
      String[] var4 = var3.split(", ");
      this.debuggers = new ArrayList<>(Arrays.asList(var4));
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("enabled", this.debuggers.toString());
      return var1;
   }

   public boolean isDebugging(String var1) {
      for (String var3 : this.debuggers) {
         if (var3.equalsIgnoreCase(var1)) {
            return true;
         }
      }

      return false;
   }

   public void addDebugger(String var1) {
      this.debuggers.add(var1);
      this.sync();
   }

   public void removeDebugger(String var1) {
      this.debuggers.remove(var1);
      this.sync();
   }

   public void addAvailableDebugger(String var1) {
      this.availableDebuggers.add(var1);
   }

   public void addAvailableDebugger(String... var1) {
      this.availableDebuggers.addAll(Arrays.asList(var1));
   }

   @Generated
   public ArrayList<String> getDebuggers() {
      return this.debuggers;
   }

   @Generated
   public ArrayList<String> getAvailableDebuggers() {
      return this.availableDebuggers;
   }
}
