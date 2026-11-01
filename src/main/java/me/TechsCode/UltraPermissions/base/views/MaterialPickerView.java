package me.TechsCode.UltraPermissions.base.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class MaterialPickerView extends PageableGUI<XMaterial> {
   private static final Phrase CHOOSE_ACTION = Phrase.create("materialPickerView.choose.action", "**Click** to choose", Colors.GRAY, Colors.AQUA);
   private static final Phrase AIR = Phrase.create("materialPickerView.air", "Air");
   private String title;
   private XMaterial[] materials;

   public MaterialPickerView(Player var1, SpigotTechPlugin var2, String var3) {
      super(var1, var2);
      this.title = var3;
      ArrayList<XMaterial> var4 = new ArrayList<>();
      Inventory var5 = Bukkit.createInventory(null, 9, "Testing");

      for (XMaterial var9 : XMaterial.values()) {
         Optional var10;
         if (var9 == XMaterial.AIR) {
            var10 = Optional.of(XMaterial.BARRIER.parseItem());
            // The displayed item label is localized in construct() through the AIR phrase.
         } else {
            try {
               var10 = var9.getAsItemStack();
            } catch (IllegalArgumentException var12) {
               continue;
            }
         }

         if (var10.isPresent()) {
            var5.setItem(0, (ItemStack)var10.get());
            if (var5.getItem(0) != null) {
               var5.setItem(0, null);
               var4.add(var9);
            }
         }
      }

      this.materials = var4.stream().sorted(Comparator.comparing(Enum::name)).toArray(XMaterial[]::new);
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   public void construct(Button var1, XMaterial var2) {
      var1.material(var2 == XMaterial.AIR ? XMaterial.BARRIER : var2).lore(CHOOSE_ACTION.get());
      // Let Minecraft translate the native item name into the player\'s client language.
      if (var2 == XMaterial.AIR) {
         var1.item().name(AIR.get());
      }
      var1.action(var2x -> this.choose(this.p, var2));
   }

   public XMaterial[] getObjects() {
      return this.materials;
   }

   public abstract void choose(Player var1, XMaterial var2);

   @Override
   public SearchFeature<XMaterial> getSearch() {
      return new BasicSearch<XMaterial>() {
         public String[] getSearchableText(XMaterial var1) {
            return new String[]{var1.getHumanName()};
         }
      };
   }
}
