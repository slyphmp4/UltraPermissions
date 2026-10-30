package me.TechsCode.UltraPermissions.base.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
   public ReflectionUtil() {
      super();
   }

   public static boolean isClassAvailable(String var0) {
      try {
         Class.forName(var0);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static <T> T instantiate(String var0) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Class var1 = Class.forName(var0);
      Constructor var2 = var1.getDeclaredConstructor();
      var2.setAccessible(true);
      return (T)var2.newInstance();
   }

   public static <T> T instantiate(Constructor<?> var0, Object... var1) {
      var0.setAccessible(true);

      try {
         return (T)var0.newInstance(var1);
      } catch (InstantiationException var3) {
         throw new RuntimeException(var3);
      } catch (IllegalAccessException var4) {
         throw new RuntimeException(var4);
      } catch (InvocationTargetException var5) {
         throw new RuntimeException(var5);
      }
   }

   public static void invoke(String var0, Object var1, Object... var2) {
      Class var3 = var1.getClass();
      Method var4 = null;

      try {
         var4 = var3.getDeclaredMethod(var0, getParameterTypes(var2));
      } catch (NoSuchMethodException var7) {
         throw new RuntimeException(var7);
      }

      var4.setAccessible(true);

      try {
         var4.invoke(var1, var2);
      } catch (IllegalAccessException | InvocationTargetException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static <T> T invoke(Method var0, Object var1, Object... var2) {
      var0.setAccessible(true);

      try {
         return (T)var0.invoke(var1, var2);
      } catch (IllegalAccessException | InvocationTargetException var4) {
         throw new RuntimeException(var4);
      }
   }

   public static Constructor<?> getConstructor(String var0, Class<?>... var1) {
      Class var2 = null;

      try {
         var2 = Class.forName(var0);
      } catch (ClassNotFoundException var5) {
         throw new RuntimeException(var5);
      }

      try {
         return var2.getDeclaredConstructor(var1);
      } catch (NoSuchMethodException var4) {
         throw new RuntimeException(var4);
      }
   }

   private static Class<?>[] getParameterTypes(Object... var0) {
      Class[] var1 = new Class[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2].getClass();
      }

      return var1;
   }
}
