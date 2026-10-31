package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SuffixColorPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.suffix_color.description", "Color of Player Suffix or Suffix of 1. Group");
   public SuffixColorPlaceholder() {
      super("suffix_color", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      if (var2.getSuffix().isPresent()) {
         return ChatColor.getLastColors(var2.getSuffix().get());
      } else if (var3.length != 0) {
         Group var4 = var3[0];
         return ChatColor.getLastColors(var4.getSuffix().orElse(""));
      } else {
         return ChatColor.WHITE.toString();
      }
   }
}
