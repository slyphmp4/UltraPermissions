package me.TechsCode.UltraPermissions.base.views;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class ConfirmationView extends GUI {
   private static final Phrase CONFIRM_TITLE = Phrase.create("confirmationView.confirm.title", "Confirm");
   private static final Phrase CONFIRM_ACTION = Phrase.create("confirmationView.confirm.action", "Click to confirm", Colors.GRAY);
   private static final Phrase DENY_TITLE = Phrase.create("confirmationView.deny.title", "Abort");
   private static final Phrase DENY_ACTION = Phrase.create("confirmationView.deny.action", "Click to abort", Colors.GRAY);
   private String title;

   public ConfirmationView(Player var1, SpigotTechPlugin var2, String var3) {
      super(var1, var2);
      this.title = var3;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(this.title);
      var1.setSlots(27);
      var1.button(12, this::ConfirmButton);
      var1.button(16, this::DenyButton);
   }

   private void ConfirmButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(CONFIRM_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(CONFIRM_ACTION.get());
      var1.action(var1x -> this.choose(true));
   }

   private void DenyButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK).name(Animation.wave(DENY_TITLE.get(), Colors.RED, Colors.WHITE)).lore(DENY_ACTION.get());
      var1.action(var1x -> this.choose(false));
   }

   public abstract void choose(boolean var1);
}
