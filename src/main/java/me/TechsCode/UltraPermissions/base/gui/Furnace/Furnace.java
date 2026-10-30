package me.TechsCode.UltraPermissions.base.gui.Furnace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Action;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public abstract class Furnace implements Listener {
   protected Player p;
   private final SpigotTechPlugin plugin;
   private boolean stopped;
   private RecurringTask recurringTask;
   private Inventory inventory;
   private HashMap<Integer, List<Action>> actions;

   public Furnace(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      this.recurringTask = var2.getScheduler().runTaskTimer(() -> {
         if (this.stopped) {
            this.recurringTask.stop();
            HandlerList.unregisterAll(this);
         } else {
            FurnaceModel var2x = new FurnaceModel();
            var2x.setTitle(this.getCurrentTitle());
            var2x.setSlots(3);
            this.construct(var2x);

            String var3;
            try {
               InventoryView var4 = var1.getOpenInventory();
               Method var5 = var4.getClass().getMethod("getTitle");
               var5.setAccessible(true);
               var3 = (String)var5.invoke(var4);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException var6) {
               throw new RuntimeException(var6);
            }

            if (this.inventory == null || !var3.equals(var2x.getTitle()) || this.inventory.getSize() != var2x.getSlots()) {
               this.inventory = Bukkit.createInventory(null, InventoryType.FURNACE, var2x.getTitle());
               var1.openInventory(this.inventory);
            }

            HashMap<Integer, me.TechsCode.UltraPermissions.base.gui.Entry> var7 = var2x.getEntries();
            IntStream.rangeClosed(1, this.inventory.getSize()).filter(var1xx -> !var7.containsKey(var1xx)).forEach(var1xx -> this.inventory.clear(var1xx - 1));
            this.actions = new HashMap<>();
            var7.forEach((var1xx, var2xx) -> {
               Button var3x = new Button();
               var2xx.button(var3x);
               this.inventory.setItem(var1xx, var3x.item().get());
               this.actions.put(var1xx, var3x.getActions());
            });
            var1.updateInventory();
         }
      }, 0L, 2L);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var2.getBootstrap());
   }

   public void preventOpening() {
      this.stopped = true;
   }

   protected abstract void construct(FurnaceModel var1);

   public void reopen() {
      if (this.inventory == null) {
         this.recurringTask.start();
         Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin.getBootstrap());
      }
   }

   private void close() {
      this.inventory = null;
      this.recurringTask.stop();
      HandlerList.unregisterAll(this);
      this.onClose();
   }

   @EventHandler
   public void click(InventoryClickEvent var1) {
      if (var1.getWhoClicked().equals(this.p)) {
         if (var1.getSlotType() == SlotType.OUTSIDE) {
            var1.setCancelled(true);
         } else {
            var1.setCancelled(true);
            if (this.actions != null) {
               if (!(var1.getClickedInventory() instanceof PlayerInventory) && this.actions.containsKey(var1.getSlot())) {
                  ActionType var2 = ActionType.fromClickType(var1.getClick());
                  this.actions.get(var1.getSlot()).forEach(var1x -> var1x.onClick(var2));
               }
            }
         }
      }
   }

   @EventHandler
   public void drag(InventoryDragEvent var1) {
      if (var1.getWhoClicked().equals(this.p)) {
         if (!(var1.getInventory() instanceof PlayerInventory)) {
            if (var1.getInventorySlots().size() > 1) {
               var1.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void leave(PlayerQuitEvent var1) {
      if (var1.getPlayer().equals(this.p)) {
         this.close();
      }
   }

   @EventHandler
   public void close(InventoryCloseEvent var1) {
      if (this.inventory != null && this.inventory.equals(var1.getInventory())) {
         this.close();
      }
   }

   public String getCurrentTitle() {
      return "Unnamed Inventory";
   }

   public void onClose() {
   }
}
