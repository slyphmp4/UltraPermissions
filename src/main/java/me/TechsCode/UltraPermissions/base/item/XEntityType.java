package me.TechsCode.UltraPermissions.base.item;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public enum XEntityType implements XBase<XEntityType, EntityType> {
   ACACIA_BOAT(XMaterial.ACACIA_BOAT),
   ACACIA_CHEST_BOAT(XMaterial.ACACIA_CHEST_BOAT),
   ALLAY(XMaterial.ALLAY_SPAWN_EGG),
   AREA_EFFECT_CLOUD(XMaterial.WHITE_WOOL),
   ARMADILLO(XMaterial.ARMADILLO_SPAWN_EGG),
   ARMOR_STAND(XMaterial.ARMOR_STAND),
   ARROW(XMaterial.ARROW),
   AXOLOTL(XMaterial.AXOLOTL_SPAWN_EGG),
   BAMBOO_CHEST_RAFT(XMaterial.BAMBOO_CHEST_RAFT),
   BAMBOO_RAFT(XMaterial.BAMBOO_RAFT),
   BAT(XMaterial.BAT_SPAWN_EGG),
   BEE(XMaterial.BEE_SPAWN_EGG),
   BIRCH_BOAT(XMaterial.BIRCH_BOAT),
   BIRCH_CHEST_BOAT(XMaterial.BIRCH_CHEST_BOAT),
   BLAZE(XMaterial.BLAZE_ROD),
   BLOCK_DISPLAY(XMaterial.BARRIER),
   BOGGED(XMaterial.MUD),
   BREEZE(XMaterial.AIR),
   BREEZE_WIND_CHARGE(XMaterial.WIND_CHARGE),
   CAMEL(XMaterial.CAMEL_SPAWN_EGG),
   CAT(XMaterial.CAT_SPAWN_EGG),
   CAVE_SPIDER(XMaterial.CAVE_SPIDER_SPAWN_EGG),
   CHERRY_BOAT(XMaterial.CHERRY_BOAT),
   CHERRY_CHEST_BOAT(XMaterial.CHERRY_CHEST_BOAT),
   CHEST_MINECART(XMaterial.CHEST_MINECART, "MINECART_CHEST"),
   CHICKEN(XMaterial.CHICKEN_SPAWN_EGG),
   COD(XMaterial.COD_SPAWN_EGG),
   COMMAND_BLOCK_MINECART(XMaterial.COMMAND_BLOCK_MINECART, "MINECART_COMMAND"),
   COW(XMaterial.COW_SPAWN_EGG),
   CREAKING(XMaterial.STRUCTURE_VOID),
   @Deprecated
   CREAKING_TRANSIENT(XMaterial.STRUCTURE_VOID),
   CREEPER(XMaterial.CREEPER_HEAD),
   DARK_OAK_BOAT(XMaterial.DARK_OAK_BOAT),
   DARK_OAK_CHEST_BOAT(XMaterial.DARK_OAK_CHEST_BOAT),
   DOLPHIN(XMaterial.DOLPHIN_SPAWN_EGG),
   DONKEY(XMaterial.DONKEY_SPAWN_EGG),
   DRAGON_FIREBALL(XMaterial.DRAGON_BREATH),
   DROWNED(XMaterial.TRIDENT),
   EGG(XMaterial.EGG),
   ELDER_GUARDIAN(XMaterial.ELDER_GUARDIAN_SPAWN_EGG),
   ENDERMAN(XMaterial.ENDERMAN_SPAWN_EGG),
   ENDERMITE(XMaterial.ENDERMITE_SPAWN_EGG),
   ENDER_DRAGON(XMaterial.DRAGON_EGG),
   ENDER_PEARL(XMaterial.ENDER_PEARL),
   END_CRYSTAL(XMaterial.END_CRYSTAL, "ENDER_CRYSTAL"),
   EVOKER(XMaterial.EVOKER_SPAWN_EGG),
   EVOKER_FANGS(XMaterial.AIR),
   EXPERIENCE_BOTTLE(XMaterial.EXPERIENCE_BOTTLE, "THROWN_EXP_BOTTLE"),
   EXPERIENCE_ORB(XMaterial.EXPERIENCE_BOTTLE),
   EYE_OF_ENDER(XMaterial.ENDER_EYE, "ENDER_SIGNAL"),
   FALLING_BLOCK(XMaterial.SAND),
   FIREBALL(XMaterial.FIRE_CHARGE),
   FIREWORK_ROCKET(XMaterial.FIREWORK_ROCKET, "FIREWORK"),
   FISHING_BOBBER(XMaterial.FISHING_ROD, "FISHING_HOOK"),
   FOX(XMaterial.FOX_SPAWN_EGG),
   FROG(XMaterial.FROG_SPAWN_EGG),
   FURNACE_MINECART(XMaterial.FURNACE_MINECART),
   GHAST(XMaterial.GHAST_TEAR),
   GIANT(XMaterial.ZOMBIE_SPAWN_EGG),
   GLOW_ITEM_FRAME(XMaterial.GLOW_ITEM_FRAME),
   GLOW_SQUID(XMaterial.GLOW_SQUID_SPAWN_EGG),
   GOAT(XMaterial.GOAT_SPAWN_EGG),
   GUARDIAN(XMaterial.GUARDIAN_SPAWN_EGG),
   HAPPY_GHAST(XMaterial.BARRIER),
   HOGLIN(XMaterial.HOGLIN_SPAWN_EGG),
   HOPPER_MINECART(XMaterial.HOPPER, "MINECART_HOPPER"),
   HORSE(XMaterial.HORSE_SPAWN_EGG),
   HUSK(XMaterial.HUSK_SPAWN_EGG),
   ILLUSIONER(XMaterial.VILLAGER_SPAWN_EGG),
   INTERACTION(XMaterial.BARRIER),
   IRON_GOLEM(XMaterial.IRON_BLOCK),
   ITEM(XMaterial.GLASS_BOTTLE, "DROPPED_ITEM"),
   ITEM_DISPLAY(XMaterial.ITEM_FRAME),
   ITEM_FRAME(XMaterial.ITEM_FRAME),
   JUNGLE_BOAT(XMaterial.JUNGLE_BOAT),
   JUNGLE_CHEST_BOAT(XMaterial.JUNGLE_CHEST_BOAT),
   LEASH_KNOT(XMaterial.LEAD, "LEASH_HITCH"),
   LIGHTNING_BOLT(XMaterial.LIGHTNING_ROD, "LIGHTNING"),
   LINGERING_POTION(XMaterial.LINGERING_POTION),
   LLAMA(XMaterial.LLAMA_SPAWN_EGG),
   LLAMA_SPIT(XMaterial.AIR),
   MAGMA_CUBE(XMaterial.MAGMA_CUBE_SPAWN_EGG),
   MANGROVE_BOAT(XMaterial.MANGROVE_BOAT),
   MANGROVE_CHEST_BOAT(XMaterial.MANGROVE_CHEST_BOAT),
   MARKER(XMaterial.BARRIER),
   MINECART(XMaterial.MINECART),
   MOOSHROOM(XMaterial.MOOSHROOM_SPAWN_EGG, "MUSHROOM_COW"),
   MULE(XMaterial.MULE_SPAWN_EGG),
   OAK_BOAT(XMaterial.OAK_BOAT, "BOAT"),
   OAK_CHEST_BOAT(XMaterial.OAK_CHEST_BOAT, "CHEST_BOAT"),
   OCELOT(XMaterial.OCELOT_SPAWN_EGG),
   OMINOUS_ITEM_SPAWNER(XMaterial.BARRIER),
   PAINTING(XMaterial.PAINTING),
   PALE_OAK_BOAT(XMaterial.BARRIER),
   PALE_OAK_CHEST_BOAT(XMaterial.BARRIER),
   PANDA(XMaterial.PANDA_SPAWN_EGG),
   PARROT(XMaterial.PARROT_SPAWN_EGG),
   PHANTOM(XMaterial.PHANTOM_SPAWN_EGG),
   PIG(XMaterial.PIG_SPAWN_EGG),
   PIGLIN(XMaterial.PIGLIN_SPAWN_EGG),
   PIGLIN_BRUTE(XMaterial.PIGLIN_BRUTE_SPAWN_EGG),
   PILLAGER(XMaterial.PILLAGER_SPAWN_EGG),
   PLAYER(XMaterial.PLAYER_HEAD),
   POLAR_BEAR(XMaterial.POLAR_BEAR_SPAWN_EGG),
   PUFFERFISH(XMaterial.PUFFERFISH_SPAWN_EGG),
   RABBIT(XMaterial.RABBIT_SPAWN_EGG),
   RAVAGER(XMaterial.SADDLE),
   SALMON(XMaterial.SALMON_SPAWN_EGG),
   SHEEP(XMaterial.SHEEP_SPAWN_EGG),
   SHULKER(XMaterial.SHULKER_SPAWN_EGG),
   SHULKER_BULLET(XMaterial.BARRIER),
   SILVERFISH(XMaterial.SILVERFISH_SPAWN_EGG),
   SKELETON(XMaterial.SKELETON_SPAWN_EGG),
   SKELETON_HORSE(XMaterial.SKELETON_HORSE_SPAWN_EGG),
   SLIME(XMaterial.SLIME_SPAWN_EGG),
   SMALL_FIREBALL(XMaterial.FIRE_CHARGE),
   SNIFFER(XMaterial.SNIFFER_SPAWN_EGG),
   SNOWBALL(XMaterial.SNOWBALL),
   SNOW_GOLEM(XMaterial.SNOW_GOLEM_SPAWN_EGG, "SNOWMAN"),
   SPAWNER_MINECART(XMaterial.BARRIER, "MINECART_MOB_SPAWNER"),
   SPECTRAL_ARROW(XMaterial.SPECTRAL_ARROW),
   SPIDER(XMaterial.SPIDER_SPAWN_EGG),
   SPLASH_POTION(XMaterial.SPLASH_POTION, "POTION"),
   SPRUCE_BOAT(XMaterial.SPRUCE_BOAT),
   SPRUCE_CHEST_BOAT(XMaterial.SPRUCE_CHEST_BOAT),
   SQUID(XMaterial.SQUID_SPAWN_EGG),
   STRAY(XMaterial.STRAY_SPAWN_EGG),
   STRIDER(XMaterial.STRIDER_SPAWN_EGG),
   TADPOLE(XMaterial.TADPOLE_SPAWN_EGG),
   TEXT_DISPLAY(XMaterial.BARRIER),
   TNT(XMaterial.TNT, "PRIMED_TNT"),
   TNT_MINECART(XMaterial.TNT_MINECART, "MINECART_TNT"),
   TRADER_LLAMA(XMaterial.TRADER_LLAMA_SPAWN_EGG),
   TRIDENT(XMaterial.TRIDENT),
   TROPICAL_FISH(XMaterial.TROPICAL_FISH_SPAWN_EGG),
   TURTLE(XMaterial.TURTLE_SPAWN_EGG),
   UNKNOWN(XMaterial.BARRIER),
   VEX(XMaterial.VEX_SPAWN_EGG),
   VILLAGER(XMaterial.VILLAGER_SPAWN_EGG),
   VINDICATOR(XMaterial.VINDICATOR_SPAWN_EGG),
   WANDERING_TRADER(XMaterial.WANDERING_TRADER_SPAWN_EGG),
   WARDEN(XMaterial.WARDEN_SPAWN_EGG),
   WIND_CHARGE(XMaterial.WIND_CHARGE),
   WITCH(XMaterial.WITCH_SPAWN_EGG),
   WITHER(XMaterial.WITHER_SKELETON_SKULL),
   WITHER_SKELETON(XMaterial.WITHER_SKELETON_SPAWN_EGG),
   WITHER_SKULL(XMaterial.WITHER_SKELETON_SKULL),
   WOLF(XMaterial.WOLF_SPAWN_EGG),
   ZOGLIN(XMaterial.ZOGLIN_SPAWN_EGG),
   ZOMBIE(XMaterial.ZOMBIE_SPAWN_EGG),
   ZOMBIE_HORSE(XMaterial.ZOMBIE_HORSE_SPAWN_EGG),
   ZOMBIE_VILLAGER(XMaterial.ZOMBIE_VILLAGER_SPAWN_EGG),
   ZOMBIFIED_PIGLIN(XMaterial.ZOMBIFIED_PIGLIN_SPAWN_EGG),
   COPPER_GOLEM(XMaterial.COPPER_GOLEM_SPAWN_EGG),
   MANNEQUIN(XMaterial.ARMOR_STAND),
   CAMEL_HUSK(XMaterial.CAMEL_HUSK_SPAWN_EGG),
   NAUTILUS(XMaterial.NAUTILUS_SPAWN_EGG),
   PARCHED(XMaterial.PARCHED_SPAWN_EGG),
   ZOMBIE_NAUTILUS(XMaterial.ZOMBIE_NAUTILUS_SPAWN_EGG);

   public static final XRegistry<XEntityType, EntityType> REGISTRY = XEntityType.Data.REGISTRY;
   private final EntityType entityType;
   private final XMaterial identificationMaterial;

   XEntityType(String... var3) {
      this.identificationMaterial = XMaterial.BARRIER;
      this.entityType = XEntityType.Data.REGISTRY.stdEnum(this, var3);
   }

   XEntityType(XMaterial var3, String... var4) {
      this.identificationMaterial = var3;
      this.entityType = XEntityType.Data.REGISTRY.stdEnum(this, var4);
   }

   @NotNull
   public static @Unmodifiable Collection<XEntityType> getValues() {
      return REGISTRY.getValues();
   }

   @NotNull
   public static XEntityType of(@NotNull Entity var0) {
      Objects.requireNonNull(var0, "Cannot match entity type from null entity");
      return of(var0.getType());
   }

   @NotNull
   public static XEntityType of(@NotNull EntityType var0) {
      return REGISTRY.getByBukkitForm(var0);
   }

   public static Optional<XEntityType> of(@NotNull String var0) {
      return REGISTRY.getByName(var0);
   }

   @Override
   public String[] getNames() {
      return new String[]{this.name()};
   }

   public EntityType get() {
      return this.entityType;
   }

   public XMaterial getIcon() {
      return this.identificationMaterial;
   }

   static {
      REGISTRY.discardMetadata();
   }

   private static final class Data {
      public static final XRegistry<XEntityType, EntityType> REGISTRY = new XRegistry<>(EntityType.class, XEntityType.class, var0 -> new XEntityType[var0]);

      private Data() {
         super();
      }
   }
}
