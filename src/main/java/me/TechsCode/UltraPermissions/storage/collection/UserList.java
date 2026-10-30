package me.TechsCode.UltraPermissions.storage.collection;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserList extends ArrayList<User> {
   public UserList() {
      super();
   }

   public Optional<User> uuid(UUID var1) {
      return this.stream().filter(var1x -> var1x.getUuid().equals(var1)).findFirst();
   }

   public Optional<User> name(String var1) {
      return this.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst();
   }

   public UserList usersInGroup(Stored<Group> var1) {
      return this.stream().filter(var1x -> var1x.getGroups().contains(var1)).collect(Collectors.toCollection(UserList::new));
   }
}
