package me.TechsCode.UltraPermissions.hooks;

import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class UpermsPlaceholder {
   private final String name;
   private final String description;
   private final Phrase localizedDescription;

   public UpermsPlaceholder(String var1, String var2) {
      super();
      this.name = var1;
      this.description = var2;
      this.localizedDescription = null;
   }

   public UpermsPlaceholder(String name, Phrase description) {
      this.name = name;
      this.description = null;
      this.localizedDescription = description;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.localizedDescription == null ? this.description : this.localizedDescription.get();
   }

   public String getNativePlaceholder() {
      return "{" + Text.firstUpperCase(this.name) + "}";
   }

   public String get(User var1, NServer var2) {
      if (var1 == null) {
         return "";
      }

      Player var3 = Bukkit.getPlayer(var1.getUuid());
      if (var3 != null && !var3.isOnline()) {
         var3 = null;
      }

      Group[] var4 = var1.getActiveGroups()
         .bestToWorst()
         .servers(true, var2 != null ? var2.getName() : null)
         .worlds(true, var3 != null ? var3.getWorld().getName() : "*")
         .toArray(new Group[0]);
      return this.replace(var3, var1, var4);
   }

   protected abstract String replace(Player var1, User var2, Group[] var3);
}
