package me.TechsCode.UltraPermissions.base.storage;

/** An authoritative permissions store could not be opened; never fall back to different rights. */
public final class StorageStartupException extends IllegalStateException {
   public StorageStartupException(String message) { super(message); }
   public StorageStartupException(String message, Throwable cause) { super(message, cause); }

   public static boolean isStorageFailure(Throwable failure) {
      java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
      for (Throwable current = failure; current != null && seen.add(current); current = current.getCause()) {
         if (current instanceof StorageStartupException) return true;
      }
      return false;
   }
}
