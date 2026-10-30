package me.TechsCode.UltraPermissions.events.permission;

import me.TechsCode.UltraPermissions.events.UPEvent;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionEvent extends UPEvent {
   private final Permission permission;

   public PermissionEvent(Permission var1) {
      super();
      this.permission = var1;
   }

   public Permission getPermission() {
      return this.permission;
   }
}
