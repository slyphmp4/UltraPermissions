package me.TechsCode.UltraPermissions.base.dialog;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar.ActionBar;
import me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar.Title;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public abstract class Dialog implements Listener, Runnable {
   private final Player p;
   private final SpigotTechPlugin plugin;
   private volatile boolean closed;
   private boolean inputPending;
   private final ActionBar actionBar;
   private String lastMainTitle;
   private String lastSubTitle;
   private final RecurringTask visualTask;

   public Dialog(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      this.actionBar = new ActionBar(var2);
      var2.getScheduler().run(var1::closeInventory);
      this.visualTask = var2.getScheduler().runTaskTimer(this, 1L, 1L);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var2.getBootstrap());
   }

   public abstract void onClose(Player var1);

   public abstract boolean onInput(String var1);

   public abstract String getMainTitle();

   public abstract String getSubTitle();

   public abstract String getActionBar();

   @Override
   public void run() {
      if (this.closed) return;
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { this.close(true); return; }
      String var1 = this.getMainTitle();
      String var2 = this.getSubTitle();
      String var3 = this.getActionBar();
      if (this.lastMainTitle == null || this.lastSubTitle == null || !this.lastMainTitle.equals(var1) || !this.lastSubTitle.equals(var2)) {
         Title.sendTitle(this.p, 30, 6000, 0, var1, var2);
         this.lastMainTitle = var1;
         this.lastSubTitle = var2;
      }

      if (var3 != null) {
         this.actionBar.sendActionBar(this.p, var3);
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onChat(AsyncPlayerChatEvent event) {
      if (!event.getPlayer().equals(this.p) || this.closed) return;
      event.setCancelled(true);
      final String input = event.getMessage();
      if (input.length() > 4096) return;
      synchronized (this) {
         if (this.closed || this.inputPending) return;
         this.inputPending = true;
      }
      this.plugin.getScheduler().run(() -> {
         try {
            if (this.closed) return;
            if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) { this.close(true); return; }
            this.onInput(input);
         } finally { synchronized (this) { this.inputPending = false; } }
      });
   }

   @EventHandler
   public void close(PlayerQuitEvent var1) {
      if (var1.getPlayer().equals(this.p)) {
         this.close(false);
      }
   }

   @EventHandler
   public void close(InventoryOpenEvent var1) {
      if (var1.getPlayer().equals(this.p)) {
         this.close(false);
      }
   }

   public void close(boolean var1) {
      if (this.closed) return;
      this.closed = true;
      if (!me.TechsCode.UltraPermissions.base.security.UiAccess.allowed(this.plugin, this.p)) var1 = true;
      HandlerList.unregisterAll(this);
      this.visualTask.stop();
      if (!var1) {
         this.onClose(this.p);
      }

      Title.clearTitle(this.p);
      this.actionBar.sendActionBar(this.p, "");
   }
}
