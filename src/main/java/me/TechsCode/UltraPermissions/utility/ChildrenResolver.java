package me.TechsCode.UltraPermissions.utility;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChildrenResolver {
   private static final HashMap<String, Set<String>> cache = new HashMap<>();
   private static final boolean isSpigot = new File("server.properties").exists() && new File("bukkit.yml").exists();

   public ChildrenResolver() {
      super();
   }

   public static Set<String> retrieveChildPermissions(String var0) {
      if (cache.containsKey(var0)) {
         return cache.get(var0);
      }

      HashSet var1 = !isSpigot ? new HashSet() : BukkitChildrenResolver.getInstance().retrieveChildPermissions(var0);
      var1.add(var0);
      cache.put(var0, var1);
      return var1;
   }
}
