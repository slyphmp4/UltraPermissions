package me.TechsCode.UltraPermissions.permissionlogger;

import org.bukkit.entity.Player;

public class LoggedPermission {
   private final Player p;
   private final String permission;
   private final boolean outcome;
   private final long time;
   private final String source;

   public LoggedPermission(Player var1, String var2, boolean var3, long var4, String var6) {
      super();
      this.p = var1;
      this.permission = var2;
      this.outcome = var3;
      this.time = var4;
      this.source = var6;
   }

   public Player getPlayer() {
      return this.p;
   }

   public String getPermission() {
      return this.permission;
   }

   public boolean getOutcome() {
      return this.outcome;
   }

   public String getSource() {
      return this.source;
   }

   public long getTime() {
      return this.time;
   }
}
