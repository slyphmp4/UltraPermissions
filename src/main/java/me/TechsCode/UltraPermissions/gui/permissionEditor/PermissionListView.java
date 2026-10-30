package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.PermissionColor;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.LoreLists;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class PermissionListView extends PageableGUI<PermissionWithInfo> {
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private final long opened;
   private static final Phrase NOT_ADDED_ACTION = Phrase.create(
      "permissionListView.notAdded.action", "Click to **Quick Add** permission", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase NOT_ADDED_PLACEHOLDERS_TITLE = Phrase.create("permissionListView.placeholders.title", "Please note that", Colors.GRAY);
   private static final Phrase NOT_ADDED_PLACEHOLDERS_ENTRY = Phrase.create(
      "permissionListView.placeholders.entry", "- **[%placeholder%]** is a placeholder", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase LORE_ALL_SERVERS = Phrase.create(
      "permissionListView.lore.allServersAttribute", "**•** Permission available on all Servers", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase LORE_SPECIFIC_SERVER = Phrase.create(
      "permissionListView.lore.specificServerAttribute", "**•** Only applying to Server '**%server%**'", Colors.GRAY, Colors.WHITE, Colors.WHITE
   );
   private static final Phrase LORE_ALL_WORLDS = Phrase.create(
      "permissionListView.lore.allWorldsAttribute", "**•** Permission applies to every World", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase LORE_SPECIFIC_WORLD = Phrase.create(
      "permissionListView.lore.specificWorldAttribute", "**•** Exclusive to the World '**%world%**'", Colors.GRAY, Colors.WHITE, Colors.WHITE
   );
   private static final Phrase LORE_TEMPORARY = Phrase.create(
      "permissionListView.lore.temporaryAttribute", "**•** Will expire in **%time%**", Colors.GRAY, Colors.WHITE, Colors.RED
   );
   private static final Phrase SINGLE_EDIT_ACTION = Phrase.create(
      "permissionListView.single.editAction", "**Left Click** to edit this Permission", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase SINGLE_DELETE_ACTION = Phrase.create(
      "permissionListView.single.deleteAction", "**Right Click** to **delete**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase SINGLE_SETTINGS_TITLE = Phrase.create("permissionListView.single.settingsTitle", "Settings:", Colors.GRAY);
   private static final Phrase MULTI_TITLE = Phrase.create("permissionListView.title", "%permission% (See below)");
   private static final Phrase MULTI_ACTION = Phrase.create("permissionListView.action", "Click to view all Permission Copies", Colors.GRAY);
   private static final Phrase INHERITED_VIEW_ACTION = Phrase.create(
      "permissionListView.inherited.viewAction", "**Left Click** to view this Permission", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase INHERITED_NEGATE_ACTION = Phrase.create(
      "permissionListView.inherited.negateAction", "**Right Click** to **Quick-Negate** this permission", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase INHERITED_SOURCE_DESC = Phrase.create(
      "permissionListView.inherited.sourceDesc", "This Permission originates from **%group%**", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase INHERITED_SETTINGS_TITLE = Phrase.create("permissionListView.inherited.settingsTitle", "Settings:", Colors.GRAY);

   public PermissionListView(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2);
      this.plugin = var2;
      this.holder = var3;
      this.opened = System.currentTimeMillis();
   }

   @Override
   public SearchFeature<PermissionWithInfo> getSearch() {
      return new BasicSearch<PermissionWithInfo>() {
         public String[] getSearchableText(PermissionWithInfo var1) {
            return new String[]{var1.getName(), var1.hasInfo() ? var1.getInfo().getDescription() : "xxx"};
         }
      };
   }

   public void construct(Button var1, PermissionWithInfo var2) {
      PermissionCopyList var3 = new PermissionCopyList(
         this.holder.getPermissions().name(var2.getName()), this.holder.getAdditionalPermissions().name(var2.getName())
      );
      if (var3.size() == 0) {
         this.NotAddedPermission(var1, var2);
      } else if (var3.size() == 1) {
         if (var3.inheritedCopies().size() == 1) {
            this.InheritedOnlyButton(var1, var2, var3.get(0));
         } else {
            this.SingleAddedPermissionButton(var1, var2, var3.get(0));
         }
      } else {
         this.MultiAddedPermissionButton(var1, var2, var3);
      }

      if (var2.hasInfo()) {
         var1.item().appendLore("");
         var1.item().appendLore(var2.getInfo().asLore());
      }
   }

   private void NotAddedPermission(Button var1, PermissionWithInfo var2) {
      var1.material(XMaterial.WHITE_STAINED_GLASS_PANE).name(Animation.wave(var2.getName(), Colors.WHITE, Colors.GRAY)).lore(NOT_ADDED_ACTION.get());
      if (var2.hasInfo() && var2.getInfo().hasPlaceholders()) {
         var1.item().appendLore("", NOT_ADDED_PLACEHOLDERS_TITLE.get());

         for (String var6 : var2.getInfo().getPlaceholders()) {
            var1.item().appendLore(NOT_ADDED_PLACEHOLDERS_ENTRY.get().replace("%placeholder%", var6));
         }
      }

      var1.action(var2x -> {
         if (var2x == ActionType.LEFT) {
            new PlaceholderFillDialog(this.p, this.plugin, var2.getName()) {
               @Override
               public void onBack() {
                  PermissionListView.this.reopen();
               }

               @Override
               public void onComplete(String var1) {
                  PermissionListView.this.holder.newPermission(var1).create();
                  PermissionListView.this.reopen();
               }
            };
         }
      });
   }

   private List<String> permissionSettingsLore(PermissionCopy var1) {
      ArrayList var2 = new ArrayList();
      Optional var3 = var1.getPermission().getServer();
      if (var3.isPresent()) {
         var2.add(LORE_SPECIFIC_SERVER.get().replace("%server%", var1.getColor().getColor() + (String)var3.get()));
      } else {
         var2.add(LORE_ALL_SERVERS.get());
      }

      if (var1.getPermission().getWorld().isPresent()) {
         var2.add(LORE_SPECIFIC_WORLD.get().replace("%world%", var1.getPermission().getWorld().get()));
      } else {
         var2.add(LORE_ALL_WORLDS.get());
      }

      if (var1.getPermission().getExpiration() != 0L) {
         long var4 = var1.getPermission().getExpiration() - System.currentTimeMillis();
         String var6 = Tools.getTimeString(var4, TimeUnit.MILLISECONDS, 2);
         var2.add(LORE_TEMPORARY.get().replace("%time%", var6));
      }

      return var2;
   }

   private void SingleAddedPermissionButton(Button var1, PermissionWithInfo var2, PermissionCopy var3) {
      PermissionColor var4 = PermissionColor.fromPermission(var3);
      var1.material(var4.getGlassPane())
         .name((var3.getPermission().isPositive() ? "" : Colors.RED + "-") + Animation.wave(var2.getName(), var4.getColor(), Colors.WHITE))
         .lore(SINGLE_EDIT_ACTION.get(), SINGLE_DELETE_ACTION.get(), "");
      var1.item().appendLore(SINGLE_SETTINGS_TITLE.get());
      var1.item().appendLore(this.permissionSettingsLore(var3));
      var1.action(var3x -> {
         if (var3x == ActionType.LEFT) {
            new PermissionView(this.p, this.plugin, this.holder, var2) {
               @Override
               public void onBack() {
                  PermissionListView.this.reopen();
               }
            };
         }

         if (var3x == ActionType.RIGHT) {
            var3.getPermission().remove();
            this.reopen();
         }
      });
   }

   private void MultiAddedPermissionButton(Button var1, PermissionWithInfo var2, PermissionCopyList var3) {
      var1.material(PermissionColor.cycle(var3).getGlassBlock())
         .name(Animation.wave(MULTI_TITLE.get().replace("%permission%", var2.getName()), Colors.GRAY, Colors.WHITE))
         .lore(MULTI_ACTION.get(), "")
         .amount(var3.size());
      var1.item().appendLore(LoreLists.printPermissionCopiesList(var3, this.opened));
      var1.action(var2x -> new PermissionView(this.p, this.plugin, this.holder, var2) {
         @Override
         public void onBack() {
            PermissionListView.this.reopen();
         }
      });
   }

   private void InheritedOnlyButton(Button var1, PermissionWithInfo var2, PermissionCopy var3) {
      var1.material(PermissionColor.INHERITED.getGlassPane())
         .name(Animation.wave(var2.getName(), Colors.YELLOW, Colors.WHITE))
         .lore(INHERITED_VIEW_ACTION.get());
      var1.action(var2x -> new PermissionView(this.p, this.plugin, this.holder, var2) {
         @Override
         public void onBack() {
            PermissionListView.this.reopen();
         }
      });
      if (var3.getPermission().isPositive()) {
         var1.item().appendLore(INHERITED_NEGATE_ACTION.get());
         var1.action(
            var3x -> {
               if (var3x == ActionType.RIGHT) {
                  this.holder
                     .newPermission(var2.getName())
                     .setPositive(false)
                     .setServer(var3.getPermission().getServer().orElse(null))
                     .setWorld(var3.getPermission().getWorld().orElse(null))
                     .create();
                  this.reopen();
               }
            }
         );
      }

      var1.item()
         .appendLore("", INHERITED_SOURCE_DESC.get().replace("%group%", var3.getPermission().getHolder().getName()), "", INHERITED_SETTINGS_TITLE.get());
      var1.item().appendLore(this.permissionSettingsLore(var3));
   }
}
