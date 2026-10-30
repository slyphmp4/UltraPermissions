package me.TechsCode.UltraPermissions.base;

import java.io.File;
import java.io.InputStream;
import me.TechsCode.UltraPermissions.base.addons.AddonManager;
import me.TechsCode.UltraPermissions.base.fileconf.FileConfiguration;
import me.TechsCode.UltraPermissions.base.loader.reloader.BungeePluginReloader;
import me.TechsCode.UltraPermissions.base.loader.reloader.PluginReloader;
import me.TechsCode.UltraPermissions.base.messaging.BungeeMessagingService;
import me.TechsCode.UltraPermissions.base.scheduler.BungeeScheduler;
import me.TechsCode.UltraPermissions.base.scheduler.Scheduler;
import me.TechsCode.UltraPermissions.base.storage.syncing.BungeeSyncingAgent;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.base.update.networkUpdate.BungeeUpdateAgent;
import me.TechsCode.UltraPermissions.dependencies.bstats.bungeecord.Metrics;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeTechPlugin extends TechPlugin<Plugin> {
   private BungeeScheduler scheduler;
   private BungeeMessagingService messagingService;
   private SyncingAgent syncingAgent;
   private Metrics metrics;

   public BungeeTechPlugin(Plugin var1) {
      super(var1);
   }

   @Override
   protected void onInitialization() {
      this.scheduler = new BungeeScheduler(this);
      this.messagingService = new BungeeMessagingService(this);
      this.syncingAgent = new BungeeSyncingAgent(this);
      new BungeeUpdateAgent(this);
   }

   @Override
   protected void onPlatformEnable() {
   }

   @Override
   protected void onPlatformDisable() {
   }

   @Override
   public String getName() {
      return this.getBootstrap().getDescription().getName();
   }

   @Override
   public String getVersion() {
      return this.getBootstrap().getDescription().getVersion();
   }

   @Override
   public int getBuildNumber() {
      File var1 = this.getBootstrap().getDescription().getFile();
      FileConfiguration var2 = new FileConfiguration(this, var1);
      return var2.getInt("build");
   }

   @Override
   public File getPluginFolder() {
      return this.getBootstrap().getDataFolder();
   }

   @Override
   public File getServerFolder() {
      return ProxyServer.getInstance().getPluginsFolder().getParentFile();
   }

   @Override
   public Scheduler getScheduler() {
      return this.scheduler;
   }

   public BungeeMessagingService getMessagingService() {
      return this.messagingService;
   }

   @Override
   public SyncingAgent getSyncingAgent() {
      return this.syncingAgent;
   }

   @Override
   public PluginReloader<?> getPluginReloader() {
      return new BungeePluginReloader(this);
   }

   @Override
   public AddonManager getAddonManager() {
      return null;
   }

   @Override
   protected void sendConsole(String var1) {
      this.getBootstrap().getLogger().info(var1);
   }

   @Override
   public InputStream getResource(String var1) {
      return this.getBootstrap().getResourceAsStream(var1);
   }

   @Override
   protected void disablePlugin() {
   }

   protected void enableMetrics(int var1) {
      this.metrics = new Metrics(this.getBootstrap(), var1);
   }

   public Metrics getMetrics() {
      return this.metrics;
   }
}
