package me.TechsCode.UltraPermissions.gui;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.pageableview.BasicSearch;
import me.TechsCode.UltraPermissions.base.gui.pageableview.PageableGUI;
import me.TechsCode.UltraPermissions.base.gui.pageableview.SearchFeature;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.entity.Player;

public abstract class PlayerSelectorView extends PageableGUI<User> {
   private final Player p;
   private final Group group;
   private final UltraPermissions plugin;
   private static final Phrase GUI_TITLE = Phrase.create("PlayerSelectorView.title", "Users > Select Player");
   private static final Phrase GUI_LORE = Phrase.create("PlayerSelectorView.lore", "Click to **select** player", Colors.GRAY, Colors.GREEN);

   public PlayerSelectorView(Player var1, Group var2, UltraPermissions var3) {
      super(var1, var3);
      this.p = var1;
      this.group = var2;
      this.plugin = var3;
   }

   public abstract void select(User var1);

   @Override
   public String getTitle() {
      return GUI_TITLE.get();
   }

   @Override
   public SearchFeature<User> getSearch() {
      return new BasicSearch<User>() {
         public String[] getSearchableText(User var1) {
            return new String[]{var1.getName()};
         }
      };
   }

   public User[] getObjects() {
      return this.plugin.getUsers().stream().filter(var1 -> !var1.getActiveGroups().contains(this.group)).toArray(User[]::new);
   }

   public void construct(Button var1, User var2) {
      if (var2.getSkinTexture().isPresent()) {
         var1.material(XMaterial.PLAYER_HEAD).setSkullTexture(var2.getSkinTexture().get());
      } else {
         var1.material(XMaterial.GRAY_STAINED_GLASS_PANE);
      }

      var1.item().name(Animation.fading(var2.getName(), Colors.DarkCyan, Colors.Cyan)).appendLore(GUI_LORE.get());
      var1.action(var2x -> {
         var2.addGroup(this.group);
         this.select(var2);
      });
   }
}
