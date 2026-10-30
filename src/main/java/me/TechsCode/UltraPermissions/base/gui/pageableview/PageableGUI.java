package me.TechsCode.UltraPermissions.base.gui.pageableview;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public abstract class PageableGUI<OBJ> extends GUI {
   private static final Phrase NEXT_TITLE = Phrase.create("pageableView.next.title", "Next");
   private static final Phrase NEXT_ACTION = Phrase.create("pageableView.next.action", "Click to go to the next page", Colors.GRAY);
   private static final Phrase PREVIOUS_TITLE = Phrase.create("pageableView.previous.title", "Previous");
   private static final Phrase PREVIOUS_ACTION = Phrase.create("pageableView.previous.action", "Click to go to the previous page", Colors.GRAY);
   private static final Phrase SEARCH_TITLE = Phrase.create("pageableView.search.title", "Search");
   private static final Phrase SEARCH_SUBTITLE = Phrase.create("pageableView.search.subtitle", "Type in a search term", Colors.GRAY);
   private static final Phrase SEARCH_CLOSE_ACTION = Phrase.create("pageableView.search.closeAction", "Click to **close** the search", Colors.GRAY, Colors.RED);
   private final int maxObjectsPerPage = 36;
   private final SpigotTechPlugin plugin;
   private int page = 0;
   private int pages;
   private String searchTerm = null;

   public PageableGUI(Player var1, SpigotTechPlugin var2) {
      super(var1, var2);
      this.plugin = var2;
   }

   public abstract String getTitle();

   @Override
   protected void construct(Model var1) {
      var1.setTitle(this.getTitle() + " (" + (this.page + 1) + "/" + this.pages + ")");
      var1.setSlots(54);
      SearchFeature var2 = this.getSearch();
      List var3 = Arrays.stream(this.getObjects()).filter(var2x -> {
         if (var2 == null) {
            return true;
         }

         if (this.searchTerm == null) {
            return true;
         }

         for (String var6x : var2.getSearchableText((OBJ)var2x)) {
            if (var6x != null && var6x.toLowerCase().contains(this.searchTerm.toLowerCase())) {
               return true;
            }
         }

         return false;
      }).collect(Collectors.toList());
      int var4 = Math.min(var3.size(), this.page * 36);
      int var5 = Math.min(var3.size(), var4 + 36);
      List var6 = var3.subList(var4, var5);
      int var7 = 1;

      for (Object var9 : var6) {
         var1.button(var2x -> this.construct(var2x, (OBJ)var9), var7);
         var7++;
      }

      this.pages = (int)Math.ceil(var3.size() / 36.0);
      if (this.page != 0) {
         var1.button(
            var1x -> {
               var1x.material(XMaterial.ARROW)
                  .name(Animation.fading(PREVIOUS_TITLE.get() + " page (" + (this.page - 1) + ")", Colors.DarkCyan, Colors.Cyan))
                  .lore(PREVIOUS_ACTION.get());
               var1x.action(var1xx -> {
                  this.page--;
                  if (this.page < 0) {
                     this.page = 0;
                  }
               });
            },
            46
         );
      }

      if (this.page + 1 < this.pages) {
         var1.button(
            var1x -> {
               var1x.material(XMaterial.ARROW)
                  .name(Animation.fading(NEXT_TITLE.get() + " page (" + (this.page + 1) + ")", Colors.DarkCyan, Colors.Cyan))
                  .lore(NEXT_ACTION.get());
               var1x.action(var1xx -> this.page++);
            },
            54
         );
      }

      if (var2 != null) {
         if (this.searchTerm == null) {
            var1.button(var2x -> {
               var2x.material(var2.getIcon()).name(Animation.fading(var2.getTitle(), Colors.DarkCyan, Colors.Cyan)).lore(var2.getAction());
               var2x.action(var1xx -> new UserInput(this.p, this.plugin, SEARCH_TITLE.get(), SEARCH_SUBTITLE.get(), "") {
                  @Override
                  public void onClose(Player var1) {
                     PageableGUI.this.reopen();
                  }

                  @Override
                  public boolean onResult(String var1) {
                     PageableGUI.this.searchTerm = var1;
                     PageableGUI.this.page = 0;
                     PageableGUI.this.reopen();
                     return true;
                  }
               });
            }, this.getLeftOptionSlot());
         } else {
            var1.button(var2x -> {
               var2x.material(XMaterial.REDSTONE_BLOCK).name(Animation.fading(var2.getTitle(), Colors.RED, Colors.WHITE)).lore(SEARCH_CLOSE_ACTION.get());
               var2x.action(var1xx -> {
                  this.searchTerm = null;
                  this.page = 0;
               });
            }, this.getLeftOptionSlot());
         }
      }

      if (this.hasBackButton()) {
         var1.button(50, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
      }
   }

   protected int getLeftOptionSlot() {
      return this.page != 0 ? 48 : 47;
   }

   protected int getRightOptionSlot() {
      return this.pages <= 1 ? 52 : 53;
   }

   public boolean hasBackButton() {
      return true;
   }

   public abstract void onBack();

   public abstract SearchFeature<OBJ> getSearch();

   public abstract OBJ[] getObjects();

   public abstract void construct(Button var1, OBJ var2);
}
