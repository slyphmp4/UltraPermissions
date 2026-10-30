package me.TechsCode.UltraPermissions.storage.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.StorageController;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.base.storage.Storage;
import me.TechsCode.UltraPermissions.base.storage.Stored;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.storage.PermissionCreator;
import me.TechsCode.UltraPermissions.storage.collection.GroupList;
import me.TechsCode.UltraPermissions.storage.collection.PermissionList;
import me.TechsCode.UltraPermissions.storage.collection.RankupList;

public class User extends Storable implements PermissionHolder {
   private StorageController controller;
   private UUID uuid;
   private String playerName;
   private volatile boolean superadmin;
   private Map<Stored<Group>, Long> rankups;
   private String prefix;
   private String suffix;
   private SkinTexture skinTexture;

   public User(UUID var1, String var2, boolean var3, Map<Stored<Group>, Long> var4, String var5, String var6, SkinTexture var7) {
      super();
      this.uuid = var1;
      this.playerName = var2;
      this.superadmin = var3;
      this.rankups = var4;
      this.prefix = var5;
      this.suffix = var6;
      this.skinTexture = var7;
   }

   public RankupList getRankups() {
      return this.rankups
         .entrySet()
         .stream()
         .map(var1 -> new UserRankup(this, var1.getKey(), var1.getValue()))
         .collect(Collectors.toCollection(RankupList::new))
         .bestToWorst();
   }

   public UUID getUuid() {
      return this.uuid;
   }

   @Override
   public String getName() {
      return this.playerName;
   }

   public void setPlayerName(String var1) {
      this.playerName = var1;
      this.sync();
   }

   public boolean isSuperadmin() {
      return this.superadmin;
   }

   public void setSuperadmin(boolean var1) {
      this.superadmin = var1;
      this.sync();
   }

   public void addGroup(Group var1) {
      this.rankups.put(var1.toStored(), 0L);
      this.sync();
   }

   public void addGroup(Group var1, long var2) {
      this.rankups.put(var1.toStored(), var2);
      this.sync();
   }

   public long getGroupExpiry(Stored<Group> var1) {
      return this.rankups.get(var1);
   }

   public Set<Stored<Group>> getGroups() {
      return this.rankups.keySet();
   }

   public GroupList getActiveGroups() {
      return this.rankups
         .keySet()
         .stream()
         .map(var0 -> var0.get().orElse(null))
         .filter(Objects::nonNull)
         .collect(Collectors.toCollection(GroupList::new))
         .bestToWorst();
   }

   public Optional<SkinTexture> getSkinTexture() {
      return Optional.ofNullable(this.skinTexture);
   }

   public void setSkinTexture(SkinTexture var1) {
      if (var1 != null && (this.skinTexture == null || !this.skinTexture.getUrl().equalsIgnoreCase(var1.getUrl()))) {
         this.skinTexture = var1;
         this.sync();
      }
   }

   public void removeGroup(Group var1) {
      if (this.rankups.containsKey(var1.toStored())) {
         this.rankups.remove(var1.toStored());
         this.sync();
      }
   }

   public boolean hasPrefix() {
      return this.getPrefix().isPresent();
   }

   @Override
   public Optional<String> getPrefix() {
      return Optional.ofNullable(this.prefix);
   }

   public boolean hasSuffix() {
      return this.getSuffix().isPresent();
   }

   @Override
   public Optional<String> getSuffix() {
      return Optional.ofNullable(this.suffix);
   }

   @Override
   public void setPrefix(String var1) {
      this.prefix = var1 != null ? Text.color(var1) : null;
      this.sync();
   }

   @Override
   public void setSuffix(String var1) {
      this.suffix = var1 != null ? Text.color(var1) : null;
      this.sync();
   }

   @Override
   public PermissionCreator newPermission(String var1) {
      return this.controller.getPermissionStorage().newPermission(var1, Holder.fromUser(this));
   }

   @Override
   public PermissionList getPermissions() {
      return this.controller.getPermissionStorage().getPermissions().holder(Holder.fromUser(this));
   }

   public void remove() {
      this.getPermissions().forEach(Permission::remove);
      this.destroy();
   }

   @Override
   public PermissionList getAdditionalPermissions() {
      return this.controller.getPermissionStorage().getPermissions().holder(this.getActiveGroups().getWithRecursiveInherits().holders());
   }

   public User createCopy() {
      return new User(this.uuid, this.playerName, this.superadmin, this.rankups, this.prefix, this.suffix, this.skinTexture);
   }

   @Override
   public String getKey() {
      return this.uuid.toString();
   }

   @Override
   public void setKey(String var1) {
      this.uuid = UUID.fromString(var1);
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("name", this.playerName);
      var1.addProperty("superadmin", this.superadmin);
      JsonObject var2 = new JsonObject();
      this.rankups.forEach((var1x, var2x) -> var2.addProperty(var1x.getKey(), var2x));
      if (this.rankups.size() != 0) {
         var1.add("groups", var2);
      }

      if (this.prefix != null) {
         var1.addProperty("prefix", this.prefix);
      }

      if (this.suffix != null) {
         var1.addProperty("suffix", this.suffix);
      }

      if (this.skinTexture != null) {
         var1.addProperty("skull", this.skinTexture.toString());
      }

      return var1;
   }

   @Override
   public void setState(JsonObject var1, TechPlugin var2) {
      StorageController var3 = (StorageController)var2;
      this.playerName = var1.get("name").getAsString();
      this.superadmin = var1.get("superadmin").getAsBoolean();
      this.rankups = new HashMap<>();
      if (var1.has("groups")) {
         var1.get("groups")
            .getAsJsonObject()
            .entrySet()
            .forEach(var2x -> this.rankups.put(new Stored<>(var2x.getKey(), var3::getGroupStorage), var2x.getValue().getAsLong()));
      }

      this.prefix = var1.has("prefix") ? Text.color(var1.get("prefix").getAsString()) : null;
      this.suffix = var1.has("suffix") ? Text.color(var1.get("suffix").getAsString()) : null;
      this.skinTexture = var1.has("skull") && !var1.get("skull").getAsString().contains("null:split:")
         ? new SkinTexture(var1.get("skull").getAsString())
         : null;
   }

   @Override
   public void onMount(TechPlugin var1) {
      this.controller = (StorageController)var1;
   }

   public void clean() {
      for (Stored var2 : new ArrayList<>(this.rankups.keySet())) {
         if (var2 != null & !var2.isPresent()) {
            this.rankups.remove(var2);
            this.sync();
         }
      }
   }

   public Stored<User> toStored() {
      return new Stored<>(this.getKey(), () -> (Storage<User>)this.storage);
   }

   public void _justDestroy() {
      this.destroy();
   }
}
