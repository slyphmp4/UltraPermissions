package me.TechsCode.UltraPermissions.storage;

import java.util.Optional;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.events.permission.PermissionCreationEvent;
import me.TechsCode.UltraPermissions.events.permission.PermissionDeletionEvent;
import me.TechsCode.UltraPermissions.events.permission.PermissionPolarityChangeEvent;
import me.TechsCode.UltraPermissions.events.permission.PermissionServerChangeEvent;
import me.TechsCode.UltraPermissions.events.permission.PermissionWorldChangeEvent;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class SpigotPermissionStorage extends PermissionStorage {
   public SpigotPermissionStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, var2);
   }

   @Override
   public void onCreation(Permission var1) {
      super.onCreation(var1);
      PermissionCreationEvent var2 = new PermissionCreationEvent(var1);
      var2.call(this.plugin, var2);
   }

   @Override
   public void onDestroy(Permission var1) {
      super.onDestroy(var1);
      PermissionDeletionEvent var2 = new PermissionDeletionEvent(var1);
      var2.call(this.plugin, var2);
   }

   @Override
   public void onChange(Permission var1, Permission var2) {
      super.onChange(var1, var2);
      Optional var3 = var1.getServer();
      Optional var4 = var2.getServer();
      if (!var3.equals(var4)) {
         PermissionServerChangeEvent var5 = new PermissionServerChangeEvent(var2, var3, var4);
         var5.call(this.plugin, var5);
      }

      Optional var8 = var1.getWorld();
      Optional var6 = var2.getWorld();
      if (!var8.equals(var6)) {
         PermissionWorldChangeEvent var7 = new PermissionWorldChangeEvent(var2, var8, var6);
         var7.call(this.plugin, var7);
      }

      if (!var3.equals(var4)) {
         PermissionServerChangeEvent var9 = new PermissionServerChangeEvent(var2, var1.getServer(), var2.getServer());
         var9.call(this.plugin, var9);
      }

      if (var1.isPositive() != var2.isPositive()) {
         PermissionPolarityChangeEvent var10 = new PermissionPolarityChangeEvent(var2, var1.isPositive(), var2.isPositive());
         var10.call(this.plugin, var10);
      }
   }
}
