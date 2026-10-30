package me.TechsCode.UltraPermissions.base.loader;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.TechPlugin;

public class Loader {
   private TechPlugin<?> techPlugin;

   public Loader(Object var1, Class<? extends TechPlugin<?>> var2, ClassLoader var3) {
      super();
      Optional var4 = LoaderUtil.getMainClass(var2, var3);
      if (var4.isPresent()) {
         try {
            this.techPlugin = (TechPlugin<?>)((Class)var4.get()).getDeclaredConstructors()[0].newInstance(var1);
         } catch (InstantiationException | IllegalAccessException | InvocationTargetException var6) {
            var6.printStackTrace();
         }
      }
   }

   public void unload() {
      if (this.techPlugin != null) {
         this.techPlugin.disable();
      }
   }
}
