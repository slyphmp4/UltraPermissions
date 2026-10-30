package me.TechsCode.UltraPermissions.base.expire;

public class ExpireTime {
   private final ExpireTimeType type;
   private final long time;

   private ExpireTime(ExpireTimeType var1, long var2) {
      super();
      this.type = var1;
      this.time = var2;
   }

   public static ExpireTime of(ExpireTimeType var0, long var1) {
      return new ExpireTime(var0, var1);
   }

   public static ExpireTime release() {
      return new ExpireTime(ExpireTimeType.RELEASE, -1L);
   }

   public ExpireTimeType getType() {
      return this.type;
   }

   public long getTime() {
      return this.time;
   }

   public boolean isRelease() {
      return this.getType() == ExpireTimeType.RELEASE;
   }
}
