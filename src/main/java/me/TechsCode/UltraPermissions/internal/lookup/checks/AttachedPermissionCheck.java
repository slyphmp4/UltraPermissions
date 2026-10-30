package me.TechsCode.UltraPermissions.internal.lookup.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import org.bukkit.permissions.PermissionAttachment;

public abstract class AttachedPermissionCheck implements LookupCheck {
   public AttachedPermissionCheck() {
      super();
   }

   @Override
   public LookupOutcome perform(String var1) {
      List<Entry<String, Boolean>> var2 = new ArrayList<>(this.getAttachments()).stream().flatMap(var0 -> var0.getPermissions().entrySet().stream()).collect(Collectors.toList());
      boolean var3 = false;
      boolean var4 = false;

      for (Entry var6 : var2) {
         if (((String)var6.getKey()).equalsIgnoreCase(var1)) {
            if ((Boolean)var6.getValue()) {
               var3 = true;
            } else {
               var4 = true;
            }
         }
      }

      if (var4) {
         return new LookupOutcome(false, "Negated by Permission Attachment");
      } else {
         return var3 ? new LookupOutcome(true, "Allowed by Permission Attachment") : null;
      }
   }

   public abstract List<PermissionAttachment> getAttachments();
}
