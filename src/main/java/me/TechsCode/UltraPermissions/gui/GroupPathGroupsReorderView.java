package me.TechsCode.UltraPermissions.gui;

import java.util.ArrayList;
import java.util.Collections;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import org.bukkit.entity.Player;

public abstract class GroupPathGroupsReorderView extends PageableGUI<Group> {
   private final UltraPermissions plugin;
   private Group selected;
   private GroupPath groupPath;
   private ArrayList<Group> newGroupPath;
   private static final Phrase ENTRY_DESELECT_ACTION = Phrase.create(
      "groupPathGroupsReorderView.entry.deselectAction", "Click to **deselect** this group", Colors.GRAY, Colors.RED
   );
   private static final Phrase ENTRY_SWAP_ACTION = Phrase.create(
      "groupPathGroupsReorderView.entry.swapAction", "Click to swap with **%group%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase ENTRY_GRAB_ACTION = Phrase.create(
      "groupPathGroupsReorderView.entry.grabAction", "Click to **grab** this group", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase TITLE = Phrase.create("groupPathGroupsReorderView.title", "Path Sorting");

   public GroupPathGroupsReorderView(Player var1, UltraPermissions var2, GroupPath var3) {
      super(var1, var2);
      this.plugin = var2;
      this.newGroupPath = new ArrayList<>();
      this.newGroupPath.addAll(var3.getGroupPath());
      this.groupPath = var3;
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.GOLD, Colors.YELLOW, Colors.WHITE))
         .setDamage(var2.getDamage());
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
               Collections.swap(this.newGroupPath, this.newGroupPath.indexOf(this.selected), this.newGroupPath.indexOf(var2));
               this.groupPath.setGroupPath(this.newGroupPath);
            }

            this.selected = null;
         } else {
            this.selected = var2;
         }
      });
   }

   public Group[] getObjects() {
      return this.groupPath.getGroupPath().toArray(new Group[0]);
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
