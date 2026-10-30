package me.TechsCode.UltraPermissions.hooks.placeholders;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class PrefixesPlaceholder extends UpermsPlaceholder {
   public PrefixesPlaceholder() {
      super("prefixes", "Replaced with player prefix & group prefixes");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      ArrayList var4 = new ArrayList();
      if (var2.getPrefix().isPresent()) {
         var4.add(var2.getPrefix().get());
      }

      for (Group var8 : var3) {
         if (var8.getPrefix().isPresent()) {
            var4.add(var8.getPrefix().get());
         }
      }

      return String.join(" ", var4);
   }
}
