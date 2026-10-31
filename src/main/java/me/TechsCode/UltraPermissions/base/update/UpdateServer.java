package me.TechsCode.UltraPermissions.base.update;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import me.TechsCode.UltraPermissions.base.TechPlugin;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class UpdateServer {
   public static final String RELEASES_URL = "https://github.com/slyphmp4/UltraPermissions/releases";
   private static final String RELEASES_API = "https://api.github.com/repos/slyphmp4/UltraPermissions/releases/latest";
   private static final Pattern VERSION = Pattern.compile("^[vV]?[0-9]+(?:\\.[0-9]+){1,3}(?:-[A-Za-z0-9.-]+)?(?:\\+[A-Za-z0-9.-]+)?$");
   private static final long SUCCESS_CACHE_MS = TimeUnit.MINUTES.toMillis(30);
   private static final long FAILURE_CACHE_MS = TimeUnit.MINUTES.toMillis(2);
   private static volatile LatestRelease cached = new LatestRelease(503, null, 0L);
   private CloseableHttpClient httpClient;

   private synchronized CloseableHttpClient httpClient() {
      if (this.httpClient == null) {
         this.httpClient = HttpClients.custom().setDefaultRequestConfig(
            org.apache.http.client.config.RequestConfig.custom()
               .setConnectTimeout(5000).setSocketTimeout(5000).setConnectionRequestTimeout(2000).build()
         ).build();
      }
      return this.httpClient;
   }

   public IsAvailableResponse isAvailable() {
      return new IsAvailableResponse(this.latestRelease().status);
   }

   public IsUpToDateResponse isUpToDate(TechPlugin<?> plugin) {
      if (!"UltraPermissions".equalsIgnoreCase(plugin.getName())) {
         return new IsUpToDateResponse(404, false, null, "Unknown plugin");
      }
      LatestRelease release = this.latestRelease();
      if (release.status != 200 || release.version == null) {
         return new IsUpToDateResponse(release.status, false, null, "GitHub Releases unavailable");
      }
      boolean newer = isNewerVersion(release.version, plugin.getVersion());
      return new IsUpToDateResponse(200, !newer, release.version, "GitHub Releases");
   }

   private LatestRelease latestRelease() {
      LatestRelease result = cached;
      long now = System.currentTimeMillis();
      long lifetime = result.status == 200 ? SUCCESS_CACHE_MS : FAILURE_CACHE_MS;
      if (now - result.checkedAt < lifetime) {
         return result;
      }
      synchronized (UpdateServer.class) {
         result = cached;
         now = System.currentTimeMillis();
         lifetime = result.status == 200 ? SUCCESS_CACHE_MS : FAILURE_CACHE_MS;
         if (now - result.checkedAt < lifetime) {
            return result;
         }
         int status = 503;
         String version = null;
         try {
            HttpGet request = new HttpGet(RELEASES_API);
            request.addHeader("Accept", "application/vnd.github+json");
            request.addHeader("User-Agent", "UltraPermissions-GitHub-Updater");
            try (CloseableHttpResponse response = this.httpClient().execute(request)) {
               status = response.getStatusLine().getStatusCode();
               HttpEntity entity = response.getEntity();
               if (status == 200 && entity != null) {
                  JsonObject json = new Gson().fromJson(EntityUtils.toString(entity), JsonObject.class);
                  if (json != null && json.has("tag_name") && json.get("tag_name").isJsonPrimitive()
                     && (!json.has("draft") || !json.get("draft").getAsBoolean())
                     && (!json.has("prerelease") || !json.get("prerelease").getAsBoolean())) {
                     String tag = json.get("tag_name").getAsString();
                     if (VERSION.matcher(tag).matches()) {
                        version = tag.replaceFirst("^[vV]", "");
                     }
                  }
                  if (version == null) {
                     status = 502;
                  }
               }
            }
         } catch (Exception ignored) {
            status = 503;
         }
         cached = new LatestRelease(status, version, System.currentTimeMillis());
         return cached;
      }
   }

   /**
    * Compare a published release tag with a local version without treating v5.6.10
    * and 5.6.10 as different. A prerelease is older than the same stable version.
    */
   public static boolean isNewerVersion(String latest, String installed) {
      if (latest == null || installed == null
         || !VERSION.matcher(latest).matches() || !VERSION.matcher(installed).matches()) {
         return false;
      }
      String[] left = latest.replaceFirst("^[vV]", "").split("\\+", 2)[0].split("-", 2);
      String[] right = installed.replaceFirst("^[vV]", "").split("\\+", 2)[0].split("-", 2);
      String[] leftNumbers = left[0].split("\\.");
      String[] rightNumbers = right[0].split("\\.");
      try {
         for (int i = 0; i < Math.max(leftNumbers.length, rightNumbers.length); i++) {
            long a = i < leftNumbers.length ? Long.parseLong(leftNumbers[i]) : 0L;
            long b = i < rightNumbers.length ? Long.parseLong(rightNumbers[i]) : 0L;
            if (a != b) {
               return a > b;
            }
         }
         if (left.length == 1 && right.length > 1) return true;
         if (left.length > 1 && right.length == 1) return false;
         if (left.length == 1) return false;
         String[] a = left[1].split("\\.");
         String[] b = right[1].split("\\.");
         for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (a[i].equals(b[i])) continue;
            boolean aNumeric = a[i].matches("[0-9]+");
            boolean bNumeric = b[i].matches("[0-9]+");
            if (aNumeric && bNumeric) return Long.parseLong(a[i]) > Long.parseLong(b[i]);
            if (aNumeric != bNumeric) return !aNumeric;
            return a[i].compareTo(b[i]) > 0;
         }
         return a.length > b.length;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   // The former purchase/Discord update backend is intentionally retired.
   // Do not download executable JARs or restart servers from a remote response.
   @Deprecated public JsonObject getRequest(String path, JsonObject payload) { return null; }
   @Deprecated public JsonObject postRequest(String path, JsonObject payload) { return null; }
   @Deprecated public JsonObject patchRequest(String path, JsonObject payload) { return null; }
   @Deprecated public GetPluginsResponse getPlugins(String token) { return new GetPluginsResponse(410); }
   @Deprecated public CreateUpdateRequestResponse createUpdateRequest(TechPlugin<?> plugin) { return new CreateUpdateRequestResponse(410); }
   @Deprecated public GetUpdateRequestResponse getUpdateRequest(String token) { return new GetUpdateRequestResponse(410); }
   @Deprecated public UpdateUpdateRequestResponse updateUpdateRequest(String token, TechPlugin<?> plugin, String version) {
      return new UpdateUpdateRequestResponse(410);
   }
   @Deprecated public String getAuthorizeUrl(String token) { return RELEASES_URL; }
   @Deprecated public Thread downloadJar(TechPlugin<?> plugin, String token) {
      plugin.log("Automatic update installation is disabled. Download the verified JAR from GitHub Releases.");
      return new Thread();
   }
   @Deprecated public boolean hasJarDownloadFailed() { return true; }

   public void destroy() {
      if (this.httpClient != null) {
         try {
            this.httpClient.close();
         } catch (IOException ignored) {
         }
      }
   }

   private static final class LatestRelease {
      private final int status;
      private final String version;
      private final long checkedAt;

      private LatestRelease(int status, String version, long checkedAt) {
         this.status = status;
         this.version = version;
         this.checkedAt = checkedAt;
      }
   }
}
