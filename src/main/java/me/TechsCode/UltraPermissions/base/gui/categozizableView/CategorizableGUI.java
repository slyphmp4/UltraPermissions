package me.TechsCode.UltraPermissions.base.gui.categozizableView;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class CategorizableGUI extends GUI {
   private static final Phrase TITLE = Phrase.create("categorizableGUI.title", "Category > %");
   private static final Phrase PAGE_SELECTED = Phrase.create(
      "categorizableGUI.select.indicator", "This category is currently **selected**", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase SELECT_PAGE_ACTION = Phrase.create("categorizableGUI.select.action", "Click to show Categories", Colors.GRAY);
   protected SpigotTechPlugin plugin;
   private int current;
   private final int[][] layouts = new int[][]{{5}, {4, 6}, {4, 5, 6}, {3, 4, 6, 7}, {3, 4, 5, 6, 7}, {2, 3, 4, 6, 7, 8}, {2, 3, 4, 5, 6, 7, 8}};

   public CategorizableGUI(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
      this.current = 0;
   }

   public abstract void onBack();

   @Override
   protected void construct(Model var1) {
      var1.setSlots(54);
   }
}
