package me.TechsCode.UltraPermissions.base.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;

public class LocalRepository {
   private TechPlugin plugin;
   private File folder;

   public LocalRepository(TechPlugin var1) {
      super();
      this.plugin = var1;
      this.folder = new File(var1.getPluginFolder().getAbsolutePath() + "/Maven");
   }

   public File getIfPresent(Dependency var1) {
      File var2 = new File(this.folder.getAbsolutePath() + "/" + var1.toURL());
      return var2.exists() ? var2 : null;
   }

   public boolean load(Dependency var1, Collection<String> var2) {
      File var3 = new File(this.folder.getAbsolutePath() + "/" + var1.toURL());

      for (String var5 : var2) {
         String var6 = var5 + "/" + var1.toURL();

         try {
            FileUtils.copyURLToFile(new URL(var6), var3);
            return true;
         } catch (IOException var8) {
         }
      }

      return false;
   }
}
