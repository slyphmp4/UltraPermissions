package me.TechsCode.UltraPermissions.base.scheduler;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SpigotScheduler implements Scheduler {
   private SpigotTechPlugin plugin;

   public SpigotScheduler(SpigotTechPlugin var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public void run(Runnable var1) {
      Bukkit.getScheduler().runTask((Plugin)this.plugin.getBootstrap(), var1);
   }

   @Override
   public void runAsync(Runnable var1) {
      Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin.getBootstrap(), var1);
   }

   @Override
   public Task runTaskLater(Runnable var1, long var2) {
      BukkitTask var4 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin.getBootstrap(), var1, var2);
      return this.toGenericTask(var4);
   }

   @Override
   public RecurringTask runTaskTimer(Runnable var1, long var2, long var4) {
      return this.toRecurringTask(() -> Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin.getBootstrap(), var1, var2, var4));
   }

   @Override
   public Task runTaskLaterAsync(Runnable var1, long var2) {
      BukkitTask var4 = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)this.plugin.getBootstrap(), var1, var2);
      return this.toGenericTask(var4);
   }

   @Override
   public RecurringTask runTaskTimerAsync(Runnable var1, long var2, long var4) {
      return this.toRecurringTask(() -> Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin.getBootstrap(), var1, var2, var4));
   }

   private Task toGenericTask(final BukkitTask var1) {
      return new Task() {
         @Override
         public void cancel() {
            var1.cancel();
         }
      };
   }

   private RecurringTask toRecurringTask(final SpigotScheduler.TaskCreator var1) {
      return new RecurringTask() {
         private BukkitTask bukkitTask = var1.build();

         @Override
         public void stop() {
            if (this.bukkitTask != null) {
               this.bukkitTask.cancel();
               this.bukkitTask = null;
            }
         }

         @Override
         public void start() {
            if (this.bukkitTask == null) {
               this.bukkitTask = var1.build();
            }
         }
      };
   }

   interface TaskCreator {
      BukkitTask build();
   }
}
