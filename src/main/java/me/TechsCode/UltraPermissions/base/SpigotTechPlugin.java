package me.TechsCode.UltraPermissions.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import me.TechsCode.UltraPermissions.base.legacy.task.TaskManager;
import me.TechsCode.UltraPermissions.base.loader.reloader.PluginReloader;
import me.TechsCode.UltraPermissions.base.loader.reloader.SpigotPluginReloader;
import me.TechsCode.UltraPermissions.base.messaging.SpigotMessagingService;
import me.TechsCode.UltraPermissions.base.scheduler.Scheduler;
import me.TechsCode.UltraPermissions.base.scheduler.SpigotScheduler;
import me.TechsCode.UltraPermissions.base.source.Maven;
import me.TechsCode.UltraPermissions.base.storage.syncing.SpigotSyncingAgent;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.base.update.networkUpdate.SpigotUpdateAgent;
import me.TechsCode.UltraPermissions.dependencies.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SpigotTechPlugin extends TechPlugin<JavaPlugin> {
   private SpigotScheduler scheduler;
   private SpigotMessagingService messagingService;
   private SyncingAgent syncingAgent;
   private SpigotUpdateAgent updateAgent;
   private Metrics metrics;

   public SpigotTechPlugin(JavaPlugin var1) {
      super(var1);
   }

   @Override
   protected void onInitialization() {
      this.scheduler = new SpigotScheduler(this);
      this.messagingService = new SpigotMessagingService(this);
      this.syncingAgent = new SpigotSyncingAgent(this);
      this.updateAgent = new SpigotUpdateAgent(this);
   }

   @Override
   protected void onPlatformEnable() {
      Maven.initialize(this);
      Maven.addRepository("https://repo.everit.biz/artifactory/public-release");
      new TaskManager(this);
   }

   @Override
   protected void onPlatformDisable() {
      this.messagingService.onDisable();
   }

   @Override
   public String getName() {
      return this.getBootstrap().getName();
   }

   @Override
   public String getVersion() {
      return this.getBootstrap().getDescription().getVersion();
   }

   @Override
   public int getBuildNumber() {
      InputStream var1 = this.getBootstrap().getResource("plugin.yml");
      return var1 == null
         ? 0
         : new BufferedReader(new InputStreamReader(var1))
            .lines()
            .filter(var0 -> var0.startsWith("build: "))
            .map(var0 -> var0.replace("build: ", ""))
            .map(Integer::parseInt)
            .findFirst()
            .orElse(0);
   }

   @Override
   public File getPluginFolder() {
      return this.getBootstrap().getDataFolder();
   }

   @Override
   public File getServerFolder() {
      return new File(".");
   }

   @Override
   public Scheduler getScheduler() {
      return this.scheduler;
   }

   public SpigotMessagingService getMessagingService() {
      return this.messagingService;
   }

   @Override
   public SyncingAgent getSyncingAgent() {
      return this.syncingAgent;
   }

   @Override
   public PluginReloader<?> getPluginReloader() {
      return new SpigotPluginReloader(this);
   }

   @Override
   protected void sendConsole(String var1) {
      Bukkit.getConsoleSender().sendMessage(var1);
   }

   public SpigotUpdateAgent getUpdateAgent() {
      return this.updateAgent;
   }

   @Override
   public InputStream getResource(String var1) {
      return this.getBootstrap().getResource(var1);
   }

   @Override
   protected void disablePlugin() {
      this.getBootstrap().getPluginLoader().disablePlugin((Plugin)this.getBootstrap());
   }

   protected void enableMetrics(int var1) {
      this.metrics = new Metrics((Plugin)this.getBootstrap(), var1);
   }

   public Metrics getMetrics() {
      return this.metrics;
   }

   public String getOwnServerName() {
      return this.getMySQLManager().getRegistry().getServerName();
   }
}
