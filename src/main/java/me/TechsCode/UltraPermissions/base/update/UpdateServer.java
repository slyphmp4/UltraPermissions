package me.TechsCode.UltraPermissions.base.update;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.regex.Pattern;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.security.BoundedInput;
import me.TechsCode.UltraPermissions.base.update.responses.CreateUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.responses.GetPluginsResponse;
import me.TechsCode.UltraPermissions.base.update.responses.GetUpdateRequestResponse;
import me.TechsCode.UltraPermissions.base.update.responses.IsAvailableResponse;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;
import me.TechsCode.UltraPermissions.base.update.responses.UpdateUpdateRequestResponse;
import me.TechsCode.UltraPermissions.dependencies.gson.Gson;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonElement;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonArray;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

/** Read-only GitHub Releases metadata. Never downloads or executes an update JAR. */
public class UpdateServer {
   public static final String RELEASES_URL = "https://github.com/slyphmp4/UltraPermissions/releases";
   private static final URI RELEASES_API = URI.create("https://api.github.com/repos/slyphmp4/UltraPermissions/releases/latest");
   private static final Pattern VERSION = Pattern.compile(
      "^[vV]?[0-9]+(?:\\.[0-9]+){1,3}(?:-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$"
   );
   private static final int MAX_RELEASE_BYTES = 512 * 1024;
   private static final long MAX_ASSET_BYTES = 64L * 1024 * 1024;
   private static final Pattern SHA256 = Pattern.compile("sha256:[0-9a-fA-F]{64}");
   private static final long SUCCESS_CACHE_MS = TimeUnit.MINUTES.toMillis(30);
   private static final long FAILURE_CACHE_MS = TimeUnit.MINUTES.toMillis(2);
   private static final long RATE_LIMIT_FALLBACK_MS = TimeUnit.HOURS.toMillis(1);
   private static final long MAX_RATE_LIMIT_MS = TimeUnit.HOURS.toMillis(24);
   private static final HttpClient CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .followRedirects(HttpClient.Redirect.NEVER)
      .build();
   private static volatile LatestRelease cached = new LatestRelease(503, null, null, null, 0L, 0L, "Not checked yet");

   public IsAvailableResponse isAvailable() {
      return new IsAvailableResponse(this.latestRelease().status);
   }

   public IsUpToDateResponse isUpToDate(TechPlugin<?> plugin) {
      return this.isUpToDate(plugin.getName(), plugin.getVersion());
   }

   public IsUpToDateResponse isUpToDate(String pluginName, String installedVersion) {
      if (!"UltraPermissions".equalsIgnoreCase(pluginName)) {
         return new IsUpToDateResponse(404, false, null, "Unknown plugin");
      }
      LatestRelease release = this.latestRelease();
      if (release.status != 200 || release.version == null) {
         return new IsUpToDateResponse(release.status, false, null, "GitHub Releases unavailable: " + release.detail);
      }
      boolean newer = isNewerVersion(release.version, installedVersion);
      String releasePage = RELEASES_URL + "/tag/" + release.tagName;
      return new IsUpToDateResponse(200, !newer, release.version, "GitHub Releases", releasePage, release.asset);
   }

   private LatestRelease latestRelease() {
      LatestRelease result = cached;
      long now = System.currentTimeMillis();
      if (now >= result.checkedAt && now < result.nextCheckAt) return result;

      // One asynchronous caller performs the network request while concurrent join
      // checks share the cached response. Never invoke this from a Bukkit main thread.
      synchronized (UpdateServer.class) {
         result = cached;
         now = System.currentTimeMillis();
         if (now >= result.checkedAt && now < result.nextCheckAt) return result;
         cached = this.fetchRelease(now);
         return cached;
      }
   }

   private LatestRelease fetchRelease(long startedAt) {
      int status = 503;
      String version = null;
      String tag = null;
      ReleaseAsset asset = null;
      long ttl = FAILURE_CACHE_MS;
      String detail = "HTTP response " + status;
      try {
         HttpRequest request = HttpRequest.newBuilder(RELEASES_API)
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "UltraPermissions-GitHub-Updater")
            .GET()
            .build();
         HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
         status = response.statusCode();
         detail = "HTTP response " + status;
         try (InputStream input = response.body()) {
            if (status == 200) {
               String body = new String(BoundedInput.read(input, MAX_RELEASE_BYTES), StandardCharsets.UTF_8);
               tag = publishedStableTag(body);
               if (tag != null) {
                  version = tag.replaceFirst("^[vV]", "");
                  asset = publishedVerifiedAsset(body, tag);
                  // GitHub publishes the release page before CI finishes uploading
                  // its JAR. Recheck soon instead of caching that incomplete state
                  // for half an hour and reporting a persistent false failure.
                  ttl = asset == null ? FAILURE_CACHE_MS : SUCCESS_CACHE_MS;
               } else {
                  status = 502;
                  detail = "Published GitHub Release tag is missing or invalid";
               }
            } else if (status == 404) {
               ttl = SUCCESS_CACHE_MS;
            } else if (status == 403 || status == 429) {
               ttl = retryDelayMillis(
                  response.headers().firstValue("Retry-After").orElse(null),
                  response.headers().firstValue("X-RateLimit-Reset").orElse(null),
                  System.currentTimeMillis()
               );
            }
         }
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
         status = 503;
         detail = "Update check interrupted";
      } catch (IOException | RuntimeException failed) {
         status = 503;
         // Avoid logging raw exceptions: HttpClient error messages may contain URLs.
         detail = "GitHub metadata request failed (" + failed.getClass().getSimpleName() + ")";
      }
      long finishedAt = System.currentTimeMillis();
      return new LatestRelease(status, version, tag, asset, finishedAt, finishedAt + ttl, detail);
   }

   /** Require a published, non-prerelease GitHub release and a plain numeric stable tag. */
   private static String publishedStableTag(String body) {
      try {
         JsonObject data = new Gson().fromJson(body, JsonObject.class);
         if (data == null || !data.has("tag_name") || !data.has("draft") || !data.has("prerelease")
            || !data.has("published_at")) return null;
         JsonElement name = data.get("tag_name");
         JsonElement draft = data.get("draft");
         JsonElement pre = data.get("prerelease");
         if (!name.isJsonPrimitive() || !name.getAsJsonPrimitive().isString()
            || !draft.isJsonPrimitive() || !draft.getAsJsonPrimitive().isBoolean()
            || !pre.isJsonPrimitive() || !pre.getAsJsonPrimitive().isBoolean()
            || data.get("published_at").isJsonNull()
            || draft.getAsBoolean() || pre.getAsBoolean()) return null;
         String tag = name.getAsString();
         // SemVer suffixes denote prereleases even if the GitHub flag was incorrectly set.
         return validVersion(tag) && !tag.contains("-") ? tag : null;
      } catch (RuntimeException malformed) {
         return null;
      }
   }


   /** Accept exactly one published executable JAR, with GitHub's own SHA-256 digest. */
   private static ReleaseAsset publishedVerifiedAsset(String body, String tag) {
      if (tag == null || !validVersion(tag) || tag.contains("-")) return null;
      try {
         JsonObject data = new Gson().fromJson(body, JsonObject.class);
         if (data == null || !data.has("assets") || !data.get("assets").isJsonArray()) return null;
         JsonArray assets = data.getAsJsonArray("assets");
         if (assets.size() > 100) return null;
         ReleaseAsset selected = null;
         String version = tag.replaceFirst("^[vV]", "");
         for (JsonElement item : assets) {
            if (!item.isJsonObject()) continue;
            JsonObject asset = item.getAsJsonObject();
            if (!asset.has("name") || !asset.has("digest") || !asset.has("size")
               || !asset.has("browser_download_url") || !asset.has("state")) continue;
            String name = asset.get("name").getAsString();
            if (!name.equalsIgnoreCase("UltraPermissions.jar")
               && !name.equalsIgnoreCase("UltraPermissions-" + version + ".jar")
               && !name.equalsIgnoreCase("UltraPermissions-" + tag + ".jar")) continue;
            if (!"uploaded".equals(asset.get("state").getAsString())) return null;
            String digest = asset.get("digest").getAsString();
            if (!SHA256.matcher(digest).matches()) return null;
            long size = asset.get("size").getAsLong();
            if (size < 1024 || size > MAX_ASSET_BYTES) return null;
            URI uri = URI.create(asset.get("browser_download_url").getAsString());
            if (!"https".equals(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())
               || uri.getPort() != -1 || uri.getRawUserInfo() != null
               || uri.getRawQuery() != null || uri.getRawFragment() != null
               || !("/slyphmp4/UltraPermissions/releases/download/" + tag + "/" + name).equals(uri.getPath())) return null;
            if (selected != null) return null;
            selected = new ReleaseAsset(uri, digest.substring("sha256:".length()).toLowerCase(Locale.ROOT), size);
         }
         return selected;
      } catch (RuntimeException malformed) {
         return null;
      }
   }

   /** Immutable, validated Release asset metadata; never derived from client input. */
   public static final class ReleaseAsset {
      private final URI downloadUri;
      private final String sha256;
      private final long size;

      private ReleaseAsset(URI downloadUri, String sha256, long size) {
         this.downloadUri = downloadUri;
         this.sha256 = sha256;
         this.size = size;
      }

      public URI getDownloadUri() { return downloadUri; }
      public String getSha256() { return sha256; }
      public long getSize() { return size; }
   }

   /** Never hammer the GitHub API following 403/429 or an explicit Retry-After. */
   private static long retryDelayMillis(String retryAfter, String rateReset, long now) {
      long requested = 0L;
      try {
         if (retryAfter != null) {
            requested = new BigInteger(retryAfter.trim()).multiply(BigInteger.valueOf(1000L))
               .min(BigInteger.valueOf(MAX_RATE_LIMIT_MS)).max(BigInteger.ZERO).longValue();
         }
      } catch (NumberFormatException ignored) {}
      try {
         if (rateReset != null) {
            long resetMs = new BigInteger(rateReset.trim()).multiply(BigInteger.valueOf(1000L))
               .min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            requested = Math.max(requested, Math.max(0L, resetMs - now));
         }
      } catch (NumberFormatException ignored) {}
      return Math.min(MAX_RATE_LIMIT_MS, Math.max(FAILURE_CACHE_MS,
         requested == 0L ? RATE_LIMIT_FALLBACK_MS : requested));
   }

   private static boolean validVersion(String input) {
      return input != null && input.length() <= 64 && VERSION.matcher(input).matches();
   }

   /** Compare numeric components and SemVer prerelease identifiers without integer overflow. */
   public static boolean isNewerVersion(String latest, String installed) {
      if (!validVersion(latest) || !validVersion(installed)) return false;
      String[] left = latest.replaceFirst("^[vV]", "").split("\\+", 2)[0].split("-", 2);
      String[] right = installed.replaceFirst("^[vV]", "").split("\\+", 2)[0].split("-", 2);
      String[] leftNumbers = left[0].split("\\.");
      String[] rightNumbers = right[0].split("\\.");
      for (int i = 0; i < Math.max(leftNumbers.length, rightNumbers.length); i++) {
         BigInteger a = new BigInteger(i < leftNumbers.length ? leftNumbers[i] : "0");
         BigInteger b = new BigInteger(i < rightNumbers.length ? rightNumbers[i] : "0");
         int comparison = a.compareTo(b);
         if (comparison != 0) return comparison > 0;
      }
      if (left.length == 1 && right.length > 1) return true;
      if (left.length > 1 && right.length == 1) return false;
      if (left.length == 1) return false;
      String[] a = left[1].split("\\.");
      String[] b = right[1].split("\\.");
      for (int i = 0; i < Math.min(a.length, b.length); i++) {
         if (a[i].equals(b[i])) continue;
         boolean aNumber = a[i].matches("[0-9]+");
         boolean bNumber = b[i].matches("[0-9]+");
         if (aNumber && bNumber) {
            int comparison = new BigInteger(a[i]).compareTo(new BigInteger(b[i]));
            if (comparison != 0) return comparison > 0;
            continue;
         }
         if (aNumber != bNumber) return !aNumber;
         return a[i].compareTo(b[i]) > 0;
      }
      return a.length > b.length;
   }

   // The former purchase/Discord update backend is intentionally retired.
   // No executable JAR download, file replacement or remote restart is permitted.
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

   /** Retained for binary compatibility; the shared JDK HTTP client needs no per-call close. */
   public void destroy() {}

   private static final class LatestRelease {
      private final int status;
      private final String version;
      private final String tagName;
      private final ReleaseAsset asset;
      private final long checkedAt;
      private final long nextCheckAt;
      private final String detail;

      private LatestRelease(int status, String version, String tagName, ReleaseAsset asset, long checkedAt, long nextCheckAt, String detail) {
         this.status = status;
         this.version = version;
         this.tagName = tagName;
         this.asset = asset;
         this.checkedAt = checkedAt;
         this.nextCheckAt = nextCheckAt;
         this.detail = detail;
      }
   }
}
