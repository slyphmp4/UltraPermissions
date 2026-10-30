package me.TechsCode.UltraPermissions.internal.lookup.checks;

import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.Permission;

public class DefaultPermissionCheck implements LookupCheck {
   private final OfflinePlayer offlinePlayer;

   public DefaultPermissionCheck(OfflinePlayer var1) {
      super();
      this.offlinePlayer = var1;
   }

   @Override
   public LookupOutcome perform(String var1) {
      Permission var2 = Bukkit.getPluginManager().getPermission(var1);
      return var2 != null && var2.getDefault().getValue(this.offlinePlayer.isOp()) ? new LookupOutcome(true, "Default Permission") : null;
   }
}
