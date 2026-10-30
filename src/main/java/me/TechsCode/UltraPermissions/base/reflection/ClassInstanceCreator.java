package me.TechsCode.UltraPermissions.base.reflection;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class ClassInstanceCreator {
   static Unsafe unsafe;

   public ClassInstanceCreator() {
      super();
   }

   public static <T> T create(Class<T> var0) {
      try {
         return (T)unsafe.allocateInstance(var0);
      } catch (InstantiationException var2) {
         return null;
      }
   }

   static {
      try {
         Field var0 = Unsafe.class.getDeclaredField("theUnsafe");
         var0.setAccessible(true);
         unsafe = (Unsafe)var0.get(null);
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }
}
