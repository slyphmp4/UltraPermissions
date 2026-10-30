package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;

public class PermissionCopyList extends ArrayList<PermissionCopy> {
   public PermissionCopyList(int var1) {
      super(var1);
   }

   public PermissionCopyList() {
      super();
   }

   public PermissionCopyList(Collection<? extends PermissionCopy> var1) {
      super(var1);
   }

   public PermissionCopyList(PermissionList var1, PermissionList var2) {
      super();
      var1.forEach(var1x -> this.add(new PermissionCopy(var1x, false)));
      var2.forEach(var1x -> this.add(new PermissionCopy(var1x, true)));
   }

   public List<PermissionCopy> inheritedCopies() {
      return this.stream().filter(PermissionCopy::isInherited).collect(Collectors.toList());
   }

   public List<PermissionCopy> regularCopies() {
      return this.stream().filter(var0 -> !var0.isInherited()).collect(Collectors.toList());
   }
}
