package me.TechsCode.UltraPermissions.base.addons.cloud;

import me.TechsCode.UltraPermissions.dependencies.gson.annotations.Expose;

public class CloudAddon {
   @Expose
   private String name;
   @Expose
   private String author;
   @Expose
   private String description;
   @Expose
   private String version;
   @Expose
   private String url;
   @Expose
   private String material;
   @Expose
   private int id;
   @Expose
   private int downloads;
   @Expose
   private boolean verified;

   public CloudAddon() {
      super();
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String var1) {
      this.author = var1;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String var1) {
      this.description = var1;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String var1) {
      this.version = var1;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String var1) {
      this.url = var1;
   }

   public String getMaterial() {
      return this.material;
   }

   public void setMaterial(String var1) {
      this.material = var1;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int var1) {
      this.id = var1;
   }

   public int getDownloads() {
      return this.downloads;
   }

   public void setDownloads(int var1) {
      this.downloads = var1;
   }

   public boolean isVerified() {
      return this.verified;
   }

   public void setVerified(boolean var1) {
      this.verified = var1;
   }
}
