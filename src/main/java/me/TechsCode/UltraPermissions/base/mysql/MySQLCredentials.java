package me.TechsCode.UltraPermissions.base.mysql;

public class MySQLCredentials {
   private String hostname;
   private String port;
   private String database;
   private String username;
   private String password;

   public MySQLCredentials(String var1, String var2, String var3, String var4, String var5) {
      super();
      this.hostname = var1;
      this.port = var2;
      this.database = var3;
      this.username = var4;
      this.password = var5;
   }

   public String getHostname() {
      return this.hostname;
   }

   public void setHostname(String var1) {
      this.hostname = var1;
   }

   public String getPort() {
      return this.port;
   }

   public void setPort(String var1) {
      this.port = var1;
   }

   public String getDatabase() {
      return this.database;
   }

   public void setDatabase(String var1) {
      this.database = var1;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String var1) {
      this.username = var1;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String var1) {
      this.password = var1;
   }
}
