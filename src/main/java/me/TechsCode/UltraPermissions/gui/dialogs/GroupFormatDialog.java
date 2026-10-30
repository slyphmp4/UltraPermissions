package me.TechsCode.UltraPermissions.gui.dialogs;

import java.util.Arrays;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.visual.VisualType;
import org.bukkit.entity.Player;

public abstract class GroupFormatDialog extends UserInput {
   private static final Phrase TITLE = Phrase.create("GroupFormatDialog.title", "Set Format", Colors.AQUA);
   private static final Phrase SUBTITLE = Phrase.create("GroupFormatDialog.subtitle", "Type in a new format", Colors.GRAY);
   private static final Phrase ACTION_BAR = Phrase.create("GroupFormatDialog.actionBar", "Use the placeholders in the chat", Colors.GRAY);
   private static final Phrase PLACEHOLDERS_TITLE = Phrase.create("GroupFormatDialog.placeholders.title", "Placeholders:", Colors.GREEN);
   private static final Phrase PLACEHOLDERS_ENTRY = Phrase.create(
      "GroupFormatDialog.placeholders.entry", "**>** %placeholder% **-** %description%", Colors.GRAY, Colors.BLUE, Colors.YELLOW
   );
   private static final Phrase PLACEHOLDERS_OTHER = Phrase.create(
      "GroupFormatDialog.placeholders.other", "and **{Player}** or **{DisplayName}**", Colors.GRAY, Colors.BLUE, Colors.BLUE
   );
   private static final Phrase DEFAULT = Phrase.create("GroupFormatDialog.default", "Default: **%format%**", Colors.GOLD, Colors.WHITE);
   private final UltraPermissions plugin;
   private final Group group;
   private final VisualType type;

   public GroupFormatDialog(Player var1, UltraPermissions var2, Group var3, VisualType var4) {
      super(var1, var2, TITLE, SUBTITLE, ACTION_BAR);
      this.plugin = var2;
      this.group = var3;
      this.type = var4;
      var1.sendMessage("");
      var1.sendMessage(PLACEHOLDERS_TITLE.get());
      Arrays.stream(UltraPermissions.placeholders).forEach(var1x -> {
         String var2x = PLACEHOLDERS_ENTRY.get().replace("%placeholder%", var1x.getNativePlaceholder()).replace("%description%", var1x.getDescription());
         var1.sendMessage(var2x);
      });
      var1.sendMessage(PLACEHOLDERS_OTHER.get());
      var1.sendMessage("");
      var1.sendMessage(DEFAULT.get().replace("%format%", var4.getDefaultFormat()));
   }

   @Override
   public boolean onResult(String var1) {
      this.group.setFormat(var1);
      this.reopen();
      return true;
   }

   @Override
   public void onClose(Player var1) {
      this.reopen();
   }

   public abstract void reopen();
}
