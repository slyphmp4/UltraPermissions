package me.TechsCode.UltraPermissions.base.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/** Resolves the Folia API only on Folia so older Bukkit servers keep using their native scheduler. */
final class FoliaScheduler {
   private final Object global;
   private final Object async;
   private final Method entityScheduler;
   private final Method ownsEntity;
   private final Method globalDelayed;
   private final Method globalTimer;
   private final Method asyncNow;
   private final Method asyncDelayed;
   private final Method asyncTimer;
   private final Method entityExecute;
   private final Method entityTimer;
   private final Method cancel;

   static FoliaScheduler detect() {
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, Bukkit.getServer().getClass().getClassLoader());
      } catch (ClassNotFoundException classicServer) {
         return null;
      }
      return new FoliaScheduler();
   }

   private FoliaScheduler() {
      try {
         Method globalScheduler = Server.class.getMethod("getGlobalRegionScheduler");
         Method asyncScheduler = Server.class.getMethod("getAsyncScheduler");
         this.global = invoke(globalScheduler, Bukkit.getServer());
         this.async = invoke(asyncScheduler, Bukkit.getServer());
         this.entityScheduler = Entity.class.getMethod("getScheduler");
         this.ownsEntity = Server.class.getMethod("isOwnedByCurrentRegion", Entity.class);

         Class<?> globalType = globalScheduler.getReturnType();
         Class<?> asyncType = asyncScheduler.getReturnType();
         Class<?> entityType = entityScheduler.getReturnType();
         this.globalDelayed = globalType.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
         this.globalTimer = globalType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
         this.asyncNow = asyncType.getMethod("runNow", Plugin.class, Consumer.class);
         this.asyncDelayed = asyncType.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
         this.asyncTimer = asyncType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
         this.entityExecute = entityType.getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
         this.entityTimer = entityType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
         this.cancel = globalDelayed.getReturnType().getMethod("cancel");
      } catch (ReflectiveOperationException incompatible) {
         throw new IllegalStateException("Could not initialize Folia's scheduler API", incompatible);
      }
   }

   boolean owns(Entity entity) {
      return (Boolean) invoke(ownsEntity, Bukkit.getServer(), entity);
   }

   boolean executeFor(Plugin plugin, Entity entity, Runnable action, Runnable retired, long delay) {
      Object scheduler = invoke(entityScheduler, entity);
      return (Boolean) invoke(entityExecute, scheduler, plugin, action, retired, Math.max(1L, delay));
   }

   Task repeatFor(Plugin plugin, Entity entity, Runnable action, long delay, long period) {
      Object scheduler = invoke(entityScheduler, entity);
      return task(invoke(entityTimer, scheduler, plugin, callback(action), null, Math.max(1L, delay), period));
   }

   Task later(Plugin plugin, Runnable action, long delay) {
      return task(invoke(globalDelayed, global, plugin, callback(action), Math.max(1L, delay)));
   }

   Task repeat(Plugin plugin, Runnable action, long delay, long period) {
      return task(invoke(globalTimer, global, plugin, callback(action), Math.max(1L, delay), period));
   }

   void runAsync(Plugin plugin, Runnable action) {
      invoke(asyncNow, async, plugin, callback(action));
   }

   Task laterAsync(Plugin plugin, Runnable action, long delay) {
      if (delay <= 0L) {
         return task(invoke(asyncNow, async, plugin, callback(action)));
      }
      return task(invoke(asyncDelayed, async, plugin, callback(action), millis(delay), TimeUnit.MILLISECONDS));
   }

   Task repeatAsync(Plugin plugin, Runnable action, long delay, long period) {
      return task(invoke(asyncTimer, async, plugin, callback(action), millis(Math.max(1L, delay)), millis(period), TimeUnit.MILLISECONDS));
   }

   private static long millis(long ticks) {
      return Math.multiplyExact(ticks, 50L);
   }

   private static Consumer<Object> callback(Runnable action) {
      return ignored -> action.run();
   }

   private Task task(Object scheduled) {
      if (scheduled == null) return null; // The entity was already retired.
      return new Task() {
         @Override
         public void cancel() {
            invoke(cancel, scheduled);
         }
      };
   }

   private static Object invoke(Method method, Object receiver, Object... args) {
      try {
         return method.invoke(receiver, args);
      } catch (InvocationTargetException failure) {
         Throwable cause = failure.getCause();
         if (cause instanceof RuntimeException runtime) throw runtime;
         if (cause instanceof Error error) throw error;
         throw new IllegalStateException("Folia scheduler call failed", cause);
      } catch (IllegalAccessException failure) {
         throw new IllegalStateException("Folia scheduler API is inaccessible", failure);
      }
   }
}
