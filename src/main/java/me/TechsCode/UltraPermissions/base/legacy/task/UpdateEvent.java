package me.TechsCode.UltraPermissions.base.legacy.task;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpdateEvent extends Event {
   private UpdateTime time;
   private static final HandlerList handlers = new HandlerList();

   public UpdateEvent(UpdateTime var1) {
      super();
      this.time = var1;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public UpdateTime getUpdateTime() {
      return this.time;
   }
}
