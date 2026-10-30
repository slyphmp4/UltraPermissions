package me.TechsCode.UltraPermissions.base.item;

import java.util.Arrays;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class XModule<XForm extends XModule<XForm, BukkitForm>, BukkitForm> implements XBase<XForm, BukkitForm> {
   private final BukkitForm bukkitForm;
   private final String[] names;

   @ApiStatus.Internal
   protected XModule(BukkitForm var1, String[] var2) {
      super();
      this.bukkitForm = (BukkitForm)var1;
      this.names = var2;
   }

   @NotNull
   @Override
   public final String name() {
      return this.names[0];
   }

   @ApiStatus.Experimental
   protected void setEnumName(XRegistry<XForm, BukkitForm> var1, String var2) {
      if (this.names[0] != null) {
         throw new IllegalStateException("Enum name already set " + var2 + " -> " + Arrays.toString(this.names));
      }

      this.names[0] = var2;
      Object var3 = var1.getBukkit(this.names);
      if (this.bukkitForm != var3) {
         var1.std((XForm)this);
      }
   }

   @ApiStatus.Internal
   @Override
   public String[] getNames() {
      return this.names;
   }

   @Nullable
   @Override
   public final BukkitForm get() {
      return this.bukkitForm;
   }

   @Override
   public final String toString() {
      return (this.isSupported() ? "" : "!") + this.getClass().getSimpleName() + "(" + this.name() + ")";
   }

   @Override
   public final int hashCode() {
      return super.hashCode();
   }

   @Deprecated
   @Override
   public final boolean equals(Object var1) {
      return super.equals(var1);
   }
}
