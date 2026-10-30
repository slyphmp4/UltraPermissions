package me.TechsCode.UltraPermissions.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.events.group.GroupChangeEvent;
import me.TechsCode.UltraPermissions.events.group.GroupCreationEvent;
import me.TechsCode.UltraPermissions.events.group.GroupDeletionEvent;
import me.TechsCode.UltraPermissions.events.group.GroupPrefixChangeEvent;
import me.TechsCode.UltraPermissions.events.group.GroupSuffixChangeEvent;
import me.TechsCode.UltraPermissions.storage.objects.Group;

public class SpigotGroupStorage extends GroupStorage {
   public SpigotGroupStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, var2);
   }

   @Override
   public void onCreation(Group var1) {
      super.onCreation(var1);
      GroupCreationEvent var2 = new GroupCreationEvent(var1);
      var2.call(this.plugin, var2);
   }

   @Override
   public void onDestroy(Group var1) {
      super.onDestroy(var1);
      GroupDeletionEvent var2 = new GroupDeletionEvent(var1);
      var2.call(this.plugin, var2);
   }

   @Override
   public void onChange(Group var1, Group var2) {
      super.onChange(var1, var2);
      GroupChangeEvent var3 = new GroupChangeEvent(var1, var2);
      var3.call(this.plugin, var3);
      if (!var1.getPrefix().equals(var2.getPrefix())) {
         GroupPrefixChangeEvent var4 = new GroupPrefixChangeEvent(var2, var1.getPrefix().orElse(null), var2.getPrefix().orElse(null));
         var4.call(this.plugin, var4);
      }

      if (!var1.getSuffix().equals(var2.getSuffix())) {
         GroupSuffixChangeEvent var5 = new GroupSuffixChangeEvent(var2, var1.getSuffix().orElse(null), var2.getSuffix().orElse(null));
         var5.call(this.plugin, var5);
      }
   }
}
