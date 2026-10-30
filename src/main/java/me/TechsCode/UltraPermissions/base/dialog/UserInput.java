package me.TechsCode.UltraPermissions.base.dialog;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public abstract class UserInput extends Dialog {
   private static final Phrase LEFT_CLICK_CLOSE = Phrase.create("userInput.leftClickClose", "**Left Click** to close", Colors.GRAY, Colors.YELLOW);
   private final Player p;
   private final String mainTitle;
   private final String subTitle;
   private String actionbar;
   private long lastTimeMoved = 0L;

   public UserInput(Player var1, SpigotTechPlugin var2, Object var3, Object var4) {
      super(var1, var2);
      this.p = var1;
      this.mainTitle = var3.toString();
      this.subTitle = var4.toString();
   }

   public UserInput(Player var1, SpigotTechPlugin var2, Object var3, Object var4, Object var5) {
      super(var1, var2);
      this.p = var1;
      this.mainTitle = var3.toString();
      this.subTitle = var4.toString();
      this.actionbar = var5.toString();
   }

   public abstract boolean onResult(String var1);

   @Override
   public boolean onInput(String var1) {
      boolean var2 = this.onResult(var1);
      if (var2) {
         this.close(true);
      }

      return true;
   }

   @EventHandler
   public void move(PlayerMoveEvent var1) {
      if (var1.getPlayer().equals(this.p)) {
         this.lastTimeMoved = System.currentTimeMillis();
      }
   }

   @EventHandler
   public void close(PlayerInteractEvent var1) {
      if (var1.getPlayer().equals(this.p) && (var1.getAction() == Action.LEFT_CLICK_AIR || var1.getAction() == Action.LEFT_CLICK_BLOCK)) {
         this.close(false);
      }
   }

   private boolean hasMoved() {
      return this.lastTimeMoved != 0L && System.currentTimeMillis() - this.lastTimeMoved < 3000L;
   }

   @Override
   public String getMainTitle() {
      return this.mainTitle;
   }

   @Override
   public String getSubTitle() {
      return this.hasMoved() ? LEFT_CLICK_CLOSE.get() : this.subTitle;
   }

   @Override
   public String getActionBar() {
      return this.actionbar;
   }
}
