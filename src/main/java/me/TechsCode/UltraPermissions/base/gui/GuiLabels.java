package me.TechsCode.UltraPermissions.base.gui;

import me.TechsCode.UltraPermissions.base.translations.Phrase;

public final class GuiLabels {
   private static final Phrase INVENTORY = Phrase.create("guiLabels.inventory", "Unnamed Inventory");
   private static final Phrase FURNACE = Phrase.create("guiLabels.furnace", "Unnamed Furnace");
   private static final Phrase CRAFTING_TABLE = Phrase.create("guiLabels.craftingTable", "Unnamed Crafting Table");

   private GuiLabels() {
   }

   public static String inventory() {
      return INVENTORY.get();
   }

   public static String furnace() {
      return FURNACE.get();
   }

   public static String craftingTable() {
      return CRAFTING_TABLE.get();
   }
}
