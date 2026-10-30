package me.TechsCode.UltraPermissions.base.scheduler;

public abstract class RecurringTask {
   public RecurringTask() {
      super();
   }

   public abstract void stop();

   public abstract void start();
}
