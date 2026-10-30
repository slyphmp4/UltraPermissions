package me.TechsCode.UltraPermissions.base.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArgument;
import me.TechsCode.UltraPermissions.base.misc.Callback;
import me.TechsCode.UltraPermissions.base.misc.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandNode<T, ARGUMENT extends Argument<T>> {
   private final CommandNode<?, ?> parent;
   private final ARGUMENT argument;
   private final List<CommandNode<?, ?>> children;
   private final List<Requirement> requirements;
   private String description;
   private Action<T> action;

   public static CommandNode<String, SpecificArgument> create(String var0) {
      return new CommandNode<>(null, new SpecificArgument(var0));
   }

   public CommandNode(CommandNode<?, ?> var1, ARGUMENT var2) {
      super();
      this.parent = var1;
      this.children = new ArrayList<>();
      this.argument = (ARGUMENT)var2;
      this.requirements = new ArrayList<>();
      this.description = null;
      this.action = null;
   }

   public <NEW_T, NEW_ARGUMENT extends Argument<NEW_T>> CommandNode<T, ARGUMENT> newNode(NEW_ARGUMENT var1, Setter<CommandNode<NEW_T, NEW_ARGUMENT>> var2) {
      CommandNode var3 = new CommandNode(this, (ARGUMENT)var1);
      var2.set(var3);
      this.children.add(var3);
      return this;
   }

   public void description(String var1) {
      this.description = var1;
   }

   public void require(Requirement var1) {
      this.requirements.add(var1);
   }

   public void action(Action<T> var1) {
      this.action = var1;
   }

   public CommandNode<?, ?> getCurrentNode(CommandSender var1, Player var2, List<String> var3) {
      return this.getCurrentNode(var1, var2, var3, 0);
   }

   private CommandNode<?, ?> getCurrentNode(CommandSender var1, Player var2, List<String> var3, int var4) {
      if (var3.size() == var4) {
         return null;
      }

      String var5 = (String)var3.get(var4);
      if (this.argument.hasMatch(var5)) {
         for (Requirement var7 : this.requirements) {
            if (!var7.isMatching(var1, var2, var3)) {
               return null;
            }
         }

         int var10 = var3.size() - (var4 + 1);
         if (var10 == 0) {
            return this;
         }

         for (CommandNode var8 : this.children) {
            CommandNode var9 = var8.getCurrentNode(var1, var2, var3, var4 + 1);
            if (var9 != null) {
               return var9;
            }
         }
      }

      return null;
   }

   public void execute(CommandSender var1, Player var2, Arguments var3, Callback<ExecuteCallback> var4) {
      this.execute(var1, var2, var3, 0, var4);
   }

   private boolean execute(CommandSender var1, Player var2, Arguments var3, int var4, Callback<ExecuteCallback> var5) {
      if (var3.size() == var4) {
         return false;
      }

      String var6 = var3.get(var4);
      if (this.argument.hasMatch(var6)) {
         for (Requirement var8 : this.requirements) {
            if (!var8.isMatching(var1, var2, var3)) {
               var8.onUnmatched(var1, var2);
               return true;
            }
         }

         for (CommandNode var11 : this.children) {
            boolean var9 = var11.execute(var1, var2, var3, var4 + 1, var5);
            if (var9) {
               return true;
            }
         }

         if (this.action != null) {
            this.action.run(var1, var2, var3);
            var5.run(new ExecuteCallback(this, ExecuteResult.SUCCESSFUL));
         } else {
            var5.run(new ExecuteCallback(this, ExecuteResult.NO_ACTION));
         }

         return true;
      } else {
         return false;
      }
   }

   public String getUsagePath() {
      return this.getNodesOnPath().stream().map(var0 -> var0.argument.getUsage()).collect(Collectors.joining(" "));
   }

   public ArgumentValue<T> getValue(Arguments var1) {
      int var2 = 0;

      for (CommandNode var3 = this.parent; var3 != null; var2++) {
         var3 = var3.parent;
      }

      String var4 = var1.get(var2);
      return this.argument.get(var4);
   }

   public List<String> getSpareArguments(Arguments var1) {
      int var2 = 0;

      for (CommandNode var3 = this; var3 != null; var2++) {
         var3 = var3.parent;
      }

      return var1.subList(var2, var1.size());
   }

   public List<CommandNode<?, ?>> getEndNodes() {
      if (this.children.size() == 0) {
         return Collections.singletonList(this);
      }

      ArrayList var1 = new ArrayList();

      for (CommandNode var3 : this.children) {
         var1.addAll(var3.getEndNodes());
      }

      return var1;
   }

   public List<CommandNode<?, ?>> getNodesOnPath() {
      ArrayList var1 = new ArrayList();
      var1.add(this);
      CommandNode var2 = this;

      while (var2 != null) {
         var2 = var2.parent;
         if (var2 != null) {
            var1.add(var2);
         }
      }

      Collections.reverse(var1);
      return var1;
   }

   public List<CommandNode<?, ?>> getChildren() {
      return this.children;
   }

   public boolean hasDescription() {
      return this.description != null;
   }

   public String getDescription() {
      return this.description;
   }

   public ARGUMENT getArgument() {
      return this.argument;
   }

   public List<Requirement> getRequirements() {
      return this.requirements;
   }

   public List<Requirement> getAllRequirements() {
      return this.getNodesOnPath().stream().flatMap(var0 -> var0.getRequirements().stream()).filter(Objects::nonNull).collect(Collectors.toList());
   }
}
