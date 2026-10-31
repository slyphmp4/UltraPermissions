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
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public abstract class UserGroupAddView extends PageableGUI<Group> {
   private final UltraPermissions plugin;
   private final User user;
   private static final Phrase TITLE = Phrase.create("userGroupAddView.title", "%user% > Add Group");
   private static final Phrase DURATION_TITLE = Phrase.create("userGroupAddView.duration.title", "Duration >");
   private static final Phrase DURATION_PERMANENT = Phrase.create("userGroupAddView.duration.permanent", "Permanent");
   private static final Phrase ENTRY_ACTION = Phrase.create(
      "userGroupAddView.entry.action", "Click to **add** to **%user%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );

   public UserGroupAddView(Player var1, UltraPermissions var2, User var3) {
      super(var1, var2);
      this.plugin = var2;
      this.user = var3;
   }

   public Group[] getObjects() {
      return this.plugin.getGroups().stream().filter(var1 -> !this.user.getGroups().contains(var1.toStored())).toArray(Group[]::new);
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%user%", this.user.getName());
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.Green, Colors.WHITE))
         .lore(ENTRY_ACTION.get().replace("%user%", this.user.getName()))
         .setDamage(var2.getDamage());
      var1.action(var2x -> new TimePickerView(this.p, this.plugin, DURATION_TITLE.get(), DURATION_PERMANENT.get(), true, 0L) {
         @Override
         public void onResult(long var1) {
            if (var1 == 0L) {
               UserGroupAddView.this.user.addGroup(var2);
            } else {
               long var3 = System.currentTimeMillis() + var1 * 1000L;
               UserGroupAddView.this.user.addGroup(var2, var3);
            }

            UserGroupAddView.this.onBack();
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
