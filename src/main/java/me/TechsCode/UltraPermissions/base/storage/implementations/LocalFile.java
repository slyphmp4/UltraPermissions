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
      this.saveChange(var1, null, var2);
   }

   @Override
   public void create(String var1, JsonObject var2, WriteCallback var3) {
      this.update(var1, var2, var3);
   }

   @Override
   public void update(String var1, JsonObject var2, WriteCallback var3) {
      String encoded = Base64.encodeBase64String(var2.toString().getBytes(StandardCharsets.UTF_8));
      this.saveChange(var1, encoded, var3);
   }

   private void saveChange(String key, String value, WriteCallback callback) {
      synchronized (this.configuration) {
         Object previous = this.configuration.get(key);
         try {
            this.configuration.set(key, value);
            this.configuration.save();
         } catch (RuntimeException failure) {
            this.configuration.set(key, previous);
            callback.onFailure(failure);
            return;
         }
         callback.onSuccess();
      }
   }

   @Override
   public void read(String var1, ReadCallback var2) {
      synchronized (this.configuration) {
         HashMap<String, JsonObject> rows = new HashMap<>();
         JsonParser parser = new JsonParser();
         try {
            for (String key : this.configuration.getKeys(false)) {
               String value = this.configuration.getString(key);
               if (value == null) {
                  throw new IllegalStateException("Missing value for key " + key);
               }
               if (Base64.isBase64(value)) {
                  value = new String(Base64.decodeBase64(value), StandardCharsets.UTF_8);
               }
               var json = parser.parse(value);
               if (json == null || !json.isJsonObject()) {
                  throw new IllegalStateException("Invalid storage value for key " + key);
               }
               rows.put(key, json.getAsJsonObject());
            }
         } catch (RuntimeException malformed) {
            throw new IllegalStateException("Cannot load storage " + this.file + "; original data was not changed", malformed);
         }
         var2.onSuccess(rows);
      }
   }
}
