package me.TechsCode.UltraPermissions.base.loader;

import java.lang.reflect.Modifier;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.reflection.indexer.ClasspathIndexer;
import me.TechsCode.UltraPermissions.base.reflection.indexer.Node;

public class LoaderUtil {
   public LoaderUtil() {
      super();
   }

   public static Optional<Class<?>> getMainClass(Class<? extends TechPlugin<?>> var0, ClassLoader var1) {
      Optional var2 = ClasspathIndexer.index().classes().stream().filter(var2x -> {
         if (!var2x.getClassName().startsWith("me.TechsCode")) {
            return false;
         }

         try {
            Class var3 = var1.loadClass(var2x.getClassName());
            return var0.isAssignableFrom(var3) && !Modifier.isAbstract(var3.getModifiers());
         } catch (NoClassDefFoundError | ClassNotFoundException var4) {
            return false;
         }
      }).findFirst();
      return var2.isPresent() ? ((Node)var2.get()).getAsClass() : Optional.empty();
   }
}
