package me.TechsCode.UltraPermissions.storage.objects;

import java.util.Optional;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.internal.Node;

public class Permission extends Storable {
   private int id;
   private Holder holder;
   private String world;
   private String server;
   private String permission;
   private boolean positive;
   private long expiration;

   public Permission(int var1, Holder var2, String var3, String var4, String var5, boolean var6, long var7) {
      super();
      this.id = var1;
      this.holder = var2;
      this.world = var3;
      this.server = var4;
      this.permission = var5;
      this.positive = var6;
      this.expiration = var7;
   }

   public int getId() {
      return this.id;
   }

   public Optional<String> getServer() {
      return Optional.ofNullable(this.server);
   }

   public Optional<String> getWorld() {
      return Optional.ofNullable(this.world);
   }

   public Holder getHolder() {
      return this.holder;
   }

   public String getName() {
      return this.permission;
   }

   public void setName(String var1) {
      this.permission = var1;
      this.sync();
   }

   public void setWorld(String var1) {
      this.world = var1;
      this.sync();
   }

   public void setServer(String var1) {
      this.server = var1;
      this.sync();
   }

   public String getDisplayName() {
      return (this.isPositive() ? "" : "-") + this.getName();
   }

   public boolean isPositive() {
      return this.positive;
   }

   public boolean isPermanent() {
      return this.expiration == 0L;
   }

   public void setPositive(boolean var1) {
      this.positive = var1;
      this.sync();
   }

   public long getExpiration() {
      return this.expiration;
   }

   public void setExpiration(long var1) {
      this.expiration = var1;
      this.sync();
   }

   public void remove() {
      this.destroy();
   }

   public boolean isBungeePermission() {
      return this.server != null && this.server.equals("BungeeCord");
   }

   public boolean isExpired() {
      return this.expiration != 0L && this.expiration < System.currentTimeMillis();
   }

   public void copyTo(PermissionHolder var1) {
      var1.newPermission(this.permission).setServer(this.server).setExpiration(this.expiration).setWorld(this.world).setPositive(this.positive).create();
   }

   public Node asNode() {
      return new Node(this.permission);
   }

   @Override
   public boolean equals(Object var1) {
      return !(var1 instanceof Permission var2) ? false : var2.id == this.id;
   }

   public Permission createCopy() {
      return new Permission(this.id, this.holder, this.world, this.server, this.permission, this.positive, this.expiration);
   }

   @Override
   public String getKey() {
      return this.id + "";
   }

   @Override
   public void setKey(String var1) {
      this.id = Integer.parseInt(var1);
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("holder", this.holder.toStored().getKey());
      if (this.world != null) {
         var1.addProperty("world", this.world);
      }

      if (this.server != null) {
         var1.addProperty("serverId", this.server);
      }

      var1.addProperty("permission", this.permission);
      var1.addProperty("positive", this.positive);
      var1.addProperty("expiry", this.expiration);
      return var1;
   }

   @Override
   public void setState(JsonObject var1, TechPlugin var2) {
      this.holder = Holder.fromKey(var1.get("holder"), var2);
      this.world = var1.has("world") ? var1.get("world").getAsString() : null;
      this.server = var1.has("serverId") && !var1.get("serverId").isJsonNull() ? var1.get("serverId").getAsString() : null;
      this.permission = var1.get("permission").getAsString();
      this.positive = var1.get("positive").getAsBoolean();
      this.expiration = var1.get("expiry").getAsLong();
   }

   @Override
   public void onMount(TechPlugin var1) {
   }

   public void _justDestroy() {
      this.destroy();
   }
}
