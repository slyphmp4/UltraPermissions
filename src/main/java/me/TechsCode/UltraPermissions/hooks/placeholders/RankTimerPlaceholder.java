package me.TechsCode.UltraPermissions.hooks.placeholders;

import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class RankTimerPlaceholder extends UpermsPlaceholder {
   private static final Phrase DESCRIPTION = Phrase.create("placeholder.rank_timer.description", "Rank Timer of next highest timed group");
   public RankTimerPlaceholder() {
      super("rank_timer", DESCRIPTION);
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      for (Group var7 : var3) {
         long var8 = var2.getGroupExpiry(var7.toStored());
         if (var8 != 0L) {
            long var10 = var8 - System.currentTimeMillis();
            return Tools.getTimeString(var10, TimeUnit.MILLISECONDS, 2);
         }
      }

      return "Permanent";
   }
}
