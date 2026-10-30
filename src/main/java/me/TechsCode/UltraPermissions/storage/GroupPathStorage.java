package me.TechsCode.UltraPermissions.storage;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.SimpleStorage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.storage.collection.GroupPathList;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;

public class GroupPathStorage extends SimpleStorage<GroupPath> {
   public GroupPathStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, "GroupPaths", GroupPath.class, var2, true);
   }

   public GroupPathList getGroupPaths() {
      return new GroupPathList(this.get());
   }
}
