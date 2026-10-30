package me.TechsCode.UltraPermissions.events.user;

import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserGroupRemovedEvent extends UserEvent {
   private final User user;
   private final Stored<Group> group;

   public UserGroupRemovedEvent(User var1, Stored<Group> var2) {
      super(var1);
      this.user = var1;
      this.group = var2;
   }

   public Stored<Group> getGroup() {
      return this.group;
   }

   @Override
   public User getUser() {
      return this.user;
   }
}
