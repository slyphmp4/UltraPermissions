package me.TechsCode.UltraPermissions.base.reflection.indexer;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClasspathIndexer {
   public ClasspathIndexer() {
      super();
   }

   public static NodeList index() {
      NodeList var0 = new NodeList();

      try {
         CodeSource var1 = ClasspathIndexer.class.getProtectionDomain().getCodeSource();
         if (var1 != null) {
            URL var2 = var1.getLocation();
            ZipInputStream var3 = new ZipInputStream(var2.openStream());

            while (true) {
               ZipEntry var4 = var3.getNextEntry();
               if (var4 == null) {
                  break;
               }

               var0.add(new Node(var4.getName()));
            }
         }
      } catch (IOException var5) {
         var5.printStackTrace();
      }

      return var0;
   }
}
