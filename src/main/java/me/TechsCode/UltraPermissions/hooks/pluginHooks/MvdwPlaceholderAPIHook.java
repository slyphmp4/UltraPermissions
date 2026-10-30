package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MvdwPlaceholderAPIHook {
   public MvdwPlaceholderAPIHook(UltraPermissions var1) {
      super();
      Optional var2 = var1.getThisServer();

      for (UpermsPlaceholder var6 : UltraPermissions.placeholders) {
         PlaceholderAPI.registerPlaceholder((Plugin)var1.getBootstrap(), "uperms_" + var6.getName(), var3 -> {
            Optional var4 = var1.getUsers().uuid(var3.getOfflinePlayer().getUniqueId());
            return var4.isPresent() ? var6.get((User)var4.get(), (NServer)var2.orElse(null)) : "";
         });
      }
   }

   public String replace(Player var1, String var2) {
      return PlaceholderAPI.replacePlaceholders(var1, var2);
   }
}
