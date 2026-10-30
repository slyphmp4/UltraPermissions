package me.TechsCode.UltraPermissions.base.source;

public class Dependency {
   private String groupId;
   private String artifactId;
   private String version;

   public Dependency(String var1, String var2, String var3) {
      super();
      this.groupId = var1;
      this.artifactId = var2;
      this.version = var3;
   }

   public String getGroupId() {
      return this.groupId;
   }

   public String getArtifactId() {
      return this.artifactId;
   }

   public String getVersion() {
      return this.version;
   }

   public String toURL() {
      return this.groupId.replace(".", "/") + "/" + this.artifactId + "/" + this.version + "/" + this.artifactId + "-" + this.version + ".jar";
   }

   @Override
   public boolean equals(Object var1) {
      return !(var1 instanceof Dependency var2) ? false : var2.toURL().equals(this.toURL());
   }

   @Override
   public int hashCode() {
      return this.toURL().hashCode();
   }
}
