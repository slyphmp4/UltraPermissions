package me.TechsCode.UltraPermissions.base.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.command.arguments.SpecificArgument;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar.ActionBar;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class Command extends BukkitCommand implements Listener {
   private static final Phrase USAGE_TITLE = Phrase.create("command.usage.title", "Usage:", Colors.BLUE);
   private final ActionBar actionBar;
   private final String command;
   private final ArrayList<String> alias;
   private final String perm;
   private boolean isCurrentlyRegistered;

   public Command(SpigotTechPlugin var1, String var2, String var3, String... var4) {
      this(var1, var2, "", "/" + var2, var3, var4);
   }

   public Command(SpigotTechPlugin var1, String var2, String var3) {
      this(var1, var2, "", "/" + var2, var3, new String[0]);
   }

   public Command(SpigotTechPlugin var1, String var2, String var3, String var4, String var5, String... var6) {
      super(var2, var3, var4, Arrays.asList(var6));
      this.command = var2;
      this.perm = var5;
      this.actionBar = new ActionBar(var1);
      this.alias = new ArrayList<>();
      this.alias.addAll(Arrays.asList(var6));
      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1)) {
         var1.getBootstrap().getServer().getPluginManager().registerEvents(new CommandEvent(this.perm, this.command, this.alias), (Plugin)var1.getBootstrap());
      }

      if (this.isRegistered()) {
         this.register();
      }

      RecurringTask var7 = var1.getScheduler().runTaskTimer(() -> {
         if (this.isRegistered() && !this.isCurrentlyRegistered) {
            this.register();
         }

         if (!this.isRegistered() && this.isCurrentlyRegistered) {
            this.unregister();
         }
      }, 20L, 20L);
      var1.addDisableHook(() -> {
         var7.stop();
         if (this.isCurrentlyRegistered) {
            this.unregister();
         }
      });
   }

   public abstract void define(CommandSender var1, CommandNode<String, SpecificArgument> var2);

   public boolean execute(CommandSender var1, String var2, String[] var3) {
      Arguments var4 = new Arguments();
      var4.add(this.getName());
      var4.addAll(Arrays.asList(var3));
      Player var5 = var1 instanceof Player ? (Player)var1 : null;
      CommandNode<String, SpecificArgument> var6 = CommandNode.create(this.getName());
      this.define(var1, var6);
      var6.execute(
         var1,
         var5,
         var4,
         var3x -> {
            if (var3x.getResult() == ExecuteResult.NO_ACTION) {
               var1.sendMessage("");
               var1.sendMessage(USAGE_TITLE.get());

               for (CommandNode<?, ?> var5x : var3x.getLastNode().getChildren()) {
                  boolean var6x = var5x.getAllRequirements().stream().allMatch(var3xx -> var3xx.isMatching(var1, var5, var4));
                  if (var6x) {
                     var1.sendMessage(
                        Colors.GRAY
                           + "- "
                           + Colors.YELLOW
                           + "/"
                           + var5x.getUsagePath()
                           + (var5x.hasDescription() ? " " + Colors.GRAY + var5x.getDescription() : "")
                     );
                  }
               }
            }
         }
      );
      return false;
   }

   public List<String> tabComplete(CommandSender var1, String var2, String[] var3) {
      Arguments var4 = new Arguments();
      var4.add(this.getName());
      var4.addAll(Arrays.asList(var3));
      Player var5 = var1 instanceof Player ? (Player)var1 : null;
      CommandNode<String, SpecificArgument> var6 = CommandNode.create(this.getName());
      this.define(var1, var6);
      CommandNode<?, ?> var7 = var6.getCurrentNode(var1, var5, var4.subList(0, var4.size() - 1));
      if (var7 == null) {
         return new ArrayList<>();
      }

      if (var5 != null) {
         List var8 = var7.getEndNodes();
         if (var8.size() == 1) {
            CommandNode var9 = var6.getCurrentNode(var1, var5, var4);
            CommandNode var10 = (CommandNode)var8.get(0);
            this.actionBar.sendActionBar(var5, this.getTabCompletionActionBar(var9, var10, var4));
         }
      }

      return var7.getChildren()
         .stream()
         .flatMap(var0 -> var0.getArgument().getTabCompletions().stream())
         .filter(var1x -> var1x.toLowerCase().startsWith(var3[var3.length - 1].toLowerCase()))
         .collect(Collectors.toList());
   }

   private String getTabCompletionActionBar(CommandNode<?, ?> var1, CommandNode<?, ?> var2, Arguments var3) {
      StringBuilder var4 = new StringBuilder();
      int var5 = 0;

      for (CommandNode var7 : var2.getNodesOnPath()) {
         String var8 = var7.getArgument().getUsage();
         boolean var9 = var5 < var3.size();
         String var10 = var9 ? var3.get(var5) : null;
         if (var5 == var3.size() - 1) {
            var4.append(" ").append(var10.isEmpty() ? Colors.GREEN : Colors.YELLOW).append(var10.isEmpty() ? var8 : var10);
         } else if (var5 < var3.size()) {
            var4.append(" ").append(Colors.GRAY).append(var10);
         } else {
            var4.append(" ").append(Colors.GRAY).append(var8);
         }

         var5++;
      }

      String var11 = var3.get(var3.size() - 1);
      boolean var12 = var2.equals(var1) && !var11.isEmpty() && var1.getArgument().hasMatch(var11);
      return Colors.GRAY + "/" + var4.toString().trim() + (var12 ? Colors.GREEN + "§l ✓" : "");
   }

   private void register() {
      try {
         Field var1 = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         var1.setAccessible(true);
         CommandMap var2 = (CommandMap)var1.get(Bukkit.getServer());
         var2.register("command", this);
         this.isCurrentlyRegistered = true;
      } catch (NoSuchFieldException | IllegalAccessException var3) {
         var3.printStackTrace();
      }
   }

   private void unregister() {
      try {
         Field var1 = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         var1.setAccessible(true);
         CommandMap var2 = (CommandMap)var1.get(Bukkit.getServer());
          ArrayList<Field> var3 = new ArrayList<>(Arrays.asList(var2.getClass().getDeclaredFields()));
         var3.addAll(new ArrayList<>(Arrays.asList(var2.getClass().getSuperclass().getDeclaredFields())));
         Field var4 = var3.stream().filter(var0 -> var0.getName().equals("knownCommands")).findAny().get();
         var4.setAccessible(true);
         Map var5 = (Map)var4.get(var2);
         var5.remove(this.getName());
         var4.set(var2, var5);
         this.isCurrentlyRegistered = false;
      } catch (NoSuchFieldException | IllegalAccessException var6) {
         var6.printStackTrace();
      }
   }

   public abstract boolean isRegistered();
}
