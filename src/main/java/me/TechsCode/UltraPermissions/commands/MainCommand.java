package me.TechsCode.UltraPermissions.commands;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.Command;
import me.TechsCode.UltraPermissions.base.command.CommandNode;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArgument;
import me.TechsCode.UltraPermissions.base.command.requirements.PlayerRequirement;
import me.TechsCode.UltraPermissions.commands.arguments.GroupArgument;
import me.TechsCode.UltraPermissions.commands.arguments.UserArgument;
import me.TechsCode.UltraPermissions.commands.requirements.SuperadminRequirement;
import me.TechsCode.UltraPermissions.gui.GroupView;
import me.TechsCode.UltraPermissions.gui.MigrationView;
import me.TechsCode.UltraPermissions.gui.Overview;
import me.TechsCode.UltraPermissions.gui.UserView;
import me.TechsCode.UltraPermissions.migration.MigrationAssistant;
import me.TechsCode.UltraPermissions.migration.Migrations;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.command.CommandSender;

public class MainCommand extends Command {
   private final UltraPermissions plugin;

   public MainCommand(UltraPermissions var1) {
      super(var1, "uperms", "");
      this.plugin = var1;
   }

   @Override
   public void define(CommandSender var1, CommandNode<String, SpecificArgument> var2) {
      var2.require(new PlayerRequirement(this.plugin));
      var2.require(new SuperadminRequirement(this.plugin));
      var2.newNode(new UserArgument(this.plugin, EmptyReason.NO_MATCH), var1x -> var1x.action((var2x, var3, var4) -> {
         ArgumentValue var5 = var1x.getValue(var4);
         if (var5.isValid()) {
            new UserView(var3, this.plugin, (User)var5.get()) {
               @Override
               public void onBack() {
               }

               @Override
               public boolean hasBackButton() {
                  return false;
               }
            };
         }
      }));
      var2.newNode(new GroupArgument(this.plugin, EmptyReason.NO_MATCH), var1x -> var1x.action((var2x, var3, var4) -> {
         ArgumentValue var5 = var1x.getValue(var4);
         if (var5.isValid()) {
            new GroupView(var3, this.plugin, (Group)var5.get()) {
               @Override
               public void onBack() {
               }

               @Override
               public boolean hasBackButton() {
                  return false;
               }
            };
         }
      }));
      var2.action((var1x, var2x, var3) -> {
         if (Migrations.isAvailable()) {
            MigrationAssistant var4 = Migrations.getMigrationAssistant();
            new MigrationView(var2x, this.plugin, var4);
         } else {
            new Overview(var2x, this.plugin).reopen();
         }
      });
   }

   @Override
   public boolean isRegistered() {
      return true;
   }
}
