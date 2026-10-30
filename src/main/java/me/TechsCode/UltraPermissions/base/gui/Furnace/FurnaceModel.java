package me.TechsCode.UltraPermissions.base.gui.Furnace;

import java.util.HashMap;
import me.TechsCode.UltraPermissions.base.gui.Entry;

public class FurnaceModel {
   private String title = "Unnamed Furnace";
   private int slots = 3;
   private final HashMap<Integer, Entry> entries = new HashMap<>();

   public FurnaceModel() {
      super();
   }

   public void setTitle(String var1) {
      this.title = var1;
   }

   public void setSlots(int var1) {
      this.slots = var1;
   }

   public void button(Entry var1, int var2) {
      this.entries.put(var2, var1);
   }

   public void button(int var1, Entry var2) {
      this.entries.put(var1, var2);
   }

   public void button(Entry var1, int var2, int var3) {
      this.entries.put(this.format(var2, var3), var1);
   }

   public void button(int var1, int var2, Entry var3) {
      this.entries.put(this.format(var1, var2), var3);
   }

   public String getTitle() {
      return this.title;
   }

   public int getSlots() {
      return this.slots;
   }

   public HashMap<Integer, Entry> getEntries() {
      return this.entries;
   }

   private int format(int var1, int var2) {
      return (var1 - 1) * 3 + var2;
   }
}
