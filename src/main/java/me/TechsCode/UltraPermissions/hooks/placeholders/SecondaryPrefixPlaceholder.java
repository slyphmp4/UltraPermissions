package me.TechsCode.UltraPermissions.hooks.placeholders;

import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SecondaryPrefixPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.secondaryprefix.description", "Prefix of 2. Group");
   public SecondaryPrefixPlaceholder() {
      super("secondaryprefix", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      return var3.length >= 2 && var3[1].getPrefix().isPresent() ? var3[1].getPrefix().get() : "";
   }
}
