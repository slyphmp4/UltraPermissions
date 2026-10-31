package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class RanksPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.ranks.description", "Group Name of all Groups");
   public RanksPlaceholder() {
      super("ranks", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      if (var3.length == 0) {
         return "";
      }

      String var4 = var3[0].getName();

      for (int var5 = 1; var5 < var3.length; var5++) {
         var4 = var4 + ", " + var3[var5].getName();
      }

      return var4;
   }
}
