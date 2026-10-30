package me.TechsCode.UltraPermissions.base;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Generated;
import me.TechsCode.UltraPermissions.base.addons.AddonManager;
import me.TechsCode.UltraPermissions.base.debug.DebugRegistery;
import me.TechsCode.UltraPermissions.base.expire.ExpireTime;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.loader.reloader.PluginReloader;
import me.TechsCode.UltraPermissions.base.mysql.MySQLManager;
import me.TechsCode.UltraPermissions.base.registry.RegistrationChoice;
import me.TechsCode.UltraPermissions.base.registry.Registry;
import me.TechsCode.UltraPermissions.base.scheduler.Scheduler;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.base.translations.TranslationManager;
import me.TechsCode.UltraPermissions.base.visual.Text;

public abstract class TechPlugin<BOOTSTRAP> {
   private final BOOTSTRAP bootstrap;
   private Registry registry;
   private MySQLManager mySQLManager;
   private AppearanceRegistry appearanceRegistry;
   private TranslationManager translationManager;
   private DebugRegistery debugRegistery;
   private List<Runnable> disableHooks;
   public static boolean isDebugging;
   private static TechPlugin instance;

   private boolean checkProxyVersion() {
      return this.getVersion().contains("1.16")
         || this.getVersion().contains("1.17")
         || this.getVersion().contains("1.18")
         || this.getVersion().contains("1.19");
   }

   public TechPlugin(BOOTSTRAP var1) {
      super();
      long var2 = System.currentTimeMillis();
      this.bootstrap = (BOOTSTRAP)var1;
      File var4 = new File(this.getPluginFolder().getAbsolutePath(), "debug.key");
      if (var4.exists()) {
         this.log("§e[Debugger] §rDebugging is §aenabled §r.");
         isDebugging = true;
      }

      instance = this;
      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_16_R1)) {
         Text.setEnableGradient(true);
      }

      if (this instanceof BungeeTechPlugin) {
         Text.setIsBungeeCord(true);
         if (this.checkProxyVersion()) {
            Text.setEnableGradient(true);
         } else {
            this.log("Your proxy version is not supported by " + this.getName() + ". Please use 1.16 or higher.");
         }
      }

      if (this instanceof SpigotTechPlugin) {
         Logger var5 = Logger.getLogger("NBTAPI");
         var5.setLevel(Level.SEVERE);

         try {
            me.TechsCode.UltraPermissions.dependencies.nbt.utils.MinecraftVersion.replaceLogger(var5);
            me.TechsCode.UltraPermissions.dependencies.nbt.utils.MinecraftVersion.disableUpdateCheck();
         } catch (NoClassDefFoundError var9) {
         }
      }

      ExpireTime var10 = this.getExpiry();
      if (!var10.isRelease() && var10.getTime() != -1L && System.currentTimeMillis() >= var10.getTime()) {
         this.log("§cThis plugin has expired, please ask for a new version!");
         this.disablePlugin();
      } else {
         this.log("Loading Plugin...");
         if (!var10.isRelease()) {
            this.log("§6This version is in " + var10.getType().getName().toLowerCase() + ". Use at your own risk!");
            this.log("§6" + var10.getType().getDescription());
         }

         if (!var10.isRelease() && TimeUnit.MILLISECONDS.toDays(var10.getTime() - System.currentTimeMillis()) < 8L) {
            this.log(
               "§cThis version will expire in " + Tools.getTimeString(var10.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toLowerCase() + "!"
            );
         }

         this.onInitialization();
         boolean var6 = this.onGlobalEnable();
         if (!var6) {
            this.disablePlugin();
         } else {
            this.onPlatformEnable();
            this.onEnable();
            long var7 = System.currentTimeMillis() - var2;
            this.log("Successfully loaded in " + var7 + " ms");
         }
      }
   }

   public void disable() {
      this.onDisable();
      this.onPlatformDisable();
      this.onGlobalDisable();
   }

   private boolean onGlobalEnable() {
      this.registry = new Registry(this);
      this.debugRegistery = this.getRegistry().register(DebugRegistery.class, RegistrationChoice.GLOBAL_IF_AVAILABLE);
      this.mySQLManager = new MySQLManager(this);
      this.appearanceRegistry = this.registry.register(new AppearanceRegistry(this), RegistrationChoice.GLOBAL_IF_AVAILABLE);
      this.translationManager = new TranslationManager(this);
      this.disableHooks = new ArrayList<>();
      return true;
   }

   private void onGlobalDisable() {
      this.disableHooks.forEach(Runnable::run);
   }

   protected abstract void onEnable();

   protected abstract void onDisable();

   protected abstract void disablePlugin();

   protected abstract void onInitialization();

   protected abstract void onPlatformEnable();

   protected abstract void onPlatformDisable();

   public BOOTSTRAP getBootstrap() {
      return this.bootstrap;
   }

   public abstract String getName();

   public abstract String getVersion();

   public abstract int getBuildNumber();

   public abstract File getPluginFolder();

   public abstract File getServerFolder();

   public abstract Scheduler getScheduler();

   public abstract SyncingAgent getSyncingAgent();

   public abstract InputStream getResource(String var1);

   public abstract PluginReloader<?> getPluginReloader();

   public abstract AddonManager getAddonManager();

   protected abstract void sendConsole(String var1);

   public Registry getRegistry() {
      return this.registry;
   }

   public ExpireTime getExpiry() {
      return ExpireTime.release();
   }

   public AppearanceRegistry getAppearanceRegistry() {
      return this.appearanceRegistry;
   }

   public String getPrefix() {
      return this.appearanceRegistry.isPrefixDisabled() ? "" : Text.chatColor(this.appearanceRegistry.getPrefix()) + " ";
   }

   public void log(String var1) {
      this.sendConsole("§b[§7" + this.getName() + "§b] §r" + var1);
   }

   public MySQLManager getMySQLManager() {
      return this.mySQLManager;
   }

   public DebugRegistery getDebugRegistery() {
      return this.debugRegistery;
   }

   public void addDisableHook(Runnable var1) {
      this.disableHooks.add(var1);
   }

   public boolean isAllowingInvalidMySQL() {
      return false;
   }

   public TranslationManager getTranslationManager() {
      return this.translationManager;
   }

   @Generated
   public static TechPlugin getInstance() {
      return instance;
   }
}
