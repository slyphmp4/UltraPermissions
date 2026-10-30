package me.TechsCode.UltraPermissions.base.gui;

import java.util.Arrays;
import org.bukkit.event.inventory.ClickType;

public enum ActionType {
   RIGHT(ClickType.RIGHT),
   LEFT(ClickType.LEFT),
   MIDDLE(ClickType.MIDDLE),
   Q(ClickType.DROP),
   SHIFT_RIGHT(ClickType.SHIFT_RIGHT),
   SHIFT_LEFT(ClickType.SHIFT_LEFT);

   private final ClickType nativeType;

   ActionType(ClickType var3) {
      this.nativeType = var3;
   }

   public static ActionType fromClickType(ClickType var0) {
      return Arrays.stream(values()).filter(var1 -> var1.nativeType == var0).findFirst().orElse(null);
   }
}
