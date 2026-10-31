package me.TechsCode.UltraPermissions.base.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public abstract class GUI implements Listener {
   protected Player p;
   private final SpigotTechPlugin plugin;
   private boolean stopped;
   private RecurringTask recurringTask;
   private Inventory inventory;
   private HashMap<Integer, List<Action>> actions;

   public GUI(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      int var3 = var2.getAppearanceRegistry().getGuiUpdateInterval();
      this.recurringTask = var2.getScheduler().runTaskTimer(() -> {
         if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) {
            this.revokeAccess();
         } else if (this.stopped) {
            this.recurringTask.stop();
            HandlerList.unregisterAll(this);
         } else {
            Model var3x = new Model();
            var3x.setTitle(this.getCurrentTitle());
            var3x.setSlots(this.getCurrentSlots());
            this.construct(var3x);
            if (this.inventory == null) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            String var4;
            try {
               InventoryView var5 = var1.getOpenInventory();
               Method var6 = var5.getClass().getMethod("getTitle");
               var6.setAccessible(true);
               var4 = (String)var6.invoke(var5);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException var7) {
               throw new RuntimeException(var7);
            }

            if (!var4.equals(var3x.getTitle())) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            if (this.inventory.getSize() != var3x.getSlots()) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            HashMap<Integer, Entry> var8 = var3x.getEntries();
            IntStream.rangeClosed(1, this.inventory.getSize()).filter(var1xx -> !var8.containsKey(var1xx)).forEach(var1xx -> this.inventory.clear(var1xx - 1));
            this.actions = new HashMap<>();
            var8.forEach((var1xx, var2xx) -> {
               Button var3xx = new Button();
               var2xx.button(var3xx);
               ItemStack var4x = var3xx.item().get();
               ItemMeta var5x = var4x.getItemMeta();
               if (var5x != null) {
                  try {
                     if (var5x.hasItemFlag(ItemFlag.HIDE_LORE)) {
                        var5x.removeItemFlags(new ItemFlag[]{ItemFlag.HIDE_LORE});
                        var4x.setItemMeta(var5x);
                     }
                  } catch (NoSuchFieldError var7x) {
                  }
               }

               this.inventory.setItem(var1xx - 1, var4x);
               this.actions.put(var1xx - 1, var3xx.getActions());
            });
            Bukkit.getScheduler().runTask((Plugin)var2.getBootstrap(), var1::updateInventory);
         }
      }, 0L, var3);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var2.getBootstrap());
   }

   public void preventOpening() {
      this.stopped = true;
   }

   protected abstract void construct(Model var1);

   public void reopen() {
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { revokeAccess(); return; }
      if (this.inventory == null) {
         this.recurringTask.start();
         Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin.getBootstrap());
      }
   }

   private void close() {
      this.actions = null;
      this.inventory = null;
      this.recurringTask.stop();
      HandlerList.unregisterAll(this);
      this.onClose();
   }

   private void revokeAccess() {
      Inventory old = this.inventory;
      this.inventory = null;
      this.actions = null;
      this.stopped = true;
      if (this.recurringTask != null) this.recurringTask.stop();
      HandlerList.unregisterAll(this);
      // No onClose callback: a revoked session must not open another editor.
      if (old != null && old.equals(me.TechsCode.UltraPermissions.base.security.ViewAccess.top(this.p.getOpenInventory()))) this.p.closeInventory();
   }

   @EventHandler
   public void click(InventoryClickEvent event) {
      if (!event.getWhoClicked().equals(this.p) || this.inventory == null
            || !this.inventory.equals(me.TechsCode.UltraPermissions.base.security.ViewAccess.top(event.getView()))) return;
      event.setCancelled(true);
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { revokeAccess(); return; }
      if (!this.inventory.equals(event.getClickedInventory()) || this.actions == null) return;
      List<Action> handlers = this.actions.get(event.getSlot());
      if (handlers == null) return;
      for (Action handler : new java.util.ArrayList<>(handlers)) {
         if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { revokeAccess(); return; }
         handler.onClick(ActionType.fromClickType(event.getClick()));
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

   public int getCurrentSlots() {
      return 27;
   }

   public String getCurrentTitle() {
      return GuiLabels.inventory();
   }

   public void onClose() {
   }
}
