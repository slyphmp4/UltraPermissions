package me.TechsCode.UltraPermissions.storage;

import java.util.UUID;
import java.util.function.Function;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;

public class GroupStorage extends Storage<Group> {
   public GroupStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, "Groups", Group.class, var2, true);
   }

   public void onMount(Group var1) {
   }

   public void onCreation(Group var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onDestroy(Group var1) {
      StorageController var2 = (StorageController)this.plugin;
      var2.onDataModification();
   }

   public void onChange(Group var1, Group var2) {
      StorageController var3 = (StorageController)this.plugin;
      var3.onDataModification();
   }

   @Override
   public void onDataSynchronization() {
      StorageController var1 = (StorageController)this.plugin;
      var1.onDataModification();
   }

   public GroupList getGroups() {
      GroupList var1 = new GroupList();
      var1.addAll(this.get());
      var1.bestToWorst();
      return var1;
   }

   Group createWithAvailableKey(Function<String, Group> factory) {
      return this.plugin.writeModelResult(() -> {
         // Existing files use short keys. Retain the format when a short key is free.
         for (int attempt = 0; attempt < 16; attempt++) {
            String key = UUID.randomUUID().toString().substring(0, 4);
            if (!this.instancesView.containsKey(key)) return this.create(factory.apply(key));
         }
         return this.create(factory.apply(UUID.randomUUID().toString()));
      });
   }

   public GroupCreator newGroup(String var1) {
      return new GroupCreator(this, var1);
   }
}
