package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SuffixPlaceholder extends UpermsPlaceholder {
   public SuffixPlaceholder() {
      super("suffix", "Player Suffix or Suffix of 1. Group");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      if (var2.getSuffix().isPresent()) {
         return var2.getSuffix().get();
      } else if (var3.length != 0) {
         Group var4 = var3[0];
         return var4.getSuffix().orElse("");
      } else {
         return "";
      }
   }
}
