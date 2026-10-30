package me.TechsCode.UltraPermissions.base.mysql;

import me.TechsCode.UltraPermissions.base.registry.RegistryStorable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class MySQLRegistry extends RegistryStorable {
   private String hostname;
   private String serverName = "";
   private String port;
   private String database;
   private String username;
   private String password;
   private boolean ssl;
   private int minimumIdle;
   private int maximumPoolSize;

   public MySQLRegistry() {
      super("mysql");
      this.hostname = "";
      this.port = "";
      this.database = "";
      this.username = "";
      this.password = "";
      this.ssl = true;
      this.minimumIdle = 10;
      this.maximumPoolSize = 25;
   }

   /** Optional local provisioning, also supported on the proxy; never read from network messages. */
   public void loadLocalProperties(java.io.File folder) {
      java.nio.file.Path file = folder.toPath().resolve("mysql.properties");
      if (!java.nio.file.Files.exists(file)) return;
      try {
         if (java.nio.file.Files.size(file) > 32768) throw new java.io.IOException("Configuration too large");
         java.util.Properties properties = new java.util.Properties();
         try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(file, java.nio.charset.StandardCharsets.UTF_8)) { properties.load(reader); }
         if (!properties.getProperty("ssl", "true").matches("true|false")) throw new java.io.IOException("Invalid TLS setting");
         this.hostname = properties.getProperty("hostname", "");
         this.port = properties.getProperty("port", "3306");
         this.database = properties.getProperty("database", "");
         this.username = properties.getProperty("username", "");
         this.password = properties.getProperty("password", "");
         this.ssl = Boolean.parseBoolean(properties.getProperty("ssl", "true"));
         if (!this.hasCredentials()) throw new java.io.IOException("Missing local MySQL credentials");
      } catch (java.io.IOException invalid) {
         throw new IllegalStateException("Invalid local mysql.properties; MySQL startup refused", invalid);
      }
   }

   @Override
   public void setState(JsonObject var1) {
      if (!var1.has("serverName")) {
         var1.addProperty("serverName", "");
      }

      this.serverName = var1.get("serverName").getAsString();
      this.hostname = var1.get("hostname").getAsString();
      this.port = var1.get("port").getAsString();
      this.database = var1.get("database").getAsString();
      this.username = var1.get("username").getAsString();
      this.password = var1.get("password").getAsString();
      this.ssl = !var1.has("ssl") || var1.get("ssl").getAsBoolean();
      this.minimumIdle = var1.has("minimumIdle") ? var1.get("minimumIdle").getAsInt() : 10;
      this.maximumPoolSize = var1.has("maximumPoolSize") ? var1.get("maximumPoolSize").getAsInt() : 25;
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("serverName", this.serverName);
      var1.addProperty("hostname", this.hostname);
      var1.addProperty("port", this.port);
      var1.addProperty("database", this.database);
      var1.addProperty("username", this.username);
      var1.addProperty("password", this.password);
      var1.addProperty("ssl", this.ssl);
      var1.addProperty("minimumIdle", this.minimumIdle);
      var1.addProperty("maximumPoolSize", this.maximumPoolSize);
      return var1;
   }

   public void updateState(JsonObject var1) {
      this.setState(var1);
      this.sync();
   }

   public boolean hasCredentials() {
      return !this.hostname.isEmpty() && !this.port.isEmpty() && !this.database.isEmpty() && !this.username.isEmpty() && !this.password.isEmpty();
   }

   public void setCredentials(MySQLCredentials var1) {
      if (var1 == null) {
         this.clearCredentials();
      } else {
         this.hostname = var1.getHostname();
         this.port = var1.getPort();
         this.database = var1.getDatabase();
         this.username = var1.getUsername();
         this.password = var1.getPassword();
         this.sync();
      }
   }

   public void clearCredentials() {
      this.hostname = "";
      this.port = "";
      this.database = "";
      this.username = "";
      this.password = "";
      this.sync();
   }

   public MySQLCredentials getCredentials() {
      return this.hasCredentials() ? new MySQLCredentials(this.hostname, this.port, this.database, this.username, this.password) : null;
   }

   public boolean hasSSL() {
      return this.ssl;
   }

   public void setSSL(boolean var1) {
      this.ssl = var1;
      this.sync();
   }

   public int getMinimumIdle() {
      return this.minimumIdle;
   }

   public void setMinimumIdle(int var1) {
      this.minimumIdle = var1;
      this.sync();
   }

   public int getMaximumPoolSize() {
      return this.maximumPoolSize;
   }

   public void setMaximumPoolSize(int var1) {
      this.maximumPoolSize = var1;
      this.sync();
   }

   public String getServerName() {
      return this.serverName.isEmpty() ? null : this.serverName;
   }

   public void setServerName(String var1) {
      this.serverName = var1;
      this.sync();
   }
}
