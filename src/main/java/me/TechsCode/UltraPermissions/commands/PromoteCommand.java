package me.TechsCode.UltraPermissions.commands;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.Command;
import me.TechsCode.UltraPermissions.base.command.CommandNode;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArgument;
import me.TechsCode.UltraPermissions.base.command.requirements.PermissionRequirement;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.commands.arguments.GroupPathArgument;
import me.TechsCode.UltraPermissions.commands.arguments.UserArgument;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.GroupPath;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.command.CommandSender;

public class PromoteCommand extends Command {
   private final UltraPermissions plugin;
   private static final Phrase NOT_VALID = Phrase.create("PromoteCommand.notValid", "Invalid User", Colors.GRAY);
   private static final Phrase PROMOTED = Phrase.create(
      "PromoteCommand.promoted.success",
      "Successfully promoted **%user%** from **%oldGroup%** to **%newGroup%** (**Group Path: %groupPathName%**)",
      Colors.GRAY,
      Colors.AQUA,
      Colors.GREEN,
      Colors.GREEN,
      Colors.WHITE
   );
   private static final Phrase FAILED_NOT_IN_PATH = Phrase.create(
      "PromoteCommand.promoted.failed.notInPath",
      "User **%user%** does not own a group of group path **%groupPathName%**",
      Colors.GRAY,
      Colors.AQUA,
      Colors.YELLOW
   );
   private static final Phrase FAILED_ALREADY_IN_HIGHEST = Phrase.create(
      "PromoteCommand.promoted.failed.alreadyInHighest",
      "User **%user%** is already in the highest group of the group path **%groupPathName%**",
      Colors.GRAY,
      Colors.AQUA,
      Colors.YELLOW
   );

   public PromoteCommand(UltraPermissions var1) {
      super(var1, "promote", "ultrapermissions.commands.promote");
      this.plugin = var1;
   }

   @Override
   public void define(CommandSender var1, CommandNode<String, SpecificArgument> var2) {
      var2.require(new PermissionRequirement(this.plugin, "ultrapermissions.commands.promote"));
      var2.newNode(
         new UserArgument(this.plugin, EmptyReason.NO_MATCH),
         var1x -> var1x.newNode(
            new GroupPathArgument(this.plugin, EmptyReason.NO_MATCH),
            var2x -> var2x.action(
               (var3, var4, var5) -> {
                  ArgumentValue var6 = var1x.getValue(var5);
                  ArgumentValue var7 = var2x.getValue(var5);
                  if (var6.isValid() && var7.isValid()) {
                     Optional<Group> toBePromotedGroup = ((GroupPath)var7.get()).getGroupPath().stream()
                        .filter(((User)var6.get()).getActiveGroups()::contains)
                        .reduce((previous, current) -> current);
                     if (!toBePromotedGroup.isPresent()) {
                        var3.sendMessage(
                           this.plugin.getPrefix()
                              + FAILED_NOT_IN_PATH.get()
                                 .replace("%user%", ((User)var6.get()).getName())
                                 .replace("%groupPathName%", ((GroupPath)var7.get()).getName())
                        );
                     } else if (((GroupPath)var7.get()).getGroupPath().size()
                        <= ((GroupPath)var7.get()).getGroupPath().indexOf(toBePromotedGroup.get()) + 1) {
                        var3.sendMessage(
                           this.plugin.getPrefix()
                              + FAILED_ALREADY_IN_HIGHEST.get()
                                 .replace("%user%", ((User)var6.get()).getName())
                                 .replace("%groupPathName%", ((GroupPath)var7.get()).getName())
                        );
                     } else {
                        Optional var8 = Optional.ofNullable(
                           ((GroupPath)var7.get()).getGroupPath().get(((GroupPath)var7.get()).getGroupPath().indexOf(toBePromotedGroup.get()) + 1)
                        );
                        if (var8.isPresent()) {
                           ((User)var6.get()).addGroup((Group)var8.get(), ((User)var6.get()).getGroupExpiry(toBePromotedGroup.get().toStored()));
                           ((User)var6.get()).removeGroup(toBePromotedGroup.get());
                           var3.sendMessage(
                              this.plugin.getPrefix()
                                 + PROMOTED.get()
                                    .replace("%user%", ((User)var6.get()).getName())
                                    .replace("%oldGroup%", toBePromotedGroup.get().getName())
                                    .replace("%newGroup%", ((Group)var8.get()).getName())
                                    .replace("%groupPathName%", ((GroupPath)var7.get()).getName())
                           );
                        }
                     }
                  } else {
                     var3.sendMessage(NOT_VALID.get());
                  }
               }
            )
         )
      );
   }

   @Override
   public boolean isRegistered() {
      return true;
   }
}
