package me.TechsCode.UltraPermissions.base.legacy.utils;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import me.TechsCode.UltraPermissions.base.reflection.titleAndActionbar.Reflection;
import org.bukkit.entity.Player;

public class PlayerUtils {
   private static final Class<?> CRAFT_PLAYER = Reflection.getCraftClass("entity.CraftPlayer");

   public PlayerUtils() {
      super();
   }

   private static Object getCraftPlayer(Player var0) {
      return CRAFT_PLAYER.cast(var0);
   }

   public static GameProfile getGameProfile(Player var0) {
      Object var1 = getCraftPlayer(var0);

      try {
         return (GameProfile)CRAFT_PLAYER.getMethod("getProfile").invoke(var1);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var3) {
         var3.printStackTrace();
         return null;
      }
   }
}
