package me.TechsCode.UltraPermissions.base.views.settings;

import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import org.bukkit.entity.Player;

public abstract class SettingsPane {
   protected Player p;
   private SettingsView view;

   public SettingsPane(Player var1, SettingsView var2) {
      super();
      this.p = var1;
      this.view = var2;
   }

   public abstract String getName();

   public abstract XMaterial getIcon();

   public abstract void construct(Model var1);

   public void reopen() {
      this.view.reopen();
   }

   public int[] getInnerContainerSlots() {
      return new int[]{20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35};
   }

   public int[] getBigInnerContainerSlots() {
      return new int[]{
         10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45
      };
   }
}
