package me.TechsCode.UltraPermissions.events.permission;

import java.util.Optional;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionServerChangeEvent extends PermissionEvent {
   private final Optional<String> oldServer;
   private final Optional<String> newServer;

   public PermissionServerChangeEvent(Permission var1, Optional<String> var2, Optional<String> var3) {
      super(var1);
      this.oldServer = var2;
      this.newServer = var3;
   }

   public Optional<String> getOldServer() {
      return this.oldServer;
   }

   public Optional<String> getNewServer() {
      return this.newServer;
   }
}
