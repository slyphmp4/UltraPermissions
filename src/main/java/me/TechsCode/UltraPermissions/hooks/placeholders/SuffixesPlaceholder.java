package me.TechsCode.UltraPermissions.hooks.placeholders;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class SuffixesPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.suffixes.description", "Replaced with all group suffixes");
   public SuffixesPlaceholder() {
      super("suffixes", DESCRIPTION);
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
