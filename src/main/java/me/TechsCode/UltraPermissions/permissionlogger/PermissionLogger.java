package me.TechsCode.UltraPermissions.permissionlogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import me.TechsCode.UltraPermissions.UltraPermissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PermissionLogger implements Listener {
   private ArrayList<LoggedPermission> list = new ArrayList<>();

   public PermissionLogger(UltraPermissions var1) {
      super();
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   public void log(Player var1, String var2, boolean var3, String var4) {
      LoggedPermission var5 = new LoggedPermission(var1, var2, var3, System.currentTimeMillis(), var4);

      try {
         new ArrayList<>(this.list).stream().filter(var0 -> var0 != null).forEach(var2x -> {
            if (var2x.getPermission().equals(var5.getPermission()) && var2x.getPlayer().equals(var5.getPlayer())) {
               this.list.remove(var2x);
            }
         });
         if (this.list.size() > 500) {
            this.list.remove(0);
         }

         this.list.add(var5);
      } catch (Exception var7) {
      }
   }

   public LoggedPermission[] getLoggedPermissions() {
      return Arrays.stream(this.list.toArray(new LoggedPermission[this.list.size()]))
         .filter(Objects::nonNull)
         .sorted(Comparator.comparing(LoggedPermission::getTime, Comparator.reverseOrder()).thenComparing(LoggedPermission::getPermission))
         .toArray(LoggedPermission[]::new);
   }

   @EventHandler
   public void clear(PlayerQuitEvent var1) {
      try {
         for (LoggedPermission var3 : new ArrayList<>(this.list)) {
            if (var3 != null && var3.getPlayer() != null && var3.getPlayer().equals(var1.getPlayer())) {
               this.list.remove(var3);
            }
         }
      } catch (Exception var4) {
      }
   }

   public void clearManually(Player var1, String var2) {
      try {
         for (LoggedPermission var4 : new ArrayList<>(this.list)) {
            if (var4 != null && var4.getPlayer() != null && var4.getPermission().contains(var2) && var4.getPlayer().equals(var1.getPlayer())) {
               this.list.remove(var4);
            }
         }
      } catch (Exception var5) {
      }
   }
}
