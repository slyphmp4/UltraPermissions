package me.TechsCode.UltraPermissions.gui.settings.formatPane;

import java.util.Arrays;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.visual.VisualType;
import org.bukkit.entity.Player;

public abstract class FormatDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("formatDialog.title", "Set Format", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create("formatDialog.subtitle", "Type in a new format", Colors.GRAY);
   private static final Phrase ACTION_BAR = Phrase.create("formatDialog.actionBar", "Use the placeholders in the chat", Colors.GRAY);
   private static final Phrase PLACEHOLDERS_TITLE = Phrase.create("formatDialog.placeholders.title", "Placeholders:", Colors.GREEN);
   private static final Phrase PLACEHOLDERS_ENTRY = Phrase.create(
      "formatDialog.placeholders.entry", "**>** %placeholder% **-** %description%", Colors.GRAY, Colors.BLUE, Colors.YELLOW
   );
   private static final Phrase PLACEHOLDERS_OTHER = Phrase.create(
      "formatDialog.placeholders.other", "and **{Player}** or **{DisplayName}**", Colors.GRAY, Colors.BLUE, Colors.BLUE
   );
   private static final Phrase DEFAULT = Phrase.create("formatDialog.default", "Default: **%format%**", Colors.GOLD, Colors.WHITE);
   private final UltraPermissions plugin;
   private final VisualType type;

   public FormatDialog(Player var1, UltraPermissions var2, VisualType var3) {
      super(var1, var2, TITLE, SUBTITLE, ACTION_BAR);
      this.plugin = var2;
      this.type = var3;
      var1.sendMessage("");
      var1.sendMessage(PLACEHOLDERS_TITLE.get());
      Arrays.stream(UltraPermissions.placeholders).forEach(var1x -> {
         String var2x = PLACEHOLDERS_ENTRY.get().replace("%placeholder%", var1x.getNativePlaceholder()).replace("%description%", var1x.getDescription());
         var1.sendMessage(var2x);
      });
      var1.sendMessage(PLACEHOLDERS_OTHER.get());
      var1.sendMessage("");
      var1.sendMessage(DEFAULT.get().replace("%format%", var3.getDefaultFormat()));
   }

   @Override
   public boolean onResult(String var1) {
      this.plugin.getVisualRegistry().get().setFormat(this.type, var1);
      this.reopen();
      return true;
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   public abstract void reopen();
}
