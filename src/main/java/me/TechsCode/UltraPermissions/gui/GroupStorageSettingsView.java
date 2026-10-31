package me.TechsCode.UltraPermissions.gui;

import java.util.Optional;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.GUI;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Common;
import me.TechsCode.UltraPermissions.base.networking.NServer;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class GroupStorageSettingsView extends GUI {
   private static final Phrase STORAGE_TITLE = Phrase.create("groupStorageSettingsView.title", "%group% > Storage Settings");
   private final UltraPermissions plugin;
   private final Group group;
   private static final Phrase WORLD_TITLE = Phrase.create("groupStorageSettingsView.world.title", "World");
   private static final Phrase WORLD_ALL_WORLDS_ACTION = Phrase.create(
      "groupStorageSettingsView.world.allWorldsAction", "Click to allow this Group on all Worlds", Colors.GRAY
   );
   private static final Phrase WORLD_SPECIFIC_WORLDS_ACTION = Phrase.create(
      "groupStorageSettingsView.world.specificWorldAction", "Click to restrict this Group to a certain World", Colors.GRAY
   );
   private static final Phrase WORLD_ATTRIBUTE = Phrase.create("groupStorageSettingsView.world.attribute", "World: **%world%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase SERVER_TITLE = Phrase.create("groupStorageSettingsView.server.title", "Server");
   private static final Phrase SERVER_DISABLED_NO_NETWORK = Phrase.create(
      "groupStorageSettingsView.server.disabled", "This feature is only available for networks", Colors.GRAY
   );
   private static final Phrase SERVER_ALL_SERVERS_ACTION = Phrase.create(
      "groupStorageSettingsView.server.allServersAction", "Click to allow this Group on all Servers", Colors.GRAY
   );
   private static final Phrase SERVER_SPECIFIC_SERVERS_ACTION = Phrase.create(
      "groupStorageSettingsView.server.specificServersAction", "Click to restrict this Group to a certain Server", Colors.GRAY
   );
   private static final Phrase SERVER_ATTRIBUTE = Phrase.create("groupStorageSettingsView.server.attribute", "Server: **%server%**", Colors.GRAY, Colors.BLUE);

   public GroupStorageSettingsView(Player var1, UltraPermissions var2, Group var3) {
      super(var1, var2);
      this.plugin = var2;
      this.group = var3;
   }

   public abstract void onBack();

   @Override
   public int getCurrentSlots() {
      return 36;
   }

   @Override
   public String getCurrentTitle() {
      return STORAGE_TITLE.get().replace("%group%", this.group.getName());
   }

   @Override
   protected void construct(Model var1) {
      var1.button(12, this::WorldButton);
      var1.button(16, this::ServerButton);
      var1.button(32, var1x -> Common.BackButton(var1x, var1xx -> this.onBack()));
   }

   private void WorldButton(Button var1) {
      Optional<String> var2 = this.group.getWorld();
      var1.material(var2.isPresent() ? XMaterial.YELLOW_STAINED_GLASS : XMaterial.GRAY_STAINED_GLASS_PANE)
         .name(Animation.wave(WORLD_TITLE.get(), Colors.YELLOW, Colors.WHITE))
         .lore((var2.isPresent() ? WORLD_ALL_WORLDS_ACTION : WORLD_SPECIFIC_WORLDS_ACTION).get());
      var2.ifPresent(var1x -> var1.item().appendLore("", WORLD_ATTRIBUTE.get().replace("%world%", var1x)));
      var1.action(var2x -> {
         if (var2.isPresent()) {
            this.group.setWorld(null);
         } else {
            new WorldPickerView(this.p, this.plugin) {
               @Override
               public void onSelect(World var1) {
                  GroupStorageSettingsView.this.group.setWorld(var1.getName());
                  this.onBack();
               }

               @Override
               public String getTitle() {
                  return GroupStorageSettingsView.this.group.getName() + " > " + WORLD_TITLE.get();
               }

               @Override
               public void onBack() {
                  GroupStorageSettingsView.this.reopen();
               }
            };
         }
      });
   }

   private void ServerButton(Button var1) {
      if (!this.plugin.isConnectedToNetwork()) {
         var1.material(XMaterial.RED_STAINED_GLASS_PANE)
            .name(Animation.wave(SERVER_TITLE.get(), Colors.Red, Colors.WHITE))
            .lore(SERVER_DISABLED_NO_NETWORK.get());
      } else {
         Optional<String> var2 = this.group.getServer();
         var1.material(var2.isPresent() ? XMaterial.BLUE_STAINED_GLASS : XMaterial.GRAY_STAINED_GLASS_PANE)
            .name(Animation.wave(SERVER_TITLE.get(), Colors.DarkBlue, Colors.WHITE))
            .lore((var2.isPresent() ? SERVER_ALL_SERVERS_ACTION : SERVER_SPECIFIC_SERVERS_ACTION).get());
         var2.ifPresent(var1x -> var1.item().appendLore("", SERVER_ATTRIBUTE.get().replace("%server%", var1x)));
         var1.action(var2x -> {
            if (var2.isPresent()) {
               this.group.setServer(null);
            } else {
               new ServerPickerView(this.p, this.plugin) {
                  @Override
                  public void onSelect(NServer var1) {
                     GroupStorageSettingsView.this.group.setServer(var1.getName());
                     this.onBack();
                  }

                  @Override
                  public String getTitle() {
                     return GroupStorageSettingsView.this.group.getName() + " > " + SERVER_TITLE.get();
                  }

                  @Override
                  public void onBack() {
                     GroupStorageSettingsView.this.reopen();
                  }
               };
            }
         });
      }
   }
}
