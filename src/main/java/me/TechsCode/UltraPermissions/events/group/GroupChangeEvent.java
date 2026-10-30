package me.TechsCode.UltraPermissions.events.group;

import me.TechsCode.UltraPermissions.events.UPEvent;
import me.TechsCode.UltraPermissions.storage.objects.Group;

public class GroupChangeEvent extends UPEvent {
   private final Group oldGroup;
   private final Group newGroup;

   public GroupChangeEvent(Group var1, Group var2) {
      super();
      this.oldGroup = var1;
      this.newGroup = var2;
   }

   public Group getNewGroup() {
      return this.newGroup;
   }

   public Group getOldGroup() {
      return this.oldGroup;
   }
}
