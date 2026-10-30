package me.TechsCode.UltraPermissions.base.scheduler;

public interface Scheduler {
   void run(Runnable var1);

   void runAsync(Runnable var1);

   Task runTaskLater(Runnable var1, long var2);

   RecurringTask runTaskTimer(Runnable var1, long var2, long var4);

   Task runTaskLaterAsync(Runnable var1, long var2);

   RecurringTask runTaskTimerAsync(Runnable var1, long var2, long var4);
}
