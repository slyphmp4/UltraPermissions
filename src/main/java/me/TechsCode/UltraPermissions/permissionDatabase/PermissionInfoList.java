package me.TechsCode.UltraPermissions.permissionDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionInfoList extends ArrayList<PermissionInfo> {
   public PermissionInfoList(int var1) {
      super(var1);
   }

   public PermissionInfoList() {
      super();
   }

   public PermissionInfoList(Collection<? extends PermissionInfo> var1) {
      super(var1);
   }

   public Optional<PermissionInfo> find(String var1) {
      for (PermissionInfo var3 : this) {
         if (var3.isThisPermission(var1)) {
            return Optional.of(var3);
         }
      }

      return Optional.empty();
   }

   public PermissionInfoList plugins(List<String> var1) {
      return this.stream().filter(var1x -> var1.contains(var1x.getPlugin())).collect(Collectors.toCollection(PermissionInfoList::new));
   }
}
