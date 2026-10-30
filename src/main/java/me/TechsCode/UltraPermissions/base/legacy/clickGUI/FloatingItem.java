package me.TechsCode.UltraPermissions.base.legacy.clickGUI;

import me.TechsCode.UltraPermissions.base.item.CustomItem;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public abstract class FloatingItem extends FloatingElement {
   public FloatingItem() {
      super();
   }

   public abstract CustomItem getItem();

   @Override
   public void apply(ArmorStand var1) {
      CustomItem var2 = this.getItem();
      this.setShown(var2 != null);
      if (var1 != null && var2 != null) {
         ItemStack var3 = var2.get();
         if (var1.getPassenger() == null || !((Item)var1.getPassenger()).getItemStack().isSimilar(var3)) {
            if (var1.getPassenger() != null) {
               var1.getPassenger().remove();
            }

            Item var4 = var1.getLocation().getWorld().dropItem(var1.getLocation(), var3);
            var4.setGravity(false);
            var4.setPickupDelay(2147483647);
            var1.setPassenger(var4);
         }
      }
   }

   @Override
   public double getYOffset() {
      return 2.0;
   }
}
