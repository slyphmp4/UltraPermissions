package me.TechsCode.UltraPermissions.storage.objects;

import java.util.Optional;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonElement;

public class Holder {
   private final Stored<User> userHolder;
   private final Stored<Group> groupHolder;

   public static Holder fromUser(User var0) {
      return new Holder(var0.toStored(), null);
   }

   public static Holder fromGroup(Group var0) {
      return new Holder(null, var0.toStored());
   }

   public static Holder fromKey(JsonElement var0, TechPlugin<?> var1) {
      StorageController var2 = (StorageController)var1;
      return var0.getAsString().length() > 10
         ? new Holder(new Stored<>(var0, var2::getUserStorage), null)
         : new Holder(null, new Stored<>(var0, var2::getGroupStorage));
   }

   private Holder(Stored<User> var1, Stored<Group> var2) {
      super();
      this.userHolder = var1;
      this.groupHolder = var2;
   }

   public boolean isUser() {
      return this.userHolder != null;
   }

   public boolean isGroup() {
      return this.groupHolder != null;
   }

   public Optional<PermissionHolder> get() {
      return this.userHolder != null ? Optional.ofNullable(this.userHolder.get().orElse(null)) : Optional.ofNullable(this.groupHolder.get().orElse(null));
   }

   public String getName() {
      return this.get().map(PermissionHolder::getName).orElse("INVALID");
   }

   @Override
   public boolean equals(Object var1) {
      return !(var1 instanceof Holder var2) ? false : this.toStored().equals(var2.toStored());
   }

   public Stored<?> toStored() {
      return this.userHolder != null ? this.userHolder : this.groupHolder;
   }
}
