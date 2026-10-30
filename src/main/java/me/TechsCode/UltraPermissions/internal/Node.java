package me.TechsCode.UltraPermissions.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import me.TechsCode.UltraPermissions.utility.ChildrenResolver;

public class Node {
   private final String permission;

   public Node(String var1) {
      super();
      this.permission = var1;
   }

   public List<String> getWildcards() {
      ArrayList var1 = new ArrayList<>(Collections.singletonList("*"));
      StringBuilder var2 = new StringBuilder();

      for (String var6 : this.permission.split("\\.")) {
         if (!var2.toString().equals("")) {
            var2.append(".");
         }

         var2.append(var6);
         var1.add(var2 + ".*");
      }

      return var1;
   }

   public List<String> getSelfAndWildcards() {
      ArrayList var1 = new ArrayList();
      var1.add(this.permission);
      var1.addAll(this.getWildcards());
      return var1;
   }

   public Set<String> getSelfAndContainedPermissions() {
      return ChildrenResolver.retrieveChildPermissions(this.permission);
   }
}
