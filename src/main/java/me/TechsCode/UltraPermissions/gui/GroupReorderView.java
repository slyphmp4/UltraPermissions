package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GroupReorderView extends PageableGUI<Group> {
   private final UltraPermissions plugin;
   private Group selected;
   private static final Phrase ENTRY_DESELECT_ACTION = Phrase.create(
      "groupReorderView.entry.deselectAction", "Click to **deselect** this group", Colors.GRAY, Colors.RED
   );
   private static final Phrase ENTRY_SWAP_ACTION = Phrase.create(
      "groupReorderView.entry.swapAction", "Click to swap with **%group%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase ENTRY_GRAB_ACTION = Phrase.create("groupReorderView.entry.grabAction", "Click to **grab** this group", Colors.GRAY, Colors.GREEN);
   private static final Phrase TITLE = Phrase.create("groupReorderView.title", "Group Sorting (High to Low)");

   public GroupReorderView(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.plugin = var2;
      int var3 = 1;

      for (Group var5 : var2.getGroups().bestToWorst()) {
         var5.setPriority(var3);
         var3++;
      }
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId()).name(Animation.wave(var2.getName(), Colors.Blue, Colors.WHITE)).setDamage(var2.getDamage());
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

   public Group[] getObjects() {
      return this.plugin.getGroups().bestToWorst().toArray(new Group[0]);
   }

   @Override
   public String getTitle() {
      return TITLE.get();
   }

   @Override
   public SearchFeature<Group> getSearch() {
      return null;
   }
}
