package me.TechsCode.UltraPermissions.gui;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.ColorPalette;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.views.ConfirmationView;
import me.TechsCode.UltraPermissions.base.views.MaterialPickerView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.base.visual.LoreScroller;
import me.TechsCode.UltraPermissions.gui.dialogs.CreateGroupPathDialog;
import me.TechsCode.UltraPermissions.gui.dialogs.RenameGroupPathDialog;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class GroupPathListView extends PageableGUI<GroupPath> {
   private static final Phrase TITLE = Phrase.create("groupPathListView.titleAllGroups", "Group Paths");
   private final Player p;
   private final UltraPermissions plugin;
   private long start;
   private static final Phrase BUTTON_LORE_LEFT_CLICK = Phrase.create(
      "groupPathListView.construct.lore.leftClick", "**Left Click** to edit group path", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase BUTTON_LORE_RIGHT_CLICK = Phrase.create(
      "groupPathListView.construct.lore.rightClick", "**Right Click** to change name", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase BUTTON_LORE_MIDDLE_CLICK = Phrase.create(
      "groupPathListView.construct.lore.middleClick", "**Middle Click** to change description", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase BUTTON_LORE_SHIFT_LEFT_CLICK = Phrase.create(
      "groupPathListView.construct.lore.shiftLeftClick", "**Shift + Left Click** to change icon", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase BUTTON_LORE_SHIFT_RIGHT_CLICK = Phrase.create(
      "groupPathListView.construct.lore.shiftRightClick", "**Shift + Right Click** to %action% group path", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase BUTTON_LORE_PRESS_Q = Phrase.create(
      "groupPathListView.construct.lore.pressQ", "**Press Q** to **delete**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase BUTTON_ACTION_DELETION_TITLE = Phrase.create(
      "groupPathListView.construct.action.deletion.title", "deletion of group path '%pathName%'"
   );
   private static final Phrase BUTTON_LORE_DESCRIPTION = Phrase.create("groupPathListView.construct.lore.description", "Description", Colors.GRAY);
   private static final Phrase BUTTON_ACTION_MATERIAL_PICKER_TITLE = Phrase.create(
      "groupPathListView.construct.action.materialPicker.title", "%groupPathName% > Icon"
   );
   private static final Phrase BUTTON_LORE_GROUPS_INSIDE_PATH = Phrase.create(
      "groupPathListView.construct.lore.groupsInsidePath", "Groups inside Path", Colors.GRAY
   );
   private static final Phrase BUTTON_ACTION_DESCRIPTION_TITLE_MAIN = Phrase.create(
      "groupPathListView.construct.action.description.title.main", "Group Path Description", Colors.AQUA
   );
   private static final Phrase BUTTON_ACTION_DESCRIPTION_TITLE_SUB = Phrase.create(
      "groupPathListView.construct.action.description.title.sub", "Enter a description for the group path", Colors.GRAY
   );
   private static final Phrase BUTTON_ACTION_DESCRIPTION_TITLE_ACTION_BAR = Phrase.create(
      "groupPathListView.construct.action.description.title.actionBar", "Type in **none** to reset the description", Colors.GRAY, Colors.RED
   );
   private static final Phrase BUTTON_LORE_STATUS = Phrase.create("groupPathListView.construct.lore.status", "Status: %status%", Colors.GRAY);
   private static final Phrase ADD_TITLE = Phrase.create("groupPathListView.add.title", "Create Group Path");
   private static final Phrase ADD_ACTION = Phrase.create("groupPathListView.add.action", "Click to create group path", Colors.GRAY);
   private static final Phrase REORDER_TITLE = Phrase.create("groupPathListView.reorder.title", "Change Order");
   private static final Phrase REORDER_ACTION = Phrase.create("groupPathListView.reorder.action", "Click to modify the order", Colors.GRAY);

   public GroupPathListView(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.start = System.currentTimeMillis();
   }

   @Override
   public String getTitle() {
      return TITLE.get();
   }

   public void construct(Button var1, GroupPath var2) {
      ItemStack var3 = var2.getIcon();
      List<String> var4 = var2.getGroupPath().stream().map(var0 -> var0.getName()).collect(Collectors.toList());
      var1.material(var3.getType()).setDamage(var3.getDurability());
      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1) && var3.hasItemMeta() && var3.getItemMeta().hasCustomModelData()) {
         var1.material(var3.getType(), var3.getItemMeta().getCustomModelData()).setDamage(var3.getDurability());
      }

      var1.item()
         .name(Animation.wave(var2.getName(), ColorPalette.MAIN, Colors.WHITE))
         .lore(
            BUTTON_LORE_LEFT_CLICK.get(),
            BUTTON_LORE_RIGHT_CLICK.get(),
            BUTTON_LORE_MIDDLE_CLICK.get(),
            BUTTON_LORE_SHIFT_LEFT_CLICK.get(),
            BUTTON_LORE_SHIFT_RIGHT_CLICK.get().replace("%action%", var2.isEnabled() ? "§c" + Words.DISABLE.get() + "§7" : "§a" + Words.ENABLE.get() + "§7"),
            BUTTON_LORE_PRESS_Q.get(),
            "",
            BUTTON_LORE_STATUS.get().replace("%status%", var2.isEnabled() ? "§a" + Words.ENABLED.get() : "§c" + Words.DISABLED.get()),
            "",
            BUTTON_LORE_GROUPS_INSIDE_PATH.get() + "§7:"
         );
      if (!var2.getGroupPath().isEmpty()) {
         for (String var6 : LoreScroller.scroller(var4, 3, this.start)) {
            var1.item().appendLore("§7- §e" + var6);
         }
      } else {
         var1.item().appendLore("§7- §c" + Words.NONE.get());
      }

      if (var2.getDescription().length() > 0) {
         var1.item().appendLore("", BUTTON_LORE_DESCRIPTION.get() + "§7:", "§f" + var2.getDescription());
      }

      var1.action(
         var2x -> {
            if (var2x == ActionType.LEFT) {
               new GroupPathEditView(this.p, this.plugin, var2) {
                  @Override
                  public void onBack() {
                     GroupPathListView.this.reopen();
                  }
               };
            }

            if (var2x == ActionType.RIGHT) {
               new RenameGroupPathDialog(this.p, this.plugin, var2) {
                  @Override
                  public void reopen() {
                     GroupPathListView.this.reopen();
                  }
               };
            }

            if (var2x == ActionType.MIDDLE) {
               new UserInput(
                  this.p,
                  this.plugin,
                  BUTTON_ACTION_DESCRIPTION_TITLE_MAIN.get(),
                  BUTTON_ACTION_DESCRIPTION_TITLE_SUB.get(),
                  BUTTON_ACTION_DESCRIPTION_TITLE_ACTION_BAR.get()
               ) {
                  @Override
                  public void onClose(Player var1) {
                     GroupPathListView.this.reopen();
                  }

                  @Override
                  public boolean onResult(String var1) {
                     if (var1.equalsIgnoreCase("none")) {
                        var2.setDescription("");
                     } else {
                        var2.setDescription(var1);
                     }

                     GroupPathListView.this.reopen();
                     return true;
                  }
               };
            }

            if (var2x == ActionType.SHIFT_LEFT) {
               new MaterialPickerView(this.p, this.plugin, BUTTON_ACTION_MATERIAL_PICKER_TITLE.get().replace("%groupPathName%", var2.getName())) {
                  @Override
                  public void onBack() {
                     GroupPathListView.this.reopen();
                  }

                  @Override
                  public void choose(Player var1, XMaterial var2xx) {
                     var2.setIcon(var2xx.parseItem());
                     GroupPathListView.this.reopen();
                  }
               };
            }

            if (var2x == ActionType.SHIFT_RIGHT) {
               var2.setEnabled(!var2.isEnabled());
            }

            if (var2x == ActionType.Q) {
               new ConfirmationView(this.p, this.plugin, BUTTON_ACTION_DELETION_TITLE.get().replace("%pathName%", var2.getName())) {
                  @Override
                  public void choose(boolean var1) {
                     if (var1) {
                        var2.delete();
                     }

                     GroupPathListView.this.reopen();
                  }
               };
            }
         }
      );
   }

   private void SelectorLore(Button var1, String var2, Collection<String> var3) {
      var1.item().appendLore(var3.stream().map(var1x -> var1x.equals(var2) ? "§a> §7" + var1x : "§7  " + var1x).toArray(String[]::new));
   }

   private <X> X next(X var1, List<X> var2) {
      int var3 = var2.indexOf(var1);
      return (X)var2.get(var3 + 1 == var2.size() ? 0 : var3 + 1);
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(52, this::ReorderButton);
      var1.button(53, this::AddButton);
   }

   private void AddButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.fading(ADD_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(ADD_ACTION.get());
      var1.action(var1x -> new CreateGroupPathDialog(this.p, this.plugin) {
         @Override
         public void reopen() {
            GroupPathListView.this.reopen();
         }
      });
   }

   public void ReorderButton(Button var1) {
      var1.material(XMaterial.ITEM_FRAME).name(Animation.fading(REORDER_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(REORDER_ACTION.get());
      var1.action(var1x -> new GroupPathReorderView(this.p, this.plugin) {
         @Override
         public void onBack() {
            GroupPathListView.this.reopen();
         }
      });
   }

   public GroupPath[] getObjects() {
      return this.plugin.getGroupPaths().stream().sorted(Comparator.comparingInt(GroupPath::getPriority)).toArray(GroupPath[]::new);
   }

   @Override
   public SearchFeature<GroupPath> getSearch() {
      return new BasicSearch<GroupPath>() {
         public String[] getSearchableText(GroupPath var1) {
            return new String[]{var1.getName()};
         }
      };
   }
}
