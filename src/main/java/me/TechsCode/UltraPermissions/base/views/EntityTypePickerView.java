package me.TechsCode.UltraPermissions.base.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.LocalizedEntityNames;
import me.TechsCode.UltraPermissions.base.item.XEntityType;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class EntityTypePickerView extends PageableGUI<XEntityType> {
   private static final Phrase CHOOSE_ACTION = Phrase.create("entityTypePickerView.choose.action", "**Click** to choose", Colors.GRAY, Colors.AQUA);
   private String title;
   private XEntityType[] entityTypes;
   private final SpigotTechPlugin plugin;

   public EntityTypePickerView(Player var1, SpigotTechPlugin var2, String var3) {
      super(var1, var2);
      this.plugin = var2;
      this.title = var3;
      ArrayList<XEntityType> var4 = new ArrayList<>();
      Inventory var5 = Bukkit.createInventory(null, 9, "Testing");

      for (XEntityType var9 : XEntityType.values()) {
         Optional var10 = var9.getIcon().getAsItemStack();
         if (var10.isPresent()) {
            var5.setItem(0, (ItemStack)var10.get());
            if (var5.getItem(0) != null) {
               var5.setItem(0, null);
               var4.add(var9);
            }
         }
      }

      this.entityTypes = var4.stream().sorted(Comparator.comparing(Enum::name)).toArray(XEntityType[]::new);
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   public void construct(Button var1, XEntityType var2) {
      var1.material(var2.getIcon().getAsItemStack().get().getType())
         .name(Animation.wave(LocalizedEntityNames.get(var2, this.plugin.getTranslationManager().getSelectedLanguage()), Colors.GREEN, Colors.WHITE))
         .lore(CHOOSE_ACTION.get());
      var1.action(var2x -> this.choose(this.p, var2));
   }

   public XEntityType[] getObjects() {
      return this.entityTypes;
   }

   public abstract void choose(Player var1, XEntityType var2);

   @Override
   public SearchFeature<XEntityType> getSearch() {
      return new BasicSearch<XEntityType>() {
         public String[] getSearchableText(XEntityType var1) {
            return new String[]{var1.getIcon().getHumanName(), var1.name(), LocalizedEntityNames.get(var1, EntityTypePickerView.this.plugin.getTranslationManager().getSelectedLanguage())};
         }
      };
   }
}
