package me.TechsCode.UltraPermissions.utility;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class BukkitChildrenResolver {
   private static BukkitChildrenResolver i;
   private Set<Permission> permissions = Bukkit.getPluginManager().getPermissions();

   public static BukkitChildrenResolver getInstance() {
      if (i == null) {
         i = new BukkitChildrenResolver();
      }

      return i;
   }

   public BukkitChildrenResolver() {
      super();
   }

   public HashSet<String> retrieveChildPermissions(String var1) {
      Permission var2 = Bukkit.getServer().getPluginManager().getPermission(var1);
      if (var2 == null) {
         return new HashSet<>();
      }

      HashSet var3 = new HashSet();
      var2.getChildren().forEach((var2x, var3x) -> {
         if (var3x != null && var3x) {
            var3.add(var2x);
            var3.addAll(this.retrieveChildPermissions(var2x));
         }
      });
      return var3;
   }
}
