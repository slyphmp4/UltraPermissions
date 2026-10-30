package me.TechsCode.UltraPermissions.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.storage.implementations.LocalFile;
import me.TechsCode.UltraPermissions.base.storage.implementations.MySQL;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;

class TransferStorageGroup {
   TransferStorage<User> userStorage;
   TransferStorage<Permission> permissionStorage;
   TransferStorage<Group> groupStorage;

   public TransferStorageGroup(UltraPermissions var1, boolean var2) {
      super();
      this.userStorage = new TransferStorage<>(var1, "Users", User.class, var2 ? MySQL.class : LocalFile.class, false);
      this.permissionStorage = new TransferStorage<>(var1, "Permissions", Permission.class, var2 ? MySQL.class : LocalFile.class, false);
      this.groupStorage = new TransferStorage<>(var1, "Groups", Group.class, var2 ? MySQL.class : LocalFile.class, false);
   }

   public void copyTo(TransferStorageGroup var1) {
      new ArrayList<>(var1.userStorage.get()).forEach(User::_justDestroy);
      new ArrayList<>(var1.permissionStorage.get()).forEach(Permission::_justDestroy);
      new ArrayList<>(var1.groupStorage.get()).forEach(Group::_justDestroy);
      this.groupStorage.get().forEach(var1x -> var1.groupStorage.create(var1x.createCopy()));
      this.permissionStorage.get().forEach(var1x -> var1.permissionStorage.create(var1x.createCopy()));
      this.userStorage.get().forEach(var1x -> var1.userStorage.create(var1x.createCopy()));
   }

   public long getLastWrite() {
      return Collections.max(Arrays.asList(this.userStorage.getLastWrite(), this.permissionStorage.getLastWrite(), this.groupStorage.getLastWrite()));
   }
}
