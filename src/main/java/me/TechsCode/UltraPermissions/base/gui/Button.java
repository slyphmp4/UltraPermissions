package me.TechsCode.UltraPermissions.base.gui;

import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.base.item.CustomItem;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Button {
   private final List<Action> actions = new ArrayList<>();
   private CustomItem item;

   public Button() {
      super();
   }

   public void setItem(CustomItem var1) {
      this.item = var1;
   }

   public CustomItem material(XMaterial var1) {
      this.item = new CustomItem(var1);
      return this.item;
   }

   public CustomItem material(XMaterial var1, int var2) {
      this.item = new CustomItem(var1).addModelId(var2);
      return this.item;
   }

   public CustomItem material(Material var1) {
      this.item = new CustomItem(var1);
      return this.item;
   }

   public CustomItem material(Material var1, int var2) {
      this.item = new CustomItem(var1).addModelId(var2);
      return this.item;
   }

   public CustomItem stack(ItemStack var1) {
      this.item = new CustomItem(var1, false);
      return this.item;
   }

   public CustomItem item() {
      return this.item;
   }

   public void action(Action var1) {
      this.actions.add(var1);
   }

   public void action(ActionType var1, Runnable var2) {
      this.actions.add(var2x -> {
         if (var2x == var1) {
            var2.run();
         }
      });
   }

   public List<Action> getActions() {
      return this.actions;
   }
}
