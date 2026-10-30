package me.TechsCode.UltraPermissions.events.user;

import javax.annotation.Nullable;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserSuffixChangeEvent extends UserEvent {
   private final String oldSuffix;
   private final String newSuffix;

   public UserSuffixChangeEvent(User var1, @Nullable String var2, @Nullable String var3) {
      super(var1);
      this.oldSuffix = var2;
      this.newSuffix = var3;
   }

   public String getOldSuffix() {
      return this.oldSuffix;
   }

   public String getNewSuffix() {
      return this.newSuffix;
   }
}
