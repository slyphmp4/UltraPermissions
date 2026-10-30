package me.TechsCode.UltraPermissions.base.command;

public class ArgumentValue<T> {
   private final String raw;
   private final T t;
   private final Validity validity;

   public static <T> ArgumentValue<T> empty(String var0, EmptyReason var1) {
      return new ArgumentValue<>(var0, null, Validity.valueOf(var1.name()));
   }

   public static <T> ArgumentValue<T> of(String var0, T var1) {
      return new ArgumentValue<>(var0, (T)var1, Validity.VALID);
   }

   private ArgumentValue(String var1, T var2, Validity var3) {
      super();
      this.raw = var1;
      this.t = (T)var2;
      this.validity = var3;
   }

   public String getRaw() {
      return this.raw;
   }

   public T get() {
      return this.t;
   }

   public Validity getValidity() {
      return this.validity;
   }

   public boolean isValid() {
      return this.validity == Validity.VALID;
   }

   public boolean isMatch() {
      return this.validity != Validity.NO_MATCH;
   }
}
