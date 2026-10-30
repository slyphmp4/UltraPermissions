package me.TechsCode.UltraPermissions.base.item;

import me.TechsCode.UltraPermissions.base.item.annotations.XChange;
import me.TechsCode.UltraPermissions.base.item.annotations.XInfo;
import me.TechsCode.UltraPermissions.base.item.annotations.XMerge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class XModuleMetadata {
   private final boolean wasRemoved;
   private final XChange[] changes;
   private final XMerge[] merges;
   private final XInfo info;

   public XModuleMetadata(boolean var1, XChange[] var2, XMerge[] var3, XInfo var4) {
      super();
      this.wasRemoved = var1;
      this.changes = var2;
      this.merges = var3;
      this.info = var4;
   }

   public boolean wasRemoved() {
      return this.wasRemoved;
   }

   public XChange[] getChanges() {
      return this.changes;
   }

   public XMerge[] getMerges() {
      return this.merges;
   }

   public XInfo getInfo() {
      return this.info;
   }
}
