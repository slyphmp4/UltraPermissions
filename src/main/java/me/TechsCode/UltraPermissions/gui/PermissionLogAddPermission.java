package me.TechsCode.UltraPermissions.gui;

import java.util.ArrayList;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.permissionEditor.PermissionEditorMainView;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.entity.Player;

public abstract class PermissionLogAddPermission extends PageableGUI<PermissionHolder> {
   private final Player p;
   private final UltraPermissions plugin;
   private final String permission;
   private static final Phrase TITLE = Phrase.create("permissionLogAddPermission.title", "Permission > %permission%");
   private static final Phrase ENTRY_ACTION = Phrase.create("permissionLogAddPermission.entry.action", "Click to **add** permission", Colors.GRAY, Colors.AQUA);

   public PermissionLogAddPermission(Player var1, UltraPermissions var2, String var3) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.permission = var3;
   }

   @Override
   public String getTitle() {
      return TITLE.get().replace("%permission%", this.permission);
   }

   public void construct(Button var1, PermissionHolder var2) {
      var1.material(XMaterial.WHITE_STAINED_GLASS_PANE).name(Animation.wave(var2.getName(), Colors.Green, Colors.WHITE)).lore(ENTRY_ACTION.get());
      var1.action(var2x -> {
         var2.newPermission(this.permission).create();
         new PermissionEditorMainView(this.p, this.plugin, var2) {
            @Override
            public void onBack() {
               PermissionLogAddPermission.this.onBack();
            }
         };
      });
   }

   public PermissionHolder[] getObjects() {
      ArrayList<PermissionHolder> var1 = new ArrayList<>();
      var1.addAll(this.plugin.getGroups());
      var1.addAll(this.plugin.getUsers());
      return var1.toArray(new PermissionHolder[0]);
   }

   @Override
   public SearchFeature<PermissionHolder> getSearch() {
      return new BasicSearch<PermissionHolder>() {
         public String[] getSearchableText(PermissionHolder var1) {
            return new String[]{var1.getName()};
         }
      };
   }
}
