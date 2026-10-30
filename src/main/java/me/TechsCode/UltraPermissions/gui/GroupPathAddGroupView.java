package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import org.bukkit.entity.Player;

public abstract class GroupPathAddGroupView extends PageableGUI<Group> {
   private final UltraPermissions plugin;
   private final GroupPath groupPath;
   private static final Phrase TITLE = Phrase.create("groupPathAddGroupView.title", "%groupPath% > Add Group to Path");
   private static final Phrase ENTRY_ACTION = Phrase.create(
      "groupPathAddGroupView.entry.action", "Click to **add** to **%groupPath%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );

   public GroupPathAddGroupView(Player var1, UltraPermissions var2, GroupPath var3) {
      super(var1, var2);
      this.plugin = var2;
      this.groupPath = var3;
   }

   public Group[] getObjects() {
      return this.plugin
         .getGroups()
         .stream()
         .filter(var1 -> this.plugin.getGroupPaths().stream().noneMatch(var1x -> var1x.getGroupPath().contains(var1)))
         .toArray(Group[]::new);
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%groupPath%", this.groupPath.getName());
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.Green, Colors.WHITE))
         .lore(ENTRY_ACTION.get().replace("%groupPath%", this.groupPath.getName()))
         .setDamage(var2.getDamage());
      var1.action(var2x -> {
         this.groupPath.addGroupToGroupPath(var2);
         this.onBack();
      });
   }

   @Override
   public SearchFeature<Group> getSearch() {
      return new BasicSearch<Group>() {
         public String[] getSearchableText(Group var1) {
            return new String[]{var1.getName()};
         }
      };
   }
}
