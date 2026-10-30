package me.TechsCode.UltraPermissions.events.permission;

import java.util.Optional;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionWorldChangeEvent extends PermissionEvent {
   private final Optional<String> oldWorld;
   private final Optional<String> newWorld;

   public PermissionWorldChangeEvent(Permission var1, Optional<String> var2, Optional<String> var3) {
      super(var1);
      this.oldWorld = var2;
      this.newWorld = var3;
   }

   public Optional<String> getOldWorld() {
      return this.oldWorld;
   }

   public Optional<String> getNewWorld() {
      return this.newWorld;
   }
}
