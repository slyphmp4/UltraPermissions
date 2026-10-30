package me.TechsCode.UltraPermissions.base.addons.cloud;

public enum CloudFileType {
   JAR("jar"),
   YML("yml");

   private final String extension;

   CloudFileType(String var3) {
      this.extension = var3;
   }

   public String getExtension() {
      return this.extension;
   }
}
