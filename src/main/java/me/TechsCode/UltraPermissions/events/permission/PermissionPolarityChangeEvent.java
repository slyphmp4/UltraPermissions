package me.TechsCode.UltraPermissions.events.permission;

import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionPolarityChangeEvent extends PermissionEvent {
   private final boolean oldPolarity;
   private final boolean newPolarity;

   public PermissionPolarityChangeEvent(Permission var1, boolean var2, boolean var3) {
      super(var1);
      this.oldPolarity = var2;
      this.newPolarity = var3;
   }

   public boolean getNewPolarity() {
      return this.newPolarity;
   }

   public boolean getOldPolarity() {
      return this.oldPolarity;
   }
}
