package me.TechsCode.UltraPermissions.base.legacy.clickGUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.legacy.utils.Armorstands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public abstract class ClickGUI implements Listener {
   protected Player p;
   private SpigotTechPlugin plugin;
   private HashMap<FloatingElement, ArmorStand> items;
   private boolean opened;
   private double offsetDegree;
   private FloatingElement currentlyHovering;

   public ClickGUI(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      this.items = new HashMap<>();
      this.opened = false;
   }

   @EventHandler
   public void draw(UpdateEvent var1) {
      if (var1.getUpdateTime() == UpdateTime.TWOTICKS) {
         this.redraw();
      }
   }

   protected void redraw() {
      if (this.isFixed()) {
         this.offsetDegree = this.p.getLocation().getYaw();
      }

      double var1 = 0.0;
      FloatingElement var3 = null;

      for (Entry var5 : this.items.entrySet()) {
         FloatingElement var6 = (FloatingElement)var5.getKey();
         Location var7 = this.p.getEyeLocation();
         Vector var8 = this.p.getEyeLocation().getDirection().multiply(var6.getRadius());
         var7.add(var8);
         double var9 = this.offsetDegree + 90.0 + var6.getX() * 10.0;
         double var11 = Math.toRadians(var9);
         double var13 = Math.cos(var11);
         double var15 = var6.getY();
         double var17 = Math.sin(var11);
         Vector var19 = new Vector(var13, 0.0, var17);
         var19.multiply(var6.getRadius());
         Location var20 = this.p.getEyeLocation();
         var20.add(var19);
         var20.subtract(0.0, var15, 0.0);
         if (var6.isShown()) {
            double var21 = var20.distance(var7);
            if (var3 == null || var21 < var1) {
               var1 = var21;
               var3 = (FloatingElement)var5.getKey();
            }
         }

         var20.subtract(0.0, var6.getYOffset(), 0.0);
         ArmorStand var23 = (ArmorStand)var5.getValue();
         if (((FloatingElement)var5.getKey()).isShown()) {
            if (var23 == null) {
               var23 = (ArmorStand)var20.getWorld().spawn(var20, ArmorStand.class);
               var23.setGravity(false);
               var23.setVisible(false);
               var23.setBasePlate(false);
               var5.setValue(var23);
            } else {
               Armorstands.move(var23, var20);
            }
         } else if (var23 != null) {
            if (var23.getPassenger() != null) {
               var23.getPassenger().remove();
            }

            var23.remove();
            var5.setValue(null);
         }

         ((FloatingElement)var5.getKey()).apply(var23);
      }

      if (var1 < 0.5) {
         this.currentlyHovering = var3;
         this.currentlyHovering.hovering(true);
      } else {
         this.currentlyHovering = null;
      }

      this.items
         .keySet()
         .stream()
         .filter(var1x -> this.currentlyHovering == null || !var1x.equals(this.currentlyHovering))
         .forEach(var0 -> var0.hovering(false));
   }

   protected void addItem(FloatingElement var1) {
      this.items.put(var1, null);
   }

   protected void removeItem(FloatingElement var1) {
      ArmorStand var2 = this.items.get(var1);
      if (var2 != null) {
         if (var2.getPassenger() != null) {
            var2.getPassenger().remove();
         }

         var2.remove();
      }

      this.items.remove(var1);
   }

   protected FloatingElement[] getItems() {
      return this.items.keySet().toArray(new FloatingElement[this.items.size()]);
   }

   protected void clear() {
      Arrays.stream(this.getItems()).forEach(var1 -> this.removeItem(var1));
   }

   protected void open() {
      if (!this.opened) {
         Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin.getBootstrap());
         this.opened = true;
         this.offsetDegree = this.p.getLocation().getYaw();
      }
   }

   protected void close() {
      if (this.opened) {
         HandlerList.unregisterAll(this);
         Arrays.stream(this.getItems()).forEach(var1 -> this.removeItem(var1));
      }
   }

   @EventHandler
   public void interact(PlayerInteractEvent var1) {
      if (var1.getPlayer().equals(this.p)) {
         if (var1.getAction() == Action.LEFT_CLICK_AIR && this.currentlyHovering != null) {
            this.currentlyHovering.click();
         }
      }
   }

   protected FloatingElement getCursor() {
      return this.currentlyHovering;
   }

   public abstract boolean isFixed();
}
