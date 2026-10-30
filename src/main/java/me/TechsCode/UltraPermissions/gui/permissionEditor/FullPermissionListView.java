package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class FullPermissionListView extends PermissionListView {
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private static final Phrase TITLE = Phrase.create("fullPermissionListView.title", "%holder% > Added Permissions");
   private static final Phrase ADD_PERMISSION_TITLE = Phrase.create("fullPermissionListView.addPermission.title", "Add Permission");
   private static final Phrase ADD_PERMISSION_ADD_ACTION = Phrase.create(
      "fullPermissionListView.addPermission.addAction", "**Left Click** to add a Permission via Chat", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase ADD_PERMISSION_ADD_BUNGEE_ACTION = Phrase.create(
      "fullPermissionListView.addPermission.addBungeeAction", "**Right Click** to add **Bungee Permission**", Colors.GRAY, Colors.AQUA, Colors.RED
   );

   public FullPermissionListView(Player var1, UltraPermissions var2, PermissionHolder var3) {
      super(var1, var2, var3);
      this.plugin = var2;
      this.holder = var3;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%holder%", this.holder.getName());
   }

   public PermissionWithInfo[] getObjects() {
      List<String> var1 = this.holder.getPermissions().stream().map(Permission::getName).distinct().collect(Collectors.toList());
      List<String> var2 = this.holder.getAdditionalPermissions().stream().map(Permission::getName).distinct().collect(Collectors.toList());
      return Stream.<String>concat(var1.stream(), var2.stream())
         .map(var1x -> new PermissionWithInfo(var1x, this.plugin.getPermissionDatabase().getInfo(var1x)))
         .sorted(Comparator.comparing(PermissionWithInfo::getName))
         .toArray(PermissionWithInfo[]::new);
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(this.getRightOptionSlot(), this::AddPermissionButton);
   }

   private void AddPermissionButton(Button var1) {
      var1.material(XMaterial.NAME_TAG).name(Animation.wave(ADD_PERMISSION_TITLE.get(), Colors.Blue, Colors.WHITE)).lore(ADD_PERMISSION_ADD_ACTION.get());
      var1.action(var1x -> {
         if (var1x == ActionType.LEFT) {
            new AddPermissionDialog(this.p, this.plugin, this.holder) {
               @Override
               public void reopen() {
                  FullPermissionListView.this.reopen();
               }
            };
         }
      });
      if (this.plugin.getMySQLManager().isEnabled() && this.plugin.isConnectedToNetwork()) {
         var1.item().appendLore(ADD_PERMISSION_ADD_BUNGEE_ACTION.get());
         var1.action(var1x -> {
            if (var1x == ActionType.RIGHT) {
               new AddBungeePermissionDialog(this.p, this.plugin, this.holder) {
                  @Override
                  public void reopen() {
                     FullPermissionListView.this.reopen();
                  }
               };
            }
         });
      }
   }
}
