package me.TechsCode.UltraPermissions.storage.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Holder;

public class GroupList extends ArrayList<Group> {
   public GroupList(int var1) {
      super(var1);
   }

   public GroupList() {
      super();
   }

   public GroupList(Collection<? extends Group> var1) {
      super(var1);
   }

   public GroupList servers(boolean var1, List<String> var2) {
      return this.stream().filter(var2x -> {
         Optional var3 = var2x.getServer();
         return var1 && !var3.isPresent() || var3.isPresent() && var2.contains(var3.get());
      }).collect(Collectors.toCollection(GroupList::new));
   }

   public GroupList worlds(boolean var1, List<String> var2) {
      return this.stream().filter(var2x -> {
         Optional var3 = var2x.getWorld();
         return var1 && !var3.isPresent() || var3.isPresent() && var2.contains(var3.get());
      }).collect(Collectors.toCollection(GroupList::new));
   }

   public GroupList servers(boolean var1, String... var2) {
      return this.servers(var1, Arrays.asList(var2));
   }

   public GroupList worlds(boolean var1, String... var2) {
      return this.worlds(var1, Arrays.asList(var2));
   }

   public Optional<Group> key(String var1) {
      return this.stream().filter(var1x -> var1x.getKey().equals(var1)).findFirst();
   }

   public GroupList worstToBest() {
      this.sort(Comparator.comparing(Group::getPriority, Comparator.reverseOrder()).thenComparing(Group::getKey));
      return this;
   }

   public GroupList bestToWorst() {
      this.sort(Comparator.comparing(Group::getPriority).thenComparing(Group::getKey));
      return this;
   }

   public GroupList defaults(boolean var1) {
      return this.stream().filter(var1x -> var1x.isDefault() == var1).collect(Collectors.toCollection(GroupList::new));
   }

   public Optional<Group> name(String var1) {
      return this.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst();
   }

   public List<Holder> holders() {
      return this.stream().map(Holder::fromGroup).collect(Collectors.toList());
   }

   public GroupList getWithRecursiveInherits() {
      GroupList var1 = new GroupList(this);

      for (Group var3 : this) {
         this.collectGroups(var1, var3);
      }

      return var1;
   }

   private void collectGroups(List<Group> var1, Group var2) {
      for (Group var4 : var2.getActiveInheritedGroups()) {
         if (!var1.contains(var4)) {
            var1.add(var4);
            this.collectGroups(var1, var4);
         }
      }
   }
}
