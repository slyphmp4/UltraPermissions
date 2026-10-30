package me.TechsCode.UltraPermissions.base.reflection.utility;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ReflectionUtil {
   public static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
   public static final String SERVER_PACKAGE = CRAFTBUKKIT_PACKAGE.split("\\.").length > 3 ? CRAFTBUKKIT_PACKAGE.split("\\.")[3] : "";
   public static final int MINOR_VERSION = getMinorVersion();
   public static final int PATCH_VERSION = getPatchVersion();

   public ReflectionUtil() {
      super();
   }

   private static int getPatchVersion() {
      return MinecraftRelease.parse(Bukkit.getBukkitVersion()).patch();
   }

   private static int getMinorVersion() {
      return MinecraftRelease.parse(Bukkit.getBukkitVersion()).feature();
   }

   public static <T extends AccessibleObject> T setAccessible(@NotNull T var0) {
      var0.setAccessible(true);
      return (T)var0;
   }

   public static Class<?> getClass(boolean var0, String... var1) throws ClassNotFoundException {
      for (String var5 : var1) {
         if (var0) {
            try {
               return Class.forName(CRAFTBUKKIT_PACKAGE + "." + var5);
            } catch (ClassNotFoundException var12) {
            }
         } else {
            String[] var6;
            if (MINOR_VERSION >= 17) {
               var6 = new String[]{"net.minecraft." + var5, "net.minecraft.server." + SERVER_PACKAGE + "." + var5};
            } else {
               var6 = new String[]{"net.minecraft.server." + SERVER_PACKAGE + "." + var5, "net.minecraft." + var5};
            }

            for (String var10 : var6) {
               try {
                  return Class.forName(var10);
               } catch (ClassNotFoundException var13) {
               }
            }
         }
      }

      throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(var1));
   }

   public static List<Field> getFields(Class<?> var0, Class<?> var1) {
      return Arrays.stream(var0.getDeclaredFields())
         .filter(var1x -> var1x.getType().equals(var1))
         .map(ReflectionUtil::setAccessible)
         .collect(Collectors.toList());
   }

   public static Field getField(@NotNull Class<?> var0, @NotNull String... var1) {
      for (String var5 : var1) {
         try {
            return setAccessible(var0.getDeclaredField(var5));
         } catch (NoSuchFieldException var7) {
         }
      }

      throw new IllegalArgumentException("Class " + var0.getName() + " does not contain a field with potential names " + Arrays.toString(var1));
   }

   public static List<Method> getMethods(@NotNull Class<?> var0, @NotNull Class<?> var1, @NotNull Class<?>... var2) {
      ArrayList var3 = new ArrayList();
      Arrays.stream(var0.getMethods())
         .filter(var2x -> var1.isAssignableFrom(var2x.getReturnType()) && var2x.getParameterCount() == var2.length)
         .forEachOrdered(var2x -> {
            Class[] var3x = var2x.getParameterTypes();
            boolean var4 = IntStream.range(0, var3x.length).noneMatch(var2xx -> var3x[var2xx] != var2[var2xx]);
            if (var4) {
               var3.add(var2x);
            }
         });
      return var3;
   }

   public static Method getMethod(@NotNull Class<?> var0, @NotNull String[] var1, @NotNull Class<?>... var2) throws NoSuchMethodException {
      ArrayList var3 = new ArrayList();

      for (Method var7 : var0.getMethods()) {
         if (var7.getParameterCount() == var2.length) {
            Class[] var8 = var7.getParameterTypes();
            boolean var9 = IntStream.range(0, var8.length).noneMatch(var2x -> var8[var2x] != var2[var2x]);
            if (var9) {
               for (String var13 : var1) {
                  if (var7.getName().equals(var13)) {
                     return var7;
                  }

                  String[] var14 = var7.getName().split("_");
                  if (var14.length > 2 && var14[2].equals(var13)) {
                     return var7;
                  }
               }

               var3.add(var7.getName());
            }
         }
      }

      throw new NoSuchMethodException(
         "No method found with possible names "
            + Arrays.toString(var1)
            + " with parameters "
            + Arrays.toString(var2)
            + " in class "
            + var0.getName()
            + ". Methods with matching parameters: "
            + var3
      );
   }

   public static Constructor<?> getConstructor(@NotNull Class<?> var0, @NotNull Class<?>... var1) {
      for (Constructor var5 : var0.getDeclaredConstructors()) {
         if (var5.getParameterCount() == var1.length) {
            Class[] var6 = var5.getParameterTypes();
            boolean var7 = IntStream.range(0, var6.length).noneMatch(var2 -> var6[var2] != var1[var2]);
            if (var7) {
               return setAccessible(var5);
            }
         }
      }

      throw new IllegalArgumentException("Class " + var0.getName() + " does not contain a constructor with parameters " + Arrays.toString(var1));
   }

   public static List<Field> getInstanceFields(@NotNull Class<?> var0, @NotNull Class<?> var1) {
      return Arrays.stream(var0.getDeclaredFields())
         .filter(var1x -> var1x.getType() == var1 && !Modifier.isStatic(var1x.getModifiers()))
         .map(ReflectionUtil::setAccessible)
         .collect(Collectors.toList());
   }

   public static Constructor<?> getOnlyConstructor(@NotNull Class<?> var0) {
      Constructor[] var1 = var0.getConstructors();
      assert var1.length == 1 : "Class "
         + var0.getName()
         + " is expected to have 1 constructor, but has "
         + var1.length
         + ": \n"
         + (String)Arrays.stream(var1).map(Constructor::toString).collect(Collectors.joining("\n"));
      return var1[0];
   }

   public static Field getOnlyField(@NotNull Class<?> var0, @NotNull Class<?> var1) {
      List<Field> var2 = getFields(var0, var1);
      assert var2.size() == 1 : "Class "
         + var0.getName()
         + " is expected to have 1 field of type "
         + var1.getName()
         + ", but has "
         + var2.size()
         + ": "
         + var2.stream().map(Field::getName).toList();
      return (Field)var2.get(0);
   }

   public static Method getOnlyMethod(@NotNull Class<?> var0, @NotNull Class<?> var1, @NotNull Class<?>... var2) {
      List<Method> var3 = getMethods(var0, var1, var2);
      assert var3.size() == 1 : "Class "
         + var0.getName()
         + " is expected to have 1 method with return type "
         + var1.getName()
         + " and parameters "
         + Arrays.toString(var2)
         + ", but has "
         + var3.size()
         + ": \n"
         + (String)var3.stream().map(Method::toString).collect(Collectors.joining("\n"));
      return (Method)var3.get(0);
   }
}
