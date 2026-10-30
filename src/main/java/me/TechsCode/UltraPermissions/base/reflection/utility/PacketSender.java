package me.TechsCode.UltraPermissions.base.reflection.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketSender {
   private static final Class<?> CraftPlayerClass;
   private static final Class<?> EntityPlayerClass;
   Method sendPacket;

   public PacketSender() throws ClassNotFoundException, NoSuchMethodException {
      super();
      Class var1 = ReflectionUtil.getClass(false, "network.protocol.Packet", "Packet");
      Class var2 = ReflectionUtil.getClass(false, "server.network.ServerGamePacketListenerImpl", "server.network.PlayerConnection", "PlayerConnection");
      if (ReflectionUtil.MINOR_VERSION >= 7) {
         this.sendPacket = ReflectionUtil.getMethods(var2, void.class, var1).get(0);
      } else {
         this.sendPacket = ReflectionUtil.getMethod(var2, new String[]{"sendPacket"}, var1);
      }
   }

   public void sendPacket(@NotNull Player var1, @NotNull Object var2) {
      try {
         this.sendPacket.invoke(this.getPlayerConnection(var1), var2);
      } catch (IllegalAccessException | InvocationTargetException var4) {
         throw new RuntimeException(var4);
      }
   }

   private Object getPlayerConnection(Player var1) {
      try {
         Object var2 = CraftPlayerClass.getMethod("getHandle").invoke(CraftPlayerClass.cast(var1));
         return ReflectionUtil.getField(EntityPlayerClass, "connection", "playerConnection").get(var2);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var3) {
         throw new RuntimeException(var3);
      }
   }

   static {
      try {
         EntityPlayerClass = ReflectionUtil.getClass(false, "server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
         CraftPlayerClass = ReflectionUtil.getClass(true, "entity.CraftPlayer");
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }
}
