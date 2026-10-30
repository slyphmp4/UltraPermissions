package me.TechsCode.UltraPermissions.hooks.pluginHooks;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.CachedPlayerPermissionProvider;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.hooks.UpermsPlaceholder;
import me.TechsCode.UltraPermissions.internal.lookup.LookupCheck;
import me.TechsCode.UltraPermissions.internal.lookup.PermissionLookup;
import me.TechsCode.UltraPermissions.internal.lookup.checks.BlankCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefaultPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.DefinedPermissionCheck;
import me.TechsCode.UltraPermissions.internal.lookup.checks.OperatorLookupCheck;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIHook {
   private final UltraPermissions plugin;
   private final CachedPlayerPermissionProvider permissionProvider;

   public PlaceholderAPIHook(final UltraPermissions var1) {
      super();
      this.plugin = var1;
      this.permissionProvider = new CachedPlayerPermissionProvider(var1);
      (new PlaceholderExpansion() {
            public String getIdentifier() {
               return "uperms";
            }

            public String getAuthor() {
               return "TechsCode";
            }

            public String getVersion() {
               return "1.0.0";
            }

            public boolean persist() {
               return true;
            }

            public String onRequest(final OfflinePlayer var1x, String var2) {
               if (var1x == null) {
                  var1.log("Plugin tried getting the placeholder %uperms_" + var2 + "% with no player assigned!");
                  return "";
               }

               Optional var3 = var1.getUsers().uuid(var1x.getUniqueId());
               if (!var3.isPresent()) {
                  return "";
               }

               String[] var4 = var2.split("_");
               if (var4.length == 3) {
                  if (var4[0].equalsIgnoreCase("has") && var4[1].equalsIgnoreCase("group")) {
                     try {
                        Optional var21 = var1.getGroups().name(var4[2]);
                        if (((User)var3.get()).getGroups().contains(((Group)var21.get()).toStored())) {
                           return "true";
                        }

                        return "false";
                     } catch (NullPointerException | IndexOutOfBoundsException | NoSuchElementException var13) {
                     }
                  }

                  if (var4[0].equalsIgnoreCase("has") && var4[1].equalsIgnoreCase("permission")) {
                     PermissionLookup var20 = new PermissionLookup(var4[2]) {
                        @Override
                        public LookupCheck[] getChecks() {
                           return new LookupCheck[]{
                              new OperatorLookupCheck(var1, var1x),
                              var1.isDefaultPermissionsEnabled() ? new DefaultPermissionCheck(var1x) : new BlankCheck(),
                              new DefinedPermissionCheck() {
                                 @Override
                                 public List<Permission> getDefinedPermissions() {
                                    Optional var1xx = PlaceholderAPIHook.this.user(var1x.getName());
                                    return var1xx.isPresent()
                                       ? PlaceholderAPIHook.this.permissionProvider
                                          .retrieve(((User)var1xx.get()).getUuid(), var1x.getPlayer().getLocation().getWorld().getName())
                                       : Collections.emptyList();
                                 }
                              }
                           };
                        }
                     };
                     if (var20.perform().getResult()) {
                        return "true";
                     }

                     return "false";
                  }

                  if (var4[0].equalsIgnoreCase("permission")
                     && var4[1].equalsIgnoreCase("timer")
                     && !((User)var3.get()).getPermissions().name(var4[2]).isEmpty()) {
                     long var19 = ((User)var3.get()).getPermissions().name(var4[2]).stream().findFirst().get().getExpiration();
                     if (var19 != 0L) {
                        long var28 = var19 - System.currentTimeMillis();
                        return Tools.getTimeString(var28, TimeUnit.MILLISECONDS);
                     }

                     return "Permanent";
                  }

                  if (var4[0].equalsIgnoreCase("inherits") && var4[1].equalsIgnoreCase("group")) {
                     try {
                        Optional var5 = var1.getGroups().name(var4[2]);
                        if (((User)var3.get()).getGroups().contains(((Group)var5.get()).toStored())) {
                           return "direct";
                        }

                        for (Stored var7 : ((User)var3.get()).getGroups()) {
                           for (Stored var9 : ((Group)var7.get().get()).getInheritedGroups()) {
                              if (((Group)var9.get().get()).equals(var5.get())) {
                                 return "indirect";
                              }
                           }
                        }
                     } catch (NullPointerException | IndexOutOfBoundsException | NoSuchElementException var12) {
                     }
                  }

                  if (var4[0].equalsIgnoreCase("rank") && var4[1].equalsIgnoreCase("timer")) {
                     try {
                        Optional var18 = var1.getGroups().name(var4[2]);
                        long var26 = ((User)var3.get()).getGroupExpiry(((Group)var18.get()).toStored());
                        if (var26 != 0L) {
                           long var33 = var26 - System.currentTimeMillis();
                           return Tools.getTimeString(var33, TimeUnit.MILLISECONDS, 2);
                        }

                        return "Permanent";
                     } catch (NullPointerException | NoSuchElementException var11) {
                     }
                  }
               }

               if (var4.length == 4) {
                  if (var4[0].equalsIgnoreCase("rank") && var4[1].equalsIgnoreCase("timer") && var4[2].equalsIgnoreCase("detailed")) {
                     try {
                        Optional var17 = var1.getGroups().name(var4[3]);
                        long var25 = ((User)var3.get()).getGroupExpiry(((Group)var17.get()).toStored());
                        if (var25 != 0L) {
                           long var32 = var25 - System.currentTimeMillis();
                           return Tools.getTimeString(var32, TimeUnit.MILLISECONDS);
                        }

                        return "Permanent";
                     } catch (NullPointerException | NoSuchElementException var10) {
                     }
                  }

                  if (var4[1].equalsIgnoreCase("permission") && var4[2].equalsIgnoreCase("timer")) {
                     Optional var14 = var1.getUserStorage().getUsers().name(var4[0]);
                     if (var14.isPresent() && !((User)var14.get()).getPermissions().name(var4[3]).isEmpty()) {
                        long var24 = ((User)var14.get()).getPermissions().name(var4[3]).stream().findFirst().get().getExpiration();
                        if (var24 != 0L) {
                           long var31 = var24 - System.currentTimeMillis();
                           return Tools.getTimeString(var31, TimeUnit.MILLISECONDS, 2);
                        }

                        return "Permanent";
                     }
                  }
               }

               if (var4.length == 5 && var4[1].equalsIgnoreCase("permission") && var4[2].equalsIgnoreCase("timer") && var4[3].equalsIgnoreCase("detailed")) {
                  Optional var15 = var1.getUserStorage().getUsers().name(var4[0]);
                  if (var15.isPresent() && !((User)var15.get()).getPermissions().name(var4[4]).isEmpty()) {
                     long var23 = ((User)var15.get()).getPermissions().name(var4[4]).stream().findFirst().get().getExpiration();
                     if (var23 != 0L) {
                        long var30 = var23 - System.currentTimeMillis();
                        return Tools.getTimeString(var30, TimeUnit.MILLISECONDS);
                     }

                     return "Permanent";
                  }
               }

               for (UpermsPlaceholder var29 : UltraPermissions.placeholders) {
                  if (var29.getName().equalsIgnoreCase(var2)) {
                     return var29.get((User)var3.get(), var1.getThisServer().orElse(null));
                  }
               }

               return "";
            }
         })
         .register();
   }

   public String replace(OfflinePlayer var1, String var2) {
      return PlaceholderAPI.setPlaceholders(var1, var2);
   }

   public Optional<User> user(String var1) {
      return this.plugin.getUsers().name(var1);
   }
}
