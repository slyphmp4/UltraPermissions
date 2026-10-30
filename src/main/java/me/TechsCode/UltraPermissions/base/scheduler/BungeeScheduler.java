package me.TechsCode.UltraPermissions.base.scheduler;

import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeScheduler implements Scheduler {
   private BungeeTechPlugin plugin;

   public BungeeScheduler(BungeeTechPlugin var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public void run(Runnable var1) {
      var1.run();
   }

   @Override
   public void runAsync(Runnable var1) {
      ProxyServer.getInstance().getScheduler().runAsync(this.plugin.getBootstrap(), var1);
   }

   @Override
   public Task runTaskLater(Runnable var1, long var2) {
      final ScheduledTask var4 = ProxyServer.getInstance().getScheduler().schedule(this.plugin.getBootstrap(), var1, var2 * 1000L / 20L, TimeUnit.MILLISECONDS);
      return new Task() {
         @Override
         public void cancel() {
            var4.cancel();
         }
      };
   }

   @Override
   public RecurringTask runTaskTimer(final Runnable var1, final long var2, final long var4) {
      final ScheduledTask var6 = ProxyServer.getInstance()
         .getScheduler()
         .schedule(this.plugin.getBootstrap(), var1, var2 * 1000L / 20L, var4 * 1000L / 20L, TimeUnit.MILLISECONDS);
      return new RecurringTask() {
         private boolean cancelled;

         @Override
         public void stop() {
            var6.cancel();
            this.cancelled = true;
         }

         @Override
         public void start() {
            if (this.cancelled) {
               BungeeScheduler.this.runTaskTimer(var1, var2, var4);
            }
         }
      };
   }

   @Override
   public Task runTaskLaterAsync(Runnable var1, long var2) {
      throw new UnsupportedOperationException("The method runTaskLaterAsync is not defined for BungeeCord");
   }

   @Override
   public RecurringTask runTaskTimerAsync(Runnable var1, long var2, long var4) {
      throw new UnsupportedOperationException("The method runTaskTimerAsync is not defined for BungeeCord");
   }
}
