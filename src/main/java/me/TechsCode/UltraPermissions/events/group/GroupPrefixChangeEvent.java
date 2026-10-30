package me.TechsCode.UltraPermissions.events.group;

import me.TechsCode.UltraPermissions.storage.objects.Group;

public class GroupPrefixChangeEvent extends GroupEvent {
   private final String oldPrefix;
   private final String newPrefix;

   public GroupPrefixChangeEvent(Group var1, String var2, String var3) {
      super(var1);
      this.oldPrefix = var2;
      this.newPrefix = var3;
   }

   public String getOldPrefix() {
      return this.oldPrefix;
   }

   public String getNewPrefix() {
      return this.newPrefix;
   }
}
