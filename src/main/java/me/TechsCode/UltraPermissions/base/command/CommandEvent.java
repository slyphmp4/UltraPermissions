package me.TechsCode.UltraPermissions.base.command;

import java.util.ArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class CommandEvent implements Listener {
   String perm;
   String command;
   ArrayList<String> alias;

   public CommandEvent(String var1, String var2, ArrayList<String> var3) {
      super();
      this.perm = var1;
      this.command = var2;
      this.alias = var3;
   }

   @EventHandler
   public void command(PlayerCommandSendEvent var1) {
      if (!var1.getPlayer().hasPermission(this.perm) && !this.perm.isEmpty()) {
         var1.getCommands().remove(this.command);
         this.alias.forEach(var1x -> var1.getCommands().remove(var1x));
      }
   }
}
