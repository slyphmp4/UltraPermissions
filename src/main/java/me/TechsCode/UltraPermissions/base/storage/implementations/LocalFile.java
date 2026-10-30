package me.TechsCode.UltraPermissions.base.storage.implementations;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.fileconf.FileConfiguration;
import me.TechsCode.UltraPermissions.base.storage.ReadCallback;
import me.TechsCode.UltraPermissions.base.storage.StorageImplementation;
import me.TechsCode.UltraPermissions.base.storage.WriteCallback;
import me.TechsCode.UltraPermissions.dependencies.commons.codec.binary.Base64;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParser;

public class LocalFile extends StorageImplementation {
   private File file;
   private FileConfiguration configuration;

   public LocalFile(TechPlugin var1, String var2) {
      super(var1, var2, false);
      this.file = new File(var1.getPluginFolder().getAbsolutePath() + "/" + var2 + ".json");
      this.configuration = new FileConfiguration(var1, this.file);
   }

   @Override
   public void destroy(String var1, WriteCallback var2) {
      synchronized (this.configuration) {
         this.configuration.set(var1, null);
         this.configuration.save();
         var2.onSuccess();
      }
   }

   @Override
   public void create(String var1, JsonObject var2, WriteCallback var3) {
      this.update(var1, var2, var3);
   }

   @Override
   public void update(String var1, JsonObject var2, WriteCallback var3) {
      synchronized (this.configuration) {
         String var5 = Base64.encodeBase64String(var2.toString().getBytes(StandardCharsets.UTF_8));
         this.configuration.set(var1, var5);
         this.configuration.save();
         var3.onSuccess();
      }
   }

   @Override
   public void read(String var1, ReadCallback var2) {
      synchronized (this.configuration) {
         HashMap var4 = new HashMap();
         JsonParser var5 = new JsonParser();

         for (String var7 : this.configuration.getKeys(false)) {
            String var8 = this.configuration.getString(var7);
            if (Base64.isBase64(var8)) {
               var8 = new String(Base64.decodeBase64(var8), StandardCharsets.UTF_8);
            }

            JsonObject var9 = (JsonObject)var5.parse(var8);
            var4.put(var7, var9);
         }

         var2.onSuccess(var4);
      }
   }
}
