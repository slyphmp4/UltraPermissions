package me.TechsCode.UltraPermissions.base.fileconf;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Save a complete configuration beside its destination and atomically replace
 * the previous file only after the temporary file was fully written.
 */
public final class AtomicFileSave {
   private AtomicFileSave() {
   }

   @FunctionalInterface
   public interface Writer {
      void write(File file) throws IOException;
   }

   public static void save(File destination, Writer writer) throws IOException {
      Path target = destination.toPath().toAbsolutePath();
      Path parent = target.getParent();
      Files.createDirectories(parent);
      Path temporary = Files.createTempFile(parent, "." + destination.getName() + "-", ".tmp");
      try {
         writer.write(temporary.toFile());
         try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
            channel.force(true);
         }
         // If ATOMIC_MOVE is unsupported, fail without modifying the original.
         Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(temporary);
      }
   }
}
