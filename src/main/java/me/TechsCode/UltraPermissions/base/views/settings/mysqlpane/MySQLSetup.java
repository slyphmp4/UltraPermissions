package me.TechsCode.UltraPermissions.base.views.settings.mysqlpane;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class MySQLSetup {
   private static final Phrase TITLE = Phrase.create("mysqlSetup.title", "MySQL Setup", Colors.AQUA);
   private static final Phrase SUB_TITLE = Phrase.create("mysqlSetup.subtitle", "Type in **%** into Chat", Colors.GRAY, Colors.YELLOW);
   private static final Phrase ACTION_BAR = Phrase.create("mysqlSetup.actionBar", "Default: **%**", Colors.GRAY, Colors.GREEN);
   private final Player p;
   private final SpigotTechPlugin plugin;
   private int stageNum;

   public MySQLSetup(Player var1, SpigotTechPlugin var2) {
      super();
      this.p = var1;
      this.plugin = var2;
      this.stageNum = -1;
      this.nextStage();
   }

   public void nextStage() {
      this.stageNum++;
      SetupStage[] var1 = this.getStages();
      if (this.stageNum >= var1.length) {
         this.onCompletion();
      } else {
         final SetupStage var2 = var1[this.stageNum];
         new UserInput(
            this.p, this.plugin, TITLE.get(), SUB_TITLE.get().replace("%", var2.getFieldName()), ACTION_BAR.get().replace("%", var2.getDefaultValue())
         ) {
            @Override
            public void onClose(Player var1) {
               MySQLSetup.this.onClose();
            }

            @Override
            public boolean onResult(String var1) {
               var2.getSetFunction().set(var1);
               MySQLSetup.this.nextStage();
               return true;
            }
         };
      }
   }

   public abstract void onClose();

   public abstract void onCompletion();

   public abstract SetupStage[] getStages();
}
