package me.TechsCode.UltraPermissions.commands;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.CachedOnlineChecker;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.Command;
import me.TechsCode.UltraPermissions.base.command.CommandNode;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.base.command.arguments.AnyArgument;
import me.TechsCode.UltraPermissions.base.command.arguments.AnyIntegerArgument;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArgument;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArguments;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.commands.arguments.BooleanArgument;
import me.TechsCode.UltraPermissions.commands.arguments.GroupArgument;
import me.TechsCode.UltraPermissions.commands.arguments.PermissionArgument;
import me.TechsCode.UltraPermissions.commands.arguments.ServerArgument;
import me.TechsCode.UltraPermissions.commands.arguments.StringArgument;
import me.TechsCode.UltraPermissions.commands.arguments.UserArgument;
import me.TechsCode.UltraPermissions.commands.arguments.WorldArgument;
import me.TechsCode.UltraPermissions.commands.requirements.GrantGroupsRequirement;
import me.TechsCode.UltraPermissions.commands.requirements.MySQLEnabledRequirement;
import me.TechsCode.UltraPermissions.commands.requirements.SuperadminRequirement;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;
import me.TechsCode.UltraPermissions.transfer.TransferAssistant;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class UPCCommand extends Command {
   private final UltraPermissions plugin;
   private CachedOnlineChecker cachedOnlineChecker;
   private static final Phrase ALL_SERVERS_LABEL = Phrase.create("command.scope.allServers", "All Servers");
   private static final Phrase GROUP_NAME_USAGE = Phrase.create("command.usage.groupName", "GroupName");
   private static final Phrase COPY_NAME_USAGE = Phrase.create("command.usage.copyName", "Name of Copy");
   private static final Phrase PLAYER_NAME_USAGE = Phrase.create("command.usage.playerName", "<PlayerName>");
   private static final Phrase PRIORITY_USAGE = Phrase.create("command.usage.priority", "Priority");
   private static final Phrase PREFIX_USAGE = Phrase.create("command.usage.prefix", "<Prefix>");
   private static final Phrase SUFFIX_USAGE = Phrase.create("command.usage.suffix", "<Suffix>");
   private static final Phrase INVALID_ARGUMENTS = Phrase.create("command.invalidArguments", "Invalid Arguments");
   private static final Phrase LIST_PLAYER_PERMISSIONS = Phrase.create(
      "command.listPlayerPermissions", "Permissions of User **%user%**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase LIST_GROUP_PERMISSIONS = Phrase.create(
      "command.listGroupPermissions", "Permissions of Group **%group%**", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase CREATE_GROUP_NO_SPACES = Phrase.create(
      "command.createGroup.noSpaces", "The Group Name **should not** contain spaces", Colors.GRAY, Colors.RED
   );
   private static final Phrase CREATE_GROUP_NO_COLORS = Phrase.create(
      "command.createGroup.noColors", "The Group Name **should not** contain Color Codes", Colors.GRAY, Colors.RED
   );
   private static final Phrase CREATE_GROUP_NAME_TAKEN = Phrase.create("command.createGroup.nameTaken", "This Group Name is already in use", Colors.GRAY);
   private static final Phrase CREATE_GROUP_SUCCESS = Phrase.create(
      "command.createGroup.success", "Successfully create a new Group (**%groupName%**)", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase CREATE_GROUP_SUCCESS_BUNGEE = Phrase.create(
      "command.createGroup.success.bungee", "Successfully create a new Group (**%groupName%**) for **%server%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );
   private static final Phrase COPY_GROUP_SUCCESSFUL = Phrase.create(
      "command.copyGroup.successful", "Created a copy of Group **%group%** called **%copyGroup%**", Colors.GRAY, Colors.YELLOW, Colors.GREEN
   );
   private static final Phrase COPY_GROUP_UNSUCCESSFUL = Phrase.create(
      "command.copyGroup.unsuccessful", "A Group with the name **%group%** already exists", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_ADD_GROUP = Phrase.create(
      "command.playerAddGroup", "Added Group **%group%** to **%player%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );
   private static final Phrase PLAYER_ADD_GROUP_TIMED = Phrase.create(
      "command.playerAddGroupTimed", "Added Group **%group%** to **%player%** for **%operator% %time%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA, Colors.RED
   );
   private static final Phrase PLAYER_ADD_GROUP_NOT_TIME_ALLOWED = Phrase.create(
      "command.playerAddGroup.notTimeAllowed",
      "You can't grant Group **%group%** to users for a duration of **%duration%**",
      Colors.GRAY,
      Colors.GREEN,
      Colors.YELLOW
   );
   private static final Phrase PLAYER_ADD_GROUP_WORD_PERMANENT = Phrase.create("command.playerAddGroup.word.permanent", "Permanent", Colors.RED);
   private static final Phrase GROUP_ALREADY_HAS_PERMISSION = Phrase.create(
      "command.groupAlreadyHasPermission", "The group **%group%** already has the permission **%permission%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );
   private static final Phrase GROUP_ADD_PERMISSION = Phrase.create(
      "command.groupAddPermission", "Added permission **%permission%** to **%group%**", Colors.GRAY, Colors.GREEN, Colors.AQUA
   );
   private static final Phrase GROUP_ADD_PERMISSION_BUNGEE = Phrase.create(
      "command.groupAddPermission.bungee",
      "Added permission **%permission%** to **%group%** for **%server%**",
      Colors.GRAY,
      Colors.GREEN,
      Colors.AQUA,
      Colors.YELLOW
   );
   private static final Phrase PLAYER_ALREADY_HAS_PERMISSION = Phrase.create(
      "command.playerAlreadyHasPermission", "The player **%player%** already has the permission **%permission%**", Colors.GRAY, Colors.GREEN, Colors.YELLOW
   );
   private static final Phrase PLAYER_ADD_PERMISSION = Phrase.create(
      "command.playerAddPermission", "Added permission **%permission%** to **%player%**", Colors.GRAY, Colors.GREEN, Colors.AQUA
   );
   private static final Phrase PLAYER_ADD_PERMISSION_BUNGEE = Phrase.create(
      "command.playerAddPermission.bungee",
      "Added permission **%permission%** to **%player%** for **%server%**",
      Colors.GRAY,
      Colors.GREEN,
      Colors.AQUA,
      Colors.YELLOW
   );
   private static final Phrase PLAYER_ALREADY_SUPER_ADMIN = Phrase.create(
      "command.playerAlreadySuperAdmin", "**%player%** is already a superadmin", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_ADD_SUPER_ADMIN = Phrase.create(
      "command.playerAddSuperAdmin", "Successfully added **%player%** as a superadmin", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase REGISTER_USER_ALREADY_EXISTING = Phrase.create(
      "command.registerUser.failed.alreadyExisting", "The UUID **%uuid%** is already registered", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase REGISTER_USER_INVALID_UUID = Phrase.create("command.registerUser.failed.invalidUUID", "Invalid UUID", Colors.GRAY);
   private static final Phrase REGISTER_USER_SUCCESS = Phrase.create(
      "command.registerUser.success", "Successfully registered the user **%playerName%** (**%uuid%**)", Colors.GRAY, Colors.YELLOW, Colors.WHITE
   );
   private static final Phrase PLAYER_REMOVE_GROUP = Phrase.create(
      "command.playerRemoveGroup", "Successfully removed **%player%** from group **%group%**", Colors.GRAY, Colors.AQUA, Colors.YELLOW
   );
   private static final Phrase PLAYER_NOT_IN_GROUP = Phrase.create(
      "command.playerNotInGroup", "The player **%player%** is not in the group **%group%**", Colors.GRAY, Colors.AQUA, Colors.YELLOW
   );
   private static final Phrase DELETE_USER_SUCCESSFUL = Phrase.create(
      "command.deleteUser.successful", "Successfully removed the user **%user%**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase DELETE_USER_UNSUCCESSFUL = Phrase.create(
      "command.deleteUser.unsuccessful", "The user **%user%** needs to be offline", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase REMOVE_PERMISSION_UNSUCCESSFUL = Phrase.create(
      "command.groupRemovePermission.unsuccessful", "Did not find permission **%permission%** for **%target%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );
   private static final Phrase REMOVE_PERMISSION_SUCCESSFUL = Phrase.create(
      "command.groupRemovePermission.successful", "Removed permission **%permission%** from **%target%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );
   private static final Phrase REMOVE_PERMISSION_SUCCESSFUL_MULTI = Phrase.create(
      "command.groupRemovePermission.successfulMultiple",
      "Removed **%count%** copies of **%permission%** from **%target%**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.YELLOW,
      Colors.AQUA
   );
   private static final Phrase PLAYER_NOT_SUPER_ADMIN = Phrase.create(
      "command.playerNotSuperAdmin", "**%player%** is not a superadmin", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_REMOVE_SUPER_ADMIN = Phrase.create(
      "command.playerRemoveSuperAdmin", "Successfully removed **%player%** from the superadmins", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase GROUP_RESTRICT_TO_WORLD_ALL_WORLDS = Phrase.create(
      "command.restrictGroupToWorld.allWorlds",
      "Changed settings of **%group%** to allow this Group on **all Worlds**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.WHITE
   );
   private static final Phrase GROUP_RESTRICT_TO_WORLD_SPECIFIC_WORLD = Phrase.create(
      "command.restrictGroupToWorld.specificWorld",
      "Changed settings of **%group%** to restrict this Group to **%world%**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.WHITE
   );
   private static final Phrase GROUP_RESTRICT_TO_WORLD_INVALID = Phrase.create("command.restrictGroupToWorld.invalid", "Invalid world", Colors.GRAY);
   private static final Phrase DELETE_GROUP_CONFIRM = Phrase.create(
      "command.deleteGroup.confirm", "Successfully **deleted** group **%group%**", Colors.GRAY, Colors.RED, Colors.YELLOW
   );
   private static final Phrase PURGE_SUPER_ADMINS_CONFIRM = Phrase.create(
      "command.purgeSuperAdmins.confirm", "Successfully **purged** all **Super Admins**", Colors.GRAY, Colors.RED, Colors.GREEN
   );
   private static final Phrase PURGE_GROUP_CONFIRM = Phrase.create(
      "command.purgeGroup.confirm", "Successfully **purged** all Players from **%group%**", Colors.GRAY, Colors.RED, Colors.YELLOW
   );
   private static final Phrase PURGE_GROUPPERMISSION_CONFIRM = Phrase.create(
      "command.purgeGroupPermission.confirm", "Successfully **purged** **%permission%** from all Groups", Colors.GRAY, Colors.RED, Colors.GREEN
   );
   private static final Phrase PURGE_PLAYERPERMISSION_CONFIRM = Phrase.create(
      "command.purgePlayerPermission.confirm", "Successfully **purged** **%permission%** from all Players", Colors.GRAY, Colors.RED, Colors.GREEN
   );
   private static final Phrase PURGE_ALL_PLAYERPERMISSIONS_CONFIRM = Phrase.create(
      "command.purgeAllPlayerPermissions.confirm", "Successfully **purged** all Permissions from **%user%**", Colors.GRAY, Colors.RED, Colors.AQUA
   );
   private static final Phrase PURGE_PLAYER_GROUPS_CONFIRM = Phrase.create(
      "command.purgePlayerGroups.confirm", "Successfully **purged** all groups from **%player%**", Colors.GRAY, Colors.RED, Colors.GREEN
   );
   private static final Phrase PLAYER_CLEARED_GROUPS = Phrase.create(
      "command.playerClearedGroups", "Cleared all groups of **%player%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_SET_GROUPS = Phrase.create(
      "command.playerSetGroups", "Set the groups **%groups%** for **%player%**", Colors.GRAY, Colors.YELLOW, Colors.AQUA
   );
   private static final Phrase CHANGED_GROUP_PRIORITY = Phrase.create(
      "command.setGroupPriority", "Changed the priority of **%group%** to **%priority%**", Colors.GRAY, Colors.YELLOW, Colors.Green
   );
   private static final Phrase GROUP_SET_PREFIX_SET = Phrase.create(
      "command.groupSetPrefix.set", "Set the prefix of **%group%** to **%prefix%**", Colors.GRAY, Colors.YELLOW, Colors.WHITE
   );
   private static final Phrase GROUP_SET_PREFIX_CLEAR = Phrase.create(
      "command.groupSetPrefix.clear", "Cleared the prefix of **%group%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase GROUP_SET_SUFFIX_SET = Phrase.create(
      "command.groupSetSuffix.set", "Set the suffix of **%group%** to **%suffix%**", Colors.GRAY, Colors.YELLOW, Colors.WHITE
   );
   private static final Phrase GROUP_SET_SUFFIX_CLEAR = Phrase.create(
      "command.groupSetSuffix.clear", "Cleared the suffix of **%group%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_SET_PREFIX_CLEAR_ALL = Phrase.create(
      "command.playerSetPrefix.clearAll", "Successfully cleared **%count% prefixes**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_SET_PREFIX_CLEAR = Phrase.create(
      "command.playerSetPrefix.clear", "Cleared the prefix of **%user%**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase PLAYER_SET_PREFIX_SET = Phrase.create(
      "command.playerSetPrefix.set", "Set the prefix of **%user%** to **%prefix%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase PLAYER_SET_SUFFIX_CLEAR_ALL = Phrase.create(
      "command.playerSetSuffix.clearAll", "Successfully cleared **%count% suffixes**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PLAYER_SET_SUFFIX_CLEAR = Phrase.create(
      "command.playerSetSuffix.clear", "Cleared the suffix of **%user%**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase PLAYER_SET_SUFFIX_SET = Phrase.create(
      "command.playerSetSuffix.set", "Set the suffix of **%user%** to **%suffix%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase TRANSFER_ACCOUNT_SUCCESSFUL_GROUPS = Phrase.create(
      "command.transferAccount.successful.groups",
      "Successfully transferred all **groups** from **%oldTarget%** to **%newTarget%**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.AQUA,
      Colors.GREEN
   );
   private static final Phrase TRANSFER_ACCOUNT_SUCCESSFUL_PERMISSIONS = Phrase.create(
      "command.transferAccount.successful.permissions",
      "Successfully transferred all **permissions** from **%oldTarget%** to **%newTarget%**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.AQUA,
      Colors.GREEN
   );
   private static final Phrase TRANSFER_ACCOUNT_SUCCESSFUL_ALL_DATA = Phrase.create(
      "command.transferAccount.successful.allData",
      "Successfully transferred all **groups** and **permissions** from **%oldTarget%** to **%newTarget%**",
      Colors.GRAY,
      Colors.YELLOW,
      Colors.YELLOW,
      Colors.AQUA,
      Colors.GREEN
   );
   private static final Phrase TRANSFER_SOURCES_EQUAL = Phrase.create("command.transfer.sourcesEqual", "From and To should not be equal!", Colors.GRAY);
   private static final Phrase NO_VALID_USER = Phrase.create("command.noValidUser", "Could not find the user **%user%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase NO_VALID_GROUP = Phrase.create("command.noValidGroup", "Could not find the group **%group%**", Colors.GRAY, Colors.YELLOW);

   public UPCCommand(UltraPermissions var1) {
      super(var1, "upc", "");
      this.plugin = var1;
   }

   @Override
   public void define(CommandSender var1, CommandNode<String, SpecificArgument> var2) {
      var2.require(new GrantGroupsRequirement(this.plugin));
      this.AddGroupCommand(var2);
      this.AddGroupPermission(var2);
      this.AddPlayerPermission(var2);
      this.AddSuperAdmin(var2);
      this.copyGroupCommand(var2);
      this.createGroupCommand(var2);
      this.DeleteGroup(var2);
      this.DeleteUser(var2);
      this.ListGroupPermissions(var2);
      this.ListPlayerPermissions(var2);
      this.PurgeSuperAdmins(var2);
      this.PurgeGroup(var2);
      this.PurgeGroupPermission(var2);
      this.PurgeAllPlayerPermission(var2);
      this.PurgePlayerPermission(var2);
      this.PurgePlayerGroups(var2);
      this.RegisterUser(var2);
      this.RemoveGroup(var2);
      this.RemoveGroupPermission(var2);
      this.RemovePlayerPermission(var2);
      this.RemoveSuperAdmin(var2);
      this.RestrictGroupToWorld(var2);
      this.SetGroups(var2);
      this.SetGroupPriority(var2);
      this.SetGroupPrefix(var2);
      this.SetGroupSuffix(var2);
      this.SetPlayerPrefix(var2);
      this.SetPlayerSuffix(var2);
      this.TransferAccount(var2);
      this.Transfer(var2);
      // Central authorization: administrative branches fail closed when a leaf forgets its own requirement.
      for (CommandNode<?, ?> branch : var2.getChildren()) {
         String name = branch.getArgument().getUsage().toLowerCase(java.util.Locale.ROOT);
         if (!java.util.Set.of("addgroup", "listgrouppermissions", "listplayerpermissions").contains(name)) {
            branch.require(new SuperadminRequirement(this.plugin));
         }
      }
   }

   private void ListPlayerPermissions(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("listPlayerPermissions"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.NO_MATCH),
            var1xx -> var1xx.action(
               (var2, var3, var4) -> {
                  ArgumentValue var5 = var1xx.getValue(var4);
                  if (var5.isValid()) {
                     if (!((User)var5.get()).getPermissions().isEmpty()) {
                        var2.sendMessage(this.plugin.getPrefix() + LIST_PLAYER_PERMISSIONS.get().replace("%user%", ((User)var5.get()).getName()));
                        ((User)var5.get())
                           .getPermissions()
                           .stream()
                           .map(Permission::getDisplayName)
                           .sorted(Comparator.naturalOrder())
                           .forEach(var1xxxx -> var2.sendMessage(Colors.GRAY + "- " + (var1xxxx.startsWith("-") ? Colors.RED : Colors.GREEN) + var1xxxx));
                     }
                  }
               }
            )
         )
      );
   }

   private void ListGroupPermissions(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("listGroupPermissions"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.NO_MATCH),
            var1xx -> var1xx.action(
               (var2, var3, var4) -> {
                  ArgumentValue var5 = var1xx.getValue(var4);
                  if (var5.isValid()) {
                     if (!((Group)var5.get()).getPermissions().isEmpty()) {
                        var2.sendMessage(this.plugin.getPrefix() + LIST_GROUP_PERMISSIONS.get().replace("%group%", ((Group)var5.get()).getName()));
                        ((Group)var5.get())
                           .getPermissions()
                           .stream()
                           .map(Permission::getDisplayName)
                           .sorted(Comparator.naturalOrder())
                           .forEach(var1xxxx -> var2.sendMessage(Colors.GRAY + "- " + (var1xxxx.startsWith("-") ? Colors.RED : Colors.GREEN) + var1xxxx));
                     }
                  }
               }
            )
         )
      );
   }

   private void createGroupCommand(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("createGroup"),
         var1x -> var1x.newNode(
            new StringArgument(this.plugin, GROUP_NAME_USAGE),
            var1xx -> {
               var1xx.require(new SuperadminRequirement(this.plugin));
               if (this.plugin.getThisServer().isPresent()) {
                  var1xx.newNode(
                     new ServerArgument(this.plugin),
                     var2 -> var2.action(
                        (var3, var4, var5) -> {
                           ArgumentValue var6 = var1xx.getValue(var5);
                           ArgumentValue var7 = var2.getValue(var5);
                           if (var7.isValid()) {
                              String var8 = var7.get() != null ? ((NServer)var7.get()).getName() : ALL_SERVERS_LABEL.get();
                              if (((String)var6.get()).contains(" ")) {
                                 var3.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NO_SPACES);
                              } else if (((String)var6.get()).contains("&")) {
                                 var3.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NO_COLORS);
                              } else {
                                 boolean var9 = this.plugin.getGroups().name((String)var6.get()).isPresent();
                                 if (var9) {
                                    var3.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NAME_TAKEN);
                                 } else {
                                    this.plugin
                                       .newGroup((String)var6.get())
                                       .setPrefix(Colors.GREEN + (String)var6.get())
                                       .setServer(var7.get() != null ? ((NServer)var7.get()).getName() : null)
                                       .create();
                                    var3.sendMessage(
                                       this.plugin.getPrefix()
                                          + CREATE_GROUP_SUCCESS_BUNGEE.get().replace("%groupName%", (CharSequence)var6.get()).replace("%server%", var8)
                                    );
                                 }
                              }
                           }
                        }
                     )
                  );
               } else {
                  var1xx.require(new SuperadminRequirement(this.plugin));
                  var1xx.action((var2, var3, var4) -> {
                     ArgumentValue var5 = var1xx.getValue(var4);
                     if (((String)var5.get()).contains(" ")) {
                        var2.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NO_SPACES);
                     } else if (((String)var5.get()).contains("&")) {
                        var2.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NO_COLORS);
                     } else {
                        boolean var6 = this.plugin.getGroups().name((String)var5.get()).isPresent();
                        if (var6) {
                           var2.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_NAME_TAKEN);
                        } else {
                           this.plugin.newGroup((String)var5.get()).setPrefix(Colors.GREEN + (String)var5.get()).create();
                           var2.sendMessage(this.plugin.getPrefix() + CREATE_GROUP_SUCCESS.get().replace("%groupName%", (CharSequence)var5.get()));
                        }
                     }
                  });
               }
            }
         )
      );
   }

   private void copyGroupCommand(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("copyGroup"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new StringArgument(this.plugin, COPY_NAME_USAGE),
               var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action(
                     (var3, var4, var5) -> {
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        if (this.plugin.getGroups().name((String)var7.get()).isPresent()) {
                           var3.sendMessage(this.plugin.getPrefix() + COPY_GROUP_UNSUCCESSFUL.get().replace("%group%", (CharSequence)var7.get()));
                        } else {
                           this.plugin
                              .newGroup((String)var7.get())
                              .setIcon(((Group)var6.get()).getIcon())
                              .setPrefix(((Group)var6.get()).getPrefix().orElse(null))
                              .setSuffix(((Group)var6.get()).getSuffix().orElse(null))
                              .setServer(((Group)var6.get()).getServer().orElse(null))
                              .setWorld(((Group)var6.get()).getWorld().orElse(null))
                              .create();
                           Optional var8 = this.plugin.getGroups().name((String)var7.get());

                           for (Permission var10 : ((Group)var6.get()).getPermissions()) {
                              ((Group)var8.get())
                                 .newPermission(var10.getName())
                                 .setExpiration(var10.getExpiration())
                                 .setServer(var10.getServer().orElse(null))
                                 .setWorld(var10.getWorld().orElse(null))
                                 .setPositive(var10.isPositive())
                                 .create();
                           }

                           for (Stored var12 : ((Group)var6.get()).getInheritedGroups()) {
                              ((Group)var8.get()).getInheritedGroups().add(var12);
                           }

                           var3.sendMessage(
                              this.plugin.getPrefix()
                                 + COPY_GROUP_SUCCESSFUL.get()
                                    .replace("%group%", ((Group)var6.get()).getName())
                                    .replace("%copyGroup%", ((Group)var8.get()).getName())
                           );
                        }
                     }
                  );
               }
            )
         )
      );
   }

   private void AddGroupCommand(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("AddGroup"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new GroupArgument(this.plugin, EmptyReason.INVALID),
               var2 -> var2.action(
                  (var3, var4, var5) -> {
                     ArgumentValue var6 = var1xx.getValue(var5);
                     ArgumentValue var7 = var2.getValue(var5);
                     boolean var8 = false;
                     boolean var9 = false;
                     long var10 = Tools.getTimeSecondsFromString(String.join(" ", var5));
                     long var12 = var10 == 0L ? 0L : System.currentTimeMillis() + 1000L * var10;
                     Optional var14 = null;
                     if (var4 != null) {
                        var14 = this.plugin.getUsers().uuid(var4.getUniqueId());
                     }

                     if (this.isValidUser(var3, var6) && this.isValidGroup(var3, var7)) {
                        if (var4 != null && var14.isPresent()) {
                           var8 = ((User)var14.get())
                              .getGroups()
                              .stream()
                              .anyMatch(
                                 var1xxxxx -> var1xxxxx.isPresent() && var1xxxxx.get().get().getGrantGroups().containsKey(((Group)var7.get()).toStored())
                              );
                           var9 = ((User)var14.get())
                              .getGroups()
                              .stream()
                              .anyMatch(
                                 var3x -> var3x.isPresent() && var3x.get().get().getGrantGroups().containsKey(((Group)var7.get()).toStored())
                                    ? var3x.get()
                                       .get()
                                       .getGrantGroups()
                                       .entrySet()
                                       .stream()
                                       .anyMatch(
                                          var3xx -> var3xx.getKey().equals(((Group)var7.get()).toStored())
                                             && (var3xx.getValue() >= var10 && var10 != 0L || var3xx.getValue() == 0L)
                                       )
                                    : false
                              );
                        }

                        if (!var9 && var4 != null) {
                           var14 = this.plugin.getUsers().uuid(var4.getUniqueId());
                           if (var14.isPresent() && !((User)var14.get()).isSuperadmin()) {
                              var3.sendMessage(
                                 this.plugin.getPrefix()
                                    + PLAYER_ADD_GROUP_NOT_TIME_ALLOWED.get()
                                       .replace("%group%", ((Group)var7.get()).getName())
                                       .replace("%duration%", var12 != 0L ? Tools.getTimeString(var10) : PLAYER_ADD_GROUP_WORD_PERMANENT.get())
                              );
                              return;
                           }
                        }

                        if (var8 && var9 || var4 == null || var14.isPresent() && ((User)var14.get()).isSuperadmin()) {
                           boolean var15 = false;
                           String var16 = ((Group)var7.get()).getName();
                           String var17 = ((User)var6.get()).getName();
                           String var18 = var12 != 0L ? Tools.getTimeString(var10) : "none";
                           String var19 = "";
                           if (var5.contains("+")) {
                              var19 = "+";
                           }

                           if (var5.contains("-")) {
                              var19 = "-";
                           }

                           Phrase var20 = var12 != 0L ? PLAYER_ADD_GROUP_TIMED : PLAYER_ADD_GROUP;
                           var3.sendMessage(
                              this.plugin.getPrefix()
                                 + var20.get().replace("%group%", var16).replace("%player%", var17).replace("%time%", var18).replace("%operator%", var19)
                           );
                           if (((User)var6.get()).getActiveGroups().name(var16).isPresent()) {
                              var15 = true;
                           }

                           if (var5.contains("+") && var15) {
                              long var23 = ((User)var6.get()).getGroupExpiry(((Group)var7.get()).toStored());
                              ((User)var6.get()).addGroup((Group)var7.get(), var23 + 1000L * var10);
                           } else if (var5.contains("-") && var15) {
                              long var21 = ((User)var6.get()).getGroupExpiry(((Group)var7.get()).toStored());
                              ((User)var6.get()).addGroup((Group)var7.get(), var21 - 1000L * var10);
                           } else {
                              ((User)var6.get()).addGroup((Group)var7.get(), var12);
                           }
                        }
                     }
                  }
               )
            )
         )
      );
   }

   private void AddGroupPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("AddGroupPermission"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new PermissionArgument(this.plugin),
               var2 -> {
                  if (this.plugin.getThisServer().isPresent()) {
                     var2.newNode(
                        new ServerArgument(this.plugin),
                        var3 -> {
                           var3.newNode(
                              new WorldArgument(this.plugin, EmptyReason.INVALID),
                              var4 -> {
                                 var4.require(new SuperadminRequirement(this.plugin));
                                 var4.action(
                                    (var5, var6, var7) -> {
                                       ArgumentValue var8 = var1xx.getValue(var7);
                                       ArgumentValue var9 = var2.getValue(var7);
                                       ArgumentValue var10 = var3.getValue(var7);
                                       ArgumentValue var11 = var4.getValue(var7);
                                       if (var10.isValid()) {
                                          String var12 = var10.get() != null ? ((NServer)var10.get()).getName() : ALL_SERVERS_LABEL.get();
                                          if (this.isValidGroup(var5, var8)) {
                                             CommandOptions var13 = new CommandOptions(this.plugin, var2.getSpareArguments(var7));
                                             boolean var14 = ((Group)var8.get())
                                                .getPermissions()
                                                .name((String)var9.get())
                                                .servers(var13.getServer() == null, var13.getServer())
                                                .worlds(var11.get() == null, var13.getWorld())
                                                .stream()
                                                .anyMatch(var1xxxxxxx -> var1xxxxxxx.getExpiration() == var13.getExpiration());
                                             if (var14) {
                                                String var16 = GROUP_ALREADY_HAS_PERMISSION.get()
                                                   .replace("%group%", ((Group)var8.get()).getName())
                                                   .replace("%permission%", (CharSequence)var9.get());
                                                var5.sendMessage(this.plugin.getPrefix() + var16);
                                                return;
                                             }

                                             ((Group)var8.get())
                                                .newPermission((String)var9.get())
                                                .setServer(var10.get() != null ? ((NServer)var10.get()).getName() : null)
                                                .setWorld(var13.getWorld())
                                                .setExpiration(var13.getExpiration())
                                                .create();
                                             String var15 = GROUP_ADD_PERMISSION_BUNGEE.get()
                                                .replace("%permission%", (CharSequence)var9.get())
                                                .replace("%group%", ((Group)var8.get()).getName())
                                                .replace("%server%", var12);
                                             var5.sendMessage(this.plugin.getPrefix() + var15 + var13.getCombinedInfo());
                                          }
                                       }
                                    }
                                 );
                              }
                           );
                           var3.require(new SuperadminRequirement(this.plugin));
                           var3.action(
                              (var4, var5, var6) -> {
                                 ArgumentValue var7 = var1xx.getValue(var6);
                                 ArgumentValue var8 = var2.getValue(var6);
                                 ArgumentValue var9 = var3.getValue(var6);
                                 if (var9.isValid()) {
                                    String var10 = var9.get() != null ? ((NServer)var9.get()).getName() : ALL_SERVERS_LABEL.get();
                                    if (this.isValidGroup(var4, var7)) {
                                       CommandOptions var11 = new CommandOptions(this.plugin, var2.getSpareArguments(var6));
                                       boolean var12 = ((Group)var7.get())
                                          .getPermissions()
                                          .name((String)var8.get())
                                          .servers(var11.getServer() == null, var11.getServer())
                                          .worlds(true)
                                          .stream()
                                          .anyMatch(var1xxxxxx -> var1xxxxxx.getExpiration() == var11.getExpiration());
                                       if (var12) {
                                          String var14 = GROUP_ALREADY_HAS_PERMISSION.get()
                                             .replace("%group%", ((Group)var7.get()).getName())
                                             .replace("%permission%", (CharSequence)var8.get());
                                          var4.sendMessage(this.plugin.getPrefix() + var14);
                                          return;
                                       }

                                       ((Group)var7.get())
                                          .newPermission((String)var8.get())
                                          .setServer(var9.get() != null ? ((NServer)var9.get()).getName() : null)
                                          .setWorld(var11.getWorld())
                                          .setExpiration(var11.getExpiration())
                                          .create();
                                       String var13 = GROUP_ADD_PERMISSION_BUNGEE.get()
                                          .replace("%permission%", (CharSequence)var8.get())
                                          .replace("%group%", ((Group)var7.get()).getName())
                                          .replace("%server%", var10);
                                       var4.sendMessage(this.plugin.getPrefix() + var13 + var11.getCombinedInfo());
                                    }
                                 }
                              }
                           );
                        }
                     );
                  } else {
                     var2.newNode(
                        new WorldArgument(this.plugin, EmptyReason.INVALID),
                        var3 -> {
                           var3.require(new SuperadminRequirement(this.plugin));
                           var3.action(
                              (var4, var5, var6) -> {
                                 ArgumentValue var7 = var1xx.getValue(var6);
                                 ArgumentValue var8 = var2.getValue(var6);
                                 ArgumentValue var9 = var3.getValue(var6);
                                 if (this.isValidGroup(var4, var7)) {
                                    CommandOptions var10 = new CommandOptions(this.plugin, var2.getSpareArguments(var6));
                                    boolean var11 = ((Group)var7.get())
                                       .getPermissions()
                                       .name((String)var8.get())
                                       .servers(var10.getServer() == null, var10.getServer())
                                       .worlds(var9.get() == null, var10.getWorld())
                                       .stream()
                                       .anyMatch(var1xxxxxx -> var1xxxxxx.getExpiration() == var10.getExpiration());
                                    if (var11) {
                                       String var13 = GROUP_ALREADY_HAS_PERMISSION.get()
                                          .replace("%group%", ((Group)var7.get()).getName())
                                          .replace("%permission%", (CharSequence)var8.get());
                                       var4.sendMessage(this.plugin.getPrefix() + var13);
                                       return;
                                    }

                                    ((Group)var7.get())
                                       .newPermission((String)var8.get())
                                       .setServer(var10.getServer())
                                       .setWorld(var10.getWorld())
                                       .setExpiration(var10.getExpiration())
                                       .create();
                                    String var12 = GROUP_ADD_PERMISSION.get()
                                       .replace("%permission%", (CharSequence)var8.get())
                                       .replace("%group%", ((Group)var7.get()).getName());
                                    var4.sendMessage(this.plugin.getPrefix() + var12 + var10.getCombinedInfo());
                                 }
                              }
                           );
                        }
                     );
                     var2.require(new SuperadminRequirement(this.plugin));
                     var2.action(
                        (var3, var4, var5) -> {
                           ArgumentValue var6 = var1xx.getValue(var5);
                           ArgumentValue var7 = var2.getValue(var5);
                           if (this.isValidGroup(var3, var6)) {
                              CommandOptions var8 = new CommandOptions(this.plugin, var2.getSpareArguments(var5));
                              boolean var9 = ((Group)var6.get())
                                 .getPermissions()
                                 .name((String)var7.get())
                                 .servers(var8.getServer() == null, var8.getServer())
                                 .worlds(true)
                                 .stream()
                                 .anyMatch(var1xxxxx -> var1xxxxx.getExpiration() == var8.getExpiration());
                              if (var9) {
                                 String var11 = GROUP_ALREADY_HAS_PERMISSION.get()
                                    .replace("%group%", ((Group)var6.get()).getName())
                                    .replace("%permission%", (CharSequence)var7.get());
                                 var3.sendMessage(this.plugin.getPrefix() + var11);
                                 return;
                              }

                              ((Group)var6.get())
                                 .newPermission((String)var7.get())
                                 .setServer(var8.getServer())
                                 .setWorld(var8.getWorld())
                                 .setExpiration(var8.getExpiration())
                                 .create();
                              String var10 = GROUP_ADD_PERMISSION.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%group%", ((Group)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var10 + var8.getCombinedInfo());
                           }
                        }
                     );
                  }
               }
            )
         )
      );
   }

   private void AddPlayerPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("AddPlayerPermission"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new PermissionArgument(this.plugin),
               var2 -> {
                  if (this.plugin.getThisServer().isPresent()) {
                     var2.newNode(
                        new ServerArgument(this.plugin),
                        var3 -> var3.newNode(
                           new WorldArgument(this.plugin, EmptyReason.INVALID),
                           var4 -> {
                              var4.require(new SuperadminRequirement(this.plugin));
                              var4.action(
                                 (var5, var6, var7) -> {
                                    ArgumentValue var8 = var1xx.getValue(var7);
                                    ArgumentValue var9 = var2.getValue(var7);
                                    ArgumentValue var10 = var3.getValue(var7);
                                    ArgumentValue var11 = var4.getValue(var7);
                                    if (var10.isValid()) {
                                       String var12 = var10.get() != null ? ((NServer)var10.get()).getName() : ALL_SERVERS_LABEL.get();
                                       if (this.isValidUser(var5, var8)) {
                                          CommandOptions var13 = new CommandOptions(this.plugin, var3.getSpareArguments(var7));
                                          long var14 = var13.getExpiration();
                                          var13.setOperator(var7.contains("+") ? "+" : (var7.contains("-") ? "-" : ""));
                                          Optional var16 = ((User)var8.get())
                                             .getPermissions()
                                             .name((String)var9.get())
                                             .servers(var13.getServer() == null, var13.getServer())
                                             .worlds(var11.get() == null, var13.getWorld())
                                             .stream()
                                             .findFirst();
                                          if (var13.getOperator().equalsIgnoreCase("+") && var16.isPresent()) {
                                             long var20 = ((Permission)var16.get()).getExpiration();
                                             var14 = var20 + var13.getSecs() * 1000L;
                                             ((Permission)var16.get()).remove();
                                          } else if (var13.getOperator().equalsIgnoreCase("-") && var16.isPresent()) {
                                             long var19 = ((Permission)var16.get()).getExpiration();
                                             var14 = var19 - var13.getSecs() * 1000L;
                                             ((Permission)var16.get()).remove();
                                          } else if (var16.isPresent()) {
                                             String var17 = PLAYER_ALREADY_HAS_PERMISSION.get()
                                                .replace("%player%", ((User)var8.get()).getName())
                                                .replace("%permission%", (CharSequence)var9.get());
                                             var5.sendMessage(this.plugin.getPrefix() + var17);
                                             return;
                                          }

                                          ((User)var8.get())
                                             .newPermission((String)var9.get())
                                             .setServer(var10.get() != null ? ((NServer)var10.get()).getName() : null)
                                             .setWorld(var13.getWorld())
                                             .setExpiration(var14)
                                             .create();
                                          String var21 = PLAYER_ADD_PERMISSION_BUNGEE.get()
                                             .replace("%permission%", (CharSequence)var9.get())
                                             .replace("%player%", ((User)var8.get()).getName())
                                             .replace("%server%", var12);
                                          var5.sendMessage(this.plugin.getPrefix() + var21 + var13.getCombinedInfo());
                                       }
                                    }
                                 }
                              );
                              var3.require(new SuperadminRequirement(this.plugin));
                              var3.action(
                                 (var4x, var5, var6) -> {
                                    ArgumentValue var7 = var1xx.getValue(var6);
                                    ArgumentValue var8 = var2.getValue(var6);
                                    ArgumentValue var9 = var3.getValue(var6);
                                    if (var9.isValid()) {
                                       String var10 = var9.get() != null ? ((NServer)var9.get()).getName() : ALL_SERVERS_LABEL.get();
                                       if (this.isValidUser(var4x, var7)) {
                                          CommandOptions var11 = new CommandOptions(this.plugin, var3.getSpareArguments(var6));
                                          long var12 = var11.getExpiration();
                                          var11.setOperator(var6.contains("+") ? "+" : (var6.contains("-") ? "-" : ""));
                                          Optional var14 = ((User)var7.get())
                                             .getPermissions()
                                             .name((String)var8.get())
                                             .worlds(true)
                                             .servers(var11.getServer() == null, var11.getServer())
                                             .stream()
                                             .findFirst();
                                          if (var11.getOperator().equalsIgnoreCase("+") && var14.isPresent()) {
                                             long var18 = ((Permission)var14.get()).getExpiration();
                                             var12 = var18 + var11.getSecs() * 1000L;
                                             ((Permission)var14.get()).remove();
                                          } else if (var11.getOperator().equalsIgnoreCase("-") && var14.isPresent()) {
                                             long var17 = ((Permission)var14.get()).getExpiration();
                                             var12 = var17 - var11.getSecs() * 1000L;
                                             ((Permission)var14.get()).remove();
                                          } else if (var14.isPresent()) {
                                             String var15 = PLAYER_ALREADY_HAS_PERMISSION.get()
                                                .replace("%player%", ((User)var7.get()).getName())
                                                .replace("%permission%", (CharSequence)var8.get());
                                             var4x.sendMessage(this.plugin.getPrefix() + var15);
                                             return;
                                          }

                                          ((User)var7.get())
                                             .newPermission((String)var8.get())
                                             .setServer(var9.get() != null ? ((NServer)var9.get()).getName() : null)
                                             .setExpiration(var12)
                                             .create();
                                          String var19 = PLAYER_ADD_PERMISSION_BUNGEE.get()
                                             .replace("%permission%", (CharSequence)var8.get())
                                             .replace("%player%", ((User)var7.get()).getName())
                                             .replace("%server%", var10);
                                          var4x.sendMessage(this.plugin.getPrefix() + var19 + var11.getCombinedInfo());
                                       }
                                    }
                                 }
                              );
                           }
                        )
                     );
                  } else {
                     var2.newNode(
                        new WorldArgument(this.plugin, EmptyReason.INVALID),
                        var3 -> {
                           var3.require(new SuperadminRequirement(this.plugin));
                           var3.action(
                              (var4, var5, var6) -> {
                                 ArgumentValue var7 = var1xx.getValue(var6);
                                 ArgumentValue var8 = var2.getValue(var6);
                                 ArgumentValue var9 = var3.getValue(var6);
                                 if (this.isValidUser(var4, var7)) {
                                    CommandOptions var10 = new CommandOptions(this.plugin, var2.getSpareArguments(var6));
                                    long var11 = var10.getExpiration();
                                    var10.setOperator(var6.contains("+") ? "+" : (var6.contains("-") ? "-" : ""));
                                    Optional var13 = ((User)var7.get())
                                       .getPermissions()
                                       .name((String)var8.get())
                                       .servers(var10.getServer() == null, var10.getServer())
                                       .worlds(var9.get() == null, var10.getWorld())
                                       .stream()
                                       .findFirst();
                                    if (var10.getOperator().equalsIgnoreCase("+") && var13.isPresent()) {
                                       long var17 = ((Permission)var13.get()).getExpiration();
                                       var11 = var17 + var10.getSecs() * 1000L;
                                       ((Permission)var13.get()).remove();
                                    } else if (var10.getOperator().equalsIgnoreCase("-") && var13.isPresent()) {
                                       long var16 = ((Permission)var13.get()).getExpiration();
                                       var11 = var16 - var10.getSecs() * 1000L;
                                       ((Permission)var13.get()).remove();
                                    } else if (var13.isPresent()) {
                                       String var14 = PLAYER_ALREADY_HAS_PERMISSION.get()
                                          .replace("%player%", ((User)var7.get()).getName())
                                          .replace("%permission%", (CharSequence)var8.get());
                                       var4.sendMessage(this.plugin.getPrefix() + var14);
                                       return;
                                    }

                                    ((User)var7.get())
                                       .newPermission((String)var8.get())
                                       .setServer(var10.getServer())
                                       .setWorld(var10.getWorld())
                                       .setExpiration(var11)
                                       .create();
                                    String var18 = PLAYER_ADD_PERMISSION.get()
                                       .replace("%permission%", (CharSequence)var8.get())
                                       .replace("%player%", ((User)var7.get()).getName());
                                    var4.sendMessage(this.plugin.getPrefix() + var18 + var10.getCombinedInfo());
                                 }
                              }
                           );
                        }
                     );
                     var2.require(new SuperadminRequirement(this.plugin));
                     var2.action(
                        (var3, var4, var5) -> {
                           ArgumentValue var6 = var1xx.getValue(var5);
                           ArgumentValue var7 = var2.getValue(var5);
                           if (this.isValidUser(var3, var6)) {
                              CommandOptions var8 = new CommandOptions(this.plugin, var2.getSpareArguments(var5));
                              long var9 = var8.getExpiration();
                              var8.setOperator(var5.contains("+") ? "+" : (var5.contains("-") ? "-" : ""));
                              Optional var11 = ((User)var6.get())
                                 .getPermissions()
                                 .name((String)var7.get())
                                 .servers(var8.getServer() == null, var8.getServer())
                                 .stream()
                                 .findFirst();
                              if (var8.getOperator().equalsIgnoreCase("+") && var11.isPresent()) {
                                 long var15 = ((Permission)var11.get()).getExpiration();
                                 var9 = var15 + var8.getSecs() * 1000L;
                                 ((Permission)var11.get()).remove();
                              } else if (var8.getOperator().equalsIgnoreCase("-") && var11.isPresent()) {
                                 long var14 = ((Permission)var11.get()).getExpiration();
                                 var9 = var14 - var8.getSecs() * 1000L;
                                 ((Permission)var11.get()).remove();
                              } else if (var11.isPresent()) {
                                 String var12 = PLAYER_ALREADY_HAS_PERMISSION.get()
                                    .replace("%player%", ((User)var6.get()).getName())
                                    .replace("%permission%", (CharSequence)var7.get());
                                 var3.sendMessage(this.plugin.getPrefix() + var12);
                                 return;
                              }

                              ((User)var6.get()).newPermission((String)var7.get()).setServer(var8.getServer()).setExpiration(var9).create();
                              String var16 = PLAYER_ADD_PERMISSION.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%player%", ((User)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var16 + var8.getCombinedInfo());
                           }
                        }
                     );
                  }
               }
            )
         )
      );
   }

   private void AddSuperAdmin(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("AddSuperAdmin"), var1x -> var1x.newNode(new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            if (this.isValidUser(var2, var5)) {
               if (((User)var5.get()).isSuperadmin()) {
                  var2.sendMessage(this.plugin.getPrefix() + PLAYER_ALREADY_SUPER_ADMIN.get().replace("%player%", ((User)var5.get()).getName()));
               } else {
                  ((User)var5.get()).setSuperadmin(true);
                  var2.sendMessage(this.plugin.getPrefix() + PLAYER_ADD_SUPER_ADMIN.get().replace("%player%", ((User)var5.get()).getName()));
               }
            }
         });
      }));
   }

   private void RegisterUser(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("RegisterUser"),
         var1x -> var1x.newNode(
            new AnyArgument("<UUID>"),
            var1xx -> var1xx.newNode(
               new AnyArgument(PLAYER_NAME_USAGE),
               var2 -> var2.newNode(
                  new BooleanArgument(this.plugin, EmptyReason.INVALID),
                  var3 -> var3.action(
                     (var4, var5, var6) -> {
                        ArgumentValue var7 = var1xx.getValue(var6);
                        ArgumentValue var8 = var2.getValue(var6);
                        ArgumentValue var9 = var3.getValue(var6);

                        try {
                           UUID var10 = UUID.fromString((String)var7.get());
                           boolean var11 = this.plugin.getUsers().uuid(var10).isPresent();
                           if (var11) {
                              var4.sendMessage(this.plugin.getPrefix() + REGISTER_USER_ALREADY_EXISTING.get().replace("%uuid%", var10.toString()));
                           } else {
                              this.plugin.registerUser(var10, var8.get() == null ? "NOT DEFINED" : (String)var8.get(), (Boolean)var9.get());
                              var4.sendMessage(
                                 this.plugin.getPrefix()
                                    + REGISTER_USER_SUCCESS.get().replace("%playerName%", (CharSequence)var8.get()).replace("%uuid%", var10.toString())
                              );
                           }
                        } catch (IllegalArgumentException var12) {
                           var4.sendMessage(this.plugin.getPrefix() + REGISTER_USER_INVALID_UUID.get());
                        }
                     }
                  )
               )
            )
         )
      );
   }

   private void RemoveGroup(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("RemoveGroup"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> var1xx.newNode(new GroupArgument(this.plugin, EmptyReason.INVALID), var2 -> {
               var2.require(new SuperadminRequirement(this.plugin));
               var2.action((var3, var4, var5) -> {
                  ArgumentValue var6 = var1xx.getValue(var5);
                  ArgumentValue var7 = var2.getValue(var5);
                  if (this.isValidUser(var3, var6) && this.isValidGroup(var3, var7)) {
                     Phrase var8;
                     if (((User)var6.get()).getGroups().contains(((Group)var7.get()).toStored())) {
                        ((User)var6.get()).removeGroup((Group)var7.get());
                        var8 = PLAYER_REMOVE_GROUP;
                     } else {
                        var8 = PLAYER_NOT_IN_GROUP;
                     }

                     String var9 = var8.get().replace("%player%", ((User)var6.get()).getName()).replace("%group%", ((Group)var7.get()).getName());
                     var3.sendMessage(this.plugin.getPrefix() + var9);
                  }
               });
            })
         )
      );
   }

   private void DeleteUser(CommandNode<String, SpecificArgument> var1) {
      this.cachedOnlineChecker = CachedOnlineChecker.scan();
      var1.newNode(new SpecificArgument("deleteUser"), var1x -> var1x.newNode(new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            if (this.cachedOnlineChecker.isOnline(((User)var5.get()).getUuid())) {
               var2.sendMessage(this.plugin.getPrefix() + DELETE_USER_UNSUCCESSFUL.get().replace("%user%", ((User)var5.get()).getName()));
            }

            if (!this.cachedOnlineChecker.isOnline(((User)var5.get()).getUuid())) {
               ((User)var5.get()).remove();
               var2.sendMessage(this.plugin.getPrefix() + DELETE_USER_SUCCESSFUL.get().replace("%user%", ((User)var5.get()).getName()));
            }
         });
      }));
   }

   private void RemoveGroupPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("RemoveGroupPermission"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new PermissionArgument(this.plugin),
               var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action(
                     (var3, var4, var5) -> {
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        if (this.isValidGroup(var3, var6)) {
                           PermissionList var8 = ((Group)var6.get()).getPermissions().name((String)var7.get());
                           var8.forEach(Permission::remove);
                           if (var8.size() == 0) {
                              String var11 = REMOVE_PERMISSION_UNSUCCESSFUL.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%target%", ((Group)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var11);
                              return;
                           }

                           if (var8.size() == 1) {
                              String var10 = REMOVE_PERMISSION_SUCCESSFUL.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%target%", ((Group)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var10);
                              return;
                           }

                           String var9 = REMOVE_PERMISSION_SUCCESSFUL_MULTI.get()
                              .replace("%permission%", (CharSequence)var7.get())
                              .replace("%target%", ((Group)var6.get()).getName())
                              .replace("%count%", Integer.toString(var8.size()));
                           var3.sendMessage(this.plugin.getPrefix() + var9);
                        }
                     }
                  );
               }
            )
         )
      );
   }

   private void RemovePlayerPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("RemovePlayerPermission"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new PermissionArgument(this.plugin),
               var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action(
                     (var3, var4, var5) -> {
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        if (this.isValidUser(var3, var6)) {
                           PermissionList var8 = ((User)var6.get()).getPermissions().name((String)var7.get());
                           var8.forEach(Permission::remove);
                           if (var8.size() == 0) {
                              String var11 = REMOVE_PERMISSION_UNSUCCESSFUL.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%target%", ((User)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var11);
                              return;
                           }

                           if (var8.size() == 1) {
                              String var10 = REMOVE_PERMISSION_SUCCESSFUL.get()
                                 .replace("%permission%", (CharSequence)var7.get())
                                 .replace("%target%", ((User)var6.get()).getName());
                              var3.sendMessage(this.plugin.getPrefix() + var10);
                              return;
                           }

                           String var9 = REMOVE_PERMISSION_SUCCESSFUL_MULTI.get()
                              .replace("%permission%", (CharSequence)var7.get())
                              .replace("%target%", ((User)var6.get()).getName())
                              .replace("%count%", Integer.toString(var8.size()));
                           var3.sendMessage(this.plugin.getPrefix() + var9);
                        }
                     }
                  );
               }
            )
         )
      );
   }

   private void RemoveSuperAdmin(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("RemoveSuperAdmin"), var1x -> var1x.newNode(new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            if (this.isValidUser(var2, var5)) {
               if (((User)var5.get()).isSuperadmin()) {
                  ((User)var5.get()).setSuperadmin(false);
                  var2.sendMessage(this.plugin.getPrefix() + PLAYER_REMOVE_SUPER_ADMIN.get().replace("%player%", ((User)var5.get()).getName()));
               } else {
                  var2.sendMessage(this.plugin.getPrefix() + PLAYER_NOT_SUPER_ADMIN.get().replace("%player%", ((User)var5.get()).getName()));
               }
            }
         });
      }));
   }

   private void RestrictGroupToWorld(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("RestrictGroupToWorld"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new WorldArgument(this.plugin, EmptyReason.INVALID),
               var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action(
                     (var3, var4, var5) -> {
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        if (var7.get() == null) {
                           ((Group)var6.get()).setWorld(null);
                           var3.sendMessage(
                              this.plugin.getPrefix() + GROUP_RESTRICT_TO_WORLD_ALL_WORLDS.get().replace("%group%", ((Group)var6.get()).getName())
                           );
                        } else if (!(var7.get() instanceof World)) {
                           var3.sendMessage(this.plugin.getPrefix() + GROUP_RESTRICT_TO_WORLD_INVALID.get());
                        } else {
                           ((Group)var6.get()).setWorld(((World)var7.get()).getName());
                           var3.sendMessage(
                              this.plugin.getPrefix()
                                 + GROUP_RESTRICT_TO_WORLD_SPECIFIC_WORLD.get()
                                    .replace("%group%", ((Group)var6.get()).getName())
                                    .replace("%world%", ((World)var7.get()).getName())
                           );
                        }
                     }
                  );
               }
            )
         )
      );
   }

   private void DeleteGroup(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("DeleteGroup"), var1x -> var1x.newNode(new GroupArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            ((Group)var5.get()).remove();
            var2.sendMessage(this.plugin.getPrefix() + DELETE_GROUP_CONFIRM.get());
         });
      }));
   }

   private void PurgeSuperAdmins(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgeSuperAdmins"), var1x -> {
         var1x.require(new SuperadminRequirement(this.plugin));
         var1x.action((var1xx, var2, var3) -> {
            for (User var5 : this.plugin.getUsers()) {
               var5.setSuperadmin(false);
            }

            var1xx.sendMessage(this.plugin.getPrefix() + PURGE_SUPER_ADMINS_CONFIRM.get());
         });
      });
   }

   private void PurgeGroup(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgeGroup"), var1x -> var1x.newNode(new GroupArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            if (this.isValidGroup(var2, var5)) {
               for (User var7 : this.plugin.getUsers()) {
                  var7.removeGroup((Group)var5.get());
               }

               var2.sendMessage(this.plugin.getPrefix() + PURGE_GROUP_CONFIRM.get().replace("%group%", ((Group)var5.get()).getName()));
            }
         });
      }));
   }

   private void PurgeGroupPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgeGroupPermission"), var1x -> var1x.newNode(new PermissionArgument(this.plugin), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);

            for (Group var7 : this.plugin.getGroups()) {
               PermissionList var8 = var7.getPermissions().name((String)var5.get());
               var8.forEach(Permission::remove);
            }

            var2.sendMessage(this.plugin.getPrefix() + PURGE_GROUPPERMISSION_CONFIRM.get().replace("%permission%", (CharSequence)var5.get()));
         });
      }));
   }

   private void PurgePlayerPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgePlayerPermission"), var1x -> {
         var1x.require(new SuperadminRequirement(this.plugin));
         var1x.newNode(new PermissionArgument(this.plugin), var1xx -> var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);

            for (User var7 : this.plugin.getUsers()) {
               PermissionList var8 = var7.getPermissions().name((String)var5.get());
               var8.forEach(Permission::remove);
            }

            var2.sendMessage(this.plugin.getPrefix() + PURGE_PLAYERPERMISSION_CONFIRM.get().replace("%permission%", (CharSequence)var5.get()));
         }));
      });
   }

   private void PurgeAllPlayerPermission(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgeAllPlayerPermission"), var1x -> {
         var1x.require(new SuperadminRequirement(this.plugin));
         var1x.newNode(new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);
            if (var5.isValid()) {
               for (Permission var7 : ((User)var5.get()).getPermissions()) {
                  var7.remove();
               }

               var2.sendMessage(this.plugin.getPrefix() + PURGE_ALL_PLAYERPERMISSIONS_CONFIRM.get().replace("%user%", ((User)var5.get()).getName()));
            }
         }));
      });
   }

   private void PurgePlayerGroups(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(new SpecificArgument("PurgePlayerGroups"), var1x -> var1x.newNode(new UserArgument(this.plugin, EmptyReason.INVALID), var1xx -> {
         var1xx.require(new SuperadminRequirement(this.plugin));
         var1xx.action((var2, var3, var4) -> {
            ArgumentValue var5 = var1xx.getValue(var4);

            for (Group var7 : ((User)var5.get()).getActiveGroups()) {
               ((User)var5.get()).removeGroup(var7);
            }

            var2.sendMessage(this.plugin.getPrefix() + PURGE_PLAYER_GROUPS_CONFIRM.get().replace("%player%", ((User)var5.get()).getName()));
         });
      }));
   }

   private void SetGroups(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetGroups"),
         var1x -> var1x.newNode(
            new UserArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> {
               var1xx.require(new SuperadminRequirement(this.plugin));
               var1xx.action(
                  (var2, var3, var4) -> {
                     ArgumentValue var5 = var1xx.getValue(var4);
                     if (this.isValidUser(var2, var5)) {
                        ((User)var5.get()).getRankups().forEach(UserRankup::remove);
                        GroupList var6 = var1xx.getSpareArguments(var4)
                           .stream()
                           .map(var1xxxx -> this.plugin.getGroups().name(var1xxxx).orElse(null))
                           .filter(Objects::nonNull)
                           .collect(Collectors.toCollection(GroupList::new));

                        for (Group var8 : var6) {
                           ((User)var5.get()).addGroup(var8);
                        }

                        if (var6.size() == 0) {
                           var2.sendMessage(this.plugin.getPrefix() + PLAYER_CLEARED_GROUPS.get().replace("%player%", ((User)var5.get()).getName()));
                        } else {
                           String var9 = var6.stream().map(Group::getName).collect(Collectors.joining(", "));
                           String var10 = PLAYER_SET_GROUPS.get().replace("%player%", ((User)var5.get()).getName()).replace("%groups%", var9);
                           var2.sendMessage(this.plugin.getPrefix() + var10);
                        }
                     }
                  }
               );
            }
         )
      );
   }

   private void SetGroupPriority(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetGroupPriority"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> var1xx.newNode(
               new AnyIntegerArgument(PRIORITY_USAGE, EmptyReason.INVALID),
               var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action(
                     (var3, var4, var5) -> {
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        int var8 = 1;

                        try {
                           for (Group var10 : this.plugin.getGroups().bestToWorst()) {
                              var10.setPriority(var8);
                              var8++;
                           }

                           int var12 = ((Group)var6.get()).getPriority();
                           Optional var13 = this.plugin.getGroups().stream().filter(var1xxxxx -> var1xxxxx.getPriority() == (Integer)var7.get()).findFirst();
                           ((Group)var6.get()).setPriority((Integer)var7.get());
                           ((Group)var13.get()).setPriority(var12);
                           var3.sendMessage(
                              this.plugin.getPrefix()
                                 + CHANGED_GROUP_PRIORITY.get()
                                    .replace("%group%", ((Group)var6.get()).getName())
                                    .replace("%priority%", ((Integer)var7.get()).toString())
                           );
                        } catch (NullPointerException | NoSuchElementException var11) {
                           var3.sendMessage(this.plugin.getPrefix() + Colors.GRAY + INVALID_ARGUMENTS.get());
                        }
                     }
                  );
               }
            )
         )
      );
   }

   private void SetGroupPrefix(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetGroupPrefix"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> {
               var1xx.newNode(new SpecificArgument("none"), var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action((var2x, var3, var4) -> {
                     ArgumentValue var5 = var1xx.getValue(var4);
                     if (this.isValidGroup(var2x, var5)) {
                        ((Group)var5.get()).setPrefix(null);
                        var2x.sendMessage(this.plugin.getPrefix() + GROUP_SET_PREFIX_CLEAR.get().replace("%group%", ((Group)var5.get()).getName()));
                     }
                  });
               });
               var1xx.newNode(
                  new AnyArgument(PREFIX_USAGE),
                  var2 -> {
                     var2.require(new SuperadminRequirement(this.plugin));
                     var2.action(
                        (var3, var4, var5) -> {
                           ArgumentValue var6 = var1xx.getValue(var5);
                           ArgumentValue var7 = var2.getValue(var5);
                           String var8 = String.join(" ", var2.getSpareArguments(var5));
                           if (var8.isEmpty()) {
                              var8 = null;
                           }

                           if (this.isValidGroup(var3, var6)) {
                              ((Group)var6.get()).setPrefix((String)var7.get() + (var8 == null ? "" : " " + var8));
                              String var9 = GROUP_SET_PREFIX_SET.get()
                                 .replace("%group%", ((Group)var6.get()).getName())
                                 .replace("%prefix%", (String)var7.get() + (var8 == null ? "" : " " + var8));
                              var3.sendMessage(this.plugin.getPrefix() + var9);
                           }
                        }
                     );
                  }
               );
            }
         )
      );
   }

   private void SetGroupSuffix(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetGroupSuffix"),
         var1x -> var1x.newNode(
            new GroupArgument(this.plugin, EmptyReason.INVALID),
            var1xx -> {
               var1xx.newNode(new SpecificArgument("none"), var2 -> {
                  var2.require(new SuperadminRequirement(this.plugin));
                  var2.action((var2x, var3, var4) -> {
                     ArgumentValue var5 = var1xx.getValue(var4);
                     if (this.isValidGroup(var2x, var5)) {
                        ((Group)var5.get()).setSuffix(null);
                        var2x.sendMessage(this.plugin.getPrefix() + GROUP_SET_SUFFIX_CLEAR.get().replace("%group%", ((Group)var5.get()).getName()));
                     }
                  });
               });
               var1xx.newNode(
                  new AnyArgument(SUFFIX_USAGE),
                  var2 -> var2.action(
                     (var3, var4, var5) -> {
                        var2.require(new SuperadminRequirement(this.plugin));
                        ArgumentValue var6 = var1xx.getValue(var5);
                        ArgumentValue var7 = var2.getValue(var5);
                        String var8 = String.join(" ", var2.getSpareArguments(var5));
                        if (var8.isEmpty()) {
                           var8 = null;
                        }

                        if (this.isValidGroup(var3, var6)) {
                           ((Group)var6.get()).setSuffix((String)var7.get() + (var8 == null ? "" : " " + var8));
                           String var9 = GROUP_SET_SUFFIX_SET.get()
                              .replace("%group%", ((Group)var6.get()).getName())
                              .replace("%suffix%", (String)var7.get() + (var8 == null ? "" : " " + var8));
                           var3.sendMessage(this.plugin.getPrefix() + var9);
                        }
                     }
                  )
               );
            }
         )
      );
   }

   private void SetPlayerPrefix(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetPlayerPrefix"),
         var1x -> {
            var1x.newNode(new SpecificArgument("clearAll"), var1xx -> {
               var1xx.require(new SuperadminRequirement(this.plugin));
               var1xx.action((var1xxx, var2, var3) -> {
                  UserList var4 = this.plugin.getUsers().stream().filter(User::hasPrefix).collect(Collectors.toCollection(UserList::new));
                  var4.forEach(var0 -> var0.setPrefix(null));
                  var1xxx.sendMessage(this.plugin.getPrefix() + PLAYER_SET_PREFIX_CLEAR_ALL.get().replace("%count%", Integer.toString(var4.size())));
               });
            });
            var1x.newNode(
               new UserArgument(this.plugin, EmptyReason.INVALID),
               var1xx -> {
                  var1xx.newNode(new SpecificArgument("none"), var2 -> {
                     var2.require(new SuperadminRequirement(this.plugin));
                     var2.action((var2x, var3, var4) -> {
                        ArgumentValue var5 = var1xx.getValue(var4);
                        if (this.isValidUser(var2x, var5)) {
                           ((User)var5.get()).setPrefix(null);
                           var2x.sendMessage(this.plugin.getPrefix() + PLAYER_SET_PREFIX_CLEAR.get().replace("%user%", ((User)var5.get()).getName()));
                        }
                     });
                  });
                  var1xx.newNode(
                     new AnyArgument(PREFIX_USAGE),
                     var2 -> {
                        var2.require(new SuperadminRequirement(this.plugin));
                        var2.action(
                           (var3, var4, var5) -> {
                              ArgumentValue var6 = var1xx.getValue(var5);
                              ArgumentValue var7 = var2.getValue(var5);
                              String var8 = String.join(" ", var2.getSpareArguments(var5));
                              if (var8.isEmpty()) {
                                 var8 = null;
                              }

                              if (this.isValidUser(var3, var6)) {
                                 ((User)var6.get()).setPrefix((String)var7.get() + (var8 == null ? "" : " " + var8));
                                 String var9 = PLAYER_SET_PREFIX_SET.get()
                                    .replace("%user%", ((User)var6.get()).getName())
                                    .replace("%prefix%", (String)var7.get() + (var8 == null ? "" : " " + var8));
                                 var3.sendMessage(this.plugin.getPrefix() + var9);
                              }
                           }
                        );
                     }
                  );
               }
            );
         }
      );
   }

   private void SetPlayerSuffix(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("SetPlayerSuffix"),
         var1x -> {
            var1x.newNode(new SpecificArgument("clearAll"), var1xx -> {
               var1xx.require(new SuperadminRequirement(this.plugin));
               var1xx.action((var1xxx, var2, var3) -> {
                  UserList var4 = this.plugin.getUsers().stream().filter(User::hasSuffix).collect(Collectors.toCollection(UserList::new));
                  var4.forEach(var0 -> var0.setSuffix(null));
                  var1xxx.sendMessage(this.plugin.getPrefix() + PLAYER_SET_SUFFIX_CLEAR_ALL.get().replace("%count%", Integer.toString(var4.size())));
               });
            });
            var1x.newNode(
               new UserArgument(this.plugin, EmptyReason.INVALID),
               var1xx -> {
                  var1xx.newNode(new SpecificArgument("none"), var2 -> {
                     var2.require(new SuperadminRequirement(this.plugin));
                     var2.action((var2x, var3, var4) -> {
                        ArgumentValue var5 = var1xx.getValue(var4);
                        if (this.isValidUser(var2x, var5)) {
                           ((User)var5.get()).setSuffix(null);
                           var2x.sendMessage(this.plugin.getPrefix() + PLAYER_SET_SUFFIX_CLEAR.get().replace("%user%", ((User)var5.get()).getName()));
                        }
                     });
                  });
                  var1xx.newNode(
                     new AnyArgument(SUFFIX_USAGE),
                     var2 -> var2.action(
                        (var3, var4, var5) -> {
                           ArgumentValue var6 = var1xx.getValue(var5);
                           ArgumentValue var7 = var2.getValue(var5);
                           String var8 = String.join(" ", var2.getSpareArguments(var5));
                           if (var8.isEmpty()) {
                              var8 = null;
                           }

                           if (this.isValidUser(var3, var6)) {
                              ((User)var6.get()).setSuffix((String)var7.get() + (var8 == null ? "" : " " + var8));
                              String var9 = PLAYER_SET_SUFFIX_SET.get()
                                 .replace("%user%", ((User)var6.get()).getName())
                                 .replace("%suffix%", (String)var7.get() + (var8 == null ? "" : " " + var8));
                              var3.sendMessage(this.plugin.getPrefix() + var9);
                           }
                        }
                     )
                  );
               }
            );
         }
      );
   }

   private void TransferAccount(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("TransferAccount"),
         var1x -> var1x.newNode(
            new SpecificArguments(new String[]{"Groups", "Permissions", "All"}),
            var1xx -> var1xx.newNode(
               new SpecificArguments(new String[]{"Overwrite", "Append"}),
               var2 -> var2.newNode(
                  new UserArgument(this.plugin, EmptyReason.INVALID),
                  var3 -> var3.newNode(
                     new UserArgument(this.plugin, EmptyReason.INVALID),
                     var4 -> {
                        var4.require(new SuperadminRequirement(this.plugin));
                        var4.action(
                           (var5, var6, var7) -> {
                              ArgumentValue var8 = var1xx.getValue(var7);
                              ArgumentValue var9 = var2.getValue(var7);
                              ArgumentValue var10 = var3.getValue(var7);
                              ArgumentValue var11 = var4.getValue(var7);
                              if (this.isValidUser(var5, var10) && this.isValidUser(var5, var11)) {
                                 if (var10.get() == var11.get()) {
                                    var5.sendMessage(this.plugin.getPrefix() + TRANSFER_SOURCES_EQUAL.get());
                                 }

                                 if (((String)var9.get()).equalsIgnoreCase("Overwrite")) {
                                    if (((String)var8.get()).equalsIgnoreCase("Groups") || ((String)var8.get()).equalsIgnoreCase("All")) {
                                       for (Stored var13 : ((User)var11.get()).getGroups()) {
                                          ((User)var11.get()).removeGroup((Group)var13.get().get());
                                       }
                                    }

                                    if (((String)var8.get()).equalsIgnoreCase("Permissions") || ((String)var8.get()).equalsIgnoreCase("All")) {
                                       ((User)var11.get()).getPermissions().stream().forEach(Permission::remove);
                                    }
                                 }

                                 if (((String)var8.get()).equalsIgnoreCase("Groups")) {
                                    for (Stored var16 : ((User)var10.get()).getGroups()) {
                                       ((User)var11.get()).addGroup((Group)var16.get().get());
                                       ((User)var10.get()).removeGroup((Group)var16.get().get());
                                    }

                                    var5.sendMessage(
                                       this.plugin.getPrefix()
                                          + TRANSFER_ACCOUNT_SUCCESSFUL_GROUPS.get()
                                             .replace("%oldTarget%", ((User)var10.get()).getName())
                                             .replace("%newTarget%", ((User)var11.get()).getName())
                                    );
                                 }

                                 if (((String)var8.get()).equalsIgnoreCase("Permissions")) {
                                    ((User)var10.get()).getPermissions().stream().forEach(var1xxxxxxx -> {
                                       var1xxxxxxx.copyTo((PermissionHolder)var11.get());
                                       var1xxxxxxx.remove();
                                    });
                                    var5.sendMessage(
                                       this.plugin.getPrefix()
                                          + TRANSFER_ACCOUNT_SUCCESSFUL_PERMISSIONS.get()
                                             .replace("%oldTarget%", ((User)var10.get()).getName())
                                             .replace("%newTarget%", ((User)var11.get()).getName())
                                    );
                                 }

                                 if (((String)var8.get()).equalsIgnoreCase("All")) {
                                    for (Stored var17 : ((User)var10.get()).getGroups()) {
                                       ((User)var11.get()).addGroup((Group)var17.get().get());
                                       ((User)var10.get()).removeGroup((Group)var17.get().get());
                                    }

                                    ((User)var10.get()).getPermissions().stream().forEach(var1xxxxxxx -> {
                                       var1xxxxxxx.copyTo((PermissionHolder)var11.get());
                                       var1xxxxxxx.remove();
                                    });
                                    var5.sendMessage(
                                       this.plugin.getPrefix()
                                          + TRANSFER_ACCOUNT_SUCCESSFUL_ALL_DATA.get()
                                             .replace("%oldTarget%", ((User)var10.get()).getName())
                                             .replace("%newTarget%", ((User)var11.get()).getName())
                                    );
                                 }
                              }
                           }
                        );
                     }
                  )
               )
            )
         )
      );
   }

   private void Transfer(CommandNode<String, SpecificArgument> var1) {
      var1.newNode(
         new SpecificArgument("Transfer"),
         var1x -> {
            var1x.require(new SuperadminRequirement(this.plugin));
            var1x.require(new MySQLEnabledRequirement(this.plugin));
            var1x.newNode(
               new SpecificArguments(new String[]{"File", "MySQL"}),
               var1xx -> var1xx.newNode(new SpecificArguments(new String[]{"File", "MySQL"}), var2 -> var2.action((var3, var4, var5) -> {
                  String var6 = var1xx.getValue(var5).get();
                  String var7 = var2.getValue(var5).get();
                  if (var6.equalsIgnoreCase(var7)) {
                     var3.sendMessage(this.plugin.getPrefix() + TRANSFER_SOURCES_EQUAL);
                  }

                  if (var6.equals("File") && var7.equals("MySQL")) {
                     new TransferAssistant(this.plugin, true);
                  }

                  if (var6.equals("MySQL") && var7.equals("File")) {
                     new TransferAssistant(this.plugin, false);
                  }
               }))
            );
         }
      );
   }

   @Override
   public boolean isRegistered() {
      return true;
   }

   private boolean isValidUser(CommandSender var1, ArgumentValue<User> var2) {
      if (!var2.isValid()) {
         var1.sendMessage(this.plugin.getPrefix() + NO_VALID_USER.get().replace("%user%", var2.getRaw()));
      }

      return var2.isValid();
   }

   private boolean isValidGroup(CommandSender var1, ArgumentValue<Group> var2) {
      if (!var2.isValid()) {
         var1.sendMessage(this.plugin.getPrefix() + NO_VALID_GROUP.get().replace("%group%", var2.getRaw()));
      }

      return var2.isValid();
   }
}
