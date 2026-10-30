package me.TechsCode.UltraPermissions.base.source;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.TechPlugin;

public class Maven {
   private static boolean initialized = false;
   private static TechPlugin plugin;
   private static final List<String> repositories = new ArrayList<>(Collections.singletonList("https://repo1.maven.org/maven2"));
   private static JarClassLoader classLoader;
   private static LocalRepository localRepository;
   private static Set<Dependency> loadedDependencies;
   private static Set<Dependency> failedDependencies;

   public Maven() {
      super();
   }

   public static void initialize(SpigotTechPlugin var0) {
      if (!initialized) {
         plugin = var0;
         classLoader = new JarClassLoader(var0);
         localRepository = new LocalRepository(var0);
         loadedDependencies = new HashSet<>();
         failedDependencies = new HashSet<>();
         initialized = true;
      }
   }

   public static void addRepository(String var0) {
      Preconditions.checkArgument(initialized, "Call #initialize before using Maven");
      if (!repositories.contains(var0)) {
         repositories.add(var0);
      }
   }

   public static boolean loadDependency(String var0, String var1, String var2) {
      Dependency var3 = new Dependency(var0, var1, var2);
      return loadDependency(var3);
   }

   public static boolean loadDependency(Dependency var0) {
      Preconditions.checkArgument(initialized, "Call #initialize before using Maven");
      if (failedDependencies.contains(var0)) {
         return false;
      }

      if (loadedDependencies.contains(var0)) {
         return true;
      }

      File var1 = localRepository.getIfPresent(var0);
      if (var1 == null) {
         localRepository.load(var0, repositories);
         var1 = localRepository.getIfPresent(var0);
      }

      if (var1 != null) {
         loadedDependencies.add(var0);
         plugin.log("Loaded dependency " + var0.getGroupId() + ":" + var0.getArtifactId() + ":" + var0.getVersion());
      } else {
         failedDependencies.add(var0);
         plugin.log("Could not load " + var0.getGroupId() + " / " + var0.getArtifactId() + " on " + var0.getVersion());
      }

      return var1 != null;
   }

   public static boolean isLoaded(Dependency var0) {
      return loadedDependencies.contains(var0);
   }
}
