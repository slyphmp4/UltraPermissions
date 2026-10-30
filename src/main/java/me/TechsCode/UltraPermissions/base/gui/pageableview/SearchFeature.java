package me.TechsCode.UltraPermissions.base.gui.pageableview;

import me.TechsCode.UltraPermissions.base.item.XMaterial;

public abstract class SearchFeature<OBJ> {
   private final XMaterial icon;
   private final String title;
   private final String action;

   public SearchFeature(XMaterial var1, String var2, String var3) {
      super();
      this.icon = var1;
      this.title = var2;
      this.action = var3;
   }

   public abstract String[] getSearchableText(OBJ var1);

   public XMaterial getIcon() {
      return this.icon;
   }

   public String getTitle() {
      return this.title;
   }

   public String getAction() {
      return this.action;
   }
}
