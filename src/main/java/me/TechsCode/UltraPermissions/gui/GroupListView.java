package me.TechsCode.UltraPermissions.gui;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.dialogs.CreateGroupDialog;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.entity.Player;

public abstract class GroupListView extends PageableGUI<Group> {
   private static final Phrase TITLE = Phrase.create("groupListView.titleAllGroups", "Groups > Showing %count% Groups");
   private final Player p;
   private final UltraPermissions plugin;
   private boolean showAll;
   private int showing = 0;
   private static final Phrase GROUP_OPEN_ACTION = Phrase.create("groupListView.openAction", "**Left Click** to open", Colors.GRAY, Colors.AQUA);
   private static final Phrase GROUP_STORAGE_SETTINGS_ACTION = Phrase.create(
      "groupListView.storageSettingsAction", "**Press Q** to view Storage Settings", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase GROUP_STORAGE_DEFAULT_INDICATOR = Phrase.create("groupListView.defaultIndicator", "This is a default group", Colors.GREEN);
   private static final Phrase GROUP_ATTRIBUTE_SERVER = Phrase.create("groupListview.attributes.server", "Server: **%server%**", Colors.GRAY, Colors.GOLD);
   private static final Phrase GROUP_ATTRIBUTE_WORLD = Phrase.create("groupListview.attributes.world", "World: **%world%**", Colors.GRAY, Colors.GOLD);
   private static final Phrase GROUP_ATTRIBUTE_PREFIX = Phrase.create("groupListview.attributes.prefix", "Prefix: **%prefix%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase GROUP_ATTRIBUTE_SUFFIX = Phrase.create("groupListview.attributes.suffix", "Suffix: **%suffix%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase GROUP_INHERITS_TITLE = Phrase.create("groupListview.inherits.title", "Inherited Groups:", Colors.GRAY);
   private static final Phrase GROUP_INHERITS_ENTRY = Phrase.create("groupListview.inherits.entry", "- **%group%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase GROUP_INHERITS_NONE = Phrase.create("groupListview.inherits.none", "- **none**", Colors.GRAY, Colors.RED);
   private static final Phrase GROUP_PERMISSION_NODES_COUNT = Phrase.create(
      "groupListview.permissions.count", "**%count%** Permission Nodes", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase ADD_TITLE = Phrase.create("groupListView.add.title", "Create Group");
   private static final Phrase ADD_ACTION = Phrase.create("groupListView.add.action", "Click to create group", Colors.GRAY);
   private static final Phrase REORDER_TITLE = Phrase.create("groupListView.reorder.title", "Change Order");
   private static final Phrase REORDER_ACTION = Phrase.create("groupListView.reorder.action", "Click to modify the order", Colors.GRAY);
   private static final Phrase SHOW_ALL_TITLE = Phrase.create("groupListView.showAll.title", "Show all");
   private static final Phrase SHOW_ALL_ACTION = Phrase.create(
      "groupListView.showAll.action", "Click to show all groups from all Servers & Worlds", Colors.GRAY
   );

   public GroupListView(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.showAll = false;
      int var3 = this.getObjects().length;
      this.showAll = true;
      int var4 = this.getObjects().length;
      if (var3 != var4) {
         this.showAll = false;
      }
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%count%", Integer.toString(this.showing));
   }

   public void construct(Button var1, Group var2) {
      var1.material(var2.getIcon(), var2.getModelId())
         .name(Animation.wave(var2.getName(), Colors.GOLD, Colors.YELLOW, Colors.WHITE))
         .setDamage(var2.getDamage());
      var1.item().appendLore(GROUP_OPEN_ACTION.get()).appendLore(GROUP_STORAGE_SETTINGS_ACTION.get());
      boolean var3 = var2.isDefault();
      Optional<String> var4 = var2.getWorld();
      Optional<String> var5 = var2.getServer();
      if (var3 || var4.isPresent() || var5.isPresent()) {
         var1.item().appendLore("");
      }

      if (var2.isDefault()) {
         var1.item().appendLore(GROUP_STORAGE_DEFAULT_INDICATOR.get());
      }

      var5.ifPresent(var1x -> var1.item().appendLore(GROUP_ATTRIBUTE_SERVER.get().replace("%server%", var1x)));
      var4.ifPresent(var1x -> var1.item().appendLore(GROUP_ATTRIBUTE_WORLD.get().replace("%world%", var1x)));
      String var6 = var2.getPrefix().orElse(Colors.RED + Words.NONE.get());
      String var7 = var2.getSuffix().orElse(Colors.RED + Words.NONE.get());
      var1.item().appendLore("", GROUP_ATTRIBUTE_PREFIX.get().replace("%prefix%", var6), GROUP_ATTRIBUTE_SUFFIX.get().replace("%suffix%", var7), "");
      var1.item().appendLore(GROUP_INHERITS_TITLE.get());
      GroupList var8 = var2.getActiveInheritedGroups().bestToWorst();
      var8.forEach(var1x -> var1.item().appendLore(GROUP_INHERITS_ENTRY.get().replace("%group%", var1x.getName())));
      if (var8.isEmpty()) {
         var1.item().appendLore(GROUP_INHERITS_NONE.get());
      }

      int var9 = var2.getPermissions().size();
      var1.item().appendLore("", GROUP_PERMISSION_NODES_COUNT.get().replace("%count%", Integer.toString(var9)));
      var1.action(var2x -> {
         if (var2x == ActionType.Q) {
            new GroupStorageSettingsView(this.p, this.plugin, var2) {
               @Override
               public void onBack() {
                  GroupListView.this.reopen();
               }
            };
         } else {
            new GroupView(this.p, this.plugin, var2) {
               @Override
               public void onBack() {
                  GroupListView.this.reopen();
               }
            };
         }
      });
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      if (!this.showAll) {
         var1.button(47, this::ShowAllButton);
      }

      var1.button(52, this::ReorderButton);
      var1.button(53, this::AddButton);
   }

   @Override
   protected int getLeftOptionSlot() {
      return this.showAll ? 48 : super.getLeftOptionSlot();
   }

   private void AddButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.fading(ADD_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADD_ACTION.get());
      var1.action(var1x -> new CreateGroupDialog(this.p, this.plugin) {
         @Override
         public void reopen() {
            GroupListView.this.reopen();
         }
      });
   }

   public void ReorderButton(Button var1) {
      var1.material(XMaterial.ITEM_FRAME).name(Animation.fading(REORDER_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(REORDER_ACTION.get());
      var1.action(var1x -> new GroupReorderView(this.p, this.plugin) {
         @Override
         public void onBack() {
            GroupListView.this.reopen();
         }
      });
   }

   public void ShowAllButton(Button var1) {
      var1.material(XMaterial.ENDER_EYE).name(Animation.fading(SHOW_ALL_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(SHOW_ALL_ACTION.split(50));
      var1.action(var1x -> this.showAll = true);
   }

   public Group[] getObjects() {
      GroupList var1 = this.plugin.getGroups();
      if (!this.showAll) {
         var1 = var1.servers(true, this.plugin.getThisServer().map(NServer::getName).orElse(null)).worlds(true, this.p.getWorld().getName());
      }

      this.showing = var1.size();
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
}
