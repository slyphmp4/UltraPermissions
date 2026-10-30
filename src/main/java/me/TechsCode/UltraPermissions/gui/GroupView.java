package me.TechsCode.UltraPermissions.gui;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.views.ConfirmationView;
import me.TechsCode.UltraPermissions.base.views.MaterialPickerView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.gui.dialogs.GroupDeleteView;
import me.TechsCode.UltraPermissions.gui.dialogs.GroupFormatDialog;
import me.TechsCode.UltraPermissions.gui.dialogs.GroupRenameDialog;
import me.TechsCode.UltraPermissions.gui.dialogs.PrefixChangeDialog;
import me.TechsCode.UltraPermissions.gui.dialogs.SuffixChangeDialog;
import me.TechsCode.UltraPermissions.gui.permissionEditor.PermissionCopyList;
import me.TechsCode.UltraPermissions.gui.permissionEditor.PermissionEditorMainView;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.visual.VisualType;
import org.bukkit.entity.Player;

public abstract class GroupView extends GUI {
   private static final Phrase TITLE = Phrase.create("groupView.title", "Groups > %group%");
   private final UltraPermissions plugin;
   private final Group group;
   private final long opened;
   private static final Phrase SET_DEFAULT_TITLE = Phrase.create("groupView.setDefault.title", "Default Group");
   private static final Phrase SET_DEFAULT_SET_ACTION = Phrase.create(
      "groupView.setDefault.setAction", "**Click** to make this group **default**", Colors.GRAY, Colors.AQUA, Colors.YELLOW
   );
   private static final Phrase SET_DEFAULT_UNSET_ACTION = Phrase.create(
      "groupView.setDefault.unsetAction", "**Click** to **undefault** this group", Colors.GRAY, Colors.AQUA, Colors.YELLOW
   );
   private static final Phrase SET_DEFAULT_ASSIGN_OPTION_TITLE = Phrase.create(
      "groupView.setDefault.assignOption.title", "Who will get the default groups?", Colors.GRAY
   );
   private static final Phrase SET_DEFAULT_UNSET_DESC = Phrase.create(
      "groupView.setDefault.desc", "This could be a **destructive action**, pay attention before changing", Colors.GRAY, Colors.RED
   );
   private static final Phrase VIEW_PERMISSIONS_TITLE = Phrase.create("groupView.viewPermissions.title", "View Permissions");
   private static final Phrase VIEW_PERMISSIONS_ACTION = Phrase.create(
      "groupView.viewPermissions.action", "**Click** to view Permissions", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase VIEW_PERMISSIONS_LIST_TITLE = Phrase.create("groupView.viewPermissions.listTitle", "Permission Nodes:", Colors.GRAY);
   private static final Phrase INHERITANCE_TITLE = Phrase.create("groupView.inheritance.title", "Inheritances");
   private static final Phrase INHERITANCE_ACTION = Phrase.create("groupView.inheritance.action", "**Click** to edit inheritances", Colors.GRAY, Colors.AQUA);
   private static final Phrase INHERITANCE_LIST_TITLE = Phrase.create("groupView.inheritance.listTitle", "Inherited Groups:", Colors.GRAY);
   private static final Phrase INHERITANCE_LIST_ITEM = Phrase.create("groupView.inheritance.listItem", "- **%group%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase INHERITANCE_LIST_NONE_ITEM = Phrase.create("groupView.inheritance.listNoneItem", "- **None**", Colors.GRAY, Colors.RED);
   private static final Phrase GRANT_GROUPS_TITLE = Phrase.create("groupView.grantGroups.title", "Grant Groups");
   private static final Phrase GRANT_GROUPS_ACTION = Phrase.create(
      "groupView.grantGroups.action", "**Click** to allow granting more groups", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase GRANT_GROUPS_LIST_TITLE = Phrase.create("groupView.grantGroups.listTitle", "Allowed Groups:", Colors.GRAY);
   private static final Phrase GRANT_GROUPS_LIST_ITEM = Phrase.create("groupView.grantGroups.listItem", "- **%group%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase GRANT_GROUPS_LIST_NONE_ITEM = Phrase.create("groupView.grantGroups.listNoneItem", "- **None**", Colors.GRAY, Colors.RED);
   private static final Phrase GRANT_GROUPS_DESC = Phrase.create("groupView.grantGroups.desc", "Granting Groups to Players", Colors.GRAY);
   private static final Phrase PREFIX_SUFFIX_TITLE = Phrase.create("groupView.prefixSuffix.title", "Prefix and Suffix");
   private static final Phrase PREFIX_SUFFIX_PREFIX_ACTION = Phrase.create(
      "groupView.prefixSuffix.prefixAction", "**Left Click** to edit prefix", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase PREFIX_SUFFIX_SUFFIX_ACTION = Phrase.create(
      "groupView.prefixSuffix.suffixAction", "**Right Click** to edit suffix", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase PREFIX_SUFFIX_PREFIX_ATTRIBUTE = Phrase.create(
      "groupView.prefixSuffix.prefixAttribute", "Prefix: **%prefix%**", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase PREFIX_SUFFIX_SUFFIX_ATTRIBUTE = Phrase.create(
      "groupView.prefixSuffix.suffixAttribute", "Suffix: **%suffix%**", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase RENAME_TITLE = Phrase.create("groupView.rename.title", "Rename Group");
   private static final Phrase RENAME_ACTION = Phrase.create("groupView.rename.action", "**Click** to rename group", Colors.GRAY, Colors.AQUA);
   private static final Phrase CHANGE_ICON_TITLE = Phrase.create("groupView.changeIcon.title", "Change Icon");
   private static final Phrase CHANGE_ICON_ACTION = Phrase.create(
      "groupView.changeIcon.action", "**Left Click** to **change** Icon", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase CHANGE_ICON_ACTION_RIGHT_CLICK = Phrase.create(
      "groupView.changeIcon.action.rightClick", "**Right Click** to **change** Damage Value", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase CHANGE_ICON_ACTION_SHIFT_RIGHT_CLICK = Phrase.create(
      "groupView.changeIcon.action.shiftRightClick", "**Shift + Right Click** to **change** Custom Model Data", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase CHANGE_ICON_LORE_DAMAGE_VALUE = Phrase.create(
      "groupView.changeIcon.lore.damageValue", "Damage Value: **%damageValue%**", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase CHANGE_ICON_LORE_CUSTOM_MODEL_ID = Phrase.create(
      "groupView.changeIcon.lore.customModelId", "Custom Model ID: **%customModelId%**", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase CHANGE_ICON_INPUT_CUSTOM_MODEL_TITLE_MAIN = Phrase.create(
      "groupView.changeIcon.input.customModelId.title.main", "Custom Model ID"
   );
   private static final Phrase CHANGE_ICON_INPUT_CUSTOM_MODEL_TITLE_SUB = Phrase.create(
      "groupView.changeIcon.input.customModelId.title.sub", "Type in Custom Model ID", Colors.GRAY
   );
   private static final Phrase CHANGE_ICON_INPUT_DURABILITY_TITLE_MAIN = Phrase.create("groupView.changeIcon.input.damageValue.title.main", "Durability");
   private static final Phrase CHANGE_ICON_INPUT_DURABILITY_TITLE_SUB = Phrase.create(
      "groupView.changeIcon.input.damageValue.title.sub", "Type in Durability value", Colors.GRAY
   );
   private static final Phrase CHANGE_ICON_INPUT_EXAMPLE = Phrase.create(
      "groupView.changeIcon.input.example", "Example: **0** or **215**", Colors.GRAY, Colors.YELLOW, Colors.YELLOW
   );
   private static final Phrase NOT_A_VALID_NUMBER = Phrase.create("groupView.changeIcon.input.notAValidNumber", "Not a valid number", Colors.GRAY);
   private static final Phrase DELETE_TITLE = Phrase.create("groupView.deleteAndPurge.title", "Delete & Purge Group");
   private static final Phrase DELETE_ACTION = Phrase.create(
      "groupView.deleteAndPurge.action.leftClick", "**Left Click** to **delete** this Group", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase PURGE_ACTION = Phrase.create(
      "groupView.deleteAndPurge.action.rightClick", "**Right Click** to **purge** all Players from this Group", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase DELETE_DESC = Phrase.create("groupView.deleteAndPurge.desc", "This action is **permanent**", Colors.GRAY, Colors.RED);
   private static final Phrase PURGE_ACTION_CONFIRM = Phrase.create(
      "groupView.deleteAndPurge.action.confirm", "Successfully **purged** all Players from **%group%**", Colors.GRAY, Colors.RED, Colors.GREEN
   );
   private static final Phrase COPY_GROUP_TITLE = Phrase.create("groupView.copyGroup.title", "Copy Group");
   private static final Phrase COPY_GROUP_ACTION = Phrase.create(
      "groupView.copyGroup.action", "**Click** to create a new copy of this Group", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase COPY_GROUP_USER_INPUT_TITLE_MAIN = Phrase.create("groupView.copyGroup.userInput.title.main", "Create new Copy", Colors.AQUA);
   private static final Phrase COPY_GROUP_USER_INPUT_TITLE_SUB = Phrase.create(
      "groupView.copyGroup.userInput.title.sub", "Type in the name of the new Group", Colors.GRAY
   );
   private static final Phrase COPY_GROUP_GROUP_EXISTS = Phrase.create(
      "groupView.copyGroup.groupExists", "A Group with the name **%group%** already exists", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase COPY_GROUP_SUCCESSFUL = Phrase.create(
      "groupView.copyGroup.successful", "Created a copy of Group **%group%** called **%copyGroup%**", Colors.GRAY, Colors.YELLOW, Colors.GREEN
   );
   private static final Phrase CHAT_FORMAT_TITLE = Phrase.create("groupView.chatFormat.title", "Chat Format");
   private static final Phrase CHAT_FORMAT_ACTION_LEFT_CLICK = Phrase.create(
      "groupView.chatFormat.action.leftClick", "**Left Click** to change format", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase CHAT_FORMAT_ACTION_RIGHT_CLICK = Phrase.create(
      "groupView.chatFormat.action.rightClick", "**Right Click** to **disable**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase CHAT_FORMAT_ACTION_PRESS_Q = Phrase.create(
      "groupView.chatFormat.action.pressQ", "**Press Q** to reset the format to **default**", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase CHAT_FORMAT_DESC = Phrase.create("groupView.chatFormat.desc", "Format:", Colors.GREEN);
   private static final Phrase ENTRY_FORMAT_DISABLED = Phrase.create("groupView.format.disabled", "Feature disabled", Colors.RED);
   private static final Phrase USERS_TITLE = Phrase.create("groupView.users.title", "Users");
   private static final Phrase USERS_ACTION = Phrase.create("groupView.users.action", "Click to view **Users**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase USERS_AMOUNT_ATTRIBUTE = Phrase.create("groupView.users.amountAttribute", "Amount: **%count%**", Colors.GRAY, Colors.YELLOW);

   public GroupView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.plugin = var2;
      this.group = var3;
      this.opened = System.currentTimeMillis();
   }

   public abstract void onBack();

   public boolean hasBackButton() {
      return true;
   }

   @Override
   public int getCurrentSlots() {
      return 54;
   }

   @Override
   public String getCurrentTitle() {
      return TITLE.get().replace("%group%", this.group.getName());
   }

   @Override
   protected void construct(Model var1) {
      var1.button(this::SetDefaultButton, 13);
      var1.button(this::ViewPermissionsButton, 15);
      var1.button(this::ViewInheritanceButton, 20);
      var1.button(this::ViewGrantGroupsButton, 23);
      var1.button(this::PrefixAndSuffixButton, 26);
      var1.button(this::RenameButton, 31);
      var1.button(this::UsersButton, 33);
      var1.button(this::ChangeIconButton, 38);
      var1.button(this::ChatFormatButton, 44);
      var1.button(this::CopyGroupButton, 46);
      var1.button(this::DeleteAndPurgeButton, 54);
      if (this.hasBackButton()) {
         var1.button(50, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
      }
   }

   private void SetDefaultButton(Button var1) {
      var1.material(this.group.isDefault() ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.WHITE_STAINED_GLASS_PANE)
         .name(Animation.wave(SET_DEFAULT_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore((this.group.isDefault() ? SET_DEFAULT_UNSET_ACTION : SET_DEFAULT_SET_ACTION).get(), "");
      var1.item().appendLore(SET_DEFAULT_ASSIGN_OPTION_TITLE.get(), Colors.WHITE + this.plugin.getDefaultGroupAssignOption().getExplanation());
      var1.item().appendLore("", SET_DEFAULT_UNSET_DESC.get());
      var1.action(var1x -> this.group.setDefault(!this.group.isDefault()));
   }

   private void ViewPermissionsButton(Button var1) {
      var1.material(XMaterial.WRITABLE_BOOK)
         .name(Animation.wave(VIEW_PERMISSIONS_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore(VIEW_PERMISSIONS_ACTION.get(), "");
      PermissionCopyList var2 = new PermissionCopyList(this.group.getPermissions(), this.group.getInheritedPermissions());
      var1.item().appendLore(VIEW_PERMISSIONS_LIST_TITLE.get());
      var1.item().appendLore(LoreLists.printPermissionCopiesList(var2, this.opened));
      var1.action(var1x -> new PermissionEditorMainView(this.p, this.plugin, this.group) {
         @Override
         public void onBack() {
            GroupView.this.reopen();
         }
      });
   }

   private void ViewInheritanceButton(Button var1) {
      var1.material(XMaterial.PAPER).name(Animation.wave(INHERITANCE_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue)).lore(INHERITANCE_ACTION.get(), "");
      GroupList var2 = this.group.getActiveInheritedGroups();
      var1.item().appendLore(INHERITANCE_LIST_TITLE.get());
      var2.forEach(var1x -> var1.item().appendLore(INHERITANCE_LIST_ITEM.get().replace("%group%", var1x.getName())));
      if (var2.isEmpty()) {
         var1.item().appendLore(INHERITANCE_LIST_NONE_ITEM.get());
      }

      var1.action(var1x -> new InheritedGroupListView(this.p, this.plugin, this.group) {
         @Override
         public void onBack() {
            GroupView.this.reopen();
         }
      });
   }

   private void ViewGrantGroupsButton(Button var1) {
      var1.material(XMaterial.BOOKSHELF)
         .name(Animation.wave(GRANT_GROUPS_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore(GRANT_GROUPS_ACTION.get(), "");
      var1.item().appendLore(GRANT_GROUPS_DESC.get(), "");
      var1.item().appendLore(GRANT_GROUPS_LIST_TITLE.get());
      if (this.group.getGrantGroups().size() == 0) {
         var1.item().appendLore(GRANT_GROUPS_LIST_NONE_ITEM.get());
      }

      for (Stored var3 : this.group.getGrantGroups().keySet()) {
         if (var3.get().isPresent()) {
            var1.item().appendLore(GRANT_GROUPS_LIST_ITEM.get().replace("%group%", ((Group)var3.get().get()).getName()));
         }
      }

      var1.action(var1x -> new GrantGroupsListView(this.p, this.plugin, this.group) {
         @Override
         public void onBack() {
            GroupView.this.reopen();
         }
      });
   }

   private void PrefixAndSuffixButton(Button var1) {
      String var2 = this.group.getPrefix().orElse(Colors.RED + Words.NONE.get());
      String var3 = this.group.getSuffix().orElse(Colors.RED + Words.NONE.get());
      var1.material(XMaterial.NAME_TAG)
         .name(Animation.wave(PREFIX_SUFFIX_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore(
            PREFIX_SUFFIX_PREFIX_ACTION.get(),
            PREFIX_SUFFIX_SUFFIX_ACTION.get(),
            "",
            PREFIX_SUFFIX_PREFIX_ATTRIBUTE.get().replace("%prefix%", var2),
            PREFIX_SUFFIX_SUFFIX_ATTRIBUTE.get().replace("%suffix%", var3)
         );
      var1.action(var1x -> {
         if (var1x == ActionType.LEFT) {
            new PrefixChangeDialog(this.p, this.plugin, this.group) {
               @Override
               public void reopen() {
                  GroupView.this.reopen();
               }
            };
         }
      });
      var1.action(var1x -> {
         if (var1x == ActionType.RIGHT) {
            new SuffixChangeDialog(this.p, this.plugin, this.group) {
               @Override
               public void reopen() {
                  GroupView.this.reopen();
               }
            };
         }
      });
   }

   private void RenameButton(Button var1) {
      var1.material(XMaterial.BOOK).name(Animation.wave(RENAME_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue)).lore(RENAME_ACTION.get());
      var1.action(var1x -> new GroupRenameDialog(this.p, this.plugin, this.group) {
         @Override
         public void reopen() {
            GroupView.this.reopen();
         }
      });
   }

   private void ChangeIconButton(Button var1) {
      var1.material(this.group.getIcon(), this.group.getModelId())
         .name(Animation.wave(CHANGE_ICON_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore(CHANGE_ICON_ACTION.get())
         .setDamage(this.group.getDamage());
      if (XMaterial.isDamageable(this.group.getIcon().getBukkitMaterial().get())) {
         var1.item().appendLore(CHANGE_ICON_ACTION_RIGHT_CLICK.get());
      }

      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1)) {
         var1.item().appendLore(CHANGE_ICON_ACTION_SHIFT_RIGHT_CLICK.get());
      }

      var1.item().appendLore("", "§7Icon: §f" + this.group.getIcon().getHumanName());
      if (XMaterial.isDamageable(this.group.getIcon().getBukkitMaterial().get())) {
         var1.item().appendLore(CHANGE_ICON_LORE_DAMAGE_VALUE.get().replace("%damageValue%", String.valueOf(this.group.getDamage())));
      }

      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1)) {
         var1.item().appendLore(CHANGE_ICON_LORE_CUSTOM_MODEL_ID.get().replace("%customModelId%", String.valueOf(this.group.getModelId())));
      }

      var1.action(
         var1x -> {
            if (var1x == ActionType.RIGHT && XMaterial.isDamageable(this.group.getIcon().getBukkitMaterial().get())) {
               new UserInput(
                  this.p,
                  this.plugin,
                  "§d§l" + CHANGE_ICON_INPUT_DURABILITY_TITLE_MAIN.get(),
                  CHANGE_ICON_INPUT_DURABILITY_TITLE_SUB.get(),
                  CHANGE_ICON_INPUT_EXAMPLE.get()
               ) {
                  @Override
                  public void onClose(Player var1) {
                     GroupView.this.reopen();
                  }

                  @Override
                  public boolean onResult(String var1) {
                     try {
                        GroupView.this.group.setDamage(Integer.parseInt(var1));
                        GroupView.this.reopen();
                        return true;
                     } catch (NumberFormatException var3) {
                        GroupView.this.p.sendMessage(GroupView.this.plugin.getPrefix() + GroupView.NOT_A_VALID_NUMBER.get());
                        return false;
                     }
                  }
               };
            }

            if (var1x == ActionType.SHIFT_RIGHT && MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1)) {
               new UserInput(
                  this.p,
                  this.plugin,
                  "§d§l" + CHANGE_ICON_INPUT_CUSTOM_MODEL_TITLE_MAIN.get(),
                  CHANGE_ICON_INPUT_CUSTOM_MODEL_TITLE_SUB.get(),
                  CHANGE_ICON_INPUT_EXAMPLE.get()
               ) {
                  @Override
                  public void onClose(Player var1) {
                     GroupView.this.reopen();
                  }

                  @Override
                  public boolean onResult(String var1) {
                     try {
                        GroupView.this.group.setModelId(Integer.parseInt(var1));
                        GroupView.this.reopen();
                        return true;
                     } catch (NumberFormatException var3) {
                        GroupView.this.p.sendMessage(GroupView.this.plugin.getPrefix() + GroupView.NOT_A_VALID_NUMBER.get());
                        return false;
                     }
                  }
               };
            }

            if (var1x == ActionType.LEFT) {
               new MaterialPickerView(this.p, this.plugin, this.group.getName() + " > Icon") {
                  @Override
                  public void choose(Player var1, XMaterial var2) {
                     GroupView.this.group.setIcon(var2);
                     GroupView.this.reopen();
                  }

                  @Override
                  public void onBack() {
                     GroupView.this.reopen();
                  }
               };
            }
         }
      );
   }

   private void DeleteAndPurgeButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK)
         .name(Animation.wave(DELETE_TITLE.get(), Colors.Red, Colors.AliceBlue))
         .lore(DELETE_ACTION.get(), PURGE_ACTION.get(), "", DELETE_DESC.get());
      var1.action(
         var1x -> {
            if (var1x == ActionType.LEFT) {
               new GroupDeleteView(this.p, this.plugin, this.group) {
                  @Override
                  public void reopen() {
                     GroupView.this.reopen();
                  }
               };
            }

            if (var1x == ActionType.RIGHT) {
               new ConfirmationView(this.p, this.plugin, "Confirm purging: " + this.group.getName()) {
                  @Override
                  public void choose(boolean var1) {
                     if (var1) {
                        for (User var3 : GroupView.this.plugin.getUserStorage().getUsers()) {
                           var3.removeGroup(GroupView.this.group);
                        }

                        this.p
                           .sendMessage(
                              GroupView.this.plugin.getPrefix() + GroupView.PURGE_ACTION_CONFIRM.get().replace("%group%", GroupView.this.group.getName())
                           );
                     }

                     GroupView.this.reopen();
                  }
               };
            }
         }
      );
   }

   private void CopyGroupButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.wave(COPY_GROUP_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue)).lore(COPY_GROUP_ACTION.get());
      var1.action(
         var1x -> new UserInput(this.p, this.plugin, COPY_GROUP_USER_INPUT_TITLE_MAIN.get(), COPY_GROUP_USER_INPUT_TITLE_SUB.get()) {
            @Override
            public void onClose(Player var1) {
               GroupView.this.reopen();
            }

            @Override
            public boolean onResult(String var1) {
               if (GroupView.this.plugin.getGroups().name(var1).isPresent()) {
                  GroupView.this.p.sendMessage(GroupView.this.plugin.getPrefix() + GroupView.COPY_GROUP_GROUP_EXISTS.get().replace("%group%", var1));
                  return false;
               }

               GroupView.this.plugin
                  .newGroup(var1)
                  .setIcon(GroupView.this.group.getIcon())
                  .setPrefix(GroupView.this.group.getPrefix().orElse(null))
                  .setSuffix(GroupView.this.group.getSuffix().orElse(null))
                  .setServer(GroupView.this.group.getServer().orElse(null))
                  .setWorld(GroupView.this.group.getWorld().orElse(null))
                  .create();
               Optional var2 = GroupView.this.plugin.getGroups().name(var1);

               for (Permission var4 : GroupView.this.group.getPermissions()) {
                  ((Group)var2.get())
                     .newPermission(var4.getName())
                     .setExpiration(var4.getExpiration())
                     .setServer(var4.getServer().orElse(null))
                     .setWorld(var4.getWorld().orElse(null))
                     .setPositive(var4.isPositive())
                     .create();
               }

               for (Stored var6 : GroupView.this.group.getInheritedGroups()) {
                  ((Group)var2.get()).getInheritedGroups().add(var6);
               }

               GroupView.this.reopen();
               GroupView.this.p
                  .sendMessage(
                     GroupView.this.plugin.getPrefix()
                        + GroupView.COPY_GROUP_SUCCESSFUL
                           .get()
                           .replace("%group%", GroupView.this.group.getName())
                           .replace("%copyGroup%", ((Group)var2.get()).getName())
                  );
               return true;
            }
         }
      );
   }

   private void ChatFormatButton(Button var1) {
      var1.material(XMaterial.OAK_SIGN)
         .name(Animation.wave(CHAT_FORMAT_TITLE.get(), Colors.RED, Colors.AliceBlue))
         .lore(
            CHAT_FORMAT_ACTION_LEFT_CLICK.get(),
            CHAT_FORMAT_ACTION_RIGHT_CLICK.get(),
            CHAT_FORMAT_ACTION_PRESS_Q.get(),
            "",
            CHAT_FORMAT_DESC.get(),
            this.group.getFormat() == null ? ENTRY_FORMAT_DISABLED.get() : Colors.WHITE + Text.color(this.group.getFormat())
         );
      var1.action(var1x -> {
         if (var1x == ActionType.Q) {
            this.group.resetFormat();
         } else if (var1x == ActionType.RIGHT) {
            this.group.setFormat("none");
         } else {
            new GroupFormatDialog(this.p, this.plugin, this.group, VisualType.CHAT) {
               @Override
               public void reopen() {
                  GroupView.this.reopen();
               }
            };
         }
      });
   }

   private void UsersButton(Button var1) {
      int var2 = this.plugin.getUsers().usersInGroup(this.group.toStored()).size();
      var1.material(XMaterial.PLAYER_HEAD)
         .name(Animation.wave(USERS_TITLE.get(), Colors.DeepSkyBlue, Colors.AliceBlue))
         .lore(USERS_ACTION.get(), "", USERS_AMOUNT_ATTRIBUTE.get().replace("%count%", Integer.toString(var2)));
      var1.action(var1x -> new GroupUserListView(this.p, this.plugin, this.group) {
         @Override
         public void onBack() {
            GroupView.this.reopen();
         }
      });
   }
}
