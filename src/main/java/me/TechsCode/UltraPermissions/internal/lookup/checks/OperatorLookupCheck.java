package me.TechsCode.UltraPermissions.internal.lookup.checks;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.OfflinePlayer;

public class OperatorLookupCheck implements LookupCheck {
   private final OfflinePlayer offlinePlayer;
   private final UltraPermissions plugin;

   public OperatorLookupCheck(UltraPermissions var1, OfflinePlayer var2) {
      super();
      this.offlinePlayer = var2;
      this.plugin = var1;
   }

   @Override
   public LookupOutcome perform(String var1) {
      if (this.plugin.isNegativePermsOverrideEnabled()) {
         if (this.offlinePlayer.isOp()) {
            Optional var2 = this.plugin.getUsers().uuid(this.offlinePlayer.getUniqueId());
            if (var2.isPresent()) {
               boolean var3 = ((User)var2.get()).getPermissions().name(var1).stream().anyMatch(var0 -> !var0.isPositive());
               return new LookupOutcome(!var3, "Operator Rights");
            }
         }

         return null;
      } else {
         return this.offlinePlayer.isOp() ? new LookupOutcome(true, "Operator Rights") : null;
      }
   }
}
