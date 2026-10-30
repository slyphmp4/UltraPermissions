package me.TechsCode.UltraPermissions;

import me.TechsCode.UltraPermissions.storage.GroupStorage;
import me.TechsCode.UltraPermissions.storage.PermissionStorage;
import me.TechsCode.UltraPermissions.storage.UserStorage;

public interface StorageController {
   void onDataModification();

   GroupStorage getGroupStorage();

   PermissionStorage getPermissionStorage();

   UserStorage getUserStorage();
}
