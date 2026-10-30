package me.TechsCode.UltraPermissions.base.legacy.clickGUI;

import org.bukkit.entity.ArmorStand;

public abstract class FloatingText extends FloatingElement {
   public FloatingText() {
      super();
   }

   public abstract String getText();

   @Override
   public void apply(ArmorStand var1) {
      String var2 = this.getText();
      this.setShown(var2 != null);
      if (var1 != null && var2 != null) {
         var1.setCustomName(var2);
         var1.setCustomNameVisible(true);
      }
   }

   @Override
   public double getYOffset() {
      return 2.5;
   }
}
