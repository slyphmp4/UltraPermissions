package me.TechsCode.UltraPermissions.gui;

import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;
import org.bukkit.entity.Player;

public abstract class UserGroupListView extends PageableGUI<UserRankup> {
   private final Player p;
   private final UltraPermissions plugin;
   private final User user;
   private static final Phrase TITLE = Phrase.create("userGroupListView.title", "%user% > Groups");
   private static final Phrase ENTRY_ACTION = Phrase.create("userGroupListView.entry.action", "Click to **remove** group", Colors.GRAY, Colors.RED);
   private static final Phrase ENTRY_TIME_TITLE = Phrase.create("userGroupListView.entry.timeTitle", "Time until removal:", Colors.GRAY);
   private static final Phrase ADD_TITLE = Phrase.create("userGroupListView.add.title", "Add");
   private static final Phrase ADD_ACTION = Phrase.create("userGroupListView.add.action", "Click to add group", Colors.GRAY);

   public UserGroupListView(Player var1, UltraPermissions var2, User var3) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.user = var3;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%user%", this.user.getName());
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(this::AddButton, this.getRightOptionSlot());
   }

   public void construct(Button var1, UserRankup var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getGroupName(), Colors.YELLOW, Colors.WHITE))
         .lore(ENTRY_ACTION.get())
         .setDamage(var2.getDamage());
      if (!var2.isPermanent()) {
         var1.item()
            .appendLore("")
            .appendLore(ENTRY_TIME_TITLE.get())
            .appendLore(Colors.RED + Tools.getTimeString(var2.getExpiry() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
      }

      var1.action(var1x -> var2.remove());
   }

   public UserRankup[] getObjects() {
      return this.user.getRankups().toArray(new UserRankup[0]);
   }

   @Override
   public SearchFeature<UserRankup> getSearch() {
      return new BasicSearch<UserRankup>() {
         public String[] getSearchableText(UserRankup var1) {
            return new String[]{var1.getGroup().get().map(Group::getName).orElse("xxx")};
         }
      };
   }

   private void AddButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.fading(ADD_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADD_ACTION.get());
      var1.action(var1x -> new UserGroupAddView(this.p, this.plugin, this.user) {
         @Override
         public void onBack() {
            UserGroupListView.this.reopen();
         }
      });
   }
}
