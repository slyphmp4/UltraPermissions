package me.TechsCode.UltraPermissions.base;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class InventoryViewWrapperFactory {
   private static final Class<?> inventoryViewClass;
   private static final HashMap<String, MethodHandle> inventoryViewGetterMethodHandles = new HashMap<>();
   private static final HashMap<String, MethodHandle> inventoryViewMethodHandles = new HashMap<>();

   public InventoryViewWrapperFactory() {
      super();
   }

   public static InventoryViewWrapperFactory.InventoryViewWrapper getInventoryViewWrapper(Object var0, @NotNull String var1, Object... var2) {
      if (var0 == null) {
         throw new NullPointerException("WrapperFactory: InventoryViewProvider is null, cannot invoke method " + var1);
      }

      String var3 = var0.getClass().getName() + var1;
      MethodHandle var4 = inventoryViewGetterMethodHandles.computeIfAbsent(var3, var3x -> {
         try {
            Class var4x = var0.getClass();
            Method var5x = var4x.getMethod(var1, Arrays.stream(var2).map(Object::getClass).toArray(Class[]::new));
            Lookup var7x = MethodHandles.lookup();
            MethodHandle var6x = var7x.unreflect(var5x);
            var6x.asFixedArity();
            return var6x;
         } catch (Throwable var8x) {
            throw new RuntimeException(var8x);
         }
      });
      Object var5 = null;

      try {
         Object[] var6 = Stream.concat(Stream.of(var0), Stream.of(var2)).toArray(Object[]::new);
         var5 = var4.invokeWithArguments(var6);
      } catch (Throwable var7) {
         throw new RuntimeException(var7);
      }

      if (var5 != null && !inventoryViewClass.isAssignableFrom(var5.getClass())) {
         throw new RuntimeException("InventoryView Wrong Class: " + var5.getClass().getName());
      } else {
         return new InventoryViewWrapperFactory.InventoryViewWrapper(var5);
      }
   }

   @NotNull
   public static InventoryViewWrapperFactory.InventoryViewWrapper getOpenInventory(@NotNull HumanEntity var0) {
      return getInventoryViewWrapper(var0, "getOpenInventory");
   }

   @NotNull
   public static InventoryViewWrapperFactory.InventoryViewWrapper getView(@NotNull InventoryEvent var0) {
      return getInventoryViewWrapper(var0, "getView");
   }

   static {
      try {
         inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
      } catch (ClassNotFoundException var1) {
         throw new RuntimeException(var1);
      }
   }

   public static class InventoryViewWrapper {
      private final Object inventoryView;

      public InventoryViewWrapper(Object var1) {
         super();
         this.inventoryView = var1;
      }

      public <T> T invokeMethod(@NotNull String var1, @NotNull Class<T> var2, Object... var3) {
         return this.invokeMethod(var1, "", var2, var3);
      }

      public <T> T invokeMethod(@NotNull String var1, @NotNull String var2, @NotNull Class<T> var3, Object... var4) {
         if (this.inventoryView == null) {
            throw new NullPointerException("Wrapper: InventoryView is null, cannot invoke method " + var1);
         }

         MethodHandle var5 = InventoryViewWrapperFactory.inventoryViewMethodHandles
            .computeIfAbsent(this.inventoryView.getClass().getName() + var1 + var2, var3x -> {
               try {
                  Class var4x = this.inventoryView.getClass();
                  Lookup var7 = MethodHandles.lookup();
                  Lookup var8x = MethodHandles.privateLookupIn(var4x, var7);
                  Method var9 = var4x.getMethod(var1, Arrays.stream(var4).map(Object::getClass).toArray(Class[]::new));
                  MethodHandle var6x = var8x.unreflect(var9);
                  var6x.asFixedArity();
                  return var6x;
               } catch (Throwable var10) {
                  throw new RuntimeException(var10);
               }
            });

         try {
            Object[] var6 = Stream.concat(Stream.of(this.inventoryView), Stream.of(var4)).toArray(Object[]::new);
            return (T)var5.invokeWithArguments(var6);
         } catch (Throwable var8) {
            throw new RuntimeException(var8);
         }
      }

      @NotNull
      public Inventory getTopInventory() {
         return this.invokeMethod("getTopInventory", Inventory.class);
      }

      @NotNull
      public HumanEntity getPlayer() {
         return this.invokeMethod("getPlayer", HumanEntity.class);
      }
   }
}
