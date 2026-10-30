package me.TechsCode.UltraPermissions.migration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsData;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExMigration extends MigrationAssistant {
   private static final Method GET_DATA_METHOD;

   public PermissionsExMigration() {
      super();
   }

   @Override
   public String getPluginName() {
      return "Permissions Ex";
   }

   @Override
   protected void migrate(UltraPermissions var1, Runnable var2) {
      PermissionManager var3 = PermissionsEx.getPermissionManager();
      HashMap<String, Group> var4 = new HashMap<>();
      var3.getGroupList()
         .forEach(
            var3x -> {
               Group var4x = var1.newGroup(var3x.getName())
                  .setPrefix(var3x.getOwnPrefix())
                  .setSuffix(var3x.getOwnSuffix())
                  .setDefault(var3x.isDefault("world") || var3x.getName().equals("default"))
                  .setPriority(var3x.getRank() + 1)
                  .create();
               var1.log("Created Group " + var4x.getName());
               this.addPermissions(var1, var4x, var3x);
               var4.put(var3x.getName(), var4x);
            }
         );
      var4.forEach((var3x, var4x) -> {
         PermissionGroup var5 = var3.getGroup(var3x);

         for (PermissionGroup var7x : var5.getParents()) {
            if (var4.containsKey(var7x.getName())) {
               var4x.addInheritance((Group)var4.get(var7x.getName()));
               var1.log("Added Inherited Group " + var7x.getName() + " to " + var4x.getName());
            }
         }
      });

      for (PermissionUser var6 : var3.getUsers()) {
         UUID var7 = Bukkit.getOfflinePlayer(var6.getName()).getUniqueId();
         User var8 = var1.registerUser(var7, var6.getName(), false);

         for (PermissionGroup var12 : var6.getGroups()) {
            if (var4.containsKey(var12.getName())) {
               var8.addGroup((Group)var4.get(var12.getName()));
            }
         }

         var8.setPrefix(var6.getOwnPrefix());
         var8.setSuffix(var6.getOwnSuffix());
         var1.log("Added User " + var8.getName());
         this.addPermissions(var1, var8, var6);
      }

      var2.run();
   }

   private void addPermissions(UltraPermissions var1, PermissionHolder var2, PermissionEntity var3) {
      for (Entry<String, List<String>> var5 : getPermanentPermissions(var3).entrySet()) {
         String var6 = null;
         if (var5.getKey() != null && !((String)var5.getKey()).isEmpty()) {
            var6 = (String)var5.getKey();
         }

         for (String var8 : var5.getValue()) {
            if (!var8.isEmpty()) {
               var2.newPermission(var8).setWorld(var6).create();
               var1.log("Added Permission " + var8 + " to " + var2.getName());
            }
         }
      }
   }

   private static Map<String, List<String>> getPermanentPermissions(PermissionEntity var0) {
      try {
         PermissionsData var1 = (PermissionsData)GET_DATA_METHOD.invoke(var0);
         return var1.getPermissionsMap();
      } catch (IllegalAccessException | InvocationTargetException var2) {
         throw new RuntimeException(var2);
      }
   }

   static {
      try {
         GET_DATA_METHOD = PermissionEntity.class.getDeclaredMethod("getData");
         GET_DATA_METHOD.setAccessible(true);
      } catch (NoSuchMethodException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }
}
