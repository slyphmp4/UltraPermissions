package me.TechsCode.UltraPermissions.internal.lookup;

import java.util.Arrays;
import java.util.Objects;
import me.TechsCode.UltraPermissions.internal.Node;

public abstract class PermissionLookup {
   private final Node permission;
   private LookupOutcome outcome;

   public PermissionLookup(String var1) {
      super();
      this.permission = new Node(var1);
      this.outcome = null;
   }

   public LookupOutcome getOutcome() {
      return this.outcome;
   }

   public LookupOutcome perform() {
      if (this.outcome != null) {
         return this.outcome;
      }

      for (String var2 : this.permission.getSelfAndWildcards()) {
         this.outcome = this.checkPermission(var2);
         if (this.outcome != null) {
            return this.outcome;
         }
      }

      return new LookupOutcome(false, null);
   }

   private LookupOutcome checkPermission(String var1) {
      return Arrays.stream(this.getChecks()).map(var1x -> var1x.perform(var1)).filter(Objects::nonNull).findFirst().orElse(null);
   }

   public abstract LookupCheck[] getChecks();
}
