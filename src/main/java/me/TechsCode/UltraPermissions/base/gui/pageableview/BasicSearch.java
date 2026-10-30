package me.TechsCode.UltraPermissions.base.gui.pageableview;

import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;

public abstract class BasicSearch<OBJ> extends SearchFeature<OBJ> {
   public static Phrase TITLE = Phrase.create("pageableView.basicSearch.title", "Search");
   public static Phrase ACTION = Phrase.create("pageableView.basicSearch.action", "Click to search", Colors.GRAY);

   public BasicSearch() {
      super(XMaterial.COMPASS, TITLE.get(), ACTION.get());
   }
}
