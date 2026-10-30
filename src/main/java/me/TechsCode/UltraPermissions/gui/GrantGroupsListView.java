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
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GrantGroupsListView extends PageableGUI<Group> {
   private final Player p;
   private final UltraPermissions plugin;
   private final Group group;
   private static final Phrase TITLE = Phrase.create("GrantGroupsListView.title", "%group% > Groups");
   private static final Phrase ENTRY_ACTION = Phrase.create("GrantGroupsListView.entry.action", "Click to **remove** group", Colors.GRAY, Colors.RED);
   private static final Phrase ENTRY_TIME_TITLE = Phrase.create("GrantGroupsListView.entry.timeTitle", "Max. duration:", Colors.GRAY);
   private static final Phrase ENTRY_PERMANENT = Phrase.create("GrantGroupsListView.entry.permanent", "Permanent", Colors.RED);
   private static final Phrase ADD_TITLE = Phrase.create("GrantGroupsListView.add.title", "Add");
   private static final Phrase ADD_ACTION = Phrase.create("GrantGroupsListView.add.action", "Click to add group", Colors.GRAY);

   public GrantGroupsListView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.group = var3;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%group%", this.group.getName());
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(this::AddButton, this.getRightOptionSlot());
   }

   public void construct(Button var1, Group var2) {
      long var3 = this.group.getGrantGroups().get(var2.toStored());
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.YELLOW, Colors.WHITE))
         .lore(
            ENTRY_ACTION.get(), "", ENTRY_TIME_TITLE.get() + (var3 == 0L ? " " + ENTRY_PERMANENT.get() : " §e" + Tools.getTimeString(var3, TimeUnit.SECONDS))
         )
         .setDamage(var2.getDamage());
      var1.action(var2x -> this.group.removeGrantGroup(var2));
   }

   public Group[] getObjects() {
      GroupList var1 = new GroupList();
      this.group.getGrantGroups().keySet().stream().filter(var1x -> !var1.contains(var1x.get().get())).forEach(var1x -> var1.add(var1x.get().get()));
      return var1.toArray(new Group[0]);
   }

   @Override
   public SearchFeature<Group> getSearch() {
      return new BasicSearch<Group>() {
         public String[] getSearchableText(Group var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   private void AddButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.fading(ADD_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADD_ACTION.get());
      var1.action(var1x -> new GrantGroupsAddView(this.p, this.plugin, this.group) {
         @Override
         public void onBack() {
            GrantGroupsListView.this.reopen();
         }
      });
   }
}
