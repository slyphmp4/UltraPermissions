package me.TechsCode.UltraPermissions.storage.objects;

import java.util.Optional;
import me.TechsCode.UltraPermissions.storage.PermissionCreator;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;

public interface PermissionHolder {
   String getName();

   PermissionList getPermissions();

   PermissionList getAdditionalPermissions();

   PermissionCreator newPermission(String var1);

   Optional<String> getPrefix();

   Optional<String> getSuffix();

   void setPrefix(String var1);

   void setSuffix(String var1);
}
