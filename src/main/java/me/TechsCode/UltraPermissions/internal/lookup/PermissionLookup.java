package me.TechsCode.UltraPermissions.internal.lookup;

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

      // Build the checks only once per lookup, not again for every wildcard candidate.
      // Their priority and the original wildcard traversal order must not change.
      LookupCheck[] checks = this.getChecks();
      for (String candidate : this.permission.getSelfAndWildcards()) {
         for (LookupCheck check : checks) {
            LookupOutcome result = check.perform(candidate);
            if (result != null) {
               this.outcome = result;
               return result;
            }
         }
      }

      // Keep negative results uncached: callers may reuse a lookup after permissions change.
      return new LookupOutcome(false, null);
   }

   public abstract LookupCheck[] getChecks();
}
