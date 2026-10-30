package me.TechsCode.UltraPermissions.base.legacy.clickGUI;

import org.bukkit.entity.ArmorStand;

public abstract class FloatingElement {
   private boolean shown = true;
   private boolean hovering;

   public FloatingElement() {
      super();
   }

   public void hovering(boolean var1) {
      if (this.hovering != var1) {
         this.hovering = var1;
         this.onHoverChange(var1);
      }
   }

   public boolean isHovering() {
      return this.hovering;
   }

   public boolean isShown() {
      return this.shown;
   }

   public void setShown(boolean var1) {
      if (this.shown != var1) {
         this.shown = var1;
      }
   }

   public abstract void apply(ArmorStand var1);

   public abstract double getYOffset();

   public abstract double getX();

   public abstract double getY();

   public abstract double getRadius();

   public abstract void click();

   protected abstract void onHoverChange(boolean var1);
}
