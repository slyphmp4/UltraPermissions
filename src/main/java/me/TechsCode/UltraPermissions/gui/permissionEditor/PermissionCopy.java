package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.PermissionColor;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionCopy {
   private final Permission permission;
   private final boolean inherited;

   public PermissionCopy(Permission var1, boolean var2) {
      super();
      this.permission = var1;
      this.inherited = var2;
   }

   public Permission getPermission() {
      return this.permission;
   }

   public String getName() {
      return this.permission.getName();
   }

   public String getColoredName() {
      PermissionColor var1 = PermissionColor.fromPermission(this);
      return var1.getColor() + this.getName();
   }

   public PermissionColor getColor() {
      return PermissionColor.fromPermission(this);
   }

   public boolean isInherited() {
      return this.inherited;
   }
}
