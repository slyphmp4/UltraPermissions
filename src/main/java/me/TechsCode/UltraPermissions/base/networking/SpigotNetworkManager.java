package me.TechsCode.UltraPermissions.base.networking;

import java.util.Optional;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;

public class SpigotNetworkManager {
   private SpigotTechPlugin plugin;
   private volatile NetworkData networkData;
   private volatile String ownIpAddress;

   public SpigotNetworkManager(SpigotTechPlugin var1) {
      super();
      this.plugin = var1;
      var1.getScheduler().runAsync(() -> this.ownIpAddress = PublicIpResolver.resolve());
      var1.getMessagingService().register(var1x -> {
         if (var1x.getKey().equals("networking")) {
            this.networkData = NetworkData.fromJsonObject(var1x.getData());
         }
      });
   }

   public Optional<NetworkData> getData() {
      return Optional.ofNullable(this.networkData);
   }

   public Optional<NServer> getThisServer() {
      if (this.networkData == null) {
         return Optional.empty();
      }

      int var1 = this.plugin.getBootstrap().getServer().getPort();
      return this.networkData.getServerList().stream().filter(var2 -> var2.getIp().equals(this.ownIpAddress) && var2.getPort() == var1).findFirst();
   }

   public Optional<NServer> getServerFromName(String var1) {
      return this.networkData == null
         ? Optional.empty()
         : this.networkData.getServerList().stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst();
   }

   public Optional<ServerList> getServersContains(String var1) {
      return this.networkData == null
         ? Optional.empty()
         : Optional.of(
            this.networkData.getServerList().stream().filter(var1x -> var1x.getName().contains(var1)).collect(Collectors.toCollection(ServerList::new))
         );
   }

   public Optional<NServer> getServerEqualsPort(int var1) {
      return this.networkData == null ? Optional.empty() : this.networkData.getServerList().stream().filter(var1x -> var1x.getPort() == var1).findFirst();
   }
}
