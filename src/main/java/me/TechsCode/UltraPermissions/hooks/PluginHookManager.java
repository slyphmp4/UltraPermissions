package me.TechsCode.UltraPermissions.hooks;

import java.util.HashMap;
import java.util.HashSet;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.MvdwPlaceholderAPIHook;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.NametagEditHook;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.PlaceholderAPIHook;
import me.TechsCode.UltraPermissions.hooks.pluginHooks.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class PluginHookManager implements Listener {
   private final UltraPermissions plugin;
   private final HashMap<String, Runnable> hooks;
   private PlaceholderAPIHook placeholderAPIHook;
   private MvdwPlaceholderAPIHook mvdwPlaceholderAPIHook;
   private VaultHook vaultHook;
   private NametagEditHook nametagEditHook;

   public PluginHookManager(UltraPermissions var1) {
      super();
      this.plugin = var1;
      this.hooks = new HashMap<>();
      this.hooks.put("NametagEdit", () -> this.nametagEditHook = new NametagEditHook(var1));
      this.hooks.put("Vault", () -> this.vaultHook = new VaultHook(var1));
      this.hooks.put("PlaceholderAPI", () -> this.placeholderAPIHook = new PlaceholderAPIHook(var1));
      this.hooks.put("MVdWPlaceholderAPI", () -> this.mvdwPlaceholderAPIHook = new MvdwPlaceholderAPIHook(var1));
      new HashSet<>(this.hooks.keySet()).forEach(var1x -> this.tryHook(false, var1x));
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var1.getBootstrap());
   }

   public void tryHook(boolean var1, String var2) {
      Runnable hook = this.hooks.get(var2);
      if (hook == null || (!var1 && !Bukkit.getPluginManager().isPluginEnabled(var2))) return;
      try {
         hook.run();
         this.hooks.remove(var2);
         this.plugin.log("Hooked into " + var2);
      } catch (LinkageError | RuntimeException incompatible) {
         // Third-party APIs are optional. A missing/changed method must not disable permissions.
         // Keep the entry available for a possible plugin re-enable with a compatible provider.
         this.plugin.log("Optional hook " + var2 + " unavailable ("
            + incompatible.getClass().getSimpleName() + "); permissions continue without it.");
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onLoad(PluginEnableEvent var1) {
      this.tryHook(true, var1.getPlugin().getName());
   }

   public String replacePlaceholders(Player var1, String var2) {
      if (this.placeholderAPIHook != null) {
         var2 = this.placeholderAPIHook.replace(var1, var2);
      }

      if (this.mvdwPlaceholderAPIHook != null) {
         var2 = this.mvdwPlaceholderAPIHook.replace(var1, var2);
      }

      return var2;
   }

   public void clearCaches() {
      if (this.vaultHook != null) {
         this.vaultHook.clearCache();
      }
   }
}
