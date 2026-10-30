package me.TechsCode.UltraPermissions;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.AddonManager;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.networking.SpigotNetworkManager;
import me.TechsCode.UltraPermissions.base.registry.RegistrationChoice;
import me.TechsCode.UltraPermissions.base.registry.SwitchableBiRegistry;
import me.TechsCode.UltraPermissions.base.storage.implementations.LocalFile;
import me.TechsCode.UltraPermissions.base.storage.implementations.MySQL;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.commands.DemoteCommand;
import me.TechsCode.UltraPermissions.commands.MainCommand;
import me.TechsCode.UltraPermissions.commands.PromoteCommand;
import me.TechsCode.UltraPermissions.commands.UPCCommand;
import me.TechsCode.UltraPermissions.hooks.PluginHookManager;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.PrefixColorPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.PrefixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.PrefixesPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.RankPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.RankTimerDetailedPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.RankTimerPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.RanksPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SecondaryPrefixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SecondarySuffixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SuffixColorPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SuffixPlaceholder;
import me.TechsCode.UltraPermissions.hooks.placeholders.SuffixesPlaceholder;
import me.TechsCode.UltraPermissions.internal.ModifiedPermissible;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionDatabase;
import me.TechsCode.UltraPermissions.permissionlogger.LoggedPermission;
import me.TechsCode.UltraPermissions.permissionlogger.PermissionLogger;
import me.TechsCode.UltraPermissions.storage.GroupCreator;
import me.TechsCode.UltraPermissions.storage.GroupPathStorage;
import me.TechsCode.UltraPermissions.storage.GroupStorage;
import me.TechsCode.UltraPermissions.storage.PermissionCreator;
import me.TechsCode.UltraPermissions.storage.PermissionStorage;
import me.TechsCode.UltraPermissions.storage.SpigotGroupStorage;
import me.TechsCode.UltraPermissions.storage.SpigotPermissionStorage;
import me.TechsCode.UltraPermissions.storage.SpigotUserStorage;
import me.TechsCode.UltraPermissions.storage.UserStorage;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.GroupPathList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.visual.VisualManager;
import me.TechsCode.UltraPermissions.visual.VisualRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UltraPermissions extends SpigotTechPlugin implements UltraPermissionsAPI, StorageController {
   private static UltraPermissionsAPI api;
   public static final UpermsPlaceholder[] placeholders = new UpermsPlaceholder[]{
      new PrefixColorPlaceholder(),
      new PrefixesPlaceholder(),
      new PrefixPlaceholder(),
      new SecondaryPrefixPlaceholder(),
      new SecondarySuffixPlaceholder(),
      new SuffixesPlaceholder(),
      new SuffixPlaceholder(),
      new SuffixColorPlaceholder(),
      new RankPlaceholder(),
      new RanksPlaceholder(),
      new RankTimerPlaceholder(),
      new RankTimerDetailedPlaceholder()
   };
   private PublicRegistry publicRegistry;
   private PrivateRegistry privateRegistry;
   private ToggledPluginsRegistry toggledPluginsRegistry;
   private SwitchableBiRegistry<VisualRegistry> visualRegistry;
   private SpigotNetworkManager networkManager;
   private GroupStorage groupStorage;
   private PermissionStorage permissionStorage;
   private UserStorage userStorage;
   private GroupPathStorage groupPathStorage;
   private PermissionDatabase permissionDatabase;
   protected PermissionLogger permissionLogger;
   private PluginHookManager pluginHookManager;
   private PluginEvents pluginEvents;
   private AddonManager addonManager;

   public UltraPermissions(JavaPlugin var1) {
      super(var1);
   }

   @Override
   public void onEnable() {
      api = this;
      this.enableMetrics(17223);
      this.publicRegistry = this.getRegistry().register(PublicRegistry.class, RegistrationChoice.GLOBAL_IF_AVAILABLE);
      this.privateRegistry = this.getRegistry().register(PrivateRegistry.class, RegistrationChoice.LOCAL);
      this.toggledPluginsRegistry = this.getRegistry().register(ToggledPluginsRegistry.class, RegistrationChoice.GLOBAL_IF_AVAILABLE);
      this.visualRegistry = new SwitchableBiRegistry<>(this.getRegistry().register(VisualRegistry.class));
      this.networkManager = new SpigotNetworkManager(this);
      boolean var1 = this.getMySQLManager().isEnabled();
      this.groupStorage = new SpigotGroupStorage(this, var1 ? MySQL.class : LocalFile.class);
      this.permissionStorage = new SpigotPermissionStorage(this, var1 ? MySQL.class : LocalFile.class);
      this.userStorage = new SpigotUserStorage(this, var1 ? MySQL.class : LocalFile.class);
      this.groupPathStorage = new GroupPathStorage(this, var1 ? MySQL.class : LocalFile.class);
      this.permissionDatabase = new PermissionDatabase(this);
      this.permissionLogger = new PermissionLogger(this);
      this.pluginHookManager = new PluginHookManager(this);
      new VisualManager(this, this.visualRegistry.get());
      new HeadPreloader(this);
      new MainCommand(this);
      new UPCCommand(this);
      new PromoteCommand(this);
      new DemoteCommand(this);
      this.pluginEvents = new PluginEvents(this, this.permissionLogger);
      this.addonManager = null;
      this.getScheduler().runTaskTimer(() -> {
         this.getUsers().forEach(User::clean);
         this.getGroups().forEach(Group::clean);
      }, 20L, 6000L);
   }

   public void issueManualSynchronization() {
      this.getSyncingAgent().notifyNewDataChanges(this.groupStorage, "*");
      this.getSyncingAgent().notifyNewDataChanges(this.permissionStorage, "*");
      this.getSyncingAgent().notifyNewDataChanges(this.userStorage, "*");
   }

   @Override
   public void onDataModification() {
      if (this.pluginEvents != null) {
         ArrayList<ModifiedPermissible> var1 = new ArrayList<>(this.pluginEvents.getPermissibles());
         var1.forEach(ModifiedPermissible::clearCache);
      }

      if (this.pluginHookManager != null) {
         this.pluginHookManager.clearCaches();
      }
   }

   public SwitchableBiRegistry<VisualRegistry> getVisualRegistry() {
      return this.visualRegistry;
   }

   @Override
   protected void onDisable() {
   }

   @Override
   public AddonManager getAddonManager() {
      return this.addonManager;
   }

   public static UltraPermissionsAPI getAPI() {
      return api;
   }

   public DefaultGroupAssignOption getDefaultGroupAssignOption() {
      return this.publicRegistry.getDefaultAssignOption();
   }

   public void setDefaultGroupAssignOption(DefaultGroupAssignOption var1) {
      this.publicRegistry.setDefaultGroupAssignOption(var1);
   }

   public void setDefaultPermissions(boolean var1) {
      this.publicRegistry.setDefaultPermissions(var1);
   }

   public boolean isDefaultPermissionsEnabled() {
      return this.publicRegistry.isDefaultPermissions();
   }

   public boolean isNegativePermsOverrideEnabled() {
      return this.publicRegistry.isNegativePermsOverride();
   }

   public void setNegativePermsOverrides(boolean var1) {
      this.publicRegistry.setNegativePermsOverride(var1);
   }

   public ArrayList<String> getToggledPluginsRegistry() {
      return this.toggledPluginsRegistry.getToggledPlugins();
   }

   public void setToggledPluginsRegistry(ArrayList<String> var1) {
      this.toggledPluginsRegistry.setToggledPlugins(var1);
   }

   @Override
   public GroupCreator newGroup(String var1) {
      return this.groupStorage.newGroup(var1);
   }

   @Override
   public PermissionCreator newPermission(String var1, Holder var2) {
      return this.permissionStorage.newPermission(var1, var2);
   }

   @Override
   public User registerUser(UUID var1, String var2, boolean var3) {
      return this.userStorage.registerUser(var1, var2, var3);
   }

   @Override
   public GroupList getGroups() {
      return this.groupStorage.getGroups();
   }

   @Override
   public UserList getUsers() {
      return this.userStorage.getUsers();
   }

   @Override
   public PermissionList getPermissions() {
      return this.permissionStorage.getPermissions();
   }

   public Optional<NServer> getThisServer() {
      return this.networkManager.getThisServer();
   }

   public PermissionDatabase getPermissionDatabase() {
      return this.permissionDatabase;
   }

   public LoggedPermission[] getLoggedPermissionChecks() {
      return this.permissionLogger.getLoggedPermissions();
   }

   public String replacePlaceholders(Player var1, String var2) {
      return this.pluginHookManager.replacePlaceholders(var1, var2);
   }

   @Override
   public GroupStorage getGroupStorage() {
      return this.groupStorage;
   }

   @Override
   public PermissionStorage getPermissionStorage() {
      return this.permissionStorage;
   }

   @Override
   public UserStorage getUserStorage() {
      return this.userStorage;
   }

   public boolean isConnectedToNetwork() {
      return this.networkManager.getData().isPresent();
   }

   public SpigotNetworkManager getNetworkManager() {
      return this.networkManager;
   }

   public PluginEvents getPluginEvents() {
      return this.pluginEvents;
   }

   public PermissionLogger getPermissionLogger() {
      return this.permissionLogger;
   }

   @Override
   public PublicRegistry getPublicRegistry() {
      return this.publicRegistry;
   }

   @Override
   public Color getPluginMainColor() {
      return ColorPalette.MAIN;
   }

   @Override
   public GroupPathList getGroupPaths() {
      return this.groupPathStorage.getGroupPaths();
   }

   public GroupPathStorage getGroupPathStorage() {
      return this.groupPathStorage;
   }
}
