package me.TechsCode.UltraPermissions.storage;

import java.util.HashMap;
import java.util.UUID;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class UserStorage extends Storage<User> {
   public UserStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, "Users", User.class, var2, true);
   }

   public void onMount(User var1) {
   }

   public void onCreation(User var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onDestroy(User var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onChange(User var1, User var2) {
      StorageController var3 = (StorageController)this.plugin;
      var3.onDataModification();
   }

   @Override
   public void onDataSynchronization() {
      StorageController var1 = (StorageController)this.plugin;
      var1.onDataModification();
   }

   public UserList getUsers() {
      UserList var1 = new UserList();
      var1.addAll(this.get());
      return var1;
   }

   public User registerUser(UUID var1, String var2, boolean var3) {
      UltraPermissionsAPI var4 = (UltraPermissionsAPI)this.plugin;
      User var5 = this.create(new User(var1, var2, false, new HashMap<>(), null, null, null));
      if (var3) {
         var4.getGroups().defaults(true).forEach(var5::addGroup);
      }

      return var5;
   }
}
