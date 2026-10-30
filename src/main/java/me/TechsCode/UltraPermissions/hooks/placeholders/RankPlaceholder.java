package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class RankPlaceholder extends UpermsPlaceholder {
   public RankPlaceholder() {
      super("rank", "Group Name of 1. Group");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      return var3.length == 0 ? "" : var3[0].getName();
   }
}
