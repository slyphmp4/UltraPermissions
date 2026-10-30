package me.TechsCode.UltraPermissions.base.messaging;

import java.io.IOException;
import java.util.*;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import me.TechsCode.UltraPermissions.base.security.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class SpigotMessagingService implements PluginMessageListener {
   private SpigotTechPlugin plugin;
   private List<QueuedMessage> queuedMessages;
   private RecurringTask sendTask;
   private List<SpigotMessagingListener> listeners;
   private final SecureMessaging security;

   public SpigotMessagingService(SpigotTechPlugin plugin) {
      this.plugin = plugin;
      this.queuedMessages = new ArrayList<>();
      this.listeners = new ArrayList<>();
      this.security = SecureMessaging.load(plugin.getPluginFolder(), false, plugin::log);
      this.sendTask = plugin.getScheduler().runTaskTimer(this::SendTask, 1L, 1L);
      if (security != null) {
         Bukkit.getMessenger().registerOutgoingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL);
         Bukkit.getMessenger().registerIncomingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL, this);
      }
   }

   public synchronized void onDisable() {
      this.sendTask.stop(); this.queuedMessages.clear();
      Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL, this);
      Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL);
   }

   private synchronized void SendTask() {
      if (security == null || queuedMessages.isEmpty()) return;
      Player carrier = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
      if (carrier == null) return;
      QueuedMessage message = queuedMessages.remove(0);
      try {
         byte[] envelope = security.encode("SERVER_TO_PROXY", security.nodeId, security.proxyId, message.encode());
         carrier.sendPluginMessage(plugin.getBootstrap(), SecureMessaging.CHANNEL, envelope);
         message.onSend();
      } catch (IOException invalid) { plugin.log("Dropped oversized/invalid secure message."); }
   }

   public void register(SpigotMessagingListener listener) { listeners.add(listener); }

   public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
      if (security == null || !SecureMessaging.CHANNEL.equals(channel)) return;
      String payload = security.decode(bytes, "PROXY_TO_SERVER", security.proxyId, security.nodeId);
      if (payload == null) return;
      final Message message;
      try {
         message = Message.decode(payload);
         if (!MessagePolicy.allowed(message, "PROXY_TO_SERVER")) return;
      } catch (RuntimeException invalid) { return; }
      for (SpigotMessagingListener listener : listeners) listener.onMessage(message);
   }

   public synchronized void send(QueuedMessage message) {
      if (security == null || !MessagePolicy.allowed(message, "SERVER_TO_PROXY") || queuedMessages.size() >= 256) return;
      String encoded = message.encode();
      if (encoded.length() > SecureMessaging.MAX_PAYLOAD) return;
      for (QueuedMessage pending : queuedMessages) if (pending.encode().equals(encoded)) return;
      queuedMessages.add(message);
   }
}
