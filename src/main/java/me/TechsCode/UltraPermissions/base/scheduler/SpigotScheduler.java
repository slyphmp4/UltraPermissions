package me.TechsCode.UltraPermissions.base.scheduler;

import java.util.function.Supplier;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SpigotScheduler implements Scheduler {
   private final Plugin plugin;
   private final FoliaScheduler folia;

   public SpigotScheduler(SpigotTechPlugin var1) {
      super();
      this.plugin = var1.getBootstrap();
      this.folia = FoliaScheduler.detect();
   }

   @Override
   public void run(Runnable var1) {
      if (this.folia == null) Bukkit.getScheduler().runTask(this.plugin, var1);
      else this.folia.later(this.plugin, var1, 1L);
   }

   @Override
   public void runAsync(Runnable var1) {
      if (this.folia == null) Bukkit.getScheduler().runTaskAsynchronously(this.plugin, var1);
      else this.folia.runAsync(this.plugin, var1);
   }

   @Override
   public Task runTaskLater(Runnable var1, long var2) {
      if (this.folia != null) return this.folia.later(this.plugin, var1, var2);
      BukkitTask var4 = Bukkit.getScheduler().runTaskLater(this.plugin, var1, var2);
      return this.toGenericTask(var4);
   }

   @Override
   public RecurringTask runTaskTimer(Runnable var1, long var2, long var4) {
      if (this.folia != null) return this.recurring(() -> this.folia.repeat(this.plugin, var1, var2, var4));
      return this.toRecurringTask(() -> Bukkit.getScheduler().runTaskTimer(this.plugin, var1, var2, var4));
   }

   @Override
   public Task runTaskLaterAsync(Runnable var1, long var2) {
      if (this.folia != null) return this.folia.laterAsync(this.plugin, var1, var2);
      BukkitTask var4 = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, var1, var2);
      return this.toGenericTask(var4);
   }

   @Override
   public RecurringTask runTaskTimerAsync(Runnable var1, long var2, long var4) {
      if (this.folia != null) return this.recurring(() -> this.folia.repeatAsync(this.plugin, var1, var2, var4));
      return this.toRecurringTask(() -> Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, var1, var2, var4));
   }

   public boolean isFolia() {
      return this.folia != null;
   }

   public boolean owns(Entity entity) {
      return this.folia == null ? Bukkit.isPrimaryThread() : this.folia.owns(entity);
   }

   public void runFor(Entity entity, Runnable action) {
      this.runForLater(entity, this.plugin, action, null, 1L);
   }

   /** Runs inline where safe, for operations that did not previously go through a scheduler. */
   public void runForIfNeeded(Entity entity, Runnable action) {
      if (this.folia == null || this.folia.owns(entity)) action.run();
      else this.runFor(entity, action);
   }

   public void runForLater(Entity entity, Runnable action, long delay) {
      this.runForLater(entity, this.plugin, action, null, delay);
   }

   /** The retirement callback must only handle bookkeeping and cannot access Bukkit state. */
   public void runForLater(Entity entity, Plugin owner, Runnable action, Runnable retired, long delay) {
      if (this.folia == null) {
         Bukkit.getScheduler().runTaskLater(owner, action, delay);
      } else if (!this.folia.executeFor(owner, entity, action, retired, delay) && retired != null) {
         retired.run();
      }
   }

   public RecurringTask runForTimer(Entity entity, Runnable action, long delay, long period) {
      if (this.folia == null) return this.runTaskTimer(action, delay, period);
      return this.recurring(() -> this.folia.repeatFor(this.plugin, entity, action, delay, period));
   }

   private RecurringTask recurring(Supplier<Task> factory) {
      return new RecurringTask() {
         private Task active = factory.get();

         @Override
         public synchronized void stop() {
            if (this.active != null) {
               this.active.cancel();
               this.active = null;
            }
         }

         @Override
         public synchronized void start() {
            if (this.active == null) this.active = factory.get();
         }
      };
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
