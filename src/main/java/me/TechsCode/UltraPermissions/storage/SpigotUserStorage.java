package me.TechsCode.UltraPermissions.storage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.events.user.UserCreationEvent;
import me.TechsCode.UltraPermissions.events.user.UserGroupAddedEvent;
import me.TechsCode.UltraPermissions.events.user.UserGroupRemovedEvent;
import me.TechsCode.UltraPermissions.events.user.UserPrefixChangeEvent;
import me.TechsCode.UltraPermissions.events.user.UserSuffixChangeEvent;
import me.TechsCode.UltraPermissions.events.user.UserSuperAdminChangeEvent;
import me.TechsCode.UltraPermissions.storage.objects.User;

public class SpigotUserStorage extends UserStorage {
   public SpigotUserStorage(TechPlugin var1, Class<? extends StorageImplementation> var2) {
      super(var1, var2);
   }

   @Override
   public void onCreation(User var1) {
      super.onCreation(var1);
      UserCreationEvent var2 = new UserCreationEvent(var1);
      var2.call(this.plugin, var2);
   }

   @Override
   public void onChange(User var1, User var2) {
      super.onChange(var1, var2);
      if (var1.isSuperadmin() != var2.isSuperadmin()) {
         UserSuperAdminChangeEvent var3 = new UserSuperAdminChangeEvent(var2, var1.isSuperadmin(), var2.isSuperadmin());
         var3.call(this.plugin, var3);
      }

      if (!var1.getPrefix().equals(var2.getPrefix())) {
         UserPrefixChangeEvent var7 = new UserPrefixChangeEvent(var2, var1.getPrefix().orElse(null), var2.getPrefix().orElse(null));
         var7.call(this.plugin, var7);
      }

      if (!var1.getSuffix().equals(var2.getSuffix())) {
         UserSuffixChangeEvent var8 = new UserSuffixChangeEvent(var2, var1.getSuffix().orElse(null), var2.getSuffix().orElse(null));
         var8.call(this.plugin, var8);
      }

      Set<me.TechsCode.UltraPermissions.base.storage.Stored<me.TechsCode.UltraPermissions.storage.objects.Group>> var9 = var1.getGroups();
      Set<me.TechsCode.UltraPermissions.base.storage.Stored<me.TechsCode.UltraPermissions.storage.objects.Group>> var4 = var2.getGroups();
      List<me.TechsCode.UltraPermissions.base.storage.Stored<me.TechsCode.UltraPermissions.storage.objects.Group>> var5 = var4.stream().filter(var1x -> !var9.contains(var1x)).collect(Collectors.toList());
      List<me.TechsCode.UltraPermissions.base.storage.Stored<me.TechsCode.UltraPermissions.storage.objects.Group>> var6 = var9.stream().filter(var1x -> !var4.contains(var1x)).collect(Collectors.toList());
      if (var5.size() > 0) {
         var5.forEach(var2x -> {
            UserGroupAddedEvent var3x = new UserGroupAddedEvent(var2, var2x, var2.getGroupExpiry(var2x));
            var3x.call(this.plugin, var3x);
         });
      }

      if (var6.size() > 0) {
         var6.forEach(var2x -> {
            UserGroupRemovedEvent var3x = new UserGroupRemovedEvent(var2, var2x);
            var3x.call(this.plugin, var3x);
         });
      }
   }
}
