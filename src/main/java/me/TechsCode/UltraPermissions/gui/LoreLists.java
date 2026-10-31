package me.TechsCode.UltraPermissions.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.base.visual.LoreScroller;
import me.TechsCode.UltraPermissions.gui.permissionEditor.PermissionCopyList;
import me.TechsCode.UltraPermissions.storage.collection.RankupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;

public class LoreLists {
   private static final Phrase INVALID_SCOPE = Phrase.create("lists.invalidScope", "INVALID");
   private static final Phrase LIST_NONE_LINE = Phrase.create("lists.all.noneLine", "- **None**", Colors.GRAY, Colors.RED);
   private static final Phrase RANK_UP_LIST_PERMANENT_LINE = Phrase.create("lists.rankUps.permanentLine", "%index%. **%group%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase RANK_UP_LIST_TEMPORARY_LINE = Phrase.create(
      "lists.rankUps.temporaryLine", "%index%. **%group%** for **%time%**", Colors.GRAY, Colors.YELLOW, Colors.RED
   );
   private static final Phrase PERMISSION_LIST_POSITIVE_INHERITED = Phrase.create(
      "lists.permissions.inheritedPositiveLine", "- **%permission%** from **%group%**", Colors.GRAY, Colors.WHITE, Colors.YELLOW
   );
   private static final Phrase PERMISSION_LIST_NEGATIVE_INHERITED = Phrase.create(
      "lists.permissions.inheritedNegativeLine", "- **-****%permission%** from **%group%**", Colors.GRAY, Colors.RED, Colors.WHITE, Colors.YELLOW
   );
   private static final Phrase PERMISSION_LIST_POSITIVE = Phrase.create("lists.permissions.positiveLine", "- **%permission%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase PERMISSION_LIST_NEGATIVE = Phrase.create(
      "lists.permissions.negativeLine", "- **-****%permission%**", Colors.GRAY, Colors.RED, Colors.WHITE
   );
   private static final Phrase PERMISSION_LIST_POSITIVE_SERVER = Phrase.create(
      "lists.permissions.serverPositiveLine", "- **%permission%** on Server **%server%**", Colors.GRAY, Colors.WHITE, Colors.BLUE
   );
   private static final Phrase PERMISSION_LIST_NEGATIVE_SERVER = Phrase.create(
      "lists.permissions.serverNegativeLine", "- **-****%permission%** on Server **%server%**", Colors.GRAY, Colors.RED, Colors.WHITE, Colors.BLUE
   );
   private static final Phrase PERMISSION_LIST_POSITIVE_WORLD = Phrase.create(
      "lists.permissions.worldPositiveLine", "- **%permission%** on World **%world%**", Colors.GRAY, Colors.WHITE, Colors.YELLOW
   );
   private static final Phrase PERMISSION_LIST_NEGATIVE_WORLD = Phrase.create(
      "lists.permissions.worldNegativeLine", "- **-****%permission%** on World **%world%**", Colors.GRAY, Colors.RED, Colors.WHITE, Colors.YELLOW
   );
   private static final Phrase PERMISSION_LIST_POSITIVE_SERVER_WORLD = Phrase.create(
      "lists.permissions.serverWorldPositiveLine", "- **%permission%** on **%server%** in **%world%**", Colors.GRAY, Colors.WHITE, Colors.BLUE, Colors.YELLOW
   );
   private static final Phrase PERMISSION_LIST_NEGATIVE_SERVER_WORLD = Phrase.create(
      "lists.permissions.serverWorldNegativeLine",
      "- **-****%permission%** on **%server%** in **%world%**",
      Colors.GRAY,
      Colors.WHITE,
      Colors.RED,
      Colors.BLUE,
      Colors.YELLOW
   );

   public LoreLists() {
      super();
   }

   public static List<String> printRankUpLoreList(RankupList var0) {
      ArrayList var1 = new ArrayList();
      int var2 = 1;

      for (UserRankup var4 : var0) {
         Optional var5 = var4.getGroup().get();
         if (var5.isPresent()) {
            Phrase var6 = var4.isPermanent() ? RANK_UP_LIST_PERMANENT_LINE : RANK_UP_LIST_TEMPORARY_LINE;
            String var7 = var6.get()
               .replace("%index%", Integer.toString(var2))
               .replace("%group%", ((Group)var5.get()).getName())
               .replace("%time%", var4.isTemporary() ? Tools.getTimeString(var4.getLeftDuration() / 1000L) : "");
            var1.add(var7);
            var2++;
         }
      }

      if (var2 == 1) {
         var1.add(LIST_NONE_LINE.get());
      }

      return var1;
   }

   public static List<String> printPermissionCopiesList(PermissionCopyList var0, long var1) {
      List var3 = var0.stream()
         .map(
            var0x -> {
               if (var0x.isInherited()) {
                  Phrase var5;
                  if (var0x.getPermission().isPositive()) {
                     var5 = PERMISSION_LIST_POSITIVE_INHERITED;
                  } else {
                     var5 = PERMISSION_LIST_NEGATIVE_INHERITED;
                  }

                  return var5.get().replace("%permission%", var0x.getColoredName()).replace("%group%", var0x.getPermission().getHolder().getName());
               } else {
                  Optional<String> var1x = var0x.getPermission().getServer();
                  Optional<String> var2 = var0x.getPermission().getWorld();
                  boolean var3x = var0x.getPermission().isPositive();
                  Phrase var4;
                  if (var1x.isPresent() && var2.isPresent()) {
                     var4 = var3x ? PERMISSION_LIST_POSITIVE_SERVER_WORLD : PERMISSION_LIST_NEGATIVE_SERVER_WORLD;
                  } else if (var1x.isPresent()) {
                     var4 = var3x ? PERMISSION_LIST_POSITIVE_SERVER : PERMISSION_LIST_NEGATIVE_SERVER;
                  } else if (var2.isPresent()) {
                     var4 = var3x ? PERMISSION_LIST_POSITIVE_WORLD : PERMISSION_LIST_NEGATIVE_WORLD;
                  } else {
                     var4 = var3x ? PERMISSION_LIST_POSITIVE : PERMISSION_LIST_NEGATIVE;
                  }

                  return var4.get()
                     .replace("%permission%", var0x.getColoredName())
                     .replace("%server%", var1x.orElse(INVALID_SCOPE.get()))
                     .replace("%world%", var2.orElse(INVALID_SCOPE.get()));
               }
            }
         )
         .collect(Collectors.toList());
      if (var3.isEmpty()) {
         var3.add(LIST_NONE_LINE.get());
      }

      return LoreScroller.scroller(var3, 15, var1);
   }
}
