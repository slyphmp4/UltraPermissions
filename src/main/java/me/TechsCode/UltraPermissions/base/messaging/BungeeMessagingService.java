package me.TechsCode.UltraPermissions.base.messaging;

import java.io.IOException;
import java.util.*;
import me.TechsCode.UltraPermissions.base.BungeeTechPlugin;
import me.TechsCode.UltraPermissions.base.security.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeMessagingService implements Listener {
   private BungeeTechPlugin plugin;
   private List<BungeeMessagingListener> listeners;
   private final SecureMessaging security;

   public BungeeMessagingService(BungeeTechPlugin plugin) {
      this.plugin = plugin; this.listeners = new ArrayList<>();
      this.security = SecureMessaging.load(plugin.getPluginFolder(), true, plugin::log);
      ProxyServer.getInstance().getPluginManager().registerListener(plugin.getBootstrap(), this);
      ProxyServer.getInstance().registerChannel(SecureMessaging.CHANNEL);
   }

   public void register(BungeeMessagingListener listener) { listeners.add(listener); }

   @EventHandler
   public void message(PluginMessageEvent event) {
      if (!SecureMessaging.CHANNEL.equals(event.getTag())) return;
      // Never forward this private channel, including malformed/client-originated packets.
      event.setCancelled(true);
      if (security == null || !(event.getSender() instanceof Server)) return;
      ServerInfo server = ((Server) event.getSender()).getInfo();
      if (ProxyServer.getInstance().getServerInfo(server.getName()) != server) return;
      String payload = security.decode(event.getData(), "SERVER_TO_PROXY", server.getName(), security.nodeId);
      if (payload == null) return;
      final Message message;
      try {
         message = Message.decode(payload);
         if (!MessagePolicy.allowed(message, "SERVER_TO_PROXY")) return;
      } catch (RuntimeException invalid) { return; }
      for (BungeeMessagingListener listener : listeners) listener.onMessage(server, message);
   }

   public void send(Message message, ServerInfo... targets) {
      if (security == null || !MessagePolicy.allowed(message, "PROXY_TO_SERVER")) return;
      String payload = message.encode();
      if (payload.length() > SecureMessaging.MAX_PAYLOAD) return;
      if (targets.length == 0) targets = ProxyServer.getInstance().getServers().values().toArray(new ServerInfo[0]);
      for (ServerInfo target : targets) {
         try {
            target.sendData(SecureMessaging.CHANNEL, security.encode("PROXY_TO_SERVER", security.nodeId, target.getName(), payload));
         } catch (IOException invalid) { plugin.log("Dropped oversized/invalid secure message."); }
      }
   }
}
