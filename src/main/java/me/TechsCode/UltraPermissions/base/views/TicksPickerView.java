package me.TechsCode.UltraPermissions.base.views;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class TicksPickerView extends GUI {
   private static final Phrase REMOVE_OneTick_ = Phrase.create("ticksPickerView.removeOneTick", "Remove 1 Tick");
   private static final Phrase REMOVE_FiveTicks_ = Phrase.create("ticksPickerView.removeFiveTicks", "Remove 5 Ticks");
   private static final Phrase REMOVE_TenTicks_ = Phrase.create("ticksPickerView.removeTenTicks", "Remove 10 Ticks");
   private static final Phrase REMOVE_HundredTicks_ = Phrase.create("ticksPickerView.removeHundredTicks", "Remove 100 Ticks");
   private static final Phrase ADD_OneTick_ = Phrase.create("ticksPickerView.addOneTick", "Add 1 Tick");
   private static final Phrase ADD_FiveTicks_ = Phrase.create("ticksPickerView.addFiveTicks", "Add 5 Ticks");
   private static final Phrase ADD_TenTicks_ = Phrase.create("ticksPickerView.addTenTicks", "Add 10 Ticks");
   private static final Phrase ADD_HundredTicks_ = Phrase.create("ticksPickerView.addHundredTicks", "Add 100 Ticks");
   private static final Phrase INCREMENT_ACTION = Phrase.create("ticksPickerView.incrementAction", "Click to add", Colors.GRAY);
   private static final Phrase DECREMENT_ACTION = Phrase.create("ticksPickerView.decrementAction", "Click to remove", Colors.GRAY);
   private static final Phrase CONFIRM_TITLE = Phrase.create("ticksPickerView.confirm.title", "Confirm");
   private static final Phrase CONFIRM_ACTION = Phrase.create("ticksPickerView.confirm.action", "Click to confirm", Colors.GRAY);
   private long ticks = 0L;
   private final String titlePrefix;
   private final String noTicksPhrase;
   private final boolean noTicksContinue;

   public TicksPickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5) {
      super(var1, var2);
      this.titlePrefix = var3;
      this.noTicksPhrase = var4;
      this.noTicksContinue = var5;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(this.titlePrefix + " " + (this.ticks == 0L ? this.noTicksPhrase : Tools.getTicksString(this.ticks)));
      var1.setSlots(54);

      for (TicksPickerView.Step var5 : TicksPickerView.Step.values()) {
         if (this.ticks + var5.baseIncrement >= 0L) {
            boolean var6 = var5.baseIncrement > 0L;
            var1.button(var5.slot, var6 ? var2 -> this.IncrementButton(var2, var5) : var2 -> this.DecrementButton(var2, var5));
         }
      }

      if (this.ticks != 0L || this.noTicksContinue) {
         var1.button(23, this::ConfirmButton);
      }
   }

   private void IncrementButton(Button var1, TicksPickerView.Step var2) {
      var1.material(XMaterial.STONE_BUTTON).name(Animation.wave(var2.phrase.get(), Colors.GREEN, Colors.WHITE)).lore(INCREMENT_ACTION.get());
      var1.action(var2x -> this.ticks = this.ticks + var2.baseIncrement);
   }

   private void DecrementButton(Button var1, TicksPickerView.Step var2) {
      var1.material(XMaterial.OAK_BUTTON).name(Animation.wave(var2.phrase.get(), Colors.RED, Colors.WHITE)).lore(DECREMENT_ACTION.get());
      var1.action(var2x -> this.ticks = this.ticks + var2.baseIncrement);
   }

   private void ConfirmButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(CONFIRM_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(CONFIRM_ACTION.get());
      var1.action(var1x -> this.onResult(this.ticks));
   }

   public abstract void onResult(long var1);

   public enum Step {
      REMOVE_OneTick(TicksPickerView.REMOVE_OneTick_, -1L, 11),
      REMOVE_FiveTicks(TicksPickerView.REMOVE_FiveTicks_, -5L, 20),
      REMOVE_TenTicks(TicksPickerView.REMOVE_TenTicks_, -10L, 29),
      REMOVE_HundredTicks(TicksPickerView.REMOVE_HundredTicks_, -100L, 38),
      ADD_OneTick(TicksPickerView.ADD_OneTick_, 1L, 17),
      ADD_FiveTicks(TicksPickerView.ADD_FiveTicks_, 5L, 26),
      ADD_TenTicks(TicksPickerView.ADD_TenTicks_, 10L, 35),
      ADD_HundredTicks(TicksPickerView.ADD_HundredTicks_, 100L, 44);

      Phrase phrase;
      long baseIncrement;
      int slot;

      Step(Phrase var3, long var4, int var6) {
         this.phrase = var3;
         this.baseIncrement = var4;
         this.slot = var6;
      }
   }
}
