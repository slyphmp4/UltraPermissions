package me.TechsCode.UltraPermissions.base.reflection.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Numeric Minecraft release, independent of CraftBukkit packages and Paper build suffixes. */
public final class MinecraftRelease {
   private static final Pattern RELEASE = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?=[.-]|$)");
   private final int feature;
   private final int patch;

   private MinecraftRelease(int feature, int patch) {
      this.feature = feature;
      this.patch = patch;
   }

   public static MinecraftRelease parse(String version) {
      Matcher match = RELEASE.matcher(version);
      if (!match.find()) {
         throw new IllegalArgumentException("Unrecognized Minecraft version: " + version);
      }
      int major = Integer.parseInt(match.group(1));
      int feature = major == 1 ? Integer.parseInt(match.group(2)) : major;
      int patch = match.group(3) == null ? 0 : Integer.parseInt(match.group(3));
      return new MinecraftRelease(feature, patch);
   }

   public int feature() { return feature; }
   public int patch() { return patch; }
}
