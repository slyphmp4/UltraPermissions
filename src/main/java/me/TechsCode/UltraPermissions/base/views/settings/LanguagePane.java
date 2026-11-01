package me.TechsCode.UltraPermissions.base.views.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.TranslationManager;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public class LanguagePane extends SettingsPane {
   private static final Phrase NAME = Phrase.create("languagePane.name", "Language");
   private static final Phrase SELECT_ACTION = Phrase.create(
      "languagePane.select.action", "**Click** to **select** this language", Colors.GRAY, Colors.AQUA, Colors.YELLOW
   );
   private static final Phrase SELECTED_INDICATOR = Phrase.create(
      "languagePane.select.indicator", "This language is **currently** selected", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase PHRASES = Phrase.create("languagePane.phrases", "Phrases: **%phrases%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase COVERAGE = Phrase.create("languagePane.coverage", "Coverage: **%coverage%%**", Colors.GRAY, Colors.GREEN);
   private static final Phrase NEXT_TITLE = Phrase.create("languagePane.next.title", "Next");
   private static final Phrase NEXT_ACTION = Phrase.create("languagePane.next.action", "Click to go to the next page", Colors.GRAY);
   private static final Phrase PREVIOUS_TITLE = Phrase.create("languagePane.previous.title", "Previous");
   private static final Phrase PREVIOUS_ACTION = Phrase.create("languagePane.previous.action", "Click to go to the previous page", Colors.GRAY);
   private final SpigotTechPlugin plugin;
   private final int maxObjectsPerPage = 14;
   private int page = 0;
   private int pages;

   public LanguagePane(Player var1, SettingsView var2, SpigotTechPlugin var3) {
      super(var1, var2);
      this.plugin = var3;
   }

   @Override
   public String getName() {
      return NAME.toString();
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.WRITABLE_BOOK;
   }

   @Override
   public void construct(Model var1) {
      int[] var2 = this.getInnerContainerSlots();
      TranslationManager var3 = this.plugin.getTranslationManager();
      Map<String, Integer> var5 = var3.getAvailableLanguages().stream().collect(Collectors.toMap(
         language -> language,
         language -> {
            Map<String, String> phrases = var3.getPhrases(language);
            return phrases == null ? 0 : phrases.size();
         }
      ));
      List<String> var4 = var5.entrySet().stream()
         .filter(entry -> entry.getValue() > 0)
         .map(Map.Entry::getKey)
         .collect(Collectors.toList());
      int var6 = var5.values().stream().mapToInt(Integer::intValue).max().orElse(1);
      int var7 = Math.min(var4.size(), this.page * 14);
      int var8 = Math.min(var4.size(), var7 + 14);
      Iterator var9 = new ArrayList(var4).subList(var7, var8).iterator();

      for (int var13 : var2) {
         if (var9.hasNext()) {
            String var14 = (String)var9.next();
            int var15 = (Integer)var5.get(var14);
            int var16 = Math.round((float)var15 / var6 * 100.0F);
            boolean var17 = var3.getSelectedLanguage().equals(var14);
            var1.button(var13, var5x -> this.LanguageButton(var5x, var14, var15, var16, var17));
         } else {
            var1.button(var13, var0 -> var0.material(XMaterial.WHITE_STAINED_GLASS_PANE).name());
         }
      }

      this.pages = (int)Math.round(Math.ceil(var4.size() / 14.0));
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

   private void LanguageButton(Button var1, String var2, int var3, int var4, boolean var5) {
      var1.material(XMaterial.PAPER)
         .name(Animation.wave(displayLanguageName(var2), Colors.GOLD, Colors.YELLOW))
         .lore(
            var5 ? SELECTED_INDICATOR.get() : SELECT_ACTION.get(),
            "",
            PHRASES.get().replace("%phrases%", var3 + ""),
            COVERAGE.get().replace("%coverage%", var4 + "")
         );
      if (var5) {
         var1.item().showEnchantments(false).addEnchantment(XEnchantment.LUCK_OF_THE_SEA.getEnchant(), 1);
      }

      var1.action(var3x -> {
         if (!var5) {
            this.plugin.getTranslationManager().setSelectedLanguage(var2);
         }
      });
   }
   private static String displayLanguageName(String language) {
      switch (language.toLowerCase(java.util.Locale.ROOT)) {
         case "russian": return "Русский";
         case "ukrainian": return "Українська";
         case "english": return "English";
         case "german": return "Deutsch";
         case "french": return "Français";
         case "spanish": return "Español";
         case "polish": return "Polski";
         case "czech": return "Čeština";
         case "dutch": return "Nederlands";
         case "brazil": return "Português (Brasil)";
         case "brazilian": return "Português (Brasil)";
         case "portuguese": return "Português";
         case "italian": return "Italiano";
         case "hungarian": return "Magyar";
         case "danish": return "Dansk";
         case "turkish": return "Türkçe";
         case "romanian": return "Română";
         default: return language;
      }
   }

}
