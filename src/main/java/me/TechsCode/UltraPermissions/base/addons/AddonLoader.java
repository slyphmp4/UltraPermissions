package me.TechsCode.UltraPermissions.base.addons;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.addons.cloud.CloudFileType;

public abstract class AddonLoader {
   protected SpigotTechPlugin plugin;
   protected CloudFileType fileType;
   protected File installedAddonsFolder;
   protected final HashMap<File, InstalledAddon> loaded = new HashMap<>();

   public AddonLoader() {
      super();
      this.installedAddonsFolder = null;
   }

   public AddonLoader setFileType(CloudFileType var1) {
      if (this.fileType != null) {
         throw new IllegalStateException("FileType is already set, you cannot set it twice.");
      }

      this.fileType = var1;
      return this;
   }

   public AddonLoader setPlugin(SpigotTechPlugin var1) {
      if (this.plugin != null) {
         throw new IllegalStateException("Plugin is already set, you cannot set it twice.");
      }

      this.installedAddonsFolder = new File(var1.getPluginFolder(), "Addons");
      this.plugin = var1;
      return this;
   }

   public void onDisable() {
      new ArrayList<>(this.loaded.values()).forEach(this::unload);
   }

   public AddonLoader start() {
      if (!this.installedAddonsFolder.exists()) {
         this.installedAddonsFolder.mkdir();
      }

      this.plugin.getScheduler().runTaskTimer(this::sync, 20L, 20L);
      return this;
   }

   protected void sync() {
      List<File> var1 = this.getAddonJarsInFolder();
      var1.stream()
         .filter(var1x -> var1x.getName().endsWith("." + this.fileType.getExtension()))
         .filter(
            var1x -> this.loaded.values().stream().noneMatch(var1xx -> var1xx.getAddonFile().getName().equals(var1x.getName()))
               || this.loaded
                  .values()
                  .stream()
                  .anyMatch(var1xx -> var1xx.getSize() != var1x.length() && var1xx.getAddonFile().getName().equals(var1x.getName()))
         )
         .forEach(this::load);
      new ArrayList<>(this.loaded.values()).stream().filter(var1x -> !var1.contains(var1x.getAddonFile())).forEach(this::unload);
   }

   protected abstract void load(File var1);

   protected abstract void unload(InstalledAddon var1);

   protected List<File> getAddonJarsInFolder() {
      return Arrays.asList(Objects.requireNonNull(this.installedAddonsFolder.listFiles()));
   }

   public File getInstalledAddonsFolder() {
      return this.installedAddonsFolder;
   }

   public CloudFileType getFileType() {
      return this.fileType;
   }

   public List<InstalledAddon> getInstalledAddons() {
      return new ArrayList<>(this.loaded.values());
   }
}
