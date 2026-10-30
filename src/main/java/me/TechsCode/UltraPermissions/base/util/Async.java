package me.TechsCode.UltraPermissions.base.util;

import java.util.concurrent.CompletableFuture;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.debug.TechException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Async {
   public Async() {
      super();
   }

   public static <T extends Runnable> BukkitTask runLater(T var0) {
      return runLater(1, var0);
   }

   public static BukkitTask runLater(int var0, Runnable var1) {
      BukkitScheduler var2 = Bukkit.getScheduler();
      JavaPlugin var3 = (JavaPlugin)TechPlugin.getInstance().getBootstrap();

      try {
         return runIfDisabled(var1)
            ? null
            : (
               var0 == 0
                  ? (var1 instanceof BukkitRunnable ? ((BukkitRunnable)var1).runTask(var3) : var2.runTask(var3, var1))
                  : (var1 instanceof BukkitRunnable ? ((BukkitRunnable)var1).runTaskLater(var3, var0) : var2.runTaskLater(var3, var1, var0))
            );
      } catch (NoSuchMethodError var5) {
         return runIfDisabled(var1)
            ? null
            : (
               var0 == 0
                  ? (var1 instanceof BukkitRunnable ? ((BukkitRunnable)var1).runTask(var3) : getTaskFromId(var2.scheduleSyncDelayedTask(var3, var1)))
                  : (
                     var1 instanceof BukkitRunnable
                        ? ((BukkitRunnable)var1).runTaskLater(var3, var0)
                        : getTaskFromId(var2.scheduleSyncDelayedTask(var3, var1, var0))
                  )
            );
      }
   }

   public static BukkitTask runAsync(Runnable var0) {
      return runLaterAsync(0, var0);
   }

   public static BukkitTask runLaterAsync(Runnable var0) {
      return runLaterAsync(0, var0);
   }

   public static BukkitTask runLaterAsync(int var0, Runnable var1) {
      BukkitScheduler var2 = Bukkit.getScheduler();
      JavaPlugin var3 = (JavaPlugin)TechPlugin.getInstance().getBootstrap();

      try {
         return runIfDisabled(var1)
            ? null
            : (
               var0 == 0
                  ? (var1 instanceof BukkitRunnable ? ((BukkitRunnable)var1).runTaskAsynchronously(var3) : var2.runTaskAsynchronously(var3, var1))
                  : (
                     var1 instanceof BukkitRunnable
                        ? ((BukkitRunnable)var1).runTaskLaterAsynchronously(var3, var0)
                        : var2.runTaskLaterAsynchronously(var3, var1, var0)
                  )
            );
      } catch (NoSuchMethodError var5) {
         return runIfDisabled(var1)
            ? null
            : (var0 == 0 ? getTaskFromId(var2.scheduleAsyncDelayedTask(var3, var1)) : getTaskFromId(var2.scheduleAsyncDelayedTask(var3, var1, var0)));
      }
   }

   public static BukkitTask runTimer(int var0, Runnable var1) {
      return runTimer(0, var0, var1);
   }

   public static BukkitTask runTimer(int var0, int var1, Runnable var2) {
      try {
         return runIfDisabled(var2)
            ? null
            : (
               var2 instanceof BukkitRunnable
                  ? ((BukkitRunnable)var2).runTaskTimer((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var0, var1)
                  : Bukkit.getScheduler().runTaskTimer((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var2, var0, var1)
            );
      } catch (NoSuchMethodError var4) {
         return runIfDisabled(var2)
            ? null
            : getTaskFromId(Bukkit.getScheduler().scheduleSyncRepeatingTask((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var2, var0, var1));
      }
   }

   public static BukkitTask runTimerAsync(int var0, Runnable var1) {
      return runTimerAsync(0, var0, var1);
   }

   public static BukkitTask runTimerAsync(int var0, int var1, Runnable var2) {
      try {
         return runIfDisabled(var2)
            ? null
            : (
               var2 instanceof BukkitRunnable
                  ? ((BukkitRunnable)var2).runTaskTimerAsynchronously((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var0, var1)
                  : Bukkit.getScheduler().runTaskTimerAsynchronously((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var2, var0, var1)
            );
      } catch (NoSuchMethodError var4) {
         return runIfDisabled(var2)
            ? null
            : getTaskFromId(Bukkit.getScheduler().scheduleAsyncRepeatingTask((JavaPlugin)TechPlugin.getInstance().getBootstrap(), var2, var0, var1));
      }
   }

   private static BukkitTask getTaskFromId(int var0) {
      for (BukkitTask var2 : Bukkit.getScheduler().getPendingTasks()) {
         if (var2.getTaskId() == var0) {
            return var2;
         }
      }

      return null;
   }

   private static boolean runIfDisabled(Runnable var0) {
      if (!((JavaPlugin)TechPlugin.getInstance().getBootstrap()).isEnabled()) {
         var0.run();
         return true;
      } else {
         return false;
      }
   }

   public static <T> void runAsyncWithCallbacks(Async.AsyncTask<T> var0, Async.AsyncCallback<T> var1, Async.ErrorCallback var2) {
      runAsync(() -> {
         CompletableFuture<T> var3 = CompletableFuture.supplyAsync(() -> {
            try {
               TechPlugin.getInstance().log("Running async task to give something back");
               return var0.runAsync();
            } catch (Exception var3x) {
               if (var2 != null) {
                  var2.onError(var3x);
               }

               return null;
            }
         });
         var3.whenComplete((var2xx, var3x) -> {
            if (var3x != null) {
               if (var2 != null) {
                  var2.onError((Exception)var3x);
               }
            } else if (var1 != null) {
               var1.onComplete(var2xx);
            }
         });
      });
   }

   public static void runAsyncWithCallbacks(Runnable var0, Runnable var1, Async.ErrorCallback var2) {
      runAsync(() -> {
         CompletableFuture var3 = CompletableFuture.runAsync(() -> {
            try {
               var0.run();
            } catch (Exception var3x) {
               if (var2 != null) {
                  var2.onError(var3x);
               }
            }
         });
         var3.whenComplete((var2xx, var3x) -> {
            if (var3x != null) {
               if (var2 != null) {
                  var2.onError((Exception)var3x);
               }
            } else if (var1 != null) {
               var1.run();
            }
         });
      });
   }

   public static void checkSync(String var0) {
      if (!Bukkit.isPrimaryThread()) {
         throw new TechException(var0);
      }
   }

   public interface AsyncCallback<T> {
      void onComplete(T var1);
   }

   public interface AsyncTask<T> {
      T runAsync() throws Exception;
   }

   public interface ErrorCallback {
      void onError(Exception var1);
   }
}
