package me.TechsCode.UltraPermissions.events.user;

import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserSuperAdminChangeEvent extends UserEvent {
   private final boolean oldSuperadmin;
   private final boolean newSuperadmin;

   public UserSuperAdminChangeEvent(User var1, boolean var2, boolean var3) {
      super(var1);
      this.oldSuperadmin = var2;
      this.newSuperadmin = var3;
   }

   public boolean isNowSuperAdmin() {
      return this.newSuperadmin;
   }

   public boolean wasSuperAdmin() {
      return this.oldSuperadmin;
   }
}
