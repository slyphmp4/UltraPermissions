package me.TechsCode.UltraPermissions.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.CachedPlayerPermissionProvider;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.LookupOutcome;
import me.TechsCode.UltraPermissions.internal.lookup.PermissionLookup;
import me.TechsCode.UltraPermissions.internal.lookup.checks.AttachedPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.BlankCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefaultPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefinedPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.OperatorLookupCheck;
import me.TechsCode.UltraPermissions.permissionlogger.PermissionLogger;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class ModifiedPermissible extends PermissibleBase implements Listener {
   private final Player p;
   private final UltraPermissions plugin;
   private final PermissionLogger permissionLogger;
   private final boolean defaultPerms;
   private final List<PermissionAttachment> attachments;
   private final CachedPlayerPermissionProvider permissionProvider;

   public ModifiedPermissible(Player var1, UltraPermissions var2, PermissionLogger var3) {
      super(var1);
      this.p = var1;
      this.plugin = var2;
      this.permissionLogger = var3;
      this.defaultPerms = var2.isDefaultPermissionsEnabled();
      this.attachments = new ArrayList<>();
      this.permissionProvider = new CachedPlayerPermissionProvider(var2);
      Bukkit.getPluginManager().registerEvents(this, (Plugin)var2.getBootstrap());
   }

   public void clearCache() {
      this.permissionProvider.clearCache();
   }

   public boolean isPermissionSet(String var1) {
      return this.hasPermission(var1);
   }

   public boolean isPermissionSet(Permission var1) {
      if (this.isPermissionSet(var1.getName())) {
         return true;
      } else {
         return this.defaultPerms ? var1.getDefault().getValue(this.isOp()) : Permission.DEFAULT_PERMISSION.getValue(this.isOp());
      }
   }

   public boolean hasPermission(Permission var1) {
      return this.hasPermission(var1.getName());
   }

   public boolean hasPermission(String var1) {
      if (var1 == null) {
         return false;
      }

      PermissionLookup var2 = new PermissionLookup(var1) {
         @Override
         public LookupCheck[] getChecks() {
            return new LookupCheck[]{
               new OperatorLookupCheck(ModifiedPermissible.this.plugin, ModifiedPermissible.this.p),
               ModifiedPermissible.this.defaultPerms ? new DefaultPermissionCheck(ModifiedPermissible.this.p) : new BlankCheck(),
               new AttachedPermissionCheck() {
                  @Override
                  public List<PermissionAttachment> getAttachments() {
                     return ModifiedPermissible.this.attachments;
                  }
               },
               new DefinedPermissionCheck() {
                  @Override
                  public List<me.TechsCode.UltraPermissions.storage.objects.Permission> getDefinedPermissions() {
                     return ModifiedPermissible.this.permissionProvider
                        .retrieve(ModifiedPermissible.this.p.getUniqueId(), ModifiedPermissible.this.p.getWorld().getName());
                  }
               }
            };
         }
      };
      LookupOutcome var3 = var2.perform();
      if (!this.plugin.getToggledPluginsRegistry().isEmpty()) {
         for (String var5 : this.plugin.getToggledPluginsRegistry()) {
            if (!var1.contains(var5)) {
               this.permissionLogger.log(this.p, var1, var3.getResult(), var3.getSource());
            }
         }

         return var3.getResult();
      } else {
         this.permissionLogger.log(this.p, var1, var3.getResult(), var3.getSource());
         return var3.getResult();
      }
   }

   public PermissionAttachment addAttachment(Plugin var1) {
      PermissionAttachment var2 = new PermissionAttachment(var1, this);
      this.attachments.add(var2);
      return var2;
   }

   public PermissionAttachment addAttachment(Plugin var1, String var2, boolean var3) {
      PermissionAttachment var4 = this.addAttachment(var1);
      var4.setPermission(var2, var3);
      return var4;
   }

   public PermissionAttachment addAttachment(Plugin var1, int var2) {
      PermissionAttachment var3 = this.addAttachment(var1);
      Bukkit.getScheduler().runTaskLater(var1, var3::remove, var2);
      return var3;
   }

   public PermissionAttachment addAttachment(Plugin var1, String var2, boolean var3, int var4) {
      PermissionAttachment var5 = this.addAttachment(var1, var4);
      var5.setPermission(var2, var3);
      return var5;
   }

   public void removeAttachment(PermissionAttachment var1) {
      this.attachments.remove(var1);
   }

   public void recalculatePermissions() {
   }

   public synchronized void clearPermissions() {
      this.attachments.clear();
   }

   public Set<PermissionAttachmentInfo> getEffectivePermissions() {
      PermissionList var1 = this.permissionProvider.retrieve(this.p.getUniqueId(), this.p.getWorld().getName());
      return var1.stream().map(var1x -> new PermissionAttachmentInfo(this, var1x.getName(), null, var1x.isPositive())).collect(Collectors.toSet());
   }
}
