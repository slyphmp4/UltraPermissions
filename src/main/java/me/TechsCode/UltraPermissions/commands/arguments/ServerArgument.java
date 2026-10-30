package me.TechsCode.UltraPermissions.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.command.Argument;
import me.TechsCode.UltraPermissions.base.command.ArgumentValue;
import me.TechsCode.UltraPermissions.base.command.EmptyReason;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;

public class ServerArgument extends Argument<NServer> {
   private UltraPermissions plugin;
   private static final Phrase SERVER_ARGUMENT = Phrase.create("ServerArgument", "<Server>");

   public ServerArgument(UltraPermissions var1) {
      super();
      this.plugin = var1;
   }

   @Override
   public ArgumentValue<NServer> get(String var1) {
      Optional<NServer> var2;
      if (var1.equalsIgnoreCase("local")) {
         var2 = this.plugin.getNetworkManager().getThisServer();
      } else {
         var2 = this.plugin.getNetworkManager().getServerFromName(var1);
      }

      return var1.equalsIgnoreCase("all")
         ? ArgumentValue.of(var1, null)
         : var2.<ArgumentValue<NServer>>map(var1x -> ArgumentValue.of(var1, var1x)).orElseGet(() -> ArgumentValue.empty(var1, EmptyReason.INVALID));
   }

   @Override
   public String getUsage() {
      return SERVER_ARGUMENT.get();
   }

   @Override
   public List<String> getTabCompletions() {
      ArrayList var1 = new ArrayList<>(Arrays.asList("local", "all"));
      this.plugin.getNetworkManager().getData().ifPresent(var1x -> var1x.getServerList().forEach(var1xx -> var1.add(var1xx.getName())));
      return var1;
   }
}
