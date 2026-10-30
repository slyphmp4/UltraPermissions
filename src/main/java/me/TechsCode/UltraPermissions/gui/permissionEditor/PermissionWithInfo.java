package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfo;

public class PermissionWithInfo {
   private final String permission;
   private final PermissionInfo info;

   public PermissionWithInfo(String var1, PermissionInfo var2) {
      super();
      this.permission = var1;
      this.info = var2;
   }

   public boolean hasInfo() {
      return this.info != null;
   }

   public String getName() {
      return this.permission;
   }

   public PermissionInfo getInfo() {
      return this.info;
   }

   public boolean isThisPermission(String var1) {
      return this.hasInfo() ? this.info.isThisPermission(var1) : this.permission.equalsIgnoreCase(var1);
   }

   @Override
   public boolean equals(Object var1) {
      return var1 instanceof PermissionWithInfo ? this.permission.equalsIgnoreCase(((PermissionWithInfo)var1).permission) : false;
   }

   @Override
   public int hashCode() {
      return this.permission.hashCode();
   }
}
