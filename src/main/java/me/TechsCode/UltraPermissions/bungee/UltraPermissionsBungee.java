package me.TechsCode.UltraPermissions.bungee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.ColorPalette;
import me.TechsCode.UltraPermissions.DefaultGroupAssignOption;
import me.TechsCode.UltraPermissions.PublicRegistry;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.networking.BungeeNetworkManager;
import me.TechsCode.UltraPermissions.base.registry.RegistrationChoice;
import me.TechsCode.UltraPermissions.base.storage.implementations.MySQL;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import me.TechsCode.UltraPermissions.internal.lookup.PermissionLookup;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefinedPermissionCheck;
import me.TechsCode.UltraPermissions.storage.GroupCreator;
import me.TechsCode.UltraPermissions.storage.GroupPathStorage;
import me.TechsCode.UltraPermissions.storage.GroupStorage;
import me.TechsCode.UltraPermissions.storage.PermissionCreator;
import me.TechsCode.UltraPermissions.storage.PermissionStorage;
import me.TechsCode.UltraPermissions.storage.UserStorage;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.GroupPathList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class UltraPermissionsBungee extends BungeeTechPlugin implements Listener, UltraPermissionsAPI, StorageController {
   private static UltraPermissionsAPI api;
   private boolean loaded;
   private HashMap<ProxiedPlayer, PermissionList> permissionsCache;
   private GroupStorage groupStorage;
   private PermissionStorage permissionStorage;
   private UserStorage userStorage;
   private GroupPathStorage groupPathStorage;
   private PublicRegistry publicRegistry;

   public UltraPermissionsBungee(Plugin var1) {
      super(var1);
   }

   @Override
   protected void onEnable() {
      api = this;
      this.loaded = false;
      new BungeeNetworkManager(this);
      if (this.getMySQLManager().isEnabled()) {
         this.onLoad();
      } else {
         this.log("Waiting for MySQL Credentials...");
      }

      this.getScheduler().runTaskTimer(() -> {
         if (!this.loaded && this.getMySQLManager().isEnabled()) {
            this.onLoad();
         }
      }, 20L, 20L);
   }

   private void onLoad() {
      this.loaded = true;
      this.permissionsCache = new HashMap<>();
      this.groupStorage = new GroupStorage(this, MySQL.class);
      this.permissionStorage = new PermissionStorage(this, MySQL.class);
      this.userStorage = new UserStorage(this, MySQL.class);
      this.groupPathStorage = new GroupPathStorage(this, MySQL.class);
      this.publicRegistry = this.getRegistry().register(PublicRegistry.class, RegistrationChoice.GLOBAL_IF_AVAILABLE);
      ProxyServer.getInstance().getPluginManager().registerListener(this.getBootstrap(), this);
   }

   @Override
   protected void onDisable() {
   }

   @EventHandler
   public void check(PermissionCheckEvent var1) {
      if (var1.getSender() instanceof ProxiedPlayer) {
         final ProxiedPlayer var2 = (ProxiedPlayer)var1.getSender();
         PermissionLookup var3 = new PermissionLookup(var1.getPermission()) {
            @Override
            public LookupCheck[] getChecks() {
               return new LookupCheck[]{new DefinedPermissionCheck() {
                  @Override
                  public List<Permission> getDefinedPermissions() {
                     PermissionList var1x = UltraPermissionsBungee.this.getPermissions(var2);
                     return var1x == null ? new ArrayList<>() : var1x;
                  }
               }};
            }
         };
         LookupOutcome var4 = var3.perform();
         var1.setHasPermission(var4.getResult());
      }
   }

   public PermissionList getPermissions(ProxiedPlayer var1) {
      if (this.permissionsCache.containsKey(var1)) {
         return this.permissionsCache.get(var1);
      } else {
         Optional var2 = this.userStorage.getUsers().uuid(var1.getUniqueId());
         if (!var2.isPresent()) {
            this.log("Cannot find the user (" + var1.getUniqueId() + "). Please check your database connection.");
            return null;
         } else {
            PermissionList var3 = ((User)var2.get()).getPermissions().bungee();
            var3.addAll(((User)var2.get()).getAdditionalPermissions().bungee());
            this.permissionsCache.put(var1, var3);
            return var3;
         }
      }
   }

   @EventHandler
   public void login(LoginEvent var1) {
      this.login(var1.getConnection().getUniqueId(), var1.getConnection().getName());
   }

   public void login(@NotNull UUID var1, @NotNull String var2) {
      Optional var3 = this.getUsers().uuid(var1);
      if (var3.isPresent()) {
         if (!((User)var3.get()).getName().equals(var2)) {
            ((User)var3.get()).setPlayerName(var2);
         }
      } else {
         this.registerUser(var1, var2, true);
      }
   }

   @Override
   public User registerUser(UUID var1, String var2, boolean var3) {
      Optional var4 = this.userStorage.getUsers().uuid(var1);
      if (var4.isPresent()) {
         return (User)var4.get();
      }

      Optional<User> var5 = this.userStorage.getUsers().stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var2)).findFirst();
      return var5.orElseGet(() -> this.userStorage.registerUser(var1, var2, this.getDefaultGroupAssignOption() == DefaultGroupAssignOption.FIRST_JOIN));
   }

   private DefaultGroupAssignOption getDefaultGroupAssignOption() {
      return this.publicRegistry.getDefaultAssignOption();
   }

   @Override
   public boolean isAllowingInvalidMySQL() {
      return true;
   }

   @Override
   public PermissionCreator newPermission(String var1, Holder var2) {
      return this.permissionStorage.newPermission(var1, var2);
   }

   @Override
   public GroupCreator newGroup(String var1) {
      return this.groupStorage.newGroup(var1);
   }

   @Override
   public PermissionList getPermissions() {
      return this.permissionStorage.getPermissions();
   }

   @Override
   public GroupList getGroups() {
      return this.groupStorage.getGroups();
   }

   @Override
   public GroupPathList getGroupPaths() {
      return this.groupPathStorage.getGroupPaths();
   }

   @Override
   public UserList getUsers() {
      return this.userStorage.getUsers();
   }

   @Override
   public Color getPluginMainColor() {
      return ColorPalette.MAIN;
   }

   @Override
   public PublicRegistry getPublicRegistry() {
      return null;
   }

   public static UltraPermissionsAPI getAPI() {
      return api;
   }

   @Override
   public void onDataModification() {
      this.permissionsCache.clear();
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
}
