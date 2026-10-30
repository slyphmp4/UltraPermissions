package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class InheritedGroupListView extends PageableGUI<Group> {
   private static final Phrase TITLE = Phrase.create("inheritedGroupListView.title", "%group% > Inherited Groups");
   private final UltraPermissions plugin;
   private final Group target;
   private static final Phrase INHERITING_ACTION = Phrase.create("inheritedGroupListView.inheriting.action", "Click to **remove**", Colors.GRAY, Colors.RED);
   private static final Phrase INHERITING_EXPLANATION = Phrase.create(
      "inheritedGroupListView.inheriting.explanation", "This will add all permissions from **%from%** to **%to%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );
   private static final Phrase NOT_INHERITING_ACTION = Phrase.create(
      "inheritedGroupListView.notInheriting.action", "Click to **add**", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase NOT_INHERITING_EXPLANATION = Phrase.create(
      "inheritedGroupListView.notInheriting.explanation", "All Permissions from **%from%** will be added to **%to%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );

   public InheritedGroupListView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.plugin = var2;
      this.target = var3;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%group%", this.target.getName());
   }

   public void construct(Button var1, Group var2) {
      boolean var3 = this.target.getInheritedGroups().contains(var2.toStored());
      if (var3) {
         this.InheritedButton(var1, var2);
      } else {
         this.NotInheritedButton(var1, var2);
      }
   }

   public Group[] getObjects() {
      return this.plugin
         .getGroups()
         .servers(true, this.target.getServer().orElse(null))
         .worlds(true, this.target.getWorld().orElse(null))
         .stream()
         .filter(var1 -> !var1.equals(this.target))
         .toArray(Group[]::new);
   }

   private void InheritedButton(Button var1, Group var2) {
      var1.material(XMaterial.YELLOW_STAINED_GLASS_PANE)
         .name(Animation.wave(var2.getName(), Colors.YELLOW, Colors.WHITE))
         .lore(INHERITING_ACTION.get(), "", INHERITING_EXPLANATION.get().replace("%from%", var2.getName()).replace("%to%", this.target.getName()));
      var1.action(var2x -> this.target.removeInheritance(var2));
   }

   private void NotInheritedButton(Button var1, Group var2) {
      var1.material(XMaterial.GRAY_STAINED_GLASS_PANE)
         .name(Animation.wave(var2.getName(), Colors.Gray, Colors.WHITE))
         .lore(NOT_INHERITING_ACTION.get(), "", NOT_INHERITING_EXPLANATION.get().replace("%from%", var2.getName()).replace("%to%", this.target.getName()));
      var1.action(var2x -> this.target.addInheritance(var2));
   }

   @Override
   public SearchFeature<Group> getSearch() {
      return null;
   }
}
