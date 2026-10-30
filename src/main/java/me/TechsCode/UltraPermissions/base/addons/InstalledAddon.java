package me.TechsCode.UltraPermissions.base.addons;

import java.io.File;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudAddon;

public class InstalledAddon {
   private final SpigotTechPlugin plugin;
   private final File addonFile;
   private final String name;
   private final String version;
   private final String author;
   private final String depend;
   private final long size;
   private final boolean loaded;

   public static InstalledAddon newLoadedAddon(SpigotTechPlugin var0, File var1, String var2, String var3, String var4, String var5) {
      return new InstalledAddon(var0, var1, var2, var3, var4, var5, true);
   }

   public static InstalledAddon newNotLoadedAddon(SpigotTechPlugin var0, File var1, String var2, String var3, String var4, String var5) {
      return new InstalledAddon(var0, var1, var2, var3, var4, var5, false);
   }

   private InstalledAddon(SpigotTechPlugin var1, File var2, String var3, String var4, String var5, String var6, boolean var7) {
      super();
      this.plugin = var1;
      this.addonFile = var2;
      this.name = var3;
      this.version = var4;
      this.author = var5;
      this.depend = var6;
      this.loaded = var7;
      this.size = var2.length();
   }

   public File getAddonFile() {
      return this.addonFile;
   }

   public String getName() {
      return this.name;
   }

   public String getVersion() {
      return this.version;
   }

   public String getAuthor() {
      return this.author;
   }

   public String getDepend() {
      return this.depend;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public long getSize() {
      return this.size;
   }

   public Optional<CloudAddon> getCloudAddon() {
      return this.plugin.getAddonManager().getCloudAddons().stream().filter(var1 -> var1.getName().equals(this.name)).findFirst();
   }

   public boolean uninstall() {
      return this.addonFile.exists() ? this.addonFile.delete() : false;
   }

   public boolean isOutOfDate() {
      Optional var1 = this.getCloudAddon();
      return var1.isPresent() && !((CloudAddon)var1.get()).getVersion().equals(this.version);
   }
}
