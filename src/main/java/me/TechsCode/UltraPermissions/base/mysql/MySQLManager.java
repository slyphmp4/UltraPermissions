package me.TechsCode.UltraPermissions.base.mysql;

import java.sql.Connection;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.debug.Debugger;
import me.TechsCode.UltraPermissions.base.mysql.credentialsTransfer.ProxyMySQLReceiver;
import me.TechsCode.UltraPermissions.base.mysql.credentialsTransfer.SpigotMySQLSender;
import me.TechsCode.UltraPermissions.base.registry.RegistrationChoice;

public class MySQLManager {
   private final TechPlugin<?> plugin;
   private final MySQLRegistry registry;
   private ConnectionFactory factory;

   public MySQLManager(TechPlugin<?> var1) {
      super();
      this.plugin = var1;
      this.registry = var1.getRegistry().register(MySQLRegistry.class, RegistrationChoice.LOCAL);
      this.registry.loadLocalProperties(var1.getPluginFolder());
      if (var1 instanceof BungeeTechPlugin) {
         Debugger.debug("MySQL", "Registering ProxyMySQLReceiver");
         new ProxyMySQLReceiver((BungeeTechPlugin)var1, this);
      } else if (var1 instanceof SpigotTechPlugin) {
         Debugger.debug("MySQL", "Registering SpigotMySQLSender");
         new SpigotMySQLSender((SpigotTechPlugin)var1, this);
      }

      this.setup();
   }

   public void setup() {
      this.close();
      if (this.registry.hasCredentials()) {
         this.factory = new ConnectionFactory(
            this.registry.getCredentials(), this.registry.hasSSL(), this.registry.getMinimumIdle(), this.registry.getMaximumPoolSize()
         );
         ConnectionTestResult var1 = this.factory.testConnection();
         if (!var1.isValid()) {
            this.factory = null;
            this.plugin.log("Could not contact MySQL Server:");
            this.plugin.log(var1.getError());
         }

         Debugger.debug("MySQL", "ConnectionFactory created");
      } else {
         this.factory = null;
      }
   }

   public MySQLRegistry getRegistry() {
      return this.registry;
   }

   public boolean isEnabled() {
      return this.factory != null;
   }

   public Connection getConnection() throws java.sql.SQLException {
      return this.factory != null ? this.factory.newConnection() : null;
   }

   public Boolean connectionAvailable() {
      if (this.factory == null) {
         return false;
      }

      int var1 = this.factory.getMaximumPoolSize();
      int var2 = this.factory.getActiveConnections();
      Debugger.debug("MySQL", "Active Connections: " + var2 + " | Max Connections: " + var1);
      return var2 < var1;
   }

   public void close() {
      if (this.factory != null) {
         this.factory.close();
         this.factory = null;
      }
   }
}
