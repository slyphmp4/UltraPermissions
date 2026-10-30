package me.TechsCode.UltraPermissions.events.user;

import me.TechsCode.UltraPermissions.events.UPEvent;
import me.TechsCode.UltraPermissions.storage.objects.User;

public abstract class UserEvent extends UPEvent {
   public final User user;

   public UserEvent(User var1) {
      super();
      this.user = var1;
   }

   public User getUser() {
      return this.user;
   }
}
