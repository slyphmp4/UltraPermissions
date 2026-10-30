package me.TechsCode.UltraPermissions.storage.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionList extends ArrayList<Permission> {
   public PermissionList(int var1) {
      super(var1);
   }

   public PermissionList() {
      super();
   }

   public PermissionList(Collection<? extends Permission> var1) {
      super(var1);
   }

   public PermissionList name(String var1) {
      return this.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList holder(List<Holder> var1) {
      return this.stream().filter(var1x -> var1.contains(var1x.getHolder())).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList holder(Holder... var1) {
      return this.holder(Arrays.asList(var1));
   }

   public PermissionList bungee() {
      return this.stream().filter(Permission::isBungeePermission).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList expired() {
      return this.stream().filter(Permission::isExpired).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList worlds(boolean var1, List<String> var2) {
      return this.stream().filter(var2x -> {
         Optional var3 = var2x.getWorld();
         return var1 && !var3.isPresent() || var3.isPresent() && var2.contains(var3.get());
      }).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList worlds(boolean var1, String... var2) {
      return this.worlds(var1, Arrays.asList(var2));
   }

   public PermissionList servers(boolean var1, List<String> var2) {
      return this.stream().filter(var2x -> {
         Optional var3 = var2x.getServer();
         return var1 && !var3.isPresent() || var3.isPresent() && var2.contains(var3.get());
      }).collect(Collectors.toCollection(PermissionList::new));
   }

   public PermissionList servers(boolean var1, String... var2) {
      return this.servers(var1, Arrays.asList(var2));
   }

   public PermissionList key(Integer var1) {
      return this.stream().filter(var1x -> var1x.getKey().equals(String.valueOf(var1))).collect(Collectors.toCollection(PermissionList::new));
   }
}
