package me.TechsCode.UltraPermissions.gui;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.views.ConfirmationView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;
import org.bukkit.entity.Player;

public abstract class GroupUserListView extends PageableGUI<UserRankup> {
   private final Group group;
   private final UltraPermissions plugin;
   private static final Phrase TITLE = Phrase.create("groupUserListView.title", "%group% > Users");
   private static final Phrase WIPE_TITLE = Phrase.create("groupUserListView.wipe.title", "Wipe Users");
   private static final Phrase WIPE_ACTION = Phrase.create(
      "groupUserListView.wipe.action", "Click to **remove** all Users from this Group", Colors.GRAY, Colors.RED
   );
   private static final Phrase ADDING_TITLE = Phrase.create("groupUserListView.addUsers.title", "Add Users");
   private static final Phrase ADDING_ACTION = Phrase.create(
      "groupUserListView.addUsers.action", "Click to **add** multiple Users to this Group", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase ENTRY_ACTION = Phrase.create("groupUserListView.entry.action", "Click to **remove** User", Colors.GRAY, Colors.RED);
   private static final Phrase ENTRY_EXPLANATION = Phrase.create(
      "groupUserListView.entry.explanation", "This group is added **permanently** to that user", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase ENTRY_TIME_TITLE = Phrase.create("groupUserListView.entry.timeTitle", "Time until removal:", Colors.GRAY);

   public GroupUserListView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.group = var3;
      this.plugin = var2;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%group%", this.group.getName());
   }

   @Override
   public SearchFeature<UserRankup> getSearch() {
      return new BasicSearch<UserRankup>() {
         public String[] getSearchableText(UserRankup var1) {
            return new String[]{var1.getUser().getName()};
         }
      };
   }

   public UserRankup[] getObjects() {
      return this.group.getRankups().toArray(new UserRankup[0]);
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(var1x -> {
         var1x.material(XMaterial.PLAYER_HEAD).name(Animation.fading(ADDING_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADDING_ACTION.get());
         var1x.action(var1xx -> new PlayerSelectorView(this.p, this.group, this.plugin) {
            @Override
            public void select(User var1) {
            }

            @Override
            public void onBack() {
               GroupUserListView.this.reopen();
            }
         });
      }, 52);
      var1.button(var1x -> {
         var1x.material(XMaterial.REDSTONE_BLOCK).name(Animation.fading(WIPE_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(WIPE_ACTION.get());
         var1x.action(var1xx -> new ConfirmationView(this.p, this.plugin, "Wipe all Users from " + this.group.getName()) {
            @Override
            public void choose(boolean var1) {
               if (var1) {
                  Arrays.stream(GroupUserListView.this.getObjects()).forEach(UserRankup::remove);
               }

               GroupUserListView.this.reopen();
            }
         });
      }, 53);
   }

   public void construct(Button var1, UserRankup var2) {
      Optional var3 = var2.getUser().getSkinTexture();
      if (var3.isPresent()) {
         var1.material(XMaterial.PLAYER_HEAD).setSkullTexture((SkinTexture)var3.get());
      } else {
         var1.material(XMaterial.GRAY_STAINED_GLASS);
      }

      var1.item().name(Animation.wave(var2.getUser().getName(), Colors.Blue, Colors.WHITE)).lore(ENTRY_ACTION.get());
      if (var2.isPermanent()) {
         var1.item().appendLore("", ENTRY_EXPLANATION.get());
      } else {
         var1.item()
            .appendLore("")
            .appendLore(ENTRY_TIME_TITLE.get())
            .appendLore(Colors.RED + Tools.getTimeString(var2.getExpiry() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
      }

      var1.action(var1x -> var2.remove());
   }
}
