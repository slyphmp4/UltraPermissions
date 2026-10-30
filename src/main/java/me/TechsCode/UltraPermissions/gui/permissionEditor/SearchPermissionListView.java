package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfo;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class SearchPermissionListView extends PermissionListView {
   private final String searchTerm;
   private final PermissionWithInfo[] permissions;

   public SearchPermissionListView(Player var1, UltraPermissions var2, PermissionHolder var3, String var4) {
      super(var1, var2, var3);
      this.searchTerm = var4;
      List var5 = Arrays.stream(Bukkit.getPluginManager().getPlugins()).<String>map(Plugin::getName).map(String::toLowerCase).collect(Collectors.toList());
      this.permissions = var2.getPermissionDatabase()
         .getAllPermissionInfos()
         .stream()
         .filter(var1x -> var5.contains(var1x.getPlugin().toLowerCase()) || var1x.getPlugin().toLowerCase().equals("bukkit"))
         .filter(var1x -> var1x.getPermission().toLowerCase().contains(var4.toLowerCase()))
         .sorted(Comparator.comparing(PermissionInfo::getPermission))
         .map(var0 -> new PermissionWithInfo(var0.getPermission(), var0))
         .toArray(PermissionWithInfo[]::new);
   }

   @Override
   public String getTitle() {
      return "Permissions with '" + this.searchTerm + "'";
   }

   public PermissionWithInfo[] getObjects() {
      return this.permissions;
   }

   @Override
   public SearchFeature<PermissionWithInfo> getSearch() {
      return null;
   }
}
