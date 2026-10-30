package me.TechsCode.UltraPermissions.migration;

import java.util.Arrays;
import org.bukkit.Bukkit;

public enum Migrations {
   LuckPerms(LuckPermsMigration.class),
   PermissionsEx(PermissionsExMigration.class);

   private Class<? extends MigrationAssistant> migrationClass;

   Migrations(Class<? extends MigrationAssistant> var3) {
      this.migrationClass = var3;
   }

   public static MigrationAssistant getMigrationAssistant() {
      Migrations var0 = Arrays.stream(values()).filter(var0x -> Bukkit.getPluginManager().isPluginEnabled(var0x.name())).findFirst().orElse(null);
      if (var0 == null) {
         return null;
      }

      try {
         return var0.migrationClass.newInstance();
      } catch (InstantiationException | IllegalAccessException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static boolean isAvailable() {
      return Arrays.stream(values()).anyMatch(var0 -> Bukkit.getPluginManager().isPluginEnabled(var0.name()));
   }
}
