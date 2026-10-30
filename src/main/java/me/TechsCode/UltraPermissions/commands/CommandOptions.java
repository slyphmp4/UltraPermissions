package me.TechsCode.UltraPermissions.commands;

import java.util.ArrayList;
import java.util.List;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.dependencies.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;

public class CommandOptions {
   private String world = null;
   private String server = null;
   private String operator;
   private long expiration = 0L;
   private final long secs;

   public CommandOptions(UltraPermissions var1, List<String> var2) {
      this(var1, var2.toArray(new String[0]));
   }

   public CommandOptions(UltraPermissions var1, String[] var2) {
      super();
      this.operator = "";
      if (ArrayUtils.contains(var2, "local")) {
         this.server = var1.getThisServer().map(NServer::getName).orElse(null);
      }

      if (ArrayUtils.contains(var2, "bungee")) {
         this.server = "BungeeCord";
      }

      this.secs = Tools.getTimeSecondsFromString(String.join(" ", var2));
      if (this.secs != 0L) {
         this.expiration = System.currentTimeMillis() + this.secs * 1000L;
      }

      Bukkit.getWorlds().stream().filter(var1x -> ArrayUtils.contains(var2, var1x.getName())).forEach(var1x -> this.world = var1x.getName());
   }

   public String getWorld() {
      return this.world;
   }

   public String getServer() {
      return this.server;
   }

   public long getExpiration() {
      return this.expiration;
   }

   public long getSecs() {
      return this.secs;
   }

   public String getCombinedInfo() {
      ArrayList var1 = new ArrayList();
      if (this.expiration != 0L) {
         var1.add(Tools.getTimeString(this.secs));
      }

      if (this.server != null) {
         var1.add(this.server);
      }

      if (this.world != null) {
         var1.add(this.world);
      }

      return var1.isEmpty()
         ? ""
         : Colors.GRAY + " (" + Colors.YELLOW + this.operator + String.join(Colors.DARK_GRAY + " | " + Colors.YELLOW, var1) + Colors.GRAY + ")";
   }

   public void setOperator(String var1) {
      this.operator = var1;
   }

   public String getOperator() {
      return this.operator;
   }
}
