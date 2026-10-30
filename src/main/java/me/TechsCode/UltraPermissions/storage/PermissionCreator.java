package me.TechsCode.UltraPermissions.storage;

import me.TechsCode.UltraPermissions.storage.objects.Holder;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public class PermissionCreator {
   private PermissionStorage storage;
   private String name;
   private Holder holder;
   private String world;
   private String server;
   private boolean positive = true;
   private long expiration;

   public PermissionCreator(PermissionStorage var1, String var2, Holder var3) {
      super();
      this.storage = var1;
      this.name = var2;
      this.holder = var3;
      if (var2.startsWith("-")) {
         this.name = var2.substring(1);
         this.positive = false;
      }

      this.name = this.name.replace(" ", ".");
   }

   public PermissionCreator setServer(String var1) {
      this.server = var1;
      return this;
   }

   public PermissionCreator setWorld(String var1) {
      this.world = var1;
      return this;
   }

   public PermissionCreator setPositive(boolean var1) {
      this.positive = var1;
      return this;
   }

   public PermissionCreator setExpiration(long var1) {
      this.expiration = var1;
      return this;
   }

   public Permission create() {
      return this.storage
         .create(new Permission(this.storage.getNextNumericId(), this.holder, this.world, this.server, this.name, this.positive, this.expiration));
   }
}
