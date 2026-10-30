package me.TechsCode.UltraPermissions.base.source;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;

public class JarClassLoader {
   public static Method ADD_URL_METHOD;
   public final URLClassLoader classLoader;

   public JarClassLoader(SpigotTechPlugin var1) {
      super();
      ClassLoader var2 = var1.getBootstrap().getClass().getClassLoader();
      this.classLoader = (URLClassLoader)var2;
   }

   public void load(File var1) {
      try {
         ADD_URL_METHOD.invoke(this.classLoader, var1.toPath().toUri().toURL());
      } catch (IllegalAccessException | InvocationTargetException | MalformedURLException var3) {
         var3.printStackTrace();
      }
   }

   static {
      try {
         ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
         ADD_URL_METHOD.setAccessible(true);
      } catch (Exception var1) {
      }
   }
}
