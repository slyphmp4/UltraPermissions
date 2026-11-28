package me.TechsCode.UltraPermissions.base.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.scheduler.RecurringTask;
import me.TechsCode.UltraPermissions.base.security.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class SpigotMessagingService implements PluginMessageListener {
   private final SpigotTechPlugin plugin;
   private final List<QueuedMessage> queuedMessages;
   private final RecurringTask sendTask;
   private final List<SpigotMessagingListener> listeners;
   private boolean sendScheduled;
   private boolean closed;
   private final SecureMessaging security;

   public SpigotMessagingService(SpigotTechPlugin plugin) {
      this.plugin = plugin;
      this.queuedMessages = new ArrayList<>();
      this.listeners = new CopyOnWriteArrayList<>();
      this.security = SecureMessaging.load(plugin.getPluginFolder(), false, plugin::log);
      this.sendTask = plugin.getScheduler().runTaskTimer(this::SendTask, 1L, 1L);
      if (security != null) {
         Bukkit.getMessenger().registerOutgoingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL);
         Bukkit.getMessenger().registerIncomingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL, this);
      }
   }

   public synchronized void onDisable() {
      this.closed = true;
      this.sendTask.stop();
      this.queuedMessages.clear();
      this.listeners.clear();
      Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL, this);
      Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin.getBootstrap(), SecureMessaging.CHANNEL);
   }

   private void SendTask() {
      Player carrier;
      synchronized (this) {
         if (closed || security == null || sendScheduled || queuedMessages.isEmpty()) return;
         carrier = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
         if (carrier == null) return;
         sendScheduled = true;
      }

      if (!plugin.getSpigotScheduler().isFolia()) {
         sendThrough(carrier);
         return;
      }
      try {
         plugin.getSpigotScheduler().runForLater(carrier, plugin.getBootstrap(),
            () -> sendThrough(carrier), this::finishSend, 1L);
      } catch (RuntimeException schedulingFailure) {
         finishSend();
         throw schedulingFailure;
      }
   }

   private void sendThrough(Player carrier) {
      try {
         if (!carrier.isOnline()) return;
         QueuedMessage message;
         synchronized (this) {
            if (closed || queuedMessages.isEmpty()) return;
            message = queuedMessages.remove(0);
         }
         byte[] envelope = security.encode("SERVER_TO_PROXY", security.nodeId, security.proxyId, message.encode());
         carrier.sendPluginMessage(plugin.getBootstrap(), SecureMessaging.CHANNEL, envelope);
         message.onSend();
      } catch (IOException invalid) {
         plugin.log("Dropped oversized/invalid secure message.");
      } finally {
         finishSend();
      }
   }

   private synchronized void finishSend() {
      sendScheduled = false;
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
      if (closed || security == null || !MessagePolicy.allowed(message, "SERVER_TO_PROXY") || queuedMessages.size() >= 256) return;
      String encoded = message.encode();
      if (encoded.length() > SecureMessaging.MAX_PAYLOAD) return;
      for (QueuedMessage pending : queuedMessages) if (pending.encode().equals(encoded)) return;
      queuedMessages.add(message);
   }
}
