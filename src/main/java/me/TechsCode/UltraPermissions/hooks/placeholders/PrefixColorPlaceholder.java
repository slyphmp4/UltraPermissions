package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PrefixColorPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.prefix_color.description", "Color of Player Prefix or Prefix of 1. Group");
   public PrefixColorPlaceholder() {
      super("prefix_color", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      if (var2.getPrefix().isPresent()) {
         return ChatColor.getLastColors(var2.getPrefix().get());
      } else if (var3.length != 0) {
         Group var4 = var3[0];
         return ChatColor.getLastColors(var4.getPrefix().orElse(""));
      } else {
         return ChatColor.WHITE.toString();
      }
   }
}
