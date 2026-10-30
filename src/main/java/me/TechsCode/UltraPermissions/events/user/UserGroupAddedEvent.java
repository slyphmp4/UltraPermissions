package me.TechsCode.UltraPermissions.events.user;

import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserGroupAddedEvent extends UserEvent {
   private final User user;
   private final Stored<Group> group;
   private final long expiry;

   public UserGroupAddedEvent(User var1, Stored<Group> var2, long var3) {
      super(var1);
      this.user = var1;
      this.group = var2;
      this.expiry = var3;
   }

   public Stored<Group> getGroup() {
      return this.group;
   }

   public long getExpiry() {
      return this.expiry;
   }

   @Override
   public User getUser() {
      return this.user;
   }
}
