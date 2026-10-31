package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SecondarySuffixPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.secondarysuffix.description", "Suffix of 2. Group");
   public SecondarySuffixPlaceholder() {
      super("secondarysuffix", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      return var3.length >= 2 && var3[1].getSuffix().isPresent() ? var3[1].getSuffix().get() : "";
   }
}
