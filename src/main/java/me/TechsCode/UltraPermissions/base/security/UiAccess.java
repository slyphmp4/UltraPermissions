package me.TechsCode.UltraPermissions.base.security;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public final class UiAccess {
    private UiAccess() {}
    public static boolean allowed(SpigotTechPlugin plugin, Player player) {
        return player != null && player.isOnline() && plugin instanceof UltraPermissions
            && ((UltraPermissions) plugin).getUsers().uuid(player.getUniqueId()).map(User::isSuperadmin).orElse(false);
    }
}
