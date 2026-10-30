package me.TechsCode.UltraPermissions.hooks.placeholders;

import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public class RankTimerDetailedPlaceholder extends UpermsPlaceholder {
   public RankTimerDetailedPlaceholder() {
      super("rank_timer_detailed", "Detailed Rank Timer of next highest timed group");
   }

   @Override
   protected String replace(Player var1, User var2, Group[] var3) {
      for (Group var7 : var3) {
         long var8 = var2.getGroupExpiry(var7.toStored());
         if (var8 != 0L) {
            long var10 = var8 - System.currentTimeMillis();
            return Tools.getTimeString(var10, TimeUnit.MILLISECONDS);
         }
      }

      return "Permanent";
   }
}
