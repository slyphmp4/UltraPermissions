package me.TechsCode.UltraPermissions;

import java.util.List;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.permissionEditor.PermissionCopy;

public enum PermissionColor {
   ALL_SERVERS(Colors.GREEN, XMaterial.LIME_STAINED_GLASS_PANE, XMaterial.LIME_STAINED_GLASS),
   SPECIFIC_SERVER(Colors.BLUE, XMaterial.BLUE_STAINED_GLASS_PANE, XMaterial.BLUE_STAINED_GLASS),
   BUNGEE_SERVER(Colors.RED, XMaterial.RED_STAINED_GLASS_PANE, XMaterial.RED_STAINED_GLASS),
   INHERITED(Colors.YELLOW, XMaterial.YELLOW_STAINED_GLASS_PANE, XMaterial.YELLOW_STAINED_GLASS);

   private final Color color;
   private final XMaterial glassPane;
   private final XMaterial glassBlock;

   PermissionColor(Color var3, XMaterial var4, XMaterial var5) {
      this.color = var3;
      this.glassPane = var4;
      this.glassBlock = var5;
   }

   public Color getColor() {
      return this.color;
   }

   public XMaterial getGlassPane() {
      return this.glassPane;
   }

   public XMaterial getGlassBlock() {
      return this.glassBlock;
   }

   public static PermissionColor fromPermission(PermissionCopy var0) {
      if (var0.isInherited()) {
         return INHERITED;
      } else if (var0.getPermission().getServer().isPresent()) {
         return var0.getPermission().getServer().get().equals("BungeeCord") ? BUNGEE_SERVER : SPECIFIC_SERVER;
      } else {
         return ALL_SERVERS;
      }
   }

   public static PermissionColor cycle(List<PermissionCopy> var0) {
      int var1 = (int)(System.currentTimeMillis() / 1000L % var0.size());
      return fromPermission((PermissionCopy)var0.get(var1));
   }
}
