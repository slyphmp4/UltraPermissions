package me.TechsCode.UltraPermissions.base.legacy;

import java.util.HashMap;
import java.util.Map.Entry;

public class Placeholders extends HashMap<String, String> {
   public Placeholders() {
      super();
   }

   public String apply(String var1) {
      String var2 = var1;

      for (Entry var4 : this.entrySet()) {
         var2 = var2.replace((CharSequence)var4.getKey(), (CharSequence)var4.getValue());
      }

      return var2;
   }

   public static Placeholders c(String var0, String var1) {
      Placeholders var2 = new Placeholders();
      var2.put(var0, var1);
      return var2;
   }
}
