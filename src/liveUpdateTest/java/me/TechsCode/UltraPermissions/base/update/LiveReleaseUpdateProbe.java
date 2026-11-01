package me.TechsCode.UltraPermissions.base.update;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import me.TechsCode.UltraPermissions.base.update.responses.IsUpToDateResponse;

/** Optional internet-enabled probe. Never executes or installs the downloaded binary. */
public final class LiveReleaseUpdateProbe {
   private LiveReleaseUpdateProbe() {}

   public static void main(String[] args) throws Exception {
      if (args.length != 1) throw new IllegalArgumentException("Expected a local plugin JAR fixture");
      Path source = Path.of(args[0]);
      if (!Files.isRegularFile(source)) throw new IllegalArgumentException("Missing fixture JAR");
      IsUpToDateResponse release = new UpdateServer().isUpToDate("UltraPermissions", "0.0");
      if (!release.isSuccessFull() || release.isUpToDate() || release.getReleaseAsset() == null)
         throw new IllegalStateException("No published stable release with a verified production JAR (HTTP "
            + release.getStatusCode() + "): " + release.getMessage());
      Path fixture = Files.createTempDirectory("ultrapermissions-live-update-");
      Path plugins = Files.createDirectories(fixture.resolve("plugins"));
      Path original = plugins.resolve("UltraPermissions.jar");
      Files.copy(source, original);
      byte[] expectedOriginal = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(original));
      File updateFolder = plugins.resolve("update").toFile();
         VerifiedReleaseInstaller.Outcome outcome = VerifiedReleaseInstaller.downloadAndStageDetailed(
            release.getReleaseAsset(), release.getLatestVersion(), null, original.toFile(),
            updateFolder, fixture.resolve("data").toFile(), () -> true);
         if (outcome.result() != VerifiedReleaseInstaller.Result.STAGED)
            throw new IllegalStateException("Live GitHub asset download failed: " + outcome.detail());
         Path staged = updateFolder.toPath().resolve("UltraPermissions.jar");
         if (!Files.isRegularFile(staged) || Files.size(staged) != release.getReleaseAsset().getSize()
            || !Arrays.equals(expectedOriginal, MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(original))))
            throw new IllegalStateException("Staging changed the running plugin or produced the wrong JAR size");
         VerifiedReleaseInstaller.Outcome repeated = VerifiedReleaseInstaller.downloadAndStageDetailed(
            release.getReleaseAsset(), release.getLatestVersion(), null, original.toFile(),
            updateFolder, fixture.resolve("data").toFile(), () -> true);
         if (repeated.result() != VerifiedReleaseInstaller.Result.ALREADY_STAGED)
            throw new IllegalStateException("Verified existing update was not recognized");
      System.out.println("PASS live GitHub Release " + release.getLatestVersion()
         + ": verified download, stage, backup and idempotent reuse; temporary fixture: " + fixture);
   }
}
