package me.TechsCode.UltraPermissions.gui.permissionEditor;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class PluginPermissionListView extends PermissionListView {
   private final PermissionHolder holder;
   private final PermissionEditorMainView.PluginEntry sourcePlugin;
   private static final Phrase TITLE = Phrase.create("pluginPermissionListView.title", "%plugin% > Permissions");
   private static final Phrase ALL_PERMISSIONS_TITLE = Phrase.create("pluginPermissionListView.allPermissions.title", "All Permissions");
   private static final Phrase ALL_PERMISSIONS_ADD_ACTION = Phrase.create(
      "pluginPermissionListView.allPermissions.addAction", "**Left Click** to **add** all Permissions", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase ALL_PERMISSIONS_REMOVE_ACTION = Phrase.create(
      "pluginPermissionListView.allPermissions.removeAction", "**Right Click** to **remove** all Permissions", Colors.GRAY, Colors.AQUA, Colors.RED
   );

   public PluginPermissionListView(Player var1, UltraPermissions var2, PermissionHolder var3, PermissionEditorMainView.PluginEntry var4) {
      super(var1, var2, var3);
      this.holder = var3;
      this.sourcePlugin = var4;
   }

   @Override
   public void reopen() {
      super.reopen();
      this.sourcePlugin.retrieveData();
   }

   public PermissionWithInfo[] getObjects() {
      return this.sourcePlugin
         .permissionsFromDatabase
         .stream()
         .map(var0 -> new PermissionWithInfo(var0.getPermission(), var0))
         .distinct()
         .toArray(PermissionWithInfo[]::new);
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%plugin%", this.sourcePlugin.name);
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(this.getRightOptionSlot(), this::AllPermissionsButton);
   }

   private void AllPermissionsButton(Button var1) {
      var1.material(XMaterial.BUCKET)
         .name(Animation.wave(ALL_PERMISSIONS_TITLE.get(), Colors.Green, Colors.WHITE))
         .lore(ALL_PERMISSIONS_ADD_ACTION.get(), ALL_PERMISSIONS_REMOVE_ACTION.get());
      var1.action(var1x -> {
         if (var1x == ActionType.LEFT) {
            PermissionList var2 = this.holder.getPermissions();
            PermissionList var3 = this.holder.getAdditionalPermissions();

            for (PermissionWithInfo var7 : this.getObjects()) {
               if (var2.name(var7.getName()).isEmpty() && var3.name(var7.getName()).isEmpty()) {
                  this.holder.newPermission(var7.getName()).create();
               }
            }
         }

         if (var1x == ActionType.RIGHT) {
            PermissionList var8 = this.holder.getPermissions();

            for (PermissionWithInfo var12 : this.getObjects()) {
               var8.name(var12.getName()).forEach(Permission::remove);
            }
         }
      });
   }
}
