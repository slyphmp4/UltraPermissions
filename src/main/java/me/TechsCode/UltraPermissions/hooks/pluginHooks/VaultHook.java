package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import me.TechsCode.UltraPermissions.UltraPermissions;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public class VaultHook {
   private final UltraPermissions plugin;
   private final VaultPermissionHook permissionHook;
   private final VaultChatHook vaultChatHook;

   public VaultHook(UltraPermissions var1) {
      super();
      this.plugin = var1;
      this.permissionHook = new VaultPermissionHook(var1);
      this.vaultChatHook = new VaultChatHook(this.permissionHook, var1);
      this.hook();
   }

   public void hook() {
      ServicesManager var1 = this.plugin.getBootstrap().getServer().getServicesManager();
      var1.register(Permission.class, this.permissionHook, (Plugin)this.plugin.getBootstrap(), ServicePriority.High);
      var1.register(Chat.class, this.vaultChatHook, (Plugin)this.plugin.getBootstrap(), ServicePriority.High);
   }

   public void unhook() {
      ServicesManager var1 = this.plugin.getBootstrap().getServer().getServicesManager();
      var1.unregister(Permission.class, this.permissionHook);
   }

   public void clearCache() {
      this.permissionHook.clearCache();
      this.vaultChatHook.clearCache();
   }
}
