package me.TechsCode.UltraPermissions.storage;

import java.util.Comparator;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionStorage extends Storage<Permission> {
   public PermissionStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, "Permissions", Permission.class, var2, true);
   }

   public void onMount(Permission var1) {
   }

   public void onCreation(Permission var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onDestroy(Permission var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onChange(Permission var1, Permission var2) {
      StorageController var3 = (StorageController)this.plugin;
      var3.onDataModification();
   }

   @Override
   public void onDataSynchronization() {
      StorageController var1 = (StorageController)this.plugin;
      var1.onDataModification();
   }

   public PermissionList getPermissions() {
      PermissionList var1 = new PermissionList();
      var1.addAll(this.get());
      var1.sort(Comparator.comparing(Permission::getId));
      return var1;
   }

   public PermissionCreator newPermission(String var1, Holder var2) {
      return new PermissionCreator(this, var1, var2);
   }
}
