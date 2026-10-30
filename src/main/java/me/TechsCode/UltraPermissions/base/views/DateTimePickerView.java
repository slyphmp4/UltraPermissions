package me.TechsCode.UltraPermissions.base.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class DateTimePickerView extends GUI {
   private static final Phrase REMOVE_SECOND_ = Phrase.create("dateTimePickerView.removeSecond", "Remove 1 Second");
   private static final Phrase REMOVE_MINUTE_ = Phrase.create("dateTimePickerView.removeMinute", "Remove 1 Minute");
   private static final Phrase REMOVE_HOUR_ = Phrase.create("dateTimePickerView.removeHour", "Remove 1 Hour");
   private static final Phrase REMOVE_DAY_ = Phrase.create("dateTimePickerView.removeDay", "Remove 1 Day");
   private static final Phrase ADD_SECOND_ = Phrase.create("dateTimePickerView.addSecond", "Add 1 Second");
   private static final Phrase ADD_MINUTE_ = Phrase.create("dateTimePickerView.addMinute", "Add 1 Minute");
   private static final Phrase ADD_HOUR_ = Phrase.create("dateTimePickerView.addHour", "Add 1 Hour");
   private static final Phrase ADD_DAY_ = Phrase.create("dateTimePickerView.addDay", "Add 1 Day");
   private static final Phrase INCREMENT_ACTION = Phrase.create(
      "dateTimePickerView.incrementAction", "**Click** to add **1**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase DECREMENT_ACTION = Phrase.create(
      "dateTimePickerView.decrementAction", "**Click** to remove **1**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase INCREMENT_ACTION_ALT = Phrase.create(
      "dateTimePickerView.incrementByAction", "**Shift + Click** to add **%number%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase DECREMENT_ACTION_ALT = Phrase.create(
      "dateTimePickerView.decrementByAction", "**Shift + Click** to remove **%number%**", Colors.GRAY, Colors.AQUA, Colors.WHITE
   );
   private static final Phrase CONFIRM_TITLE = Phrase.create("dateTimePickerView.confirm.title", "Confirm");
   private static final Phrase CONFIRM_ACTION = Phrase.create("dateTimePickerView.confirm.action", "Click to confirm", Colors.GRAY);
   private final Player p;
   private final SpigotTechPlugin plugin;
   private long time = 0L;
   private final String titlePrefix;
   private final String noTimePhrase;
   private final boolean noTimeContinue;
   private String dateTimeFormat;
   private boolean showDayButtons;
   private static final Phrase TYPE_IN_NAME = Phrase.create("dateTimePickerView.typeIn.name", "Type in date");
   private static final Phrase TYPE_IN_LORE = Phrase.create("dateTimePickerView.typeIn.lore", "Use a custom date", Colors.GRAY);
   private static final Phrase TYPE_IN_USER_INPUT_MAIN_TITLE = Phrase.create("dateTimePickerView.typeIn.userInput.mainTitle", "Custom Date", Colors.AQUA);
   private static final Phrase TYPE_IN_USER_INPUT_SUB_TITLE = Phrase.create("dateTimePickerView.typeIn.userInput.subTitle", "Enter a date", Colors.GRAY);
   private static final Phrase TYPE_IN_USER_INPUT_FORMAT = Phrase.create(
      "dateTimePickerView.typeIn.userInput.format", "Format: **%format%**", Colors.GRAY, Colors.GOLD
   );
   private static final Phrase TYPE_IN_USER_INPUT_ERROR = Phrase.create(
      "dateTimePickerView.typeIn.userInput.format.error", "There was an error parsing the date", Colors.RED
   );

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
      this.showDayButtons = true;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, boolean var6) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
      this.showDayButtons = var6;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, String var6) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.dateTimeFormat = var6;
      this.showDayButtons = true;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, String var6, boolean var7) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.dateTimeFormat = var6;
      this.showDayButtons = var7;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.time = var6;
      this.dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
      this.showDayButtons = true;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6, boolean var8) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.time = var6;
      this.dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
      this.showDayButtons = var8;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6, String var8) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.time = var6;
      this.dateTimeFormat = var8;
      this.showDayButtons = true;
   }

   public DateTimePickerView(Player var1, SpigotTechPlugin var2, String var3, String var4, boolean var5, long var6, String var8, boolean var9) {
      super(var1, var2);
      this.p = var1;
      this.plugin = var2;
      this.titlePrefix = var3;
      this.noTimePhrase = var4;
      this.noTimeContinue = var5;
      this.time = var6;
      this.dateTimeFormat = var8;
      this.showDayButtons = var9;
   }

   @Override
   protected void construct(Model var1) {
      Date var2 = new Date(this.time);
      SimpleDateFormat var3 = new SimpleDateFormat(this.dateTimeFormat);
      String var4 = var3.format(var2);
      var1.setTitle(this.titlePrefix + " " + (this.time == 0L ? this.noTimePhrase : var4));
      var1.setSlots(54);

      for (DateTimePickerView.Step var8 : DateTimePickerView.Step.values()) {
         if (this.time + var8.baseIncrement >= 0L && (!var8.isDayButton || this.showDayButtons)) {
            boolean var9 = var8.baseIncrement > 0L;
            var1.button(var8.slot, var9 ? var2x -> this.IncrementButton(var2x, var8) : var2x -> this.DecrementButton(var2x, var8));
         }
      }

      var1.button(41, this::TypeInButton);
      if (this.time != 0L || this.noTimeContinue) {
         var1.button(23, this::ConfirmButton);
      }
   }

   private void IncrementButton(Button var1, DateTimePickerView.Step var2) {
      var1.material(XMaterial.STONE_BUTTON)
         .name(Animation.wave(var2.phrase.get(), Colors.GREEN, Colors.WHITE))
         .lore(INCREMENT_ACTION.get(), INCREMENT_ACTION_ALT.get().replace("%number%", String.valueOf(var2.secondaryIncrementMultiplier)));
      var1.action(var2x -> {
         if (!var2x.equals(ActionType.SHIFT_LEFT) && !var2x.equals(ActionType.SHIFT_RIGHT)) {
            this.time = this.time + var2.baseIncrement * 1000L;
         } else {
            this.time = this.time + var2.secondaryIncrement * 1000L;
         }
      });
   }

   private void DecrementButton(Button var1, DateTimePickerView.Step var2) {
      var1.material(XMaterial.OAK_BUTTON)
         .name(Animation.wave(var2.phrase.get(), Colors.RED, Colors.WHITE))
         .lore(DECREMENT_ACTION.get(), DECREMENT_ACTION_ALT.get().replace("%number%", String.valueOf(var2.secondaryIncrementMultiplier)));
      var1.action(var2x -> {
         if (!var2x.equals(ActionType.SHIFT_LEFT) && !var2x.equals(ActionType.SHIFT_RIGHT)) {
            this.time = this.time + var2.baseIncrement * 1000L;
         } else if (this.time < Math.abs(var2.secondaryIncrement * 1000L)) {
            this.time = 0L;
         } else {
            this.time = this.time + var2.secondaryIncrement * 1000L;
         }
      });
   }

   private void ConfirmButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(CONFIRM_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(CONFIRM_ACTION.get());
      var1.action(var1x -> this.onResult(this.time));
   }

   private void TypeInButton(Button var1) {
      var1.material(XMaterial.WRITABLE_BOOK).name(Animation.wave(TYPE_IN_NAME.get(), Colors.GREEN, Colors.WHITE)).lore(TYPE_IN_LORE.get());
      var1.action(var1x -> {
         this.p.sendMessage(this.plugin.getPrefix() + TYPE_IN_USER_INPUT_FORMAT.get().replace("%format%", this.dateTimeFormat));
         new UserInput(this.p, this.plugin, "§l" + TYPE_IN_USER_INPUT_MAIN_TITLE.get(), TYPE_IN_USER_INPUT_SUB_TITLE.get()) {
            @Override
            public void onClose(Player var1) {
               DateTimePickerView.this.reopen();
            }

            @Override
            public boolean onResult(String var1) {
               try {
                  DateTimePickerView.this.time = new SimpleDateFormat(DateTimePickerView.this.dateTimeFormat).parse(var1).getTime();
                  DateTimePickerView.this.reopen();
                  return true;
               } catch (ParseException var3) {
                  DateTimePickerView.this.p.sendMessage(DateTimePickerView.this.plugin.getPrefix() + DateTimePickerView.TYPE_IN_USER_INPUT_ERROR.get());
                  return false;
               }
            }
         };
      });
   }

   public abstract void onResult(long var1);

   public enum Step {
      REMOVE_SECOND(DateTimePickerView.REMOVE_SECOND_, -1L, -1L, 10L, 11, false),
      REMOVE_MINUTE(DateTimePickerView.REMOVE_MINUTE_, -60L, -60L, 10L, 20, false),
      REMOVE_HOUR(DateTimePickerView.REMOVE_HOUR_, -3600L, -3600L, 10L, 29, false),
      REMOVE_DAY(DateTimePickerView.REMOVE_DAY_, -86400L, -86400L, 10L, 38, true),
      ADD_SECOND(DateTimePickerView.ADD_SECOND_, 1L, 1L, 10L, 17, false),
      ADD_MINUTE(DateTimePickerView.ADD_MINUTE_, 60L, 60L, 10L, 26, false),
      ADD_HOUR(DateTimePickerView.ADD_HOUR_, 3600L, 3600L, 10L, 35, false),
      ADD_DAY(DateTimePickerView.ADD_DAY_, 86400L, 86400L, 10L, 44, true);

      Phrase phrase;
      long baseIncrement;
      long secondaryIncrement;
      long secondaryIncrementMultiplier;
      int slot;
      boolean isDayButton;

      Step(Phrase var3, long var4, long var6, long var8, int var10, boolean var11) {
         this.phrase = var3;
         this.baseIncrement = var4;
         this.secondaryIncrement = var6 * var8;
         this.secondaryIncrementMultiplier = var8;
         this.slot = var10;
         this.isDayButton = var11;
      }
   }
}
