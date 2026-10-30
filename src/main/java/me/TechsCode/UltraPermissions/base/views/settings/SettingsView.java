package me.TechsCode.UltraPermissions.base.views.settings;

import java.util.Arrays;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class SettingsView extends GUI {
   private static final Phrase TITLE = Phrase.create("settingsView.title", "Settings > %");
   private static final Phrase PAGE_SELECTED = Phrase.create("settingsView.select.indicator", "This page is currently **selected**", Colors.GRAY, Colors.GREEN);
   private static final Phrase SELECT_PAGE_ACTION = Phrase.create("settingsView.select.action", "Click to show Settings", Colors.GRAY);
   private final int[][] layouts = new int[][]{
      {5}, {4, 6}, {4, 5, 6}, {3, 4, 6, 7}, {3, 4, 5, 6, 7}, {2, 3, 4, 6, 7, 8}, {2, 3, 4, 5, 6, 7, 8}, {1, 2, 3, 4, 6, 7, 8, 9}, {1, 2, 3, 4, 5, 6, 7, 8, 9}
   };
   protected SpigotTechPlugin plugin;
   private int current;
   private SettingsPane[] panes;

   public SettingsView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
      this.current = 0;
      this.panes = this.getPanes();
   }

   public abstract SettingsPane[] getPanes();

   public abstract void onBack();

   private SettingsPane getCurrent() {
      return this.panes[this.current];
   }

   @Override
   public int getCurrentSlots() {
      return 54;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(TITLE.get().replace("%", this.getCurrent().getName()));
      var1.setSlots(54);
      SettingsPane var2 = this.panes[this.current];
      int[] var3 = Arrays.stream(this.layouts).filter(var1x -> var1x.length == this.panes.length).findFirst().get();
      int var4 = 0;

      for (SettingsPane var8 : this.panes) {
         int var9 = var3[var4];
         boolean var10 = var4 == this.current;
         int var11 = var4;
         var1.button(var9, var4x -> {
            var4x.material(var8.getIcon()).name(Animation.wave(var8.getName(), Colors.Gold, Colors.YELLOW));
            if (var10) {
               var4x.item().lore(PAGE_SELECTED.get());
               var4x.item().addEnchantment(XEnchantment.LUCK_OF_THE_SEA.getEnchant(), 1).showEnchantments(false);
            } else {
               var4x.item().lore(SELECT_PAGE_ACTION.get());
            }

            var4x.action(var2xx -> this.current = var11);
         });
         var4++;
      }

      var2.construct(var1);
      var1.button(50, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
   }
}
