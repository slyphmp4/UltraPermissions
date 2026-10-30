package me.TechsCode.UltraPermissions.base.debug;

import lombok.Generated;

public class TechException extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private static boolean errorSavedAutomatically = true;

   public TechException(Throwable var1) {
      super(var1);
      if (errorSavedAutomatically) {
         Debugger.saveError(var1);
      }
   }

   public TechException(String var1) {
      super(var1);
      if (errorSavedAutomatically) {
         Debugger.saveError(this, var1);
      }
   }

   public TechException(String var1, Throwable var2) {
      this(var2, var1);
   }

   public TechException(Throwable var1, String var2) {
      super(var2, var1);
      if (errorSavedAutomatically) {
         Debugger.saveError(var1, var2);
      }
   }

   public TechException() {
      super();
      if (errorSavedAutomatically) {
         Debugger.saveError(this);
      }
   }

   @Override
   public String getMessage() {
      return "Report: " + super.getMessage();
   }

   public static void sneaky(Throwable var0) {
      try {
         SneakyThrow.sneaky(var0);
      } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError var2) {
         throw new TechException(var0);
      }
   }

   @Generated
   public static boolean isErrorSavedAutomatically() {
      return errorSavedAutomatically;
   }

   @Generated
   public static void setErrorSavedAutomatically(boolean var0) {
      errorSavedAutomatically = var0;
   }
}
