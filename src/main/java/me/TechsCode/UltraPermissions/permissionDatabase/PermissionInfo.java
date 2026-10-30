package me.TechsCode.UltraPermissions.permissionDatabase;

import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.commons.lang3.StringUtils;

public class PermissionInfo {
   private final String permission;
   private final String plugin;
   private final String description;
   private final String[] commands;
   private final String source;
   private String[] placeholders;
   private static final Phrase COMMANDS_TITLE = Phrase.create("permissionInfo.commands.title", "Commands:", Colors.GRAY);
   private static final Phrase COMMANDS_ENTRY = Phrase.create("permissionInfo.commands.entry", "- **/%command%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase DESCRIPTION_TITLE = Phrase.create("permissionInfo.description.title", "Description:", Colors.GRAY);

   public PermissionInfo(String var1, String var2, String var3, String[] var4, String var5) {
      super();
      this.permission = var1;
      this.plugin = var2;
      this.description = var3;
      this.commands = var4;
      this.source = var5;
      this.placeholders = StringUtils.substringsBetween(var1, "[", "]");
      this.placeholders = this.placeholders == null ? new String[0] : this.placeholders;
   }

   public String getPermission() {
      return this.permission;
   }

   public String getPlugin() {
      return this.plugin;
   }

   public String getDescription() {
      return this.description;
   }

   public String[] getCommands() {
      return this.commands;
   }

   public String getSource() {
      return this.source;
   }

   public boolean hasPlaceholders() {
      return this.placeholders.length != 0;
   }

   public String[] getPlaceholders() {
      return this.placeholders;
   }

   public List<String> asLore() {
      ArrayList var1 = new ArrayList();
      if (this.getCommands().length != 0) {
         var1.add(COMMANDS_TITLE.get());

         for (String var5 : this.getCommands()) {
            var5 = var5.replace("/", "");
            var1.add(COMMANDS_ENTRY.get().replace("%command%", var5));
         }

         var1.add("");
      }

      var1.add(DESCRIPTION_TITLE.get());

      for (String var10 : Tools.lineSplitter(this.getDescription(), 60)) {
         var1.add("   " + Colors.WHITE + var10);
      }

      return var1;
   }

   public boolean isThisPermission(String var1) {
      if (var1.equalsIgnoreCase(this.permission)) {
         return true;
      }

      if (!this.hasPlaceholders()) {
         return false;
      }

      String var2 = this.permission;

      for (String var6 : this.placeholders) {
         var2 = var2.replace("[" + var6 + "]", "$$$");
      }

      for (String var10 : var2.split("[$$$]")) {
         if (!var1.contains(var10)) {
            return false;
         }
      }

      return true;
   }
}
