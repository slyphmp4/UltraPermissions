package me.TechsCode.UltraPermissions.base.item;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.legacy.SkinTexture;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.visual.Text;
import me.TechsCode.UltraPermissions.dependencies.commons.codec.binary.Base64;
import me.TechsCode.UltraPermissions.dependencies.gson.Gson;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

public class CustomItem implements Serializable {
   public static Gson gson = new Gson();
   private static final int MAX_CACHED_HEAD_PROFILES = 1024;
   // Heads can be rebuilt every two ticks. Keep their identity stable across redraws,
   // and bound the cache so old skins cannot accumulate indefinitely.
   private static final Map<String, Object> HEAD_PROFILES = new LinkedHashMap<>(128, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
         return this.size() > MAX_CACHED_HEAD_PROFILES;
      }
   };
   private ItemStack is;

   public CustomItem(Material var1) {
      this(new ItemStack(var1));
   }

   public CustomItem(XMaterial var1) {
      this(var1.getAsItemStack().orElse(new ItemStack(Material.STONE)));
   }

   public CustomItem(XMaterial var1, boolean var2) {
      this(var1.getAsItemStack().orElse(new ItemStack(Material.STONE)), var2);
   }

   public CustomItem(ItemStack var1) {
      this(var1, true);
   }

   public CustomItem(ItemStack var1, boolean var2) {
      super();
      Preconditions.checkArgument(var1 != null, "ItemStack cannot be null");
      this.is = var1.clone();
      if (var2) {
         this.showAllAttributes(false);
      }
   }

   public CustomItem material(Material var1) {
      this.is.setType(var1);
      return this;
   }

   public CustomItem skull(String var1) {
      if (this.is.getItemMeta() != null && this.is.getItemMeta() instanceof SkullMeta) {
         SkullMeta var2 = (SkullMeta)this.is.getItemMeta();
         var2.setOwner(var1);
         this.is.setItemMeta(var2);
      }

      return this;
   }

   public CustomItem setSkullTexture(SkinTexture var1) {
      if (var1 == null) {
         return this;
      }

      if (var1.getUrl() != null && !var1.getUrl().equals("null")) {
         if (this.is.hasItemMeta() && this.is.getItemMeta() instanceof SkullMeta) {
            SkullMeta var2 = (SkullMeta)this.is.getItemMeta();
            this.setTextures(var2, var1);
            this.is.setItemMeta(var2);
         }

         return this;
      } else {
         return this;
      }
   }

   private void setTextures(SkullMeta var1, SkinTexture var2) {
      String url = var2.getUrl();

      try {
         Method createProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);
         Class<?> profileType = createProfile.getReturnType();
         Method setProfile = var1.getClass().getMethod("setOwnerProfile", profileType);
         Method getTextures = profileType.getMethod("getTextures");
         Method setTextures = profileType.getMethod("setTextures", PlayerTextures.class);
         Object profile = cachedHeadProfile("bukkit:" + url, () -> {
            Object created = createProfile.invoke(null, headProfileUuid(url), "skull");
            PlayerTextures textures = (PlayerTextures)getTextures.invoke(created);
            textures.setSkin(new URL(url));
            setTextures.invoke(created, textures);
            return created;
         });
         setProfile.invoke(var1, profile);
      } catch (Throwable modernFailure) {
         // Pre-profile API and third-party Bukkit implementations use the old
         // authlib representation. Preserve that compatibility path.
         try {
            GameProfile legacy = (GameProfile)cachedHeadProfile("legacy:" + url, () -> {
               String value = String.format("{'textures':{'SKIN':{'url':'%s'}}}", url);
               String encoded = new String(Base64.encodeBase64(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
               GameProfile created = new GameProfile(headProfileUuid(url), "legacy");
               created.getProperties().put("textures", new Property("textures", encoded));
               return created;
            });

            try {
               Method method = var1.getClass().getDeclaredMethod("setProfile", GameProfile.class);
               method.setAccessible(true);
               method.invoke(var1, legacy);
               return;
            } catch (Throwable missingMethod) {
               try {
                  Field field = var1.getClass().getDeclaredField("profile");
                  field.setAccessible(true);
                  field.set(var1, legacy);
               } catch (Throwable unsupportedLegacyProfile) {
               }
            }
         } catch (Throwable unsupportedLegacyProfile) {
         }
      }
   }

   private static UUID headProfileUuid(String url) {
      return UUID.nameUUIDFromBytes(("UltraPermissions:head:" + url).getBytes(StandardCharsets.UTF_8));
   }

   private static Object cachedHeadProfile(String key, Callable<Object> create) throws Exception {
      synchronized (HEAD_PROFILES) {
         Object profile = HEAD_PROFILES.get(key);
         if (profile == null) {
            profile = create.call();
            if (profile != null) {
               HEAD_PROFILES.put(key, profile);
            }
         }

         return profile;
      }
   }

   public String getSkullOwner() {
      if (this.is.getItemMeta() != null && this.is.getItemMeta() instanceof SkullMeta) {
         SkullMeta var1 = (SkullMeta)this.is.getItemMeta();
         return var1.getOwner();
      } else {
         return null;
      }
   }

   public CustomItem name() {
      return this.name("§r");
   }

   public CustomItem name(String var1) {
      if (this.is.getItemMeta() != null) {
         ItemMeta var2 = this.is.getItemMeta();
         var2.setDisplayName(var1);
         this.is.setItemMeta(var2);
      }

      return this;
   }

   public CustomItem amount(int var1) {
      this.is.setAmount(var1);
      return this;
   }

   public CustomItem lore(String... var1) {
      this.lore(Arrays.asList(var1));
      return this;
   }

   public CustomItem lore(List<String> var1) {
      if (this.is.getItemMeta() != null) {
         ItemMeta var2 = this.is.getItemMeta();
         var2.setLore(var1);
         this.is.setItemMeta(var2);
      }

      return this;
   }

   public CustomItem addEnchantment(Enchantment var1, int var2) {
      this.is.addUnsafeEnchantment(var1, var2);
      return this;
   }

   public CustomItem removeEnchantment(Enchantment var1) {
      this.is.removeEnchantment(var1);
      return this;
   }

   public CustomItem appendLoreBeginning(String... var1) {
      ArrayList var2 = new ArrayList();
      var2.addAll(Arrays.asList(var1));
      var2.addAll(this.getLore());
      this.lore(var2);
      return this;
   }

   public CustomItem appendLore(List<String> var1) {
      return this.appendLore(var1.toArray(new String[0]));
   }

   public CustomItem appendLore(String... var1) {
      ArrayList var2 = new ArrayList<>(this.getLore());
      var2.addAll(Arrays.asList(var1));
      this.lore(var2);
      return this;
   }

   public CustomItem setDamage(int var1) {
      if (this.is.getItemMeta() == null) {
         return this;
      }

      if (XMaterial.isDamageable(this.is.getType()) && MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1)) {
         Damageable var2 = (Damageable)this.is.getItemMeta();
         var2.setDamage(var1);
         this.is.setItemMeta(var2);
      } else {
         this.is.setDurability((short)var1);
      }

      return this;
   }

   public CustomItem addModelId(int var1) {
      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1) && this.is.getItemMeta() != null) {
         ItemMeta var2 = this.is.getItemMeta();
         var2.setCustomModelData(var1);
         this.is.setItemMeta(var2);
      }

      return this;
   }

   public int getModelId() {
      if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_13_R1) && this.is.getItemMeta() != null) {
         ItemMeta var1 = this.is.getItemMeta();
         return var1.getCustomModelData();
      } else {
         return 0;
      }
   }

   public XMaterial getMaterial() {
      return XMaterial.fromItem(this.is);
   }

   public Material getBukkitMaterial() {
      return this.is.getType();
   }

   public String getName() {
      return this.is.getItemMeta() != null
         ? (
            MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_16_R3)
               ? Text.color(this.is.getItemMeta().getDisplayName())
               : this.is.getItemMeta().getDisplayName()
         )
         : "Not defined";
   }

   public int getAmount() {
      return this.is.getAmount();
   }

   public ArrayList<String> getLore() {
      return this.is.getItemMeta() != null && this.is.getItemMeta().hasLore() ? new ArrayList<>(this.is.getItemMeta().getLore()) : new ArrayList<>();
   }

   public Map<Enchantment, Integer> getEnchants() {
      return this.is.getEnchantments();
   }

   public CustomItem setPlaceholders(HashMap<String, String> var1) {
      for (Entry var3 : var1.entrySet()) {
         String var4 = (String)var3.getKey();
         String var5 = (String)var3.getValue();
         this.replace(var4, var5);
      }

      return this;
   }

   public CustomItem replace(String var1, String var2) {
      if (this.getName() != null) {
         this.name(this.getName().replace(var1, var2));
      }

      if (this.getSkullOwner() != null) {
         this.skull(this.getSkullOwner().replace(var1, var2));
      }

      this.lore(this.getLore().stream().map(var2x -> var2x.replace(var1, var2)).collect(Collectors.toList()));
      return this;
   }

   public CustomItem showEnchantments(boolean var1) {
      if (this.is.getItemMeta() != null) {
         ItemMeta var2 = this.is.getItemMeta();
         if (var1) {
            var2.removeItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         } else {
            var2.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         this.is.setItemMeta(var2);
      }

      return this;
   }

   public CustomItem showAllAttributes(boolean var1) {
      if (this.is.getItemMeta() != null) {
         ItemMeta var2 = this.is.getItemMeta();
         if (var1) {
            var2.removeItemFlags(ItemFlag.values());
         } else {
            var2.addItemFlags(ItemFlag.values());
         }

         this.is.setItemMeta(var2);
      }

      return this;
   }

   public boolean equals(CustomItem var1) {
      return this.serialize().equals(var1.serialize());
   }

   public boolean isSimilar(CustomItem var1, boolean var2, boolean var3, boolean var4) {
      if (var1 == null) {
         return false;
      }

      if (var2 && this.getBukkitMaterial() != var1.getBukkitMaterial()) {
         return false;
      }

      if (var3 && this.getName() != null && var1.getName() != null && !this.getName().equals(var1.getName())) {
         return false;
      }

      if (var4) {
         if (this.getLore().size() != var1.getLore().size()) {
            return false;
         }

         int var5 = 0;

         for (String var7 : this.getLore()) {
            if (!var1.getLore().get(var5).equals(var7)) {
               return false;
            }

            var5++;
         }
      }

      return true;
   }

   public JsonObject serialize() {
      JsonObject var1 = new JsonObject();

      try {
         var1.addProperty("base64", ItemSerializer.itemTo64(this.is));
      } catch (IllegalStateException var3) {
         var3.printStackTrace();
      }

      return var1;
   }

   public static CustomItem deserialize(JsonObject var0) {
      if (var0.has("base64")) {
         try {
            ItemStack var15 = ItemSerializer.itemFrom64(var0.get("base64").getAsString());
            return new CustomItem(var15, false);
         } catch (IOException var14) {
            var14.printStackTrace();
         }
      }

      if (var0.toString().equals("{}")) {
         return new CustomItem(XMaterial.STONE);
      }

      Type var1 = (new TypeToken<Map<String, Object>>() {}).getType();
      Map var2 = null;

      try {
         var2 = gson.fromJson(var0.getAsJsonObject("itemInfo").toString(), var1);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

      if (var0.has("meta")) {
         Map<String, Object> var3 = gson.fromJson(var0.getAsJsonObject("meta").toString(), var1);

         for (Entry var5 : var3.entrySet()) {
            if (var5.getValue() instanceof Number) {
               var5.setValue(convertNumber(var5.getValue()));
            }
         }

         HashMap var16 = new HashMap();
         if (var3.containsKey("enchants")) {
            String var17 = var3.get("enchants").toString().replace("{", "").replace("}", "").replace("\"", "");

            for (String var9 : var17.split(",")) {
               String[] var10 = var9.split("=");
               Enchantment var11 = Enchantment.getByName(var10[0]);
               int var12 = Integer.parseInt(var10[1].replace(".0", ""));
               if (var11 != null) {
                  var16.put(var11, var12);
               }
            }
         }

         var3.put("enchants", var16);
         var2.put("meta", ConfigurationSerialization.deserializeObject(var3, ConfigurationSerialization.getClassByAlias("ItemMeta")));
      }

      return new CustomItem(ItemStack.deserialize(var2), false);
   }

   private static Number convertNumber(Object var0) {
      Number var1 = (Number)var0;
      if (var1 instanceof Long var2 && var2 == var2.intValue()) {
         return var2.intValue();
      } else if (var1 instanceof Float var3 && var3 == var3.intValue()) {
         return var3.intValue();
      } else {
         return var1 instanceof Double var4 && var4 == var4.intValue() ? var4.intValue() : var1;
      }
   }

   public CustomItem clone() {
      return new CustomItem(this.is.clone(), false);
   }

   public ItemStack get() {
      return this.is.clone();
   }

   public void setBukkitStack(ItemStack var1) {
      this.is = var1;
   }
}
