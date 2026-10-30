package me.TechsCode.UltraPermissions.base.storage;

import java.util.Objects;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.misc.Getter;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonElement;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonNull;

public class Stored<T extends Storable> {
   private String key;
   private Getter<Storage<T>> storageGetter;
   private T cache;

   public static <T extends Storable> Stored<T> empty() {
      return new Stored<>(JsonNull.INSTANCE, null);
   }

   public Stored(String var1, Getter<Storage<T>> var2) {
      super();
      this.key = var1;
      this.storageGetter = var2;
   }

   public Stored(JsonElement var1, Getter<Storage<T>> var2) {
      super();
      this.key = var1 != null && !var1.isJsonNull() ? var1.getAsString() : null;
      this.storageGetter = var2;
   }

   public Optional<T> get() {
      if (this.cache != null) {
         if (this.cache.isLinkedToStorage()) {
            return Optional.of(this.cache);
         }

         this.cache = null;
      }

      if (this.key == null) {
         return Optional.empty();
      }

      if (this.storageGetter == null) {
         return Optional.empty();
      }

      Storage<T> var1 = this.storageGetter.get();
      if (var1 == null) {
         return Optional.empty();
      }

      Optional<T> var2 = var1.get().stream().filter(var1x -> var1x.getKey().equals(this.key)).findFirst();
      var2.ifPresent(var1x -> this.cache = (T)var1x);
      return var2;
   }

   public boolean isPresent() {
      return this.get().isPresent();
   }

   public String getKey() {
      return this.key;
   }

   public T orElse(T var1) {
      Optional var2 = this.get();
      return (T)var2.orElse(var1);
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         Stored var2 = (Stored)var1;
         return Objects.equals(this.key, var2.key);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.key);
   }
}
