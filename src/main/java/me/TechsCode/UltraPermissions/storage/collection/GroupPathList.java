package me.TechsCode.UltraPermissions.storage.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;

public class GroupPathList extends ArrayList<GroupPath> {
   public GroupPathList(int var1) {
      super(var1);
   }

   public GroupPathList() {
      super();
   }

   public GroupPathList(Collection<? extends GroupPath> var1) {
      super(var1);
   }

   public Optional<GroupPath> name(String var1) {
      return this.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst();
   }
}
