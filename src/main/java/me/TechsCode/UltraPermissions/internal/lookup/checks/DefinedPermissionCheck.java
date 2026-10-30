package me.TechsCode.UltraPermissions.internal.lookup.checks;

import java.util.List;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import me.TechsCode.UltraPermissions.storage.objects.Permission;

public abstract class DefinedPermissionCheck implements LookupCheck {
   private final List<Permission> definedPermissions = this.getDefinedPermissions();

   public DefinedPermissionCheck() {
      super();
   }

   @Override
   public LookupOutcome perform(String var1) {
      boolean var2 = false;
      Permission var3 = null;
      boolean var4 = false;

      for (Permission var6 : this.definedPermissions) {
         for (String var8 : var6.asNode().getSelfAndContainedPermissions()) {
            if (var8.equalsIgnoreCase(var1)) {
               if (var6.isPositive()) {
                  var4 = false;
                  var2 = true;
                  var3 = var6;
               } else {
                  if (!var6.getHolder().isGroup()) {
                     return new LookupOutcome(false, "Negated from -" + var6.getName());
                  }

                  var4 = true;
               }
            }
         }
      }

      return !var4 ? (var2 ? new LookupOutcome(true, "Through Permission '" + var3.getName() + "'") : null) : new LookupOutcome(false, "Negated from -" + var1);
   }

   public abstract List<Permission> getDefinedPermissions();
}
