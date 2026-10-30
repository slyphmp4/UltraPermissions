package me.TechsCode.UltraPermissions.base.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateEvent;
import me.TechsCode.UltraPermissions.base.legacy.task.UpdateTime;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Color;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class RestartView extends GUI {
   private static final Phrase TITLE = Phrase.create("restartView.title", "Restart the Server");
   private static final Phrase RESTARTING = Phrase.create("restartView.restarting", "Restarting...");
   private static final Phrase RESTART_BUTTON_TITLE = Phrase.create("restartView.button.title", "Restart");
   private static final Phrase RESTART_BUTTON_ACTION = Phrase.create("restartView.button.action", "Click to restart the Server", Colors.GRAY);
   private boolean restarting = false;
   private final List<Integer> red = new ArrayList<>();
   private final List<Integer> orange = new ArrayList<>();
   private final List<Integer> yellow = new ArrayList<>();
   private final List<Integer> green = new ArrayList<>();

   public RestartView(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
   }

   @Override
   protected void construct(Model var1) {
      var1.setSlots(27);
      if (!this.restarting) {
         var1.setTitle(TITLE.get());
         var1.button(14, this::RestartButton);
      } else {
         var1.setTitle(RESTARTING.get());

         for (int var3 : this.red) {
            var1.button(var3, var1x -> this.TileButton(var1x, XMaterial.RED_STAINED_GLASS_PANE, Colors.RED));
         }

         for (int var7 : this.orange) {
            var1.button(var7, var1x -> this.TileButton(var1x, XMaterial.ORANGE_STAINED_GLASS_PANE, Colors.GOLD));
         }

         for (int var8 : this.yellow) {
            var1.button(var8, var1x -> this.TileButton(var1x, XMaterial.YELLOW_STAINED_GLASS_PANE, Colors.YELLOW));
         }

         for (int var9 : this.green) {
            var1.button(var9, var1x -> this.TileButton(var1x, XMaterial.LIME_STAINED_GLASS_PANE, Colors.GREEN));
         }
      }
   }

   private void TileButton(Button var1, XMaterial var2, Color var3) {
      var1.material(var2).name(Animation.wave(RESTARTING.get(), var3, Colors.WHITE));
   }

   private void RestartButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.fading(RESTART_BUTTON_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(RESTART_BUTTON_ACTION.get());
      var1.action(var1x -> this.restarting = true);
   }

   @EventHandler
   public void update(UpdateEvent var1) {
      if (var1.getUpdateTime() == UpdateTime.TICK) {
         if (this.restarting) {
            if (this.green.size() == this.getCurrentSlots()) {
               Bukkit.shutdown();
            } else {
               int var2 = 0;

               while (var2 == 0 || this.green.contains(var2)) {
                  var2 = new Random().nextInt(this.getCurrentSlots()) + 1;
               }

               if (this.yellow.contains(var2)) {
                  this.yellow.remove(Integer.valueOf(var2));
                  this.green.add(var2);
               } else if (this.orange.contains(var2)) {
                  this.orange.remove(Integer.valueOf(var2));
                  this.yellow.add(var2);
               } else if (this.red.contains(var2)) {
                  this.red.remove(Integer.valueOf(var2));
                  this.orange.add(var2);
               } else {
                  this.red.add(var2);
               }
            }
         }
      }
   }
}
