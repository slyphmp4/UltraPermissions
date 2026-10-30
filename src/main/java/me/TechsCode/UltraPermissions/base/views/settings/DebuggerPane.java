package me.TechsCode.UltraPermissions.base.views.settings;

import java.util.Iterator;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.debug.DebugRegistery;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public class DebuggerPane extends SettingsPane {
   private static final Phrase LEFT_CLICK = Phrase.create(
      "debugger.select.indicator", "**Left Click** to %action% this debug message", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase NEXT_TITLE = Phrase.create("debugger.next.title", "Next");
   private static final Phrase NEXT_ACTION = Phrase.create("debugger.next.action", "Click to go to the next page", Colors.GRAY);
   private static final Phrase PREVIOUS_TITLE = Phrase.create("debugger.previous.title", "Previous");
   private static final Phrase PREVIOUS_ACTION = Phrase.create("debugger.previous.action", "Click to go to the previous page", Colors.GRAY);
   private final SpigotTechPlugin plugin;
   private final int maxObjectsPerPage = 36;
   private int page = 0;
   private int pages;

   public DebuggerPane(Player var1, SettingsView var2, SpigotTechPlugin var3) {
      super(var1, var2);
      this.plugin = var3;
   }

   @Override
   public String getName() {
      return "Debugger";
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.REPEATING_COMMAND_BLOCK;
   }

   @Override
   public void construct(Model var1) {
      int[] var2 = this.getBigInnerContainerSlots();
      DebugRegistery var3 = this.plugin.getDebugRegistery();
      int var4 = Math.min(var3.getAvailableDebuggers().size(), this.page * 36);
      int var5 = Math.min(var3.getAvailableDebuggers().size(), var4 + 36);
      Iterator var6 = var3.getAvailableDebuggers().subList(var4, var5).iterator();

      for (int var10 : var2) {
         if (var6.hasNext()) {
            String var11 = (String)var6.next();
            boolean var12 = var3.isDebugging(var11);
            var1.button(var10, var3x -> this.DebugButton(var3x, var11, var12));
         } else {
            var1.button(var10, var0 -> var0.material(XMaterial.WHITE_STAINED_GLASS_PANE).name());
         }
      }

      this.pages = (int)Math.round(Math.ceil(var3.getAvailableDebuggers().size() / 36.0));
      if (this.page != 0) {
         var1.button(var1x -> {
            var1x.material(XMaterial.ARROW).name(Animation.fading(PREVIOUS_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(PREVIOUS_ACTION.get());
            var1x.action(var1xx -> {
               this.page--;
               if (this.page < 0) {
                  this.page = 0;
               }
            });
         }, 46);
      }

      if (this.page + 1 < this.pages) {
         var1.button(var1x -> {
            var1x.material(XMaterial.ARROW).name(Animation.fading(NEXT_TITLE.get(), Colors.DarkCyan, Colors.Cyan)).lore(NEXT_ACTION.get());
            var1x.action(var1xx -> this.page++);
         }, 54);
      }
   }

   private void DebugButton(Button var1, String var2, boolean var3) {
      var1.material(XMaterial.PAPER)
         .name(Animation.wave(var2, Colors.GOLD, Colors.YELLOW))
         .lore(LEFT_CLICK.get().replace("%action%", var3 ? Colors.RED + Words.DISABLE.get() + Colors.GRAY : Colors.GREEN + Words.ENABLE.get() + Colors.GRAY));
      if (var3) {
         var1.item().material(XMaterial.LIME_STAINED_GLASS_PANE.getBukkitMaterial().get());
      } else {
         var1.item().material(XMaterial.RED_STAINED_GLASS_PANE.getBukkitMaterial().get());
      }

      var1.action(var3x -> {
         if (!var3) {
            this.plugin.getDebugRegistery().addDebugger(var2);
         } else {
            this.plugin.getDebugRegistery().removeDebugger(var2);
         }
      });
   }
}
