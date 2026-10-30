package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.views.TimePickerView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GrantGroupsAddView extends PageableGUI<Group> {
   private final UltraPermissions plugin;
   private final Group group;
   private static final Phrase TITLE = Phrase.create("GrantGroupsAddView.title", "%group% > Add Group");
   private static final Phrase ENTRY_ACTION = Phrase.create(
      "GrantGroupsAddView.entry.action", "Click to **add** to **%group%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );
   private static final Phrase ENTRY_ACTION_TIME_TITLE_PREFIX = Phrase.create("GrantGroupsAddView.entry.action.time.titlePrefix", "Duration >", Colors.GRAY);
   private static final Phrase ENTRY_ACTION_TIME_NO_TIME = Phrase.create("GrantGroupsAddView.entry.action.time.noTimePhrase", "Permanent", Colors.GRAY);

   public GrantGroupsAddView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.plugin = var2;
      this.group = var3;
   }

   public Group[] getObjects() {
      return this.plugin.getGroups().stream().filter(var1 -> !this.group.getGrantGroups().keySet().contains(var1.toStored())).toArray(Group[]::new);
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%group%", this.group.getName());
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.Green, Colors.WHITE))
         .lore(ENTRY_ACTION.get().replace("%group%", this.group.getName()))
         .setDamage(var2.getDamage());
      var1.action(var2x -> new TimePickerView(this.p, this.plugin, ENTRY_ACTION_TIME_TITLE_PREFIX.get(), ENTRY_ACTION_TIME_NO_TIME.get(), true, 0L) {
         @Override
         public void onResult(long var1) {
            if (var1 == 0L) {
               GrantGroupsAddView.this.group.addGrantGroup(var2);
            } else {
               GrantGroupsAddView.this.group.addGrantGroup(var2, var1);
            }

            GrantGroupsAddView.this.onBack();
         }
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
