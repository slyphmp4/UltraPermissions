package me.TechsCode.UltraPermissions.base.views;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class TimePickerView extends GUI {
   private static final Phrase REMOVE_SECOND_ = Phrase.create("timePickerView.removeSecond", "Remove 1 Second");
   private static final Phrase REMOVE_MINUTE_ = Phrase.create("timePickerView.removeMinute", "Remove 1 Minute");
   private static final Phrase REMOVE_HOUR_ = Phrase.create("timePickerView.removeHour", "Remove 1 Hour");
   private static final Phrase REMOVE_DAY_ = Phrase.create("timePickerView.removeDay", "Remove 1 Day");
   private static final Phrase ADD_SECOND_ = Phrase.create("timePickerView.addSecond", "Add 1 Second");
   private static final Phrase ADD_MINUTE_ = Phrase.create("timePickerView.addMinute", "Add 1 Minute");
   private static final Phrase ADD_HOUR_ = Phrase.create("timePickerView.addHour", "Add 1 Hour");
   private static final Phrase ADD_DAY_ = Phrase.create("timePickerView.addDay", "Add 1 Day");
   private static final Phrase INCREMENT_ACTION = Phrase.create(
      "timePickerView.incrementAction", "**Click** to add **1**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase DECREMENT_ACTION = Phrase.create(
      "timePickerView.decrementAction", "**Click** to remove **1**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase INCREMENT_ACTION_ALT = Phrase.create(
      "timePickerView.incrementByAction", "**Shift + Click** to add **%number%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase DECREMENT_ACTION_ALT = Phrase.create(
      "timePickerView.decrementByAction", "**Shift + Click** to remove **%number%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase CONFIRM_TITLE = Phrase.create("timePickerView.confirm.title", "Confirm");
   private static final Phrase CONFIRM_ACTION = Phrase.create("timePickerView.confirm.action", "Click to confirm", Colors.GRAY);
   private static final Phrase MIN_TIME_TITLE = Phrase.create("timePickerView.minTime.title", "Minimum Duration Required");
   private static final Phrase MIN_TIME_LORE = Phrase.create("timePickerView.minTime.lore", "Minimum duration is set to **%time%**", Colors.GRAY, Colors.YELLOW);
   private final Player p;
   private final SpigotTechPlugin plugin;
   private long time = 0L;
   private final String titlePrefix;
   private final String noTimePhrase;
   private final boolean noTimeContinue;
   private long minTime;
   private static final Phrase TYPE_IN_NAME = Phrase.create("timePickerView.typeIn.name", "Type in duration");
   private static final Phrase TYPE_IN_LORE = Phrase.create("timePickerView.typeIn.lore", "Use a custom duration", Colors.GRAY);
   private static final Phrase TYPE_IN_USER_INPUT_MAIN_TITLE = Phrase.create("timePickerView.typeIn.userInput.mainTitle", "Custom Duration", Colors.AQUA);
   private static final Phrase TYPE_IN_USER_INPUT_SUB_TITLE = Phrase.create("timePickerView.typeIn.userInput.subTitle", "Enter a duration", Colors.GRAY);
   private static final Phrase TYPE_IN_USER_INPUT_FORMAT = Phrase.create(
      "timePickerView.typeIn.userInput.format",
      "Format: **5m** (5 Minutes), **5h** (5 Hours), **5d** (5 Days)",
      Colors.GRAY,
      Colors.GOLD,
      Colors.GOLD,
      Colors.GOLD
   );

   public TimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.minTime = var6;
   }

   public TimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6, long var8) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.time = var6;
      this.minTime = var8;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(this.titlePrefix + " " + (this.time == 0L ? this.noTimePhrase : Tools.getTimeString(this.time)));
      var1.setSlots(54);

      for (TimePickerView.Step var5 : TimePickerView.Step.values()) {
         if (this.time + var5.baseIncrement >= 0L) {
            boolean var6 = var5.baseIncrement > 0L;
            var1.button(var5.slot, var6 ? var2 -> this.IncrementButton(var2, var5) : var2 -> this.DecrementButton(var2, var5));
         }
      }

      var1.button(41, this::TypeInButton);
      if ((this.minTime <= 0L || this.time < this.minTime) && this.minTime != 0L) {
         var1.button(23, this::MinTimeButton);
      } else if (this.time != 0L || this.noTimeContinue) {
         var1.button(23, this::ConfirmButton);
      }
   }

   private void IncrementButton(Button var1, TimePickerView.Step var2) {
      var1.material(XMaterial.STONE_BUTTON)
         .name(Animation.wave(var2.phrase.get(), Colors.GREEN, Colors.WHITE))
         .lore(INCREMENT_ACTION.get(), INCREMENT_ACTION_ALT.get().replace("%number%", String.valueOf(var2.secondaryIncrementMultiplier)));
      var1.action(var2x -> {
         if (!var2x.equals(ActionType.SHIFT_LEFT) && !var2x.equals(ActionType.SHIFT_RIGHT)) {
            this.time = this.time + var2.baseIncrement;
         } else {
            this.time = this.time + var2.secondaryIncrement;
         }
      });
   }

   private void DecrementButton(Button var1, TimePickerView.Step var2) {
      var1.material(XMaterial.OAK_BUTTON)
         .name(Animation.wave(var2.phrase.get(), Colors.RED, Colors.WHITE))
         .lore(DECREMENT_ACTION.get(), DECREMENT_ACTION_ALT.get().replace("%number%", String.valueOf(var2.secondaryIncrementMultiplier)));
      var1.action(var2x -> {
         if (!var2x.equals(ActionType.SHIFT_LEFT) && !var2x.equals(ActionType.SHIFT_RIGHT)) {
            this.time = this.time + var2.baseIncrement;
         } else if (this.time < Math.abs(var2.secondaryIncrement)) {
            this.time = 0L;
         } else {
            this.time = this.time + var2.secondaryIncrement;
         }
      });
   }

   private void ConfirmButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(CONFIRM_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(CONFIRM_ACTION.get());
      var1.action(var1x -> this.onResult(this.time));
   }

   private void MinTimeButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK)
         .name(Animation.wave(MIN_TIME_TITLE.get(), Colors.RED, Colors.WHITE))
         .lore(MIN_TIME_LORE.get().replace("%time%", Tools.getTimeString(this.minTime)));
   }

   private void TypeInButton(Button var1) {
      var1.material(XMaterial.WRITABLE_BOOK).name(Animation.wave(TYPE_IN_NAME.get(), Colors.GREEN, Colors.WHITE)).lore(TYPE_IN_LORE.get());
      var1.action(var1x -> {
         this.p.sendMessage(this.plugin.getPrefix() + TYPE_IN_USER_INPUT_FORMAT.get());
         new UserInput(this.p, this.plugin, "§l" + TYPE_IN_USER_INPUT_MAIN_TITLE.get(), TYPE_IN_USER_INPUT_SUB_TITLE.get()) {
            @Override
            public void onClose(Player var1) {
               TimePickerView.this.reopen();
            }

            @Override
            public boolean onResult(String var1) {
               TimePickerView.this.time = Tools.getTimeSecondsFromString(var1);
               TimePickerView.this.reopen();
               return true;
            }
         };
      });
   }

   public abstract void onResult(long var1);

   public enum Step {
      REMOVE_SECOND(TimePickerView.REMOVE_SECOND_, -1L, -1L, 10L, 11),
      REMOVE_MINUTE(TimePickerView.REMOVE_MINUTE_, -60L, -60L, 10L, 20),
      REMOVE_HOUR(TimePickerView.REMOVE_HOUR_, -3600L, -3600L, 10L, 29),
      REMOVE_DAY(TimePickerView.REMOVE_DAY_, -86400L, -86400L, 10L, 38),
      ADD_SECOND(TimePickerView.ADD_SECOND_, 1L, 1L, 10L, 17),
      ADD_MINUTE(TimePickerView.ADD_MINUTE_, 60L, 60L, 10L, 26),
      ADD_HOUR(TimePickerView.ADD_HOUR_, 3600L, 3600L, 10L, 35),
      ADD_DAY(TimePickerView.ADD_DAY_, 86400L, 86400L, 10L, 44);

      Phrase phrase;
      long baseIncrement;
      long secondaryIncrement;
      long secondaryIncrementMultiplier;
      int slot;

      Step(Phrase var3, long var4, long var6, long var8, int var10) {
         this.phrase = var3;
         this.baseIncrement = var4;
         this.secondaryIncrement = var6 * var8;
         this.secondaryIncrementMultiplier = var8;
         this.slot = var10;
      }
   }
}
