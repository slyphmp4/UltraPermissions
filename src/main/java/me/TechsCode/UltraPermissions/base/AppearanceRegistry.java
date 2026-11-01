package me.TechsCode.UltraPermissions.base;

import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class AppearanceRegistry extends RegistryStorable {
   private String prefix;
   private boolean updaterEnabled;
   private boolean updaterNotificationsEnabled;
   private final String defaultPrefix;
   private final int defaultGuiUpdateInterval;
   private int guiUpdateInterval;

   public AppearanceRegistry(TechPlugin<?> var1) {
      super("appearance");
      this.prefix = Colors.BLUE + String.join(" ", Tools.splitCamelCase(var1.getName())) + ">";
      this.updaterEnabled = true;
      this.updaterNotificationsEnabled = true;
      this.defaultPrefix = this.prefix;
      this.guiUpdateInterval = 2;
      this.defaultGuiUpdateInterval = this.guiUpdateInterval;
   }

   @Override
   public void setState(JsonObject var1) {
      this.prefix = var1.get("prefix").getAsString();
      if (var1.get("updaterEnabled") == null || !var1.has("updaterEnabled")) {
         var1.addProperty("updaterEnabled", this.updaterEnabled);
      }

      this.updaterEnabled = var1.get("updaterEnabled").getAsBoolean();
      // Legacy updaterEnabled was the master switch: preserve disabled installations.
      if (!var1.has("updaterNotificationsEnabled") || var1.get("updaterNotificationsEnabled") == null) {
         var1.addProperty("updaterNotificationsEnabled", this.updaterEnabled);
      }
      this.updaterNotificationsEnabled = var1.get("updaterNotificationsEnabled").getAsBoolean();
      if (var1.get("guiUpdateInterval") == null || !var1.has("guiUpdateInterval")) {
         var1.addProperty("guiUpdateInterval", this.guiUpdateInterval);
      }

      this.guiUpdateInterval = var1.get("guiUpdateInterval").getAsInt();
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("prefix", this.prefix.trim());
      var1.addProperty("updaterEnabled", this.updaterEnabled);
      var1.addProperty("updaterNotificationsEnabled", this.updaterNotificationsEnabled);
      var1.addProperty("guiUpdateInterval", this.guiUpdateInterval);
      return var1;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String var1) {
      this.prefix = var1;
      this.sync();
   }

   public boolean isPrefixModified() {
      return this.prefix.length() != 0 && !this.prefix.equals(this.defaultPrefix);
   }

   public boolean isPrefixDisabled() {
      return this.prefix.length() == 0;
   }

   public void resetPrefix() {
      this.prefix = this.defaultPrefix;
      this.sync();
   }

   public void disablePrefix() {
      this.prefix = "";
      this.sync();
   }

   public void enablePrefix() {
      this.resetPrefix();
   }

   public void toggleUpdater() {
      this.updaterEnabled = !this.updaterEnabled;
      this.sync();
   }

   /** The existing updaterEnabled setting now controls automatic GitHub downloads only. */
   public boolean isUpdaterEnabled() {
      return this.updaterEnabled;
   }

   public void toggleUpdaterNotifications() {
      this.updaterNotificationsEnabled = !this.updaterNotificationsEnabled;
      this.sync();
   }

   public boolean isUpdaterNotificationsEnabled() {
      return this.updaterNotificationsEnabled;
   }

   public boolean isGuiUpdateIntervalModified() {
      return this.guiUpdateInterval != this.defaultGuiUpdateInterval;
   }

   public int getGuiUpdateInterval() {
      return this.guiUpdateInterval;
   }

   public void setGuiUpdateInterval(int var1) {
      this.guiUpdateInterval = var1;
      this.sync();
   }

   public void resetGuiUpdateInterval() {
      this.guiUpdateInterval = this.defaultGuiUpdateInterval;
      this.sync();
   }
}
