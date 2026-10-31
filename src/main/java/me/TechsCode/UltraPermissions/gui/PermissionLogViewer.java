package me.TechsCode.UltraPermissions.gui;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.dialogs.TogglePluginDialog;
import me.TechsCode.UltraPermissions.permissionDatabase.PermissionInfo;
import me.TechsCode.UltraPermissions.permissionlogger.LoggedPermission;
import org.bukkit.entity.Player;

public abstract class PermissionLogViewer extends PageableGUI<LoggedPermission> {
   private static final Phrase FILTER_TITLE = Phrase.create("permissionLogViewer.filter.title", "Filter");
   private static final Phrase TITLE = Phrase.create("permissionLogViewer.title", "Overview > Permission Log");
   private static final Phrase ITEM_ACTION = Phrase.create(
      "permissionLogViewer.item.action", "Click to add this permission to a **group** or **user**", Colors.GRAY, Colors.AQUA, Colors.AQUA
   );
   private static final Phrase RESULT_RESULT_ATTRIBUTE_PASSED = Phrase.create(
      "permissionLogViewer.item.passedAttribute", "Player **has** this Permission", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase RESULT_RESULT_ATTRIBUTE_FAILED = Phrase.create(
      "permissionLogViewer.item.failedAttribute", "Player does **not** have this Permission", Colors.GRAY, Colors.RED
   );
   private static final Phrase RESULT_TIME_ATTRIBUTE = Phrase.create(
      "permissionLogViewer.item.timeAttribute", "Checked **%duration%** ago", Colors.GRAY, Colors.RED
   );
   private static final Phrase SOURCE_ATTRIBUTE = Phrase.create(
      "permissionLogViewer.item.sourceAttribute", "Allowed by **%source%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase FILTER_TOGGLE_BUTTON = Phrase.create(
      "permissionLogViewer.filter.toggle", "**Left Click** to toggle Plugins", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase FILTER_TOGGLE_LORE = Phrase.create("permissionLogViewer.filter.lore", "Toggled Plugins:", Colors.GRAY);
   private final Player p;
   private final UltraPermissions plugin;

   public PermissionLogViewer(Player var1, UltraPermissions var2) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
   }

   @Override
   public String getTitle() {
      return TITLE.get();
   }

   public void construct(Button var1, LoggedPermission var2) {
      var1.material(var2.getOutcome() ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.RED_STAINED_GLASS_PANE)
         .name(Animation.wave(var2.getPermission(), var2.getOutcome() ? Colors.GREEN : Colors.RED, Colors.WHITE))
         .lore(ITEM_ACTION.get(), "", (var2.getOutcome() ? RESULT_RESULT_ATTRIBUTE_PASSED : RESULT_RESULT_ATTRIBUTE_FAILED).get());
      if (var2.getOutcome()) {
         var1.item().appendLore("", SOURCE_ATTRIBUTE.get().replace("%source%", var2.getSource()));
      }

      PermissionInfo var3 = this.plugin.getPermissionDatabase().getInfo(var2.getPermission());
      if (var3 != null) {
         var1.item().appendLore("");
         var1.item().appendLore(var3.asLore());
      }

      if (var2.getTime() > 1000L) {
         String var4 = Tools.getTimeString(System.currentTimeMillis() - var2.getTime(), TimeUnit.MILLISECONDS);
         var1.item().appendLore("", RESULT_TIME_ATTRIBUTE.get().replace("%duration%", var4));
      }

      var1.action(var2x -> new PermissionLogAddPermission(this.p, this.plugin, var2.getPermission()) {
         @Override
         public void onBack() {
            PermissionLogViewer.this.reopen();
         }
      });
   }

   @Override
   protected void construct(Model var1) {
      super.construct(var1);
      var1.button(this.getRightOptionSlot(), var1x -> {
         var1x.material(XMaterial.REDSTONE_BLOCK).name(Animation.wave(FILTER_TITLE.get(), Colors.AQUA, Colors.WHITE)).appendLore(FILTER_TOGGLE_BUTTON.get());
         if (!this.plugin.getToggledPluginsRegistry().isEmpty()) {
            var1x.item().appendLore("").appendLore(FILTER_TOGGLE_LORE.get());
            String var2 = "";

            for (String var4 : this.plugin.getToggledPluginsRegistry()) {
               if (!var4.equals(var2)) {
                  var2 = var4;
                  var1x.item().appendLore("§7- §c" + var4);
               }
            }
         }

         var1x.action(var1xx -> {
            if (var1xx == ActionType.LEFT) {
               new TogglePluginDialog(this.p, this.plugin) {
                  @Override
                  public void reopen() {
                     PermissionLogViewer.this.reopen();
                  }
               };
            }
         });
      });
   }

   public LoggedPermission[] getObjects() {
      return Arrays.stream(this.plugin.getLoggedPermissionChecks()).filter(var1 -> var1.getPlayer().equals(this.p)).toArray(LoggedPermission[]::new);
   }

   @Override
   public SearchFeature<LoggedPermission> getSearch() {
      return new BasicSearch<LoggedPermission>() {
         public String[] getSearchableText(LoggedPermission var1) {
            return new String[]{var1.getPermission(), var1.getSource()};
         }
      };
   }
}
