package me.TechsCode.UltraPermissions.storage.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.storage.PermissionCreator;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import me.TechsCode.UltraPermissions.visual.VisualType;

public class Group extends Storable implements PermissionHolder {
   private StorageController controller;
   private String id;
   private String name;
   private String world;
   private String server;
   private int priority;
   private Set<Stored<Group>> inheritedGroups;
   private boolean isDefault;
   private String prefix;
   private String suffix;
   private String icon;
   private int damage;
   private int modelId;
   private Map<Stored<Group>, Long> grantGroups;
   private String chatFormat;

   public Group(
      String var1,
      String var2,
      String var3,
      String var4,
      int var5,
      Set<Stored<Group>> var6,
      boolean var7,
      String var8,
      String var9,
      String var10,
      Map<Stored<Group>, Long> var11
   ) {
      super();
      this.id = var1;
      this.name = var2;
      this.world = var3;
      this.server = var4;
      this.priority = var5;
      this.inheritedGroups = var6;
      this.isDefault = var7;
      this.prefix = var8;
      this.suffix = var9;
      this.icon = var10;
      this.damage = 0;
      this.modelId = 0;
      this.chatFormat = "none";
      this.grantGroups = var11;
   }

   public Group(JsonObject var1) {
      super();
      this.name = var1.get("name").getAsString();
      this.world = var1.has("world") ? (var1.get("world").isJsonNull() ? null : var1.get("world").getAsString()) : null;
      this.server = var1.has("serverId") ? (var1.get("serverId").isJsonNull() ? null : var1.get("serverId").getAsString()) : null;
      this.priority = var1.get("priority").getAsInt();
      if (var1.has("inherits")) {
         this.inheritedGroups = Arrays.stream(var1.get("inherits").getAsString().split(";"))
            .filter(var0 -> var0.length() != 0)
            .map(var1x -> new Stored<Group>(var1x, this.controller::getGroupStorage))
            .collect(Collectors.toSet());
      } else {
         this.inheritedGroups = new HashSet<>();
      }

      this.isDefault = var1.get("default").getAsBoolean();
      this.prefix = var1.has("prefix") ? Text.color(var1.get("prefix").getAsString()) : null;
      this.suffix = var1.has("suffix") ? Text.color(var1.get("suffix").getAsString()) : null;
      this.icon = var1.get("icon").getAsString();
      if (var1.get("damage") == null || var1.get("modelId") == null) {
         var1.addProperty("damage", this.damage);
         var1.addProperty("modelId", this.modelId);
      }

      this.damage = var1.get("damage").getAsInt();
      this.modelId = var1.get("modelId").getAsInt();
      this.chatFormat = var1.get("chatFormat") == null ? "none" : var1.get("chatFormat").getAsString();
      this.grantGroups = new HashMap<>();
      if (var1.get("grantGroups") == null) {
         JsonObject var2 = new JsonObject();
         var1.add("grantGroups", var2);
      }

      if (var1.has("grantGroups")) {
         var1.get("grantGroups")
            .getAsJsonObject()
            .entrySet()
            .forEach(var1x -> this.grantGroups.put(new Stored<>(var1x.getKey(), this.controller::getGroupStorage), var1x.getValue().getAsLong()));
      }
   }

   public List<UserRankup> getRankups() {
      return this.controller
         .getUserStorage()
         .getUsers()
         .stream()
         .flatMap(var0 -> var0.getRankups().stream())
         .filter(var1 -> var1.getGroup().equals(this.toStored()))
         .collect(Collectors.toList());
   }

   @Override
   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
      this.sync();
   }

   public Optional<String> getWorld() {
      return Optional.ofNullable(this.world);
   }

   public void setWorld(String var1) {
      this.world = var1;
      this.sync();
   }

   public Optional<String> getServer() {
      return Optional.ofNullable(this.server);
   }

   public void setServer(String var1) {
      this.server = var1;
      this.sync();
   }

   public int getPriority() {
      return this.priority;
   }

   public void setPriority(int var1) {
      this.priority = var1;
      this.sync();
   }

   public Set<Stored<Group>> getInheritedGroups() {
      return this.inheritedGroups;
   }

   public GroupList getActiveInheritedGroups() {
      return this.inheritedGroups.stream().map(var0 -> var0.get().orElse(null)).filter(Objects::nonNull).collect(Collectors.toCollection(GroupList::new));
   }

   public void addInheritance(Group var1) {
      this.inheritedGroups.add(var1.toStored());
      this.sync();
   }

   public void removeInheritance(Group var1) {
      this.inheritedGroups.remove(var1.toStored());
      this.sync();
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public void setDefault(boolean var1) {
      this.isDefault = var1;
      this.sync();
   }

   @Override
   public Optional<String> getPrefix() {
      return Optional.ofNullable(this.prefix);
   }

   @Override
   public void setPrefix(String var1) {
      this.prefix = var1 != null ? Text.color(var1) : null;
      this.sync();
   }

   @Override
   public Optional<String> getSuffix() {
      return Optional.ofNullable(this.suffix);
   }

   @Override
   public void setSuffix(String var1) {
      this.suffix = var1 != null ? Text.color(var1) : null;
      this.sync();
   }

   public XMaterial getIcon() {
      return XMaterial.valueOf(this.icon);
   }

   public void setIcon(XMaterial var1) {
      this.icon = var1.name();
      this.sync();
   }

   @Override
   public PermissionList getPermissions() {
      return this.controller.getPermissionStorage().getPermissions().holder(Holder.fromGroup(this));
   }

   @Override
   public PermissionCreator newPermission(String var1) {
      return this.controller.getPermissionStorage().newPermission(var1, Holder.fromGroup(this));
   }

   public UserList getUsers() {
      return this.controller.getUserStorage().getUsers().usersInGroup(this.toStored());
   }

   public void remove() {
      this.controller.getGroupStorage().getGroups().forEach(var1 -> var1.removeInheritance(this));
      this.getUsers().forEach(var1 -> var1.removeGroup(this));
      this.getPermissions().forEach(Permission::remove);
      this.destroy();
   }

   public PermissionList getInheritedPermissions() {
      return this.controller.getPermissionStorage().getPermissions().holder(this.getActiveInheritedGroups().getWithRecursiveInherits().holders());
   }

   @Override
   public PermissionList getAdditionalPermissions() {
      return this.getInheritedPermissions();
   }

   public Group createCopy() {
      return new Group(
         this.id,
         this.name,
         this.world,
         this.server,
         this.priority,
         this.inheritedGroups,
         this.isDefault,
         this.prefix,
         this.suffix,
         this.icon,
         this.grantGroups
      );
   }

   @Override
   public String getKey() {
      return this.id + "";
   }

   @Override
   public void setKey(String var1) {
      this.id = String.valueOf(var1);
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("name", this.name);
      if (this.world != null) {
         var1.addProperty("world", this.world);
      }

      if (this.server != null) {
         var1.addProperty("serverId", this.server);
      }

      var1.addProperty("priority", this.priority);
      if (this.inheritedGroups.size() != 0) {
         String var2 = this.inheritedGroups.stream().map(Stored::getKey).collect(Collectors.joining(";"));
         var1.addProperty("inherits", var2);
      }

      var1.addProperty("default", this.isDefault);
      if (this.prefix != null) {
         var1.addProperty("prefix", this.prefix);
      }

      if (this.suffix != null) {
         var1.addProperty("suffix", this.suffix);
      }

      if (this.icon != null) {
         var1.addProperty("icon", this.icon);
      }

      var1.addProperty("damage", this.damage);
      var1.addProperty("modelId", this.modelId);
      var1.addProperty("chatFormat", this.chatFormat);
      JsonObject var3 = new JsonObject();
      this.grantGroups.forEach((var1x, var2x) -> var3.addProperty(var1x.getKey(), var2x));
      var1.add("grantGroups", var3);
      return var1;
   }

   @Override
   public void setState(JsonObject var1, TechPlugin var2) {
      StorageController var3 = (StorageController)var2;
      this.name = var1.get("name").getAsString();
      this.world = var1.has("world") ? (var1.get("world").isJsonNull() ? null : var1.get("world").getAsString()) : null;
      this.server = var1.has("serverId") ? (var1.get("serverId").isJsonNull() ? null : var1.get("serverId").getAsString()) : null;
      this.priority = var1.get("priority").getAsInt();
      if (var1.has("inherits")) {
         this.inheritedGroups = Arrays.stream(var1.get("inherits").getAsString().split(";"))
            .filter(var0 -> var0.length() != 0)
            .map(var1x -> new Stored<Group>(var1x, var3::getGroupStorage))
            .collect(Collectors.toSet());
      } else {
         this.inheritedGroups = new HashSet<>();
      }

      this.isDefault = var1.get("default").getAsBoolean();
      this.prefix = var1.has("prefix") ? Text.color(var1.get("prefix").getAsString()) : null;
      this.suffix = var1.has("suffix") ? Text.color(var1.get("suffix").getAsString()) : null;
      this.icon = var1.get("icon").getAsString();
      if (var1.get("damage") == null || var1.get("modelId") == null) {
         var1.addProperty("damage", this.damage);
         var1.addProperty("modelId", this.modelId);
      }

      this.damage = var1.get("damage").getAsInt();
      this.modelId = var1.get("modelId").getAsInt();
      this.chatFormat = var1.get("chatFormat") == null ? "none" : var1.get("chatFormat").getAsString();
      this.grantGroups = new HashMap<>();
      if (var1.get("grantGroups") == null) {
         JsonObject var4 = new JsonObject();
         var1.add("grantGroups", var4);
      }

      if (var1.has("grantGroups")) {
         var1.get("grantGroups")
            .getAsJsonObject()
            .entrySet()
            .forEach(var2x -> this.grantGroups.put(new Stored<>(var2x.getKey(), var3::getGroupStorage), var2x.getValue().getAsLong()));
      }
   }

   @Override
   public void onMount(TechPlugin var1) {
      this.controller = (StorageController)var1;
   }

   public void clean() {
      for (Stored var2 : new ArrayList<>(this.inheritedGroups)) {
         if (!var2.isPresent()) {
            this.inheritedGroups.remove(var2);
            this.sync();
         }
      }
   }

   public Stored<Group> toStored() {
      return new Stored<>(this.getKey(), () -> (Storage<Group>)this.storage);
   }

   public void _justDestroy() {
      this.destroy();
   }

   public void setFormat(String var1) {
      this.chatFormat = var1;
      this.sync();
   }

   public String getFormat() {
      return this.chatFormat.equalsIgnoreCase("none") ? null : this.chatFormat;
   }

   public void resetFormat() {
      this.chatFormat = VisualType.CHAT.getDefaultFormat();
      this.sync();
   }

   public Map<Stored<Group>, Long> getGrantGroups() {
      return this.grantGroups;
   }

   public void setGrantGroups(Map<Stored<Group>, Long> var1) {
      this.grantGroups = var1;
      this.sync();
   }

   public void addGrantGroup(Group var1) {
      this.grantGroups.put(var1.toStored(), 0L);
      this.sync();
   }

   public void addGrantGroup(Group var1, long var2) {
      this.grantGroups.put(var1.toStored(), var2);
      this.sync();
   }

   public void removeGrantGroup(Group var1) {
      this.grantGroups.remove(var1.toStored());
      this.sync();
   }

   public void setDamage(int var1) {
      this.damage = var1;
      this.sync();
   }

   public void setModelId(int var1) {
      this.modelId = var1;
      this.sync();
   }

   public int getDamage() {
      return this.damage;
   }

   public int getModelId() {
      return this.modelId;
   }
}
