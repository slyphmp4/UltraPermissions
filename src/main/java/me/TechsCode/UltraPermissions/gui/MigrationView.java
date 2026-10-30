package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.migration.MigrationAssistant;
import org.bukkit.entity.Player;

public class MigrationView extends GUI {
   private final UltraPermissions plugin;
   private final MigrationAssistant assistant;
   private static final Phrase TITLE = Phrase.create("migrationView.title", "Import > %plugin%");
   private static final Phrase START_TITLE = Phrase.create("migrationView.start.title", "Import Data");
   private static final Phrase START_ACTION = Phrase.create("migrationView.start.action", "Click to **convert** data", Colors.GRAY, Colors.LIGHT_PURPLE);
   private static final Phrase START_EXPLANATION = Phrase.create("migrationView.start.explanation", "Your existing data will be overwritten.", Colors.GRAY);
   private static final Phrase START_DESC = Phrase.create("migrationView.start.desc", "If you dont want to convert, delete %plugin%", Colors.GRAY);

   public MigrationView(Player var1, UltraPermissions var2, MigrationAssistant var3) {
      super(var1, var2);
      this.plugin = var2;
      this.assistant = var3;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(TITLE.get().replace("%plugin%", this.assistant.getPluginName()));
      var1.setSlots(45);
      var1.button(
         23,
         var1x -> {
            var1x.material(XMaterial.END_PORTAL_FRAME)
               .name(Animation.wave(START_TITLE.get(), Colors.LightPink, Colors.WHITE))
               .lore(START_ACTION.get(), "", START_EXPLANATION.get(), START_DESC.get().replace("%plugin%", this.assistant.getPluginName()));
            var1x.action(var1xx -> this.assistant.doMigration(this.plugin));
         }
      );
   }
}
