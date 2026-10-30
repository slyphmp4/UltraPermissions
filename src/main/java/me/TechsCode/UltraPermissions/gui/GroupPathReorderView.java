package me.TechsCode.UltraPermissions.gui;

import java.util.Comparator;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import org.bukkit.entity.Player;

public abstract class GroupPathReorderView extends PageableGUI<GroupPath> {
   private final UltraPermissions plugin;
   private GroupPath selected;
   private static final Phrase ENTRY_DESELECT_ACTION = Phrase.create(
      "groupPathReorderView.entry.deselectAction", "Click to **deselect** this group", Colors.GRAY, Colors.RED
   );
   private static final Phrase ENTRY_SWAP_ACTION = Phrase.create(
      "groupPathReorderView.entry.swapAction", "Click to swap with **%group%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase ENTRY_GRAB_ACTION = Phrase.create(
      "groupPathReorderView.entry.grabAction", "Click to **grab** this group", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase TITLE = Phrase.create("groupPathReorderView.title", "Group Path Sorting");

   public GroupPathReorderView(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.plugin = var2;
      int var3 = 1;

      for (GroupPath var5 : var2.getGroupPaths()) {
         var5.setPriority(var3);
         var3++;
      }
   }

   public void construct(Button var1, GroupPath var2) {
      var1.material(var2.getIcon().getType()).name(Animation.wave(var2.getName(), Colors.Blue, Colors.WHITE)).setDamage(var2.getIcon().getDurability());
      if (this.selected != null) {
         if (this.selected.equals(var2)) {
            var1.item().appendLore(ENTRY_DESELECT_ACTION.get());
            var1.item().addEnchantment(XEnchantment.LUCK_OF_THE_SEA.getEnchant(), 1);
         } else {
            var1.item().appendLore(ENTRY_SWAP_ACTION.get().replace("%group%", this.selected.getName()));
         }
      } else {
         var1.item().appendLore(ENTRY_GRAB_ACTION.get());
      }

      if (var2.getDescription().length() > 0) {
         var1.item().appendLore("", "§f" + var2.getDescription());
      }

      var1.action(var2x -> {
         if (this.selected != null) {
            if (!this.selected.equals(var2)) {
               int var3 = this.selected.getPriority();
               this.selected.setPriority(var2.getPriority());
               var2.setPriority(var3);
            }

            this.selected = null;
         } else {
            this.selected = var2;
         }
      });
   }

   public GroupPath[] getObjects() {
      return this.plugin.getGroupPaths().stream().sorted(Comparator.comparingInt(GroupPath::getPriority)).toArray(GroupPath[]::new);
   }

   @Override
   public String getTitle() {
      return TITLE.get();
   }

   @Override
   public SearchFeature<GroupPath> getSearch() {
      return null;
   }
}
