package me.TechsCode.UltraPermissions.base.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.messaging.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeNetworkManager {
   private static List<String> LOCALHOST_IPS = Arrays.asList("localhost", "127.0.0.1", "0.0.0.0", "172.17.0.1", "172.18.0.1");
   private BungeeTechPlugin plugin;
   private String ownIpAddress;

   public BungeeNetworkManager(BungeeTechPlugin var1) {
      super();
      this.plugin = var1;
      this.ownIpAddress = this.retrieveOwnIpAddress();
      var1.getScheduler().runTaskTimer(this::SendServerListInfoTask, 10L, 100L);
   }

   private String retrieveOwnIpAddress() {
      try {
         return new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())).readLine();
      } catch (IOException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   private void SendServerListInfoTask() {
      String var1 = this.plugin.getVersion();
      ServerList var2 = this.fetchServerList();
      NetworkData var3 = new NetworkData(var1, var2);
      this.plugin.getMessagingService().send(new Message("networking", var3.toJsonObject()));
   }

   private ServerList fetchServerList() {
      ProxyServer var1 = this.plugin.getBootstrap().getProxy();
      return var1.getServers()
         .entrySet()
         .stream()
         .map(
            var1x -> {
               String var2 = ((ServerInfo)var1x.getValue()).getName();
               InetSocketAddress var3 = ((ServerInfo)var1x.getValue()).getAddress();
               if (var3 == null) {
                  return null;
               }

               InetAddress var4 = var3.getAddress();
               if (var4 == null) {
                  return null;
               }

               String var5 = var4.getHostAddress();
               if (LOCALHOST_IPS.contains(var5) || var5.startsWith("192.168")) {
                  var5 = this.ownIpAddress;
               }

               int var6 = ((ServerInfo)var1x.getValue()).getAddress().getPort();
               NServer var7 = new NServer(var2, var5, var6, new ArrayList<>());
               ((ServerInfo)var1x.getValue())
                  .getPlayers()
                  .stream()
                  .map(var1xx -> new NPlayer(var1xx.getUniqueId(), var1xx.getName(), var7))
                  .forEach(var1xx -> var7.getPlayers().add(var1xx));
               return var7;
            }
         )
         .filter(Objects::nonNull)
         .collect(Collectors.toCollection(ServerList::new));
   }
}
