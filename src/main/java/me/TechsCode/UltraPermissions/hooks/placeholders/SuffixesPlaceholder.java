package me.TechsCode.UltraPermissions.hooks.placeholders;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SuffixesPlaceholder extends UpermsPlaceholder {
   public SuffixesPlaceholder() {
      super("suffixes", "Replaced with all group suffixes");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      ArrayList var4 = new ArrayList();
      if (var2.getSuffix().isPresent()) {
         var4.add(var2.getSuffix().get());
      }

      for (Group var8 : var3) {
         if (var8.getSuffix().isPresent()) {
            var4.add(var8.getSuffix().get());
         }
      }

      return String.join(" ", var4);
   }
}
