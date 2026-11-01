package me.TechsCode.UltraPermissions.base.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.function.BooleanSupplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.TechsCode.UltraPermissions.base.security.BoundedInput;
import me.TechsCode.UltraPermissions.base.update.UpdateServer.ReleaseAsset;

/**
 * Downloads but never loads or executes a GitHub release. Bukkit applies the
 * verified JAR from its native update folder at the next server plugin load.
 */
final class VerifiedReleaseInstaller {
   enum Result { STAGED, ALREADY_STAGED, CONFLICT, CANCELLED, FAILED }
   record Outcome(Result result, String detail) {
      private static Outcome of(Result result, String detail) { return new Outcome(result, detail); }
   }

   private static final HttpClient CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .followRedirects(HttpClient.Redirect.NEVER)
      .build();
   private static final Pattern PLUGIN_NAME = Pattern.compile("(?m)^name:\\s*['\"]?UltraPermissions['\"]?\\s*$");
   private static final Pattern PLUGIN_VERSION = Pattern.compile("(?m)^version:\\s*['\"]?([^'\"\\r\\n# ]+)['\"]?\\s*$");

   private VerifiedReleaseInstaller() {}

   static Result downloadAndStage(
      ReleaseAsset asset, String version, File installedJar, File updateFolder, File dataFolder, BooleanSupplier permitted
   ) {
      return downloadAndStageDetailed(asset, version, null, installedJar, updateFolder, dataFolder, permitted).result();
   }

   static Outcome downloadAndStageDetailed(
      ReleaseAsset asset, String version, String installedVersion, File installedJar,
      File updateFolder, File dataFolder, BooleanSupplier permitted
   ) {
      if (asset == null || version == null || installedJar == null || updateFolder == null
         || dataFolder == null || permitted == null) return Outcome.of(Result.FAILED, "Missing updater input");
      if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update check cancelled");
      Path original;
      try {
         Path folder = updateFolder.toPath().toAbsolutePath().normalize();
         original = resolveInstalledJar(installedJar.toPath(), folder, installedVersion);
         if (original == null) return Outcome.of(Result.FAILED,
            "Loaded JAR is outside plugins/ and no unique installed UltraPermissions JAR was found there");
         Path target = folder.resolve(original.getFileName());
         if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            return Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(target)
               && Files.size(target) == asset.getSize() && asset.getSha256().equalsIgnoreCase(sha256(target))
               ? Outcome.of(Result.ALREADY_STAGED, "Verified JAR already staged")
               : Outcome.of(Result.CONFLICT, "A different JAR already exists in plugins/update");
         }
      } catch (IOException | RuntimeException invalidDirectory) {
         return Outcome.of(Result.FAILED, "Cannot access installed JAR or Bukkit update directory ("
            + invalidDirectory.getClass().getSimpleName() + ")");
      }
      // This runs on the plugin's asynchronous scheduler. Transient network failures
      // may be retried, but validation, disk and permission failures must fail closed.
      for (int attempt = 1; attempt <= 3; attempt++) {
         if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update check cancelled");
         try (InputStream download = openVerifiedSource(asset.getDownloadUri())) {
            return stageFromStreamDetailed(download, asset, version, original, updateFolder.toPath(),
               dataFolder.toPath().resolve("auto-update-backups"), permitted);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Outcome.of(Result.CANCELLED, "Update worker interrupted");
         } catch (DownloadHttpException http) {
            if (attempt == 3 || http.status < 500 || http.status > 599)
               return Outcome.of(Result.FAILED, "GitHub asset download HTTP " + http.status);
         } catch (IOException failure) {
            if (attempt == 3) return Outcome.of(Result.FAILED,
               "Network or disk I/O while downloading/staging (" + failure.getClass().getSimpleName() + ")");
         } catch (RuntimeException failure) {
            return Outcome.of(Result.FAILED, "Updater validation error (" + failure.getClass().getSimpleName() + ")");
         }
         try {
            Thread.sleep(250L * attempt);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Outcome.of(Result.CANCELLED, "Update retry interrupted");
         }
      }
      return Outcome.of(Result.FAILED, "Download failed after retries");
   }

   private static Path resolveInstalledJar(Path loaded, Path updateFolder, String installedVersion) throws IOException {
      if (Files.isSymbolicLink(updateFolder) || updateFolder.getParent() == null) throw new IOException("Unsafe update directory");
      Path plugins = updateFolder.getParent().toRealPath();
      Path source = loaded.toAbsolutePath().normalize();
      if (!Files.isSymbolicLink(source) && Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
         && source.getParent().toRealPath().equals(plugins) && source.getFileName().toString().endsWith(".jar"))
         return source;
      // Some Bukkit-compatible loaders expose a remapped/cache JAR through JavaPlugin#getFile().
      // Bukkit's update folder instead needs the uniquely named original plugins/*.jar.
      if (installedVersion == null) return null;
      Path candidate = null;
      try (Stream<Path> entries = Files.list(plugins)) {
         for (Path entry : (Iterable<Path>) entries::iterator) {
            if (Files.isSymbolicLink(entry) || !Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
               || !entry.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) continue;
            if (!validPluginJar(entry, installedVersion)) continue;
            if (candidate != null) return null; // Do not guess when duplicate copies exist.
            candidate = entry;
         }
      }
      return candidate;
   }

   private static final class DownloadHttpException extends IOException {
      private final int status;
      private DownloadHttpException(int status) { super("HTTP " + status); this.status = status; }
   }

   private static InputStream openVerifiedSource(URI initial) throws IOException, InterruptedException {
      URI current = initial;
      for (int redirects = 0; redirects <= 3; redirects++) {
         if (!allowedHost(current, redirects == 0)) throw new IOException("Untrusted update redirect");
         HttpRequest request = HttpRequest.newBuilder(current)
            .timeout(Duration.ofMinutes(2))
            .header("Accept", "application/octet-stream")
            .header("User-Agent", "UltraPermissions-GitHub-Updater")
            .GET().build();
         HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
         int status = response.statusCode();
         if (status == 200) return response.body();
         try (InputStream unused = response.body()) {
            if (status != 301 && status != 302 && status != 303 && status != 307 && status != 308)
               throw new DownloadHttpException(status);
            String location = response.headers().firstValue("Location")
               .orElseThrow(() -> new IOException("Missing update redirect"));
            current = current.resolve(location);
         }
      }
      throw new IOException("Too many update redirects");
   }

   private static boolean allowedHost(URI uri, boolean first) {
      if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null
         || uri.getPort() != -1 || uri.getRawFragment() != null) return false;
      String host = uri.getHost().toLowerCase(Locale.ROOT);
      return first ? "github.com".equals(host)
         : "github.com".equals(host) || host.endsWith(".githubusercontent.com");
   }

   /**
    * Kept package-private to exercise the real downloader's digest, archive,
    * staging and cancellation logic in the network-disabled security suite.
    */
   static Result stageFromStream(
      InputStream input, ReleaseAsset asset, String version, Path installedJar,
      Path updateFolder, Path backupFolder, BooleanSupplier permitted
   ) throws IOException {
      return stageFromStreamDetailed(input, asset, version, installedJar, updateFolder, backupFolder, permitted).result();
   }

   private static Outcome stageFromStreamDetailed(
      InputStream input, ReleaseAsset asset, String version, Path installedJar,
      Path updateFolder, Path backupFolder, BooleanSupplier permitted
   ) throws IOException {
      if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update disabled");
      if (input == null || asset == null || version == null) return Outcome.of(Result.FAILED, "Missing download stream");
      Path source = installedJar.toAbsolutePath().normalize();
      if (Files.isSymbolicLink(source) || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
         || !source.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
         return Outcome.of(Result.FAILED, "Installed plugin JAR is missing or unsafe");
      Path installedDirectory = source.getParent().toRealPath();
      Path staging = updateFolder.toAbsolutePath().normalize();
      if (staging.getParent() == null || !staging.getParent().toRealPath().equals(installedDirectory)
         || Files.isSymbolicLink(staging)) return Outcome.of(Result.FAILED, "Update directory is not plugins/update");
      Files.createDirectories(staging);
      Path target = staging.resolve(source.getFileName().toString());
      if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
         if (Files.isSymbolicLink(target) || !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS))
            return Outcome.of(Result.CONFLICT, "Staged target is not a regular JAR");
         return Files.size(target) == asset.getSize() && asset.getSha256().equalsIgnoreCase(sha256(target))
            ? Outcome.of(Result.ALREADY_STAGED, "Verified JAR already staged")
            : Outcome.of(Result.CONFLICT, "A different JAR already exists in plugins/update");
      }

      Path temp = Files.createTempFile(staging, ".ultrapermissions-download-", ".part");
      try {
         MessageDigest digest = digest();
         long count = 0;
         try (OutputStream output = Files.newOutputStream(temp)) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
               count += read;
               if (count > asset.getSize()) return Outcome.of(Result.FAILED, "Download exceeds published asset size");
               digest.update(buffer, 0, read);
               output.write(buffer, 0, read);
               if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update disabled during download");
            }
         }
         if (count != asset.getSize()) return Outcome.of(Result.FAILED, "Downloaded byte count does not match GitHub release");
         if (!HexFormat.of().formatHex(digest.digest()).equalsIgnoreCase(asset.getSha256()))
            return Outcome.of(Result.FAILED, "Downloaded SHA-256 differs from GitHub release digest");
         if (!validPluginJar(temp, version)) return Outcome.of(Result.FAILED, "Downloaded plugin.yml name/version is invalid");
         if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update disabled before backup");
         if (!backupOriginal(source, backupFolder, version))
            return Outcome.of(Result.FAILED, "Existing backup does not match the installed plugin JAR");
         if (!permitted.getAsBoolean()) return Outcome.of(Result.CANCELLED, "Update disabled before staging");
         if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return Outcome.of(Result.CONFLICT, "Another update appeared during download");
         // Rename only a complete, verified temp JAR; never replace another staged file.
         Files.move(temp, target);
         return Outcome.of(Result.STAGED, "Verified JAR staged for next restart");
      } finally {
         Files.deleteIfExists(temp);
      }
   }

   private static boolean validPluginJar(Path jar, String version) {
      try (JarFile archive = new JarFile(jar.toFile(), true)) {
         JarEntry description = archive.getJarEntry("plugin.yml");
         if (description == null || description.getSize() > 64 * 1024) return false;
         String yaml;
         try (InputStream input = archive.getInputStream(description)) {
            yaml = new String(BoundedInput.read(input, 64 * 1024), StandardCharsets.UTF_8);
         }
         Matcher foundVersion = PLUGIN_VERSION.matcher(yaml);
         return PLUGIN_NAME.matcher(yaml).find() && foundVersion.find()
            && version.equals(foundVersion.group(1));
      } catch (IOException | RuntimeException failure) {
         return false;
      }
   }

   private static boolean backupOriginal(Path original, Path backupFolder, String version) throws IOException {
      Path backupRoot = backupFolder.toAbsolutePath().normalize();
      if (Files.isSymbolicLink(backupRoot)) return false;
      Files.createDirectories(backupRoot);
      String cleanVersion = version.replaceAll("[^0-9A-Za-z.+-]", "_");
      Path destination = backupRoot.resolve(original.getFileName().toString() + ".before-" + cleanVersion + ".bak");
      if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS))
         return !Files.isSymbolicLink(destination) && Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)
            && Files.size(destination) == Files.size(original)
            && sha256(destination).equals(sha256(original));
      Path temp = Files.createTempFile(backupRoot, ".ultrapermissions-backup-", ".part");
      try {
         Files.copy(original, temp, StandardCopyOption.REPLACE_EXISTING);
         Files.move(temp, destination);
         return true;
      } finally {
         Files.deleteIfExists(temp);
      }
   }

   private static String sha256(Path file) throws IOException {
      MessageDigest digest = digest();
      try (InputStream input = Files.newInputStream(file)) {
         byte[] bytes = new byte[16 * 1024];
         int read;
         while ((read = input.read(bytes)) != -1) digest.update(bytes, 0, read);
      }
      return HexFormat.of().formatHex(digest.digest());
   }

   private static MessageDigest digest() {
      try {
         return MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException impossible) {
         throw new IllegalStateException("JDK SHA-256 provider missing", impossible);
      }
   }
}
