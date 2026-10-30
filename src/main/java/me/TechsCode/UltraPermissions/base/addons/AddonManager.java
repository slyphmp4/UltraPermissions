package me.TechsCode.UltraPermissions.base.addons;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudAddon;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudDataFetcher;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudFileType;
import me.TechsCode.UltraPermissions.dependencies.commons.io.FileUtils;

public class AddonManager {
   private final AddonLoader loader;
   private final CloudDataFetcher cloud;

   public AddonManager(final String var1, final CloudFileType var2, AddonLoader var3, SpigotTechPlugin var4) {
      super();
      this.loader = var3.setPlugin(var4).setFileType(var2).start();
      this.cloud = new CloudDataFetcher() {
         @Override
         public String getUrl() {
            return var1;
         }

         @Override
         public CloudFileType getFileType() {
            return var2;
         }
      };
      var4.addDisableHook(this::onDisable);
   }

   public void onDisable() {
      this.loader.onDisable();
      this.cloud.onDisable();
   }

   public boolean isCloudOnline() {
      return this.cloud.isOnline();
   }

   public List<CloudAddon> getCloudAddons() {
      return this.cloud.getAddons();
   }

   public List<InstalledAddon> getInstalledAddons() {
      return this.loader.getInstalledAddons();
   }

   public AddonLoader getLoader() {
      return this.loader;
   }

   public CloudDataFetcher getCloud() {
      return this.cloud;
   }

   public void download(CloudAddon var1) {
      try {
         File var2 = new File(this.loader.getInstalledAddonsFolder().getAbsolutePath() + "/" + var1.getName() + "." + this.loader.getFileType().getExtension());
         var2.delete();
         FileUtils.copyURLToFile(new URL(var1.getUrl()), var2);
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }
}
