package me.TechsCode.UltraPermissions.storage.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.item.ItemSerializer;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.storage.Storable;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonArray;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import org.bukkit.inventory.ItemStack;

public class GroupPath extends Storable {
   private String uid = UUID.randomUUID().toString().substring(0, 6);
   private int priority = UltraPermissions.getAPI().getGroupPaths().size() + 1;
   private String name;
   private String description;
   private ArrayList<Group> groupPath;
   private ItemStack icon;
   private boolean enabled;

   public GroupPath(String var1) {
      super();
      this.name = var1;
      this.description = "";
      this.groupPath = new ArrayList<>();
      this.icon = XMaterial.GREEN_STAINED_GLASS_PANE.getAsItemStack().get();
      this.enabled = true;
   }

   @Override
   public String getKey() {
      return this.uid;
   }

   @Override
   public void setKey(String var1) {
      this.uid = var1;
      this.sync();
   }

   @Override
   public JsonObject getState() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("name", this.name);
      var1.addProperty("description", this.description);
      JsonArray var2 = new JsonArray();
      this.groupPath.forEach(var1x -> var2.add(var1x.getKey()));
      TechPlugin.getInstance().log("GroupPath: " + var2);
      var1.add("groupPath", var2);
      var1.addProperty("icon", ItemSerializer.itemTo64(this.icon));
      var1.addProperty("enabled", this.enabled);
      return var1;
   }

   @Override
   public void setState(JsonObject var1, TechPlugin<?> var2) {
      this.name = var1.get("name").getAsString();
      this.description = var1.get("description").getAsString();
      JsonArray var3 = var1.has("groupPath") ? var1.get("groupPath").getAsJsonArray() : new JsonArray();
      ArrayList var4 = new ArrayList();
      var3.forEach(var1x -> {
         if (var1x.isJsonPrimitive()) {
            String var2x = var1x.getAsString();
            Optional var3x = UltraPermissions.getAPI().getGroups().key(var2x);
            var3x.ifPresent(var4::add);
         }
      });
      this.groupPath = var4;

      try {
         this.icon = ItemSerializer.itemFrom64(var1.get("icon").getAsString());
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }

      this.enabled = var1.get("enabled").getAsBoolean();
   }

   @Override
   public void onMount(TechPlugin<?> var1) {
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
      this.sync();
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String var1) {
      this.description = var1;
      this.sync();
   }

   public ArrayList<Group> getGroupPath() {
      return this.groupPath;
   }

   public void setGroupPath(ArrayList<Group> var1) {
      this.groupPath = var1;
      this.sync();
   }

   public void addGroupToGroupPath(Group var1) {
      this.groupPath.add(var1);
      this.sync();
   }

   public void removeGroupFromGroupPath(Group var1) {
      this.groupPath.remove(var1);
      this.sync();
   }

   public ItemStack getIcon() {
      return this.icon;
   }

   public void setIcon(ItemStack var1) {
      this.icon = var1;
      this.sync();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
      this.sync();
   }

   public int getPriority() {
      return this.priority;
   }

   public void setPriority(int var1) {
      this.priority = var1;
      this.sync();
   }

   public void delete() {
      this.destroy();
   }

   @Override
   public void sync() {
      super.sync();
   }
}
