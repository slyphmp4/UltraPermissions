package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class WorldPickerView extends PageableGUI<World> {
   private static final Phrase ENTRY_ACTION = Phrase.create("worldPickerView.entry.action", "Click to **select** World", Colors.GRAY, Colors.YELLOW);

   public WorldPickerView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
   }

   public abstract void onSelect(World var1);

   @Override
   public SearchFeature<World> getSearch() {
      return new BasicSearch<World>() {
         public String[] getSearchableText(World var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   public World[] getObjects() {
      return Bukkit.getWorlds().toArray(new World[0]);
   }

   public void construct(Button var1, World var2) {
      var1.material(XMaterial.YELLOW_STAINED_GLASS).name(Animation.wave(var2.getName(), Colors.YELLOW, Colors.WHITE)).lore(ENTRY_ACTION.get());
      var1.action(var2x -> this.onSelect(var2));
   }
}
