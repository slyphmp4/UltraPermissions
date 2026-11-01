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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
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
      if (asset == null || installedJar == null || updateFolder == null || dataFolder == null || !permitted.getAsBoolean())
         return Result.FAILED;
      try (InputStream download = openVerifiedSource(asset.getDownloadUri())) {
         return stageFromStream(download, asset, version, installedJar.toPath(), updateFolder.toPath(),
            dataFolder.toPath().resolve("auto-update-backups"), permitted);
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
         return Result.FAILED;
      } catch (IOException | RuntimeException failure) {
         return Result.FAILED;
      }
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
               throw new IOException("Update asset HTTP " + status);
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
      if (input == null || asset == null || version == null || !permitted.getAsBoolean()) return Result.CANCELLED;
      Path source = installedJar.toAbsolutePath().normalize();
      if (Files.isSymbolicLink(source) || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
         || !source.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) return Result.FAILED;
      Path installedDirectory = source.getParent().toRealPath();
      Path staging = updateFolder.toAbsolutePath().normalize();
      if (staging.getParent() == null || !staging.getParent().toRealPath().equals(installedDirectory)
         || Files.isSymbolicLink(staging)) return Result.FAILED;
      Files.createDirectories(staging);
      Path target = staging.resolve(source.getFileName().toString());
      if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
         if (Files.isSymbolicLink(target) || !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS))
            return Result.CONFLICT;
         return Files.size(target) == asset.getSize() && asset.getSha256().equalsIgnoreCase(sha256(target))
            ? Result.ALREADY_STAGED : Result.CONFLICT;
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
               if (count > asset.getSize()) return Result.FAILED;
               digest.update(buffer, 0, read);
               output.write(buffer, 0, read);
               if (!permitted.getAsBoolean()) return Result.CANCELLED;
            }
         }
         if (count != asset.getSize()
            || !HexFormat.of().formatHex(digest.digest()).equalsIgnoreCase(asset.getSha256())
            || !validPluginJar(temp, version)) return Result.FAILED;
         if (!permitted.getAsBoolean()) return Result.CANCELLED;
         if (!backupOriginal(source, backupFolder, version)) return Result.FAILED;
         if (!permitted.getAsBoolean()) return Result.CANCELLED;
         if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return Result.CONFLICT;
         try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(temp, target);
         }
         return Result.STAGED;
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
         try {
            Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(temp, destination);
         }
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
