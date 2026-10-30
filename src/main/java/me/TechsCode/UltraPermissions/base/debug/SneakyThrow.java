package me.TechsCode.UltraPermissions.base.debug;

class SneakyThrow {
   SneakyThrow() {
      super();
   }

   public static void sneaky(Throwable var0) {
      throw (RuntimeException)superSneaky(var0);
   }

   private static <T extends Throwable> T superSneaky(Throwable var0) throws T {
      throw (T)var0;
   }
}
