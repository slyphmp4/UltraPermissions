package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.TechsCode.UltraPermissions.PermissionColor;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.LoreLists;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfoList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class PermissionEditorMainView extends GUI {
   private final int[] layout = new int[]{11, 12, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35};
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private final long opened;
   private final List<PermissionEditorMainView.PluginEntry> entries;
   private int page;
   private static final Phrase TITLE = Phrase.create("permissionEditorMainView.title", "%holder% > Permissions");
   private static final Phrase ENTRY_ACTION = Phrase.create("permissionEditorMainView.entry.action", "Click to view Permissions", Colors.GRAY);
   private static final Phrase ENTRY_PERMISSIONS_TITLE = Phrase.create("permissionEditorMainView.entry.permissionsTitle", "Added Permissions", Colors.GRAY);
   private static final Phrase ENTRY_FOOTER = Phrase.create(
      "permissionEditorMainView.entry.footer", "Found **%count% Permissions** for this plugin in the Permissions Database", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase NEXT_TITLE = Phrase.create("permissionEditorMainView.next.title", "Next");
   private static final Phrase NEXT_ACTION = Phrase.create("permissionEditorMainView.next.action", "Click to go to the next page", Colors.GRAY);
   private static final Phrase PREVIOUS_TITLE = Phrase.create("permissionEditorMainView.previous.title", "Previous");
   private static final Phrase PREVIOUS_ACTION = Phrase.create("permissionEditorMainView.previous.action", "Click to go to the previous page", Colors.GRAY);
   private static final Phrase ALL_PERMISSIONS_TITLE = Phrase.create("permissionEditorMainView.allPermissions.title", "All Permissions");
   private static final Phrase ALL_PERMISSIONS_ACTION = Phrase.create(
      "permissionEditorMainView.allPermissions.action", "Click to show all added Permissions", Colors.GRAY
   );
   private static final Phrase ADD_PERMISSION_TITLE = Phrase.create("permissionEditorMainView.addPermission.title", "Add Permission");
   private static final Phrase ADD_PERMISSION_ADD_ACTION = Phrase.create(
      "permissionEditorMainView.addPermission.addAction", "**Left Click** to add a Permission via Chat", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase ADD_PERMISSION_ADD_BUNGEE_ACTION = Phrase.create(
      "permissionEditorMainView.addPermission.addBungeeAction", "**Right Click** to add **Bungee Permission**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase PURGE_PERMISSION_TITLE = Phrase.create("permissionEditorMainView.purgePermission.title", "Purge specific Permission");
   private static final Phrase PURGE_PPERMISSION_ACTION = Phrase.create(
      "permissionEditorMainView.purgePlayerPermission.addAction",
      "**Left Click** to purge a Permission from all **Players**",
      Colors.GRAY,
      Colors.AQUA,
      Colors.YELLOW
   );
   private static final Phrase PURGE_GPERMISSION_ACTION = Phrase.create(
      "permissionEditorMainView.purgeGroupPermission.addAction",
      "**Right Click** to purge a Permission from all **Groups**",
      Colors.GRAY,
      Colors.AQUA,
      Colors.YELLOW
   );
   private static final Phrase SEARCH_TITLE = Phrase.create("permissionEditorMainView.search.title", "Search a Permission");
   private static final Phrase SEARCH_ACTION = Phrase.create("permissionEditorMainView.search.action", "Click to search for a Permission", Colors.GRAY);

   public PermissionEditorMainView(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2);
      this.plugin = var2;
      this.holder = var3;
      this.opened = System.currentTimeMillis();
      this.entries = Stream.concat(Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName), Arrays.stream(new String[]{"Bukkit"}))
         .sorted()
         .map(var2x -> new PermissionEditorMainView.PluginEntry(var2, var2x))
         .filter(var0 -> !var0.permissionsFromDatabase.isEmpty())
         .collect(Collectors.toList());
      this.page = 0;
   }

   @Override
   public void reopen() {
      super.reopen();
      this.entries.forEach(PermissionEditorMainView.PluginEntry::retrieveData);
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(TITLE.get().replace("%holder%", this.holder.getName()));
      var1.setSlots(54);
      int var2 = this.page * this.layout.length;

      for (int var6 : this.layout) {
         PermissionEditorMainView.PluginEntry var7 = this.entries.size() > var2 ? this.entries.get(var2) : null;
         var1.button(var6, var7 != null ? var2x -> this.PluginWithPermissionsButton(var2x, var7) : this::PlaceholderButton);
         var2++;
      }

      if (this.page != 0) {
         var1.button(10, this::PreviousButton);
      }

      if (this.entries.size() / this.layout.length > this.page) {
         var1.button(18, this::NextButton);
      }

      var1.button(47, this::SearchButton);
      var1.button(50, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
      var1.button(52, this::AddedPermissionButton);
      var1.button(53, this::TypeInButton);
      var1.button(54, this::PurgePermissionButton);
   }

   private void PluginWithPermissionsButton(Button var1, PermissionEditorMainView.PluginEntry var2) {
      if (var2.permissionCopies.isEmpty()) {
         var1.material(XMaterial.WHITE_STAINED_GLASS_PANE);
      } else if (var2.permissionCopies.size() == 1) {
         var1.material(PermissionColor.fromPermission(var2.permissionCopies.get(0)).getGlassPane());
      } else {
         var1.material(PermissionColor.cycle(var2.permissionCopies).getGlassBlock());
      }

      var1.item()
         .amount(Math.max(1, var2.permissionCopies.size()))
         .name(Animation.wave(var2.name, PermissionColor.ALL_SERVERS.getColor(), Colors.WHITE))
         .lore(ENTRY_ACTION.get(), "");
      if (!var2.permissionCopies.isEmpty()) {
         var1.item().appendLore(ENTRY_PERMISSIONS_TITLE.get());
      }

      var1.item().appendLore(LoreLists.printPermissionCopiesList(var2.permissionCopies, this.opened));
      var1.item().appendLore("");
      int var3 = var2.permissionsFromDatabase.size();
      var1.item().appendLore(Arrays.stream(ENTRY_FOOTER.split(80)).map(var1x -> var1x.replace("%count%", Integer.toString(var3))).toArray(String[]::new));
      var1.action(var2x -> new PluginPermissionListView(this.p, this.plugin, this.holder, var2) {
         @Override
         public void onBack() {
            PermissionEditorMainView.this.reopen();
         }
      });
   }

   private void PlaceholderButton(Button var1) {
      var1.material(XMaterial.GRAY_STAINED_GLASS_PANE).name();
   }

   private void NextButton(Button var1) {
      var1.material(XMaterial.ARROW).name(Animation.wave(NEXT_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(NEXT_ACTION.get());
      var1.action(var1x -> this.page++);
   }

   private void PreviousButton(Button var1) {
      var1.material(XMaterial.ARROW).name(Animation.wave(PREVIOUS_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(PREVIOUS_ACTION.get());
      var1.action(var1x -> this.page--);
   }

   private void AddedPermissionButton(Button var1) {
      var1.material(XMaterial.PAPER).name(Animation.wave(ALL_PERMISSIONS_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ALL_PERMISSIONS_ACTION.get());
      var1.item().amount(Math.min(64, this.holder.getPermissions().size() + this.holder.getAdditionalPermissions().size()));
      var1.action(var1x -> new FullPermissionListView(this.p, this.plugin, this.holder) {
         @Override
         public void onBack() {
            PermissionEditorMainView.this.reopen();
         }
      });
   }

   private void TypeInButton(Button var1) {
      var1.material(XMaterial.NAME_TAG).name(Animation.wave(ADD_PERMISSION_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADD_PERMISSION_ADD_ACTION.get());
      var1.action(var1x -> {
         if (var1x == ActionType.LEFT) {
            new AddPermissionDialog(this.p, this.plugin, this.holder) {
               @Override
               public void reopen() {
                  PermissionEditorMainView.this.reopen();
               }
            };
         }
      });
      if (this.plugin.getMySQLManager().isEnabled() && this.plugin.isConnectedToNetwork()) {
         var1.item().appendLore(ADD_PERMISSION_ADD_BUNGEE_ACTION.get());
         var1.action(var1x -> {
            if (var1x == ActionType.RIGHT) {
               new AddBungeePermissionDialog(this.p, this.plugin, this.holder) {
                  @Override
                  public void reopen() {
                     PermissionEditorMainView.this.reopen();
                  }
               };
            }
         });
      }
   }

   private void PurgePermissionButton(Button var1) {
      var1.material(XMaterial.BARRIER)
         .name(Animation.wave(PURGE_PERMISSION_TITLE.get(), Colors.RED, Colors.AliceBlue))
         .lore(PURGE_PPERMISSION_ACTION.get(), PURGE_GPERMISSION_ACTION.get());
      var1.action(var1x -> {
         if (var1x == ActionType.LEFT) {
            new PurgePlayerPermissionDialog(this.p, this.plugin, this.holder) {
               @Override
               public void reopen() {
                  PermissionEditorMainView.this.reopen();
               }
            };
         }

         if (var1x == ActionType.RIGHT) {
            new PurgeGroupPermissionDialog(this.p, this.plugin, this.holder) {
               @Override
               public void reopen() {
                  PermissionEditorMainView.this.reopen();
               }
            };
         }
      });
   }

   private void SearchButton(Button var1) {
      var1.material(XMaterial.COMPASS).name(Animation.wave(SEARCH_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(SEARCH_ACTION.get());
      var1.action(var1x -> new SearchPermissionDialog(this.p, this.plugin, this.holder) {
         @Override
         public void reopen() {
            PermissionEditorMainView.this.reopen();
         }
      });
   }

   public abstract void onBack();

   class PluginEntry {
      public final String name;
      public PermissionInfoList permissionsFromDatabase;
      public PermissionCopyList permissionCopies;

      public PluginEntry(UltraPermissions var2, String var3) {
         super();
         this.name = var3;
         this.retrieveData();
      }

      public void retrieveData() {
         PermissionList var1 = PermissionEditorMainView.this.holder.getPermissions();
         PermissionList var2 = PermissionEditorMainView.this.holder.getAdditionalPermissions();
         this.permissionsFromDatabase = PermissionEditorMainView.this.plugin.getPermissionDatabase().getPermissionInfosFromPlugin(this.name);
         this.permissionCopies = new PermissionCopyList(var1, var2)
            .stream()
            .filter(var1x -> this.permissionsFromDatabase.find(var1x.getPermission().getName()).isPresent())
            .collect(Collectors.toCollection(PermissionCopyList::new));
      }
   }
}
