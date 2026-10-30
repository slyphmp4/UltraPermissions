package me.TechsCode.UltraPermissions.base.item;

import com.google.common.base.Enums;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XEnchantment extends XModule<XEnchantment, Enchantment> {
   private static final boolean ISFLAT;
   private static final boolean IS_SUPER_FLAT;
   private static final boolean USES_WRAPPER;
   public static final XRegistry<XEnchantment, Enchantment> REGISTRY;
   public static final XEnchantment AQUA_AFFINITY;
   public static final XEnchantment BANE_OF_ARTHROPODS;
   public static final XEnchantment BINDING_CURSE;
   public static final XEnchantment BLAST_PROTECTION;
   public static final XEnchantment BREACH;
   public static final XEnchantment CHANNELING;
   public static final XEnchantment DENSITY;
   public static final XEnchantment DEPTH_STRIDER;
   public static final XEnchantment EFFICIENCY;
   public static final XEnchantment FEATHER_FALLING;
   public static final XEnchantment FIRE_ASPECT;
   public static final XEnchantment FIRE_PROTECTION;
   public static final XEnchantment FLAME;
   public static final XEnchantment FORTUNE;
   public static final XEnchantment FROST_WALKER;
   public static final XEnchantment IMPALING;
   public static final XEnchantment INFINITY;
   public static final XEnchantment KNOCKBACK;
   public static final XEnchantment LOOTING;
   public static final XEnchantment LOYALTY;
   public static final XEnchantment LUCK_OF_THE_SEA;
   public static final XEnchantment LURE;
   public static final XEnchantment MENDING;
   public static final XEnchantment MULTISHOT;
   public static final XEnchantment PIERCING;
   public static final XEnchantment POWER;
   public static final XEnchantment PROJECTILE_PROTECTION;
   public static final XEnchantment PROTECTION;
   public static final XEnchantment PUNCH;
   public static final XEnchantment QUICK_CHARGE;
   public static final XEnchantment RESPIRATION;
   public static final XEnchantment RIPTIDE;
   public static final XEnchantment SHARPNESS;
   public static final XEnchantment SILK_TOUCH;
   public static final XEnchantment SMITE;
   public static final XEnchantment SOUL_SPEED;
   public static final XEnchantment SWIFT_SNEAK;
   public static final XEnchantment THORNS;
   public static final XEnchantment UNBREAKING;
   public static final XEnchantment VANISHING_CURSE;
   public static final XEnchantment WIND_BURST;
   public static final XEnchantment SWEEPING_EDGE;
   public static final XEnchantment LUNGE;
   @Deprecated
   public static final XEnchantment[] VALUES;
   @Deprecated
   public static final Set<EntityType> EFFECTIVE_SMITE_ENTITIES;
   @Deprecated
   public static final Set<EntityType> EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES;

   private XEnchantment(Enchantment var1, String[] var2) {
      super(var1, var2);
   }

   @NotNull
   public static XEnchantment of(@NotNull Enchantment var0) {
      return REGISTRY.getByBukkitForm(var0);
   }

   public static Optional<XEnchantment> of(@NotNull String var0) {
      return REGISTRY.getByName(var0);
   }

   @Deprecated
   @NotNull
   public static XEnchantment[] values() {
      return REGISTRY.values();
   }

   @NotNull
   private static XEnchantment std(@NotNull String... var0) {
      XEnchantment var1 = REGISTRY.std(var0);
      if (USES_WRAPPER && var1.isSupported()) {
         Enchantment var2 = var1.get();
         if (var2 instanceof EnchantmentWrapper) {
            Enchantment var3 = ((EnchantmentWrapper)var2).getEnchantment();
            REGISTRY.bukkitMapping().put(var3, var1);
         }
      }

      return var1;
   }

   @Deprecated
   private static Enchantment getBukkitEnchant(String var0) {
      if (IS_SUPER_FLAT) {
         return (Enchantment)Registry.ENCHANTMENT.get(NamespacedKey.minecraft(var0.toLowerCase(Locale.ENGLISH)));
      } else {
         return ISFLAT ? Enchantment.getByKey(NamespacedKey.minecraft(var0.toLowerCase(Locale.ENGLISH))) : Enchantment.getByName(var0);
      }
   }

   @Deprecated
   public static boolean isSmiteEffectiveAgainst(@Nullable EntityType var0) {
      return var0 != null && EFFECTIVE_SMITE_ENTITIES.contains(var0);
   }

   @Deprecated
   public static boolean isArthropodsEffectiveAgainst(@Nullable EntityType var0) {
      return var0 != null && EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES.contains(var0);
   }

   @Deprecated
   @NotNull
   public static Optional<XEnchantment> matchXEnchantment(@NotNull String var0) {
      if (var0 != null && !var0.isEmpty()) {
         return of(var0);
      } else {
         throw new IllegalArgumentException("Enchantment name cannot be null or empty");
      }
   }

   @Deprecated
   @NotNull
   public static XEnchantment matchXEnchantment(@NotNull Enchantment var0) {
      Objects.requireNonNull(var0, "Cannot parse XEnchantment of a null enchantment");
      return of(var0);
   }

   @NotNull
   public ItemStack getBook(int var1) {
      ItemStack var2 = new ItemStack(Material.ENCHANTED_BOOK);
      EnchantmentStorageMeta var3 = (EnchantmentStorageMeta)var2.getItemMeta();
      var3.addStoredEnchant(this.get(), var1, true);
      var2.setItemMeta(var3);
      return var2;
   }

   @Deprecated
   @Nullable
   public Enchantment getEnchant() {
      return this.get();
   }

   static {
      boolean var2 = false;

      boolean var0;
      try {
         Class var3 = Class.forName("org.bukkit.NamespacedKey");
         Class var4 = Class.forName("org.bukkit.enchantments.Enchantment");
         var4.getDeclaredMethod("getByKey", var3);
         var0 = true;
      } catch (ClassNotFoundException | NoSuchMethodException var12) {
         var0 = false;
      }

      boolean var1;
      try {
         Class.forName("org.bukkit.Registry");
         var1 = true;
      } catch (ClassNotFoundException var11) {
         var1 = false;
      }

      for (Field var6 : Enchantment.class.getDeclaredFields()) {
         int var7 = var6.getModifiers();
         if (Modifier.isPublic(var7) && Modifier.isStatic(var7) && Modifier.isFinal(var7) && var6.getType() == Enchantment.class) {
            try {
               Object var8 = var6.get(null);
               if (var8 instanceof EnchantmentWrapper) {
                  var2 = true;
               }
            } catch (IllegalAccessException var10) {
               throw new IllegalStateException("Cannot get enchantment field for " + var6, var10);
            }
         }
      }

      ISFLAT = var0;
      IS_SUPER_FLAT = var1;
      USES_WRAPPER = var2;
      REGISTRY = new XRegistry<>(Enchantment.class, XEnchantment.class, () -> Registry.ENCHANTMENT, XEnchantment::new, var0x -> new XEnchantment[var0x]);
      AQUA_AFFINITY = std("WATER_WORKER", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE");
      BANE_OF_ARTHROPODS = std("BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS", "BANE_OF_ARTHROPOD", "ARTHROPOD");
      BINDING_CURSE = std("BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND");
      BLAST_PROTECTION = std("PROTECTION_EXPLOSIONS", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION");
      BREACH = std("BREACH");
      CHANNELING = std("CHANNELING", "CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL");
      DENSITY = std("DENSITY");
      DEPTH_STRIDER = std("DEPTH_STRIDER", "DEPTH", "STRIDER");
      EFFICIENCY = std("EFFICIENCY", "DIG_SPEED", "MINE_SPEED", "CUT_SPEED");
      FEATHER_FALLING = std("PROTECTION_FALL", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING");
      FIRE_ASPECT = std("FIRE_ASPECT", "FIRE", "MELEE_FIRE", "MELEE_FLAME");
      FIRE_PROTECTION = std("PROTECTION_FIRE", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT");
      FLAME = std("FLAME", "ARROW_FIRE", "FLAME_ARROW", "FIRE_ARROW");
      FORTUNE = std("FORTUNE", "LOOT_BONUS_BLOCKS", "BLOCKS_LOOT_BONUS");
      FROST_WALKER = std("FROST_WALKER", "FROST", "WALKER");
      IMPALING = std("IMPALING", "IMPALE", "OCEAN_DAMAGE");
      INFINITY = std("INFINITY", "ARROW_INFINITE", "INFINITE_ARROWS", "INFINITE", "UNLIMITED_ARROWS");
      KNOCKBACK = std("KNOCKBACK");
      LOOTING = std("LOOTING", "LOOT_BONUS_MOBS", "MOB_LOOT", "MOBS_LOOT_BONUS");
      LOYALTY = std("LOYALTY", "LOYAL", "RETURN");
      LUCK_OF_THE_SEA = std("LUCK_OF_THE_SEA", "LUCK", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK");
      LURE = std("LURE", "ROD_LURE");
      MENDING = std("MENDING");
      MULTISHOT = std("MULTISHOT", "TRIPLE_SHOT");
      PIERCING = std("PIERCING");
      POWER = std("POWER", "ARROW_DAMAGE", "ARROW_POWER");
      PROJECTILE_PROTECTION = std("PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION");
      PROTECTION = std("PROTECTION", "PROTECTION_ENVIRONMENTAL", "PROTECT");
      PUNCH = std("PUNCH", "ARROW_KNOCKBACK", "ARROW_PUNCH");
      QUICK_CHARGE = std("QUICK_CHARGE", "QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW");
      RESPIRATION = std("RESPIRATION", "OXYGEN", "BREATH", "BREATHING");
      RIPTIDE = std("RIPTIDE", "RIP", "TIDE", "LAUNCH");
      SHARPNESS = std("SHARPNESS", "DAMAGE_ALL", "ALL_DAMAGE", "ALL_DMG", "SHARP");
      SILK_TOUCH = std("SILK_TOUCH", "SOFT_TOUCH");
      SMITE = std("SMITE", "DAMAGE_UNDEAD", "UNDEAD_DAMAGE");
      SOUL_SPEED = std("SOUL_SPEED", "SPEED_SOUL", "SOUL_RUNNER");
      SWIFT_SNEAK = std("SWIFT_SNEAK", "SNEAK_SWIFT");
      THORNS = std("THORNS", "HIGHCRIT", "THORN", "HIGHERCRIT");
      UNBREAKING = std("UNBREAKING", "DURABILITY", "DURA");
      VANISHING_CURSE = std("VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH");
      WIND_BURST = std("WIND_BURST");
      SWEEPING_EDGE = std("SWEEPING", "SWEEPING_EDGE", "SWEEP_EDGE");
      LUNGE = std("LUNGE");
      VALUES = values();
      EntityType var13 = (EntityType)Enums.getIfPresent(EntityType.class, "BEE").orNull();
      EntityType var15 = (EntityType)Enums.getIfPresent(EntityType.class, "PHANTOM").orNull();
      EntityType var17 = (EntityType)Enums.getIfPresent(EntityType.class, "DROWNED").orNull();
      EntityType var20 = (EntityType)Enums.getIfPresent(EntityType.class, "WITHER_SKELETON").orNull();
      EntityType var23 = (EntityType)Enums.getIfPresent(EntityType.class, "SKELETON_HORSE").orNull();
      EntityType var25 = (EntityType)Enums.getIfPresent(EntityType.class, "STRAY").orNull();
      EntityType var27 = (EntityType)Enums.getIfPresent(EntityType.class, "HUSK").orNull();
      EnumSet var29 = EnumSet.of(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE);
      if (var13 != null) {
         var29.add(var13);
      }

      EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES = Collections.unmodifiableSet(var29);
      EnumSet var31 = EnumSet.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER);
      if (var15 != null) {
         var31.add(var15);
      }

      if (var17 != null) {
         var31.add(var17);
      }

      if (var20 != null) {
         var31.add(var20);
      }

      if (var23 != null) {
         var31.add(var23);
      }

      if (var25 != null) {
         var31.add(var25);
      }

      if (var27 != null) {
         var31.add(var27);
      }

      EFFECTIVE_SMITE_ENTITIES = Collections.unmodifiableSet(var31);
      if (USES_WRAPPER) {
         for (Field var21 : Enchantment.class.getDeclaredFields()) {
            int var24 = var21.getModifiers();
            if (Modifier.isPublic(var24) && Modifier.isStatic(var24) && Modifier.isFinal(var24) && var21.getType() == Enchantment.class) {
               try {
                  if (var21.get(null) instanceof EnchantmentWrapper var28) {
                     XEnchantment var30 = REGISTRY.bukkitMapping().get(var28.getEnchantment());
                     Objects.requireNonNull(var30, () -> "No main mapping found for Enchantment." + var21.getName() + " (" + var28 + ")");
                     REGISTRY.bukkitMapping().put(var28, var30);
                  }
               } catch (IllegalAccessException var9) {
                  throw new IllegalStateException("Cannot get direct enchantment field for " + var21, var9);
               }
            }
         }
      }

      REGISTRY.discardMetadata();
   }
}
