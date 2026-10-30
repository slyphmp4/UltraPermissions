package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SecondarySuffixPlaceholder extends UpermsPlaceholder {
   public SecondarySuffixPlaceholder() {
      super("secondarysuffix", "Suffix of 2. Group");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      return var3.length >= 2 && var3[1].getSuffix().isPresent() ? var3[1].getSuffix().get() : "";
   }
}
