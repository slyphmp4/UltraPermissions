package me.TechsCode.UltraPermissions.events;

import me.TechsCode.UltraPermissions.base.TechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class UPEvent extends Event {
   private static final HandlerList HANDLERS = new HandlerList();

   public UPEvent() {
      super();
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public void call(TechPlugin var1, Event var2) {
      var1.getScheduler().run(() -> Bukkit.getPluginManager().callEvent(var2));
   }
}
