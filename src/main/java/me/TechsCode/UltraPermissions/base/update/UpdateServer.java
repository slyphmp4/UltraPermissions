package me.TechsCode.UltraPermissions.base.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.update.customRequests.HttpGetWithBody;
import me.TechsCode.UltraPermissions.base.update.responses.CreateUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.responses.GetPluginsResponse;
import me.TechsCode.UltraPermissions.base.update.responses.GetUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.responses.IsAvailableResponse;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;
import me.TechsCode.UltraPermissions.base.update.responses.UpdateUpdateRequestResponse;
import me.TechsCode.UltraPermissions.dependencies.gson.Gson;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class UpdateServer {
   private final String updateServerBaseUrl = "https://update.techscode.com";
   private final String updateServerApiToken = System.getProperty("ultrapermissions.updateApiToken", "");
   private CloseableHttpClient httpClient;
   private boolean hasJarDownloadFailed = false;

   public UpdateServer() {
      super();
   }

   private void close() throws IOException {
      if (this.httpClient != null) this.httpClient.close();
   }

   private synchronized CloseableHttpClient httpClient() {
      if (this.httpClient == null) {
         this.httpClient = org.apache.http.impl.client.HttpClients.custom()
            .setDefaultRequestConfig(org.apache.http.client.config.RequestConfig.custom()
               .setConnectTimeout(5000).setSocketTimeout(5000).setConnectionRequestTimeout(2000).build())
            .build();
      }
      return this.httpClient;
   }

   public JsonObject getRequest(String var1, JsonObject var2) {
      if (this.updateServerApiToken.isBlank()) return null;
      try {
         HttpGetWithBody var3 = new HttpGetWithBody("https://update.techscode.com" + var1);
         var3.addHeader("Content-Type", "application/json");
         var3.addHeader("Authorization", this.updateServerApiToken);
         StringEntity var4 = new StringEntity(var2.toString(), ContentType.APPLICATION_JSON);
         var3.setEntity(var4);

         JsonObject var13;
         try (CloseableHttpResponse var5 = this.httpClient().execute(var3)) {
            HttpEntity var6 = var5.getEntity();
            JsonObject var7 = null;
            if (var6 != null) {
               Gson var8 = new Gson();
               String var9 = EntityUtils.toString(var6);
               var7 = var8.fromJson(var9, JsonObject.class);
            }

            var13 = var7;
         }

         return var13;
      } catch (Exception var12) {
         return null;
      }
   }

   public JsonObject postRequest(String var1, JsonObject var2) {
      if (this.updateServerApiToken.isBlank()) return null;
      try {
         HttpPost var3 = new HttpPost("https://update.techscode.com" + var1);
         var3.addHeader("Content-Type", "application/json");
         var3.addHeader("Authorization", this.updateServerApiToken);
         StringEntity var4 = new StringEntity(var2.toString(), ContentType.APPLICATION_JSON);
         var3.setEntity(var4);

         JsonObject var13;
         try (CloseableHttpResponse var5 = this.httpClient().execute(var3)) {
            HttpEntity var6 = var5.getEntity();
            JsonObject var7 = null;
            if (var6 != null) {
               Gson var8 = new Gson();
               String var9 = EntityUtils.toString(var6);
               var7 = var8.fromJson(var9, JsonObject.class);
            }

            var13 = var7;
         }

         return var13;
      } catch (Exception var12) {
         return null;
      }
   }

   public JsonObject patchRequest(String var1, JsonObject var2) {
      if (this.updateServerApiToken.isBlank()) return null;
      try {
         HttpPatch var3 = new HttpPatch("https://update.techscode.com" + var1);
         var3.addHeader("Content-Type", "application/json");
         var3.addHeader("Authorization", this.updateServerApiToken);
         StringEntity var4 = new StringEntity(var2.toString(), ContentType.APPLICATION_JSON);
         var3.setEntity(var4);

         JsonObject var13;
         try (CloseableHttpResponse var5 = this.httpClient().execute(var3)) {
            HttpEntity var6 = var5.getEntity();
            JsonObject var7 = null;
            if (var6 != null) {
               Gson var8 = new Gson();
               String var9 = EntityUtils.toString(var6);
               var7 = var8.fromJson(var9, JsonObject.class);
            }

            var13 = var7;
         }

         return var13;
      } catch (Exception var12) {
         return null;
      }
   }

   public IsAvailableResponse isAvailable() {
      JsonObject var1 = new JsonObject();
      JsonObject var2 = this.getRequest("/api/status", var1);
      if (var2 == null) {
         return new IsAvailableResponse(500);
      } else {
         int var3 = var2.has("code") ? var2.get("code").getAsInt() : 500;
         return new IsAvailableResponse(var3);
      }
   }

   public IsUpToDateResponse isUpToDate(TechPlugin<?> var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("plugin_name", var1.getName().toLowerCase());
      var2.addProperty("current_version", var1.getVersion());
      JsonObject var3 = this.getRequest("/api/plugin/version-check", var2);
      if (var3 == null) {
         return new IsUpToDateResponse(500, false, null, "Invalid Response");
      } else {
         int var4 = var3.has("code") ? var3.get("code").getAsInt() : 500;
         boolean var5 = var3.has("is_up_to_date") && var3.get("is_up_to_date").getAsBoolean();
         String var6 = var3.has("latest_version") ? var3.get("latest_version").getAsString() : null;
         String var7 = var3.has("message") ? var3.get("message").getAsString() : null;
         return new IsUpToDateResponse(var4, var5, var6, var7);
      }
   }

   public GetPluginsResponse getPlugins(String var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("update_token", var1);
      JsonObject var3 = this.getRequest("/api/get-plugins", var2);
      if (var3 == null) {
         return new GetPluginsResponse(500, null, "Invalid Response");
      } else {
         int var4 = var3.has("code") ? var3.get("code").getAsInt() : 500;
         JsonObject var5 = var3.has("data") ? var3.get("data").getAsJsonObject() : null;
         String var6 = var3.has("message") ? var3.get("message").getAsString() : null;
         return new GetPluginsResponse(var4, var5, var6);
      }
   }

   public CreateUpdateRequestResponse createUpdateRequest(TechPlugin<?> var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("plugin_name", var1.getName().toLowerCase());
      var2.addProperty("current_version", var1.getVersion());
      JsonObject var3 = this.postRequest("/api/plugin/request-update", var2);
      if (var3 == null) {
         return new CreateUpdateRequestResponse(500, null, "Invalid Response");
      } else {
         int var4 = var3.has("code") ? var3.get("code").getAsInt() : 500;
         String var5 = var3.has("update_token") ? var3.get("update_token").getAsString() : null;
         String var6 = var3.has("message") ? var3.get("message").getAsString() : null;
         return new CreateUpdateRequestResponse(var4, var5, var6);
      }
   }

   public GetUpdateRequestResponse getUpdateRequest(String var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("update_token", var1);
      JsonObject var3 = this.getRequest("/api/plugin/request-update", var2);
      if (var3 == null) {
         return new GetUpdateRequestResponse(500, null, "Invalid Response");
      } else {
         int var4 = var3.has("code") ? var3.get("code").getAsInt() : 500;
         JsonObject var5 = var3.has("update_request") ? var3.get("update_request").getAsJsonObject() : null;
         String var6 = var3.has("message") ? var3.get("message").getAsString() : null;
         UpdateRequest var7 = null;
         if (var5 != null) {
            var7 = new UpdateRequest(var5);
         }

         return new GetUpdateRequestResponse(var4, var7, var6);
      }
   }

   public UpdateUpdateRequestResponse updateUpdateRequest(String var1, TechPlugin<?> var2, String var3) {
      JsonObject var4 = new JsonObject();
      var4.addProperty("update_token", var1);
      var4.addProperty("plugin_name", var2.getName().toLowerCase());
      var4.addProperty("current_version", var2.getVersion());
      var4.addProperty("update_to", var3);
      JsonObject var5 = this.patchRequest("/api/plugin/request-update", var4);
      if (var5 == null) {
         return new UpdateUpdateRequestResponse(500, null, "Invalid Response");
      } else {
         int var6 = var5.has("code") ? var5.get("code").getAsInt() : 500;
         JsonObject var7 = var5.has("update_request") ? var5.get("update_request").getAsJsonObject() : null;
         String var8 = var5.has("message") ? var5.get("message").getAsString() : null;
         UpdateRequest var9 = null;
         if (var7 != null) {
            var9 = new UpdateRequest(var7);
         }

         return new UpdateUpdateRequestResponse(var6, var9, var8);
      }
   }

   public Thread downloadJar(TechPlugin<?> plugin, String token) {
      this.hasJarDownloadFailed = true;
      plugin.log("Automatic JAR download is disabled. Install an independently verified update manually.");
      // Callers may safely join this unstarted thread; no network request/task is started.
      return new Thread();
   }

   public boolean hasJarDownloadFailed() {
      return this.hasJarDownloadFailed;
   }

   public String getAuthorizeUrl(String var1) {
      return "https://update.techscode.com/authorize/" + var1;
   }

   public void destroy() {
      try {
         this.close();
      } catch (IOException var2) {
      }
   }
}
