package me.TechsCode.UltraPermissions.gui.settings.formatPane;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsPane;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.visual.VisualType;
import org.bukkit.entity.Player;

public class FormatPane extends SettingsPane {
   private UltraPermissions plugin;
   private static final Phrase NAME = Phrase.create("formatPane.name", "Chat & Tab List Format");
   private static final Phrase ENTRY_CHANGE_ACTION = Phrase.create("formatPane.entry.changeAction", "**Left Click** to change format", Colors.GRAY, Colors.AQUA);
   private static final Phrase ENTRY_DISABLE_ACTION = Phrase.create(
      "formatPane.entry.disableAction", "**Right Click** to **disable**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase ENTRY_DEFAULT_ACTION = Phrase.create(
      "formatPane.entry.defaultAction", "**Press Q** to reset format to **default**", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase ENTRY_FORMAT_TITLE = Phrase.create("formatPane.entry.format.title", "Format:", Colors.GREEN);
   private static final Phrase ENTRY_FORMAT_DISABLED = Phrase.create("formatPane.entry.format.disabled", "Feature disabled", Colors.RED);

   public FormatPane(Player var1, SettingsView var2, UltraPermissions var3) {
      super(var1, var2);
      this.plugin = var3;
   }

   @Override
   public String getName() {
      return NAME.get();
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.NAME_TAG;
   }

   @Override
   public void construct(Model var1) {
      for (VisualType var5 : VisualType.values()) {
         var1.button(var5.getGuiSlot() + 9, var2 -> this.FormatButton(var2, var5));
      }
   }

   private void FormatButton(Button var1, VisualType var2) {
      var1.material(var2.getMaterial())
         .name(Animation.wave(Tools.getEnumName(var2), Colors.GOLD, Colors.WHITE))
         .lore(ENTRY_CHANGE_ACTION.get(), ENTRY_DISABLE_ACTION.get());
      String var3 = this.plugin.getVisualRegistry().get().getFormat(var2);
      if (!var2.getDefaultFormat().equals("none") && (var3 == null || !var3.equals(var2.getDefaultFormat()))) {
         var1.item().appendLore(ENTRY_DEFAULT_ACTION.get());
      }

      var1.item().appendLore("", ENTRY_FORMAT_TITLE.get(), var3 == null ? ENTRY_FORMAT_DISABLED.get() : Colors.WHITE + var3);
      var1.action(var2x -> {
         if (var2x == ActionType.Q) {
            this.plugin.getVisualRegistry().get().resetFormat(var2);
         } else if (var2x == ActionType.RIGHT) {
            this.plugin.getVisualRegistry().get().setFormat(var2, "none");
         } else {
            new FormatDialog(this.p, this.plugin, var2) {
               @Override
               public void reopen() {
                  FormatPane.this.reopen();
               }
            };
         }
      });
   }
}
