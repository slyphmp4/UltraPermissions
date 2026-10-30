package me.TechsCode.UltraPermissions.base.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import me.TechsCode.UltraPermissions.base.debug.Debugger;
import me.TechsCode.UltraPermissions.dependencies.hikari.HikariConfig;
import me.TechsCode.UltraPermissions.dependencies.hikari.HikariDataSource;

public class ConnectionFactory {
   private HikariDataSource dataSource;
   private String error;

   public ConnectionFactory(MySQLCredentials var1, boolean var2, int var3, int var4) {
      super();
      Debugger.debug("MySQL", "Connecting to MySQL database...");
      try {
         String url = me.TechsCode.UltraPermissions.base.security.JdbcPolicy.url(var1, var2);
         if (var2) me.TechsCode.UltraPermissions.base.security.JdbcPolicy.requireIdentityVerification(url);
         HikariConfig config = new HikariConfig();
         config.setJdbcUrl(url);
         config.setUsername(var1.getUsername());
         config.setPassword(var1.getPassword());
         config.setMinimumIdle(Math.max(0, Math.min(var3, Math.max(1, Math.min(var4, 64)))));
         config.setMaximumPoolSize(Math.max(1, Math.min(var4, 64)));
         config.setMaxLifetime(1800000L);
         config.setConnectionTimeout(5000L);
         config.setValidationTimeout(3000L);
         config.setLeakDetectionThreshold(48000L);
         config.addDataSourceProperty("cachePrepStmts", "true");
         config.addDataSourceProperty("prepStmtCacheSize", "250");
         config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         this.dataSource = new HikariDataSource(config);
      } catch (Exception failure) {
         this.error = "MySQL initialization failed: " + failure.getClass().getSimpleName()
            + ". Remote databases require ssl=true, a trusted certificate and a driver supporting VERIFY_IDENTITY.";
         Debugger.debug("MySQL", this.error);
      }

      Debugger.debug("MySQL", "Connected to MySQL database.");
   }

   public Connection newConnection() throws SQLException {
      return this.dataSource != null ? this.dataSource.getConnection() : null;
   }

   public ConnectionTestResult testConnection() {
      if (this.error != null) {
         return new ConnectionTestResult(false, this.error);
      }

      long started = System.currentTimeMillis();
      try (Connection connection = this.newConnection()) {
         if (connection == null) return new ConnectionTestResult(false, "No MySQL connection");
         if (System.currentTimeMillis() - started > 5000L) return new ConnectionTestResult(false, "MySQL connection timed out");
         return new ConnectionTestResult(true, "Connection is valid");
      } catch (SQLException failure) {
         return new ConnectionTestResult(false, "MySQL connection failed");
      }
   }

   public void close() {
      if (this.dataSource != null) {
         this.dataSource.close();
      }
   }

   public int getMaximumPoolSize() {
      return this.dataSource.getMaximumPoolSize();
   }

   public int getActiveConnections() {
      return this.dataSource.getHikariPoolMXBean().getActiveConnections();
   }
}
