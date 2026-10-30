package me.TechsCode.UltraPermissions.gui.permissionEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.TechsCode.UltraPermissions.PermissionColor;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XEnchantment;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.gui.ServerPickerView;
import me.TechsCode.UltraPermissions.gui.WorldPickerView;
import me.TechsCode.UltraPermissions.storage.objects.Permission;
import me.TechsCode.UltraPermissions.storage.objects.PermissionHolder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class PermissionView extends GUI {
   private final UltraPermissions plugin;
   private final PermissionHolder holder;
   private final PermissionWithInfo permissionWithInfo;
   private List<PermissionCopy> permissionCopies;
   private int currentCopy;
   private static final Phrase COPY_ENTRY_SELECT_ACTION = Phrase.create(
      "permissionView.entry.selectAction", "Click to **select** this copy", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase COPY_ENTRY_SELECTED_INDICATOR = Phrase.create(
      "permissionView.entry.selectedIndicator", "This copy is currently **selected**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase COPY_ENTRY_SERVER_ATTRIBUTE = Phrase.create(
      "permissionView.entry.serverAttribute", "Server: **%server%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase COPY_ENTRY_WORLD_ATTRIBUTE = Phrase.create(
      "permissionView.entry.worldAttribute", "World: **%world%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase COPY_ENTRY_PERMANENT_DESC = Phrase.create(
      "permissionView.entry.permanentDesc", "This permission is **permanent** and cannot expire", Colors.GRAY, Colors.RED
   );
   private static final Phrase COPY_ENTRY_TEMPORARY_DESC = Phrase.create(
      "permissionView.entry.temporaryDesc", "Expiring in **%time%**", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase NEW_COPY_TITLE = Phrase.create("permissionView.newCopy.title", "Create new Copy");
   private static final Phrase NEW_COPY_ACTION = Phrase.create("permissionView.newCopy.action", "Click to create a new copy of this permission", Colors.GRAY);
   private static final Phrase SERVER_TITLE = Phrase.create("permissionView.server.title", "Server");
   private static final Phrase SERVER_ALL_SERVERS_ACTION = Phrase.create(
      "permissionView.server.allServersAction", "Click to **allow** permission on all Servers", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase SERVER_SPECIFIC_SERVER_ACTION = Phrase.create(
      "permissionView.server.specificServerAction", "Click to **restrict** permission to a certain Server", Colors.GRAY, Colors.RED
   );
   private static final Phrase SERVER_ALL_SERVERS_DESC = Phrase.create(
      "permissionView.server.allServersDesc", "Permission is available to all Servers", Colors.GRAY
   );
   private static final Phrase SERVER_SPECIFIC_SERVERS_DESC = Phrase.create(
      "permissionView.server.specificServersDesc", "Permission only available on Server **%server%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase WORLD_TITLE = Phrase.create("permissionView.world.title", "World");
   private static final Phrase WORLD_ALL_WORLDS_ACTION = Phrase.create(
      "permissionView.world.allWorldsAction", "Click to **allow** permission on all Worlds", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase WORLD_SPECIFIC_WORLD_ACTION = Phrase.create(
      "permissionView.world.specificWorldAction", "Click to **restrict** permission to a certain World", Colors.GRAY, Colors.RED
   );
   private static final Phrase WORLD_ALL_WORLDS_DESC = Phrase.create("permissionView.world.allWorldsDesc", "Permission is available to all Worlds", Colors.GRAY);
   private static final Phrase WORLD_SPECIFIC_WORLD_DESC = Phrase.create(
      "permissionView.world.specificWorldDesc", "Permission only available on World **%world%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase POLARITY_TITLE = Phrase.create("permissionView.polarity.title", "Polarity");
   private static final Phrase POLARITY_NEGATIVE_ACTION = Phrase.create(
      "permissionView.polarity.negativeAction", "Click to make this Permission **negative**", Colors.GRAY, Colors.RED
   );
   private static final Phrase POLARITY_POSITIVE_ACTION = Phrase.create(
      "permissionView.polarity.positiveAction", "Click to make this a **regular** Permission", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase POLARITY_NEGATIVE_ATTRIBUTE = Phrase.create(
      "permissionView.polarity.negativeAttribute", "This Permission is currently **negated**", Colors.GRAY, Colors.RED
   );
   private static final Phrase POLARITY_POSITIVE_ATTRIBUTE = Phrase.create(
      "permissionView.polarity.positiveAttribute", "This is currently a regular, **positive** permission", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase DELETE_TITLE = Phrase.create("permissionView.delete.title", "Delete");
   private static final Phrase DELETE_ACTION = Phrase.create("permissionView.delete.action", "Click to delete this copy", Colors.GRAY);
   private static final Phrase INHERITED_TITLE = Phrase.create("permissionView.inherited.title", "Permission Inherited");
   private static final Phrase INHERITED_ACTION = Phrase.create("permissionView.inherited.action", "Click to jump to Group", Colors.GRAY);
   private static final Phrase INHERITED_DESC = Phrase.create(
      "permissionView.inherited.desc",
      "This permission is inherited from **%group%** and can be only edited when jumping to that group",
      Colors.GRAY,
      Colors.YELLOW
   );

   public PermissionView(Player var1, UltraPermissions var2, PermissionHolder var3, PermissionWithInfo var4) {
      super(var1, var2);
      this.plugin = var2;
      this.holder = var3;
      this.permissionWithInfo = var4;
      this.reloadPermissionCopies();
   }

   private void reloadPermissionCopies() {
      this.permissionCopies = new ArrayList<>();
      this.holder
         .getPermissions()
         .stream()
         .filter(var1 -> this.permissionWithInfo.isThisPermission(var1.getName()))
         .forEach(var1 -> this.permissionCopies.add(new PermissionCopy(var1, false)));
      this.holder
         .getAdditionalPermissions()
         .stream()
         .filter(var1 -> this.permissionWithInfo.isThisPermission(var1.getName()))
         .forEach(var1 -> this.permissionCopies.add(new PermissionCopy(var1, true)));
      this.currentCopy = Math.min(this.permissionCopies.size(), this.currentCopy + 1) - 1;
   }

   @Override
   protected void construct(Model var1) {
      var1.setTitle(this.holder.getName() + " > " + this.permissionWithInfo.getName());
      var1.setSlots(45);

      for (int var2 = 0; var2 <= 8; var2++) {
         int var3 = var2;
         var1.button(
            var2 + 1,
            this.permissionCopies.size() > var2 ? var2x -> this.CopyEntryButton(var2x, this.permissionCopies.get(var3), var3) : this::PlaceholderTabButton
         );
      }

      if (this.permissionCopies.size() < 8) {
         var1.button(9, this::CreateNewCopyButton);
      }

      if (!this.permissionCopies.isEmpty()) {
         PermissionCopy var4 = this.permissionCopies.get(this.currentCopy);
         if (var4.isInherited()) {
            var1.button(23, var2x -> this.InheritedButton(var2x, var4.getPermission()));
         } else {
            var1.button(20, var2x -> this.ServerButton(var2x, var4.getPermission()));
            var1.button(22, var2x -> this.WorldButton(var2x, var4.getPermission()));
            var1.button(24, var2x -> this.PolarityButton(var2x, var4.getPermission()));
            var1.button(26, var2x -> this.DeleteButton(var2x, var4.getPermission()));
         }
      }

      var1.button(41, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
   }

   private void CopyEntryButton(Button var1, PermissionCopy var2, int var3) {
      Permission var4 = var2.getPermission();
      PermissionColor var5 = PermissionColor.fromPermission(var2);
      boolean var6 = var3 == this.currentCopy;
      var1.material(var5.getGlassPane())
         .name((var4.isPositive() ? "" : Colors.RED + "-") + Animation.wave(var4.getName(), var5.getColor(), Colors.WHITE))
         .lore(
            (var6 ? COPY_ENTRY_SELECTED_INDICATOR : COPY_ENTRY_SELECT_ACTION).get(),
            "",
            COPY_ENTRY_SERVER_ATTRIBUTE.get().replace("%server%", this.getServer(var4)),
            COPY_ENTRY_WORLD_ATTRIBUTE.get().replace("%world%", this.getWorld(var4)),
            ""
         );
      if (var2.getPermission().isPermanent()) {
         var1.item().appendLore(COPY_ENTRY_PERMANENT_DESC.get());
      } else {
         String var7 = Tools.getTimeString(var4.getExpiration() - System.currentTimeMillis(), TimeUnit.MILLISECONDS, 2);
         var1.item().appendLore(COPY_ENTRY_TEMPORARY_DESC.get().replace("%time%", var7));
      }

      if (var6) {
         var1.item().addEnchantment(XEnchantment.LUCK_OF_THE_SEA.getEnchant(), 3);
      }

      var1.action(var2x -> this.currentCopy = var3);
   }

   private void CreateNewCopyButton(Button var1) {
      var1.material(XMaterial.ANVIL).name(Animation.wave(NEW_COPY_TITLE.get(), Colors.GOLD, Colors.WHITE)).lore(NEW_COPY_ACTION.get());
      var1.action(var1x -> {
         this.holder.newPermission(this.permissionWithInfo.getName()).create();
         this.reloadPermissionCopies();
      });
   }

   private void PlaceholderTabButton(Button var1) {
      var1.material(XMaterial.GRAY_STAINED_GLASS_PANE).name();
   }

   private void ServerButton(Button var1, Permission var2) {
      var1.material(XMaterial.ENDER_CHEST)
         .name(Animation.wave(SERVER_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (var2.getServer().isPresent() ? SERVER_ALL_SERVERS_ACTION : SERVER_SPECIFIC_SERVER_ACTION).get(),
            "",
            var2.getServer().isPresent() ? SERVER_SPECIFIC_SERVERS_DESC.get().replace("%server%", var2.getServer().get()) : SERVER_ALL_SERVERS_DESC.get()
         );
      var1.action(var2x -> {
         if (var2.getServer().isPresent()) {
            var2.setServer(null);
         } else {
            new ServerPickerView(this.p, this.plugin) {
               @Override
               public void onSelect(NServer var1) {
                  var2.setServer(var1.getName());
                  this.onBack();
               }

               @Override
               public String getTitle() {
                  return var2.getName() + " > Server";
               }

               @Override
               public void onBack() {
                  PermissionView.this.reopen();
               }
            };
         }
      });
   }

   private void WorldButton(Button var1, Permission var2) {
      var1.material(XMaterial.CHEST)
         .name(Animation.wave(WORLD_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (var2.getWorld().isPresent() ? WORLD_ALL_WORLDS_ACTION : WORLD_SPECIFIC_WORLD_ACTION).get(),
            "",
            var2.getWorld().isPresent() ? WORLD_SPECIFIC_WORLD_DESC.get().replace("%world%", var2.getWorld().get()) : WORLD_ALL_WORLDS_DESC.get()
         );
      var1.action(var2x -> {
         if (var2.getWorld().isPresent()) {
            var2.setWorld(null);
         } else {
            new WorldPickerView(this.p, this.plugin) {
               @Override
               public void onSelect(World var1) {
                  var2.setWorld(var1.getName());
                  this.onBack();
               }

               @Override
               public String getTitle() {
                  return var2.getName() + " > World";
               }

               @Override
               public void onBack() {
                  PermissionView.this.reopen();
               }
            };
         }
      });
   }

   private void PolarityButton(Button var1, Permission var2) {
      var1.material(XMaterial.REPEATER)
         .name(Animation.wave(POLARITY_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (var2.isPositive() ? POLARITY_NEGATIVE_ACTION : POLARITY_POSITIVE_ACTION).get(),
            "",
            (var2.isPositive() ? POLARITY_POSITIVE_ATTRIBUTE : POLARITY_NEGATIVE_ATTRIBUTE).get()
         );
      var1.action(var1x -> var2.setPositive(!var2.isPositive()));
   }

   private void DeleteButton(Button var1, Permission var2) {
      var1.material(Material.REDSTONE_BLOCK).name(Animation.wave(DELETE_TITLE.get(), Colors.Red, Colors.WHITE)).lore(DELETE_ACTION.get());
      var1.action(var2x -> {
         var2.remove();
         if (this.permissionCopies.size() == 1) {
            this.onBack();
         } else {
            this.reloadPermissionCopies();
         }
      });
   }

   private void InheritedButton(Button var1, Permission var2) {
      var1.material(XMaterial.STICKY_PISTON).name(Animation.wave(INHERITED_TITLE.get(), Colors.GOLD, Colors.WHITE)).lore(INHERITED_ACTION.get(), "");

      for (String var6 : INHERITED_DESC.split(60)) {
         var1.item().appendLore(var6.replace("%group%", var2.getHolder().getName()));
      }

      var1.action(var2x -> {
         Optional<PermissionHolder> var3 = var2.getHolder().get();
         var3.ifPresent(var1xx -> new PermissionEditorMainView(this.p, this.plugin, var1xx) {
            @Override
            public void onBack() {
               PermissionView.this.reopen();
            }
         });
      });
   }

   private String getServer(Permission var1) {
      if (var1.getServer().isPresent()) {
         if (var1.getServer().get().equals("BungeeCord")) {
            return Colors.RED + "BungeeCord";
         } else {
            return var1.getServer().get().equals(this.plugin.getThisServer().map(NServer::getName).orElse(null)) ? "This Server" : var1.getServer().get();
         }
      } else {
         return "All Servers";
      }
   }

   private String getWorld(Permission var1) {
      return var1.getWorld().orElse("All Worlds");
   }

   public abstract void onBack();
}
