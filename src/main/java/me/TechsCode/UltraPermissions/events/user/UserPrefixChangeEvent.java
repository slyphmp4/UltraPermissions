package me.TechsCode.UltraPermissions.events.user;

import javax.annotation.Nullable;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserPrefixChangeEvent extends UserEvent {
   private final String oldPrefix;
   private final String newPrefix;

   public UserPrefixChangeEvent(User var1, @Nullable String var2, @Nullable String var3) {
      super(var1);
      this.oldPrefix = var2;
      this.newPrefix = var3;
   }

   public String getNewPrefix() {
      return this.newPrefix;
   }

   public String getOldPrefix() {
      return this.oldPrefix;
   }
}
