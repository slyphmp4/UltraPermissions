package me.TechsCode.UltraPermissions.base.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public abstract class GUI implements Listener {
   protected Player p;
   private final SpigotTechPlugin plugin;
   private volatile boolean stopped;
   private RecurringTask recurringTask;
   private Inventory inventory;
   private boolean listening;
   private HashMap<Integer, List<Action>> actions;

   public GUI(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      int var3 = var2.getAppearanceRegistry().getGuiUpdateInterval();
      this.recurringTask = var2.getSpigotScheduler().runForTimer(var1, () -> {
         if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) {
            this.revokeAccess();
         } else if (this.stopped) {
            this.recurringTask.stop();
            this.stopListening();
         } else {
            Model var3x = new Model();
            var3x.setTitle(this.getCurrentTitle());
            var3x.setSlots(this.getCurrentSlots());
            this.construct(var3x);
            if (this.inventory == null) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            // Bukkit exposes the title directly; repeated reflective lookup is costly in animated GUIs.
            String var4 = var1.getOpenInventory().getTitle();

            if (!var4.equals(var3x.getTitle())) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            if (this.inventory.getSize() != var3x.getSlots()) {
               this.inventory = Bukkit.createInventory(null, var3x.getSlots(), var3x.getTitle());
               var1.openInventory(this.inventory);
            }

            HashMap<Integer, Entry> var8 = var3x.getEntries();
            boolean inventoryChanged = false;
            for (int slot = 0; slot < this.inventory.getSize(); slot++) {
               ItemStack previous = this.inventory.getItem(slot);
               if (!var8.containsKey(slot + 1) && previous != null && !previous.getType().isAir()) {
                  this.inventory.clear(slot);
                  inventoryChanged = true;
               }
            }

            this.actions = new HashMap<>();
            for (Map.Entry<Integer, Entry> entry : var8.entrySet()) {
               Button var3xx = new Button();
               entry.getValue().button(var3xx);
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

               int slot = entry.getKey() - 1;
               if (!Objects.equals(this.inventory.getItem(slot), var4x)) {
                  this.inventory.setItem(slot, var4x);
                  inventoryChanged = true;
               }
               this.actions.put(slot, var3xx.getActions());
            }
            if (inventoryChanged) {
               var2.getSpigotScheduler().runFor(var1, var1::updateInventory);
            }
         }
      }, 0L, var3);
      this.listen();
   }

   public void preventOpening() {
      this.stopped = true;
   }

   protected abstract void construct(Model var1);

   public void reopen() {
      if (this.plugin.getSpigotScheduler().isFolia() && !this.plugin.getSpigotScheduler().owns(this.p)) {
         this.plugin.getSpigotScheduler().runFor(this.p, this::reopen);
         return;
      }
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { revokeAccess(); return; }
      if (this.inventory == null) {
         this.recurringTask.start();
         this.listen();
      }
   }

   private void listen() {
      if (!this.listening) {
         Bukkit.getPluginManager().registerEvents(this, this.plugin.getBootstrap());
         this.listening = true;
      }
   }

   private void stopListening() {
      if (this.listening) {
         HandlerList.unregisterAll(this);
         this.listening = false;
      }
   }

   private void close() {
      this.actions = null;
      this.inventory = null;
      this.recurringTask.stop();
      this.stopListening();
      this.onClose();
   }

   private void revokeAccess() {
      Inventory old = this.inventory;
      this.inventory = null;
      this.actions = null;
      this.stopped = true;
      if (this.recurringTask != null) this.recurringTask.stop();
      this.stopListening();
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
      this.plugin.writeRegionalModel(() -> {
         for (Action handler : new java.util.ArrayList<>(handlers)) {
            if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) {
               revokeAccess();
               return;
            }
            handler.onClick(ActionType.fromClickType(event.getClick()));
         }
      });
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
      if (var1.getPlayer().equals(this.p) && this.inventory != null && this.inventory.equals(var1.getInventory())) {
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
