package me.TechsCode.UltraPermissions.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.storage.objects.Group;

public class GroupCreator {
   private final GroupStorage storage;
   private final String name;
   private int id;
   private String server;
   private String world;
   private String prefix;
   private String suffix;
   private boolean default_;
   private int priority = 1;
   private XMaterial icon = XMaterial.YELLOW_STAINED_GLASS_PANE;

   public GroupCreator(GroupStorage var1, String var2) {
      super();
      this.storage = var1;
      this.name = var2;
   }

   public GroupCreator setDefault(boolean var1) {
      this.default_ = var1;
      return this;
   }

   public GroupCreator setServer(String var1) {
      this.server = var1;
      return this;
   }

   public GroupCreator setWorld(String var1) {
      this.world = var1;
      return this;
   }

   public GroupCreator setPrefix(String var1) {
      this.prefix = var1;
      return this;
   }

   public GroupCreator setSuffix(String var1) {
      this.suffix = var1;
      return this;
   }

   public GroupCreator id(int var1) {
      this.id = var1;
      return this;
   }

   public GroupCreator setPriority(int var1) {
      this.priority = var1;
      return this;
   }

   public GroupCreator setIcon(XMaterial var1) {
      this.icon = var1;
      return this;
   }

   public Group create() {
      return this.storage
         .create(
            new Group(
               UUID.randomUUID().toString().substring(0, 4),
               this.name,
               this.world,
               this.server,
               this.priority,
               new HashSet<>(),
               this.default_,
               this.prefix,
               this.suffix,
               this.icon.name(),
               new HashMap<>()
            )
         );
   }
}
